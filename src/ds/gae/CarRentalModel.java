package ds.gae;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import ds.gae.entities.Car;
import ds.gae.entities.CarRentalCompany;
import ds.gae.entities.CarType;
import ds.gae.entities.Quote;
import ds.gae.entities.Reservation;
import ds.gae.entities.ReservationConstraints;

public class CarRentalModel {

	private static CarRentalModel instance;

	public static CarRentalModel get() {
		if (instance == null)
			instance = new CarRentalModel();
		return instance;
	}

	/**
	 * g Get the car types available in the given car rental company.
	 *
	 * @param crcName
	 *            the car rental company
	 * @return The list of car types (i.e. name of car type), available in the
	 *         given car rental company.
	 */
	public Set<String> getCarTypesNames(String crcName) {
		EntityManager em = EMF.get().createEntityManager();
		try {
			List<String> names = em
					.createNamedQuery("CarType.namesByCompany", String.class)
					.setParameter("companyName", crcName)
					.getResultList();
			return new HashSet<String>(names);
		} finally {
			em.close();
		}
	}

	/**
	 * Get all registered car rental companies
	 *
	 * @return the list of car rental companies
	 */
	public Collection<String> getAllRentalCompanyNames() {
		EntityManager em = EMF.get().createEntityManager();
		try {
			List<String> names = em
					.createNamedQuery("CarRentalCompany.names", String.class)
					.getResultList();
			return new HashSet<String>(names);
		} finally {
			em.close();
		}
	}
	
	/**
	 * Get the rental company with the given name.
	 * 
	 * @param crcName
	 *            name of the car rental company
	 * @return The car rental company, or null if not found.
	 * @throw IllegalArgumentException if there exists no company with the given name
	 */
	public CarRentalCompany getRentalCompany(String crcName) throws IllegalArgumentException{
		EntityManager em = EMF.get().createEntityManager();
		try {
			return getRentalCompany(em, crcName);
		} finally {
			em.close();
		}
	}

	private CarRentalCompany getRentalCompany(EntityManager em, String crcName) 
			throws IllegalArgumentException{
		CarRentalCompany crc = em.find(CarRentalCompany.class, crcName);
		if (crc == null)
			throw new IllegalArgumentException("Company " + crcName
					+ " doesn't exist!");
		return crc;
	}
	
	/**
	 * Register a car rental company.
	 * 
	 * @param company
	 *            new car rental company
	 */
	public void addRentalCompany(CarRentalCompany company) {
		EntityManager em = EMF.get().createEntityManager();
		try {
			em.persist(company);
		} finally {
			em.close();
		}
	}

	/**
	 * Create a quote according to the given reservation constraints (tentative
	 * reservation).
	 * 
	 * @param company
	 *            name of the car renter company
	 * @param renterName
	 *            name of the car renter
	 * @param constraints
	 *            reservation constraints for the quote
	 * @return The newly created quote.
	 * 
	 * @throws ReservationException
	 *             No car available that fits the given constraints.
	 */
	public Quote createQuote(
			String company,
			String renterName,
			ReservationConstraints constraints) throws ReservationException {
		EntityManager em = EMF.get().createEntityManager();
		try {
			CarRentalCompany crc = getRentalCompany(em, company);
			return crc.createQuote(constraints, renterName);
		} finally {
			em.close();
		}
	}

	/**
	 * Confirm the given quote.
	 *
	 * @param q
	 *            Quote to confirm
	 * 
	 * @throws ReservationException
	 *             Confirmation of given quote failed.
	 */
	public void confirmQuote(Quote q) throws ReservationException {
		EntityManager em = EMF.get().createEntityManager();
		try {
			String companyName = q.getRentalCompany();
			CarRentalCompany crc = getRentalCompany(em, companyName);
			crc.confirmQuote(q);
		} finally {
			em.close();
		}
	}

	/**
	 * Confirm the given list of quotes.
	 * 
	 * @param quotes
	 *            the quotes to confirm
	 * @return The list of reservations, resulting from confirming all given
	 *         quotes.
	 * 
	 * @throws ReservationException
	 *             One of the quotes cannot be confirmed. Therefore none of the
	 *             given quotes is confirmed.
	 */
	public List<Reservation> confirmQuotes(List<Quote> quotes) throws ReservationException {
		// Group the quotes by company
		Map<String, List<Quote>> groupedQuotes = groupQuotesByCompany(quotes);
		List<Reservation> reservations = new ArrayList<Reservation>();
		try {
			// Confirm each group (in a transaction)
			for (List<Quote> group : groupedQuotes.values()) {
				reservations.addAll(confirmQuotesInCompany(group));
			}
			return reservations;
		} catch (ReservationException e) {
			// Cancel the committed reservations (outside of a transaction)
			for (Reservation res : reservations) {
				cancelReservation(res);
			}
			throw e;
		}
	}
	
	/**
	 * Group the given quotes by company.
	 * 
	 * @param quotes
	 *            the quotes to group
	 * @return A map of grouped quotes with the company name as keys.
	 */
	protected Map<String, List<Quote>> groupQuotesByCompany(Collection<Quote> quotes) {
		Map<String, List<Quote>> groupedQuotes = new HashMap<>();
		for (Quote quote : quotes) {
			List<Quote> group = groupedQuotes.get(quote.getRentalCompany());
			if (group == null) {
				group = new ArrayList<>();
				groupedQuotes.put(quote.getRentalCompany(), group);
			}
			group.add(quote);
		}
		return groupedQuotes;
	}

	/**
	 * Confirm the given list of quotes <strong>for one company</strong>.
	 * 
	 * @param quotes
	 *            the quotes to confirm
	 * @return The list of reservations, resulting from confirming all given
	 *         quotes.
	 * @throws ReservationException
	 *             One of the quotes cannot be confirmed. Therefore none of the
	 *             given quotes is confirmed.
	 */
	protected List<Reservation> confirmQuotesInCompany(List<Quote> quotes)
			throws ReservationException {
		EntityManager em = EMF.get().createEntityManager();
		EntityTransaction t = em.getTransaction();
		try {
			// Confirm the quotes inside a transaction
			// This is allowed, as all the reservations belong to the same
			// entity group (with the owning company as root entity)
			t.begin();
			List<Reservation> reservations = new ArrayList<>();
			for (Quote q : quotes) {
				CarRentalCompany crc = getRentalCompany(em, q.getRentalCompany());
				Reservation res = crc.confirmQuote(q);
				reservations.add(res);
			}
			t.commit();
			return reservations;
		} catch (ReservationException e) {
			// Roll back the transaction
			t.rollback();
			throw e;
		} finally {
			em.close();
		}
	}

	/**
	 * Cancel the given reservation.
	 * 
	 * @param res
	 *            the reservation to confirm
	 */
	public void cancelReservation(Reservation res) {
		EntityManager em = EMF.get().createEntityManager();
		try {
			CarRentalCompany crc = getRentalCompany(em, res.getRentalCompany());
			crc.cancelReservation(res);
		} finally {
			em.close();
		}
	}
	
	/**
	 * Get all reservations made by the given car renter.
	 *
	 * @param renter
	 *            name of the car renter
	 * @return the list of reservations of the given car renter
	 */
	public List<Reservation> getReservations(String renter) {
		EntityManager em = EMF.get().createEntityManager();
		try {
			return em.createNamedQuery("Reservation.reservationsByRenter",Reservation.class)
						.setParameter("clientName", renter)
						.getResultList();
		} finally {
			em.close();
		}
	}

	/**
	 * Get the car types available in the given car rental company.
	 *
	 * @param crcName
	 *            the given car rental company
	 * @return The list of car types in the given car rental company.
	 */
	public Collection<CarType> getCarTypesOfCarRentalCompany(String crcName) {
		EntityManager em = EMF.get().createEntityManager();
		try {
			return em.createNamedQuery("CarType.byCompany", CarType.class)
						.setParameter("companyName", crcName)
						.getResultList();
		} finally {
			em.close();
		}
	}

	/**
	 * Get the list of cars of the given car type in the given car rental
	 * company.
	 *
	 * @param crcName
	 *            name of the car rental company
	 * @param carType
	 *            the given car type
	 * @return A list of car IDs of cars with the given car type.
	 */
	public Collection<Integer> getCarIdsByCarType(
			String crcName,
			CarType carType) {
		Collection<Integer> out = new ArrayList<Integer>();
		for (Car c : getCarsByCarType(crcName, carType)) {
			out.add(c.getId());
		}
		return out;
	}

	/**
	 * Get the amount of cars of the given car type in the given car rental
	 * company.
	 *
	 * @param crcName
	 *            name of the car rental company
	 * @param carType
	 *            the given car type
	 * @return A number, representing the amount of cars of the given car type.
	 */
	public int getAmountOfCarsByCarType(String crcName, CarType carType) {
		return this.getCarsByCarType(crcName, carType).size();
	}

	/**
	 * Get the list of cars of the given car type in the given car rental
	 * company.
	 *
	 * @param crcName
	 *            name of the car rental company
	 * @param carType
	 *            the given car type
	 * @return List of cars of the given car type
	 */
	private List<Car> getCarsByCarType(String crcName, CarType carType) {
		EntityManager em = EMF.get().createEntityManager();
		try {
			return em.createNamedQuery("CarType.carsByCompany",Car.class)
						.setParameter("companyName", crcName)
						.setParameter("carTypeName", carType.getName())
						.getResultList();
		} finally {
			em.close();
		}
	}

	/**
	 * Check whether the given car renter has reservations.
	 *
	 * @param renter
	 *            the car renter
	 * @return True if the number of reservations of the given car renter is
	 *         higher than 0. False otherwise.
	 */
	public boolean hasReservations(String renter) {
		return this.getReservations(renter).size() > 0;
	}
}