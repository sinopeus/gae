package ds.gae;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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

	private EntityManager			em;

	private static CarRentalModel	instance;

	public static CarRentalModel get() {
		if (instance == null)
			instance = new CarRentalModel();
		return instance;
	}

	private CarRentalModel() {
		this.em = EMF.get().createEntityManager();
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
		List<String> names = em
				.createNamedQuery("CarRentalCompany.carTypes", String.class)
				.setParameter("companyName", crcName)
				.getResultList();
		return new HashSet<String>(names);
	}

	/**
	 * Get all registered car rental companies
	 *
	 * @return the list of car rental companies
	 */
	public Collection<String> getAllRentalCompanyNames() {
		List<String> names = em.createNamedQuery(
				"CarRentalCompany.names",
				String.class).getResultList();
		return new HashSet<String>(names);
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

		CarRentalCompany crc = em.find(CarRentalCompany.class, company);

		Quote out = null;

		if (crc != null) {
			out = crc.createQuote(constraints, renterName);
		} else {
			throw new ReservationException("CarRentalCompany not found.");
		}

		return out;
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

		String companyName = q.getRentalCompany();
		CarRentalCompany crc = em.find(CarRentalCompany.class, companyName);
		if (crc == null) {
			throw new IllegalArgumentException("Company " + companyName
					+ " doesn't exist!");
		}
		crc.confirmQuote(q);
	}

	/**
	 * Confirm the given list of quotes
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
	public List<Reservation> confirmQuotes(List<Quote> quotes)
			throws ReservationException {

		EntityTransaction txn = em.getTransaction();
		txn.begin();
		List<Reservation> done = new ArrayList<Reservation>();
		try {
			for (Quote quote : quotes) {
				String companyName = quote.getRentalCompany();
				CarRentalCompany crc = em.find(
						CarRentalCompany.class,
						companyName);
				if (crc == null) {
					throw new IllegalArgumentException("Company " + companyName
							+ " doesn't exist!");
				}
				done.add(crc.confirmQuote(quote));
				// em.persist(crc);
				txn.commit();
			}
		} catch (Exception e) {
			if (txn.isActive()) {
				txn.rollback();
			}
			throw new ReservationException(e.getMessage());
		}
		return done;

	}

	/**
	 * Get all reservations made by the given car renter.
	 *
	 * @param renter
	 *            name of the car renter
	 * @return the list of reservations of the given car renter
	 */
	public List<Reservation> getReservations(String renter) {
		List<Reservation> reservations = em
				.createNamedQuery(
						"Reservation.reservationsByRenter",
						Reservation.class)
				.setParameter("clientName", renter)
				.getResultList();

		return reservations;
	}

	/**
	 * Get the car types available in the given car rental company.
	 *
	 * @param crcName
	 *            the given car rental company
	 * @return The list of car types in the given car rental company.
	 */
	public Collection<CarType> getCarTypesOfCarRentalCompany(String crcName) {
		List<CarType> carTypes = em
				.createNamedQuery("CarRentalCompany.carTypes", CarType.class)
				.setParameter("companyName", crcName)
				.getResultList();
		return new HashSet<CarType>(carTypes);
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
		List<Car> cars = em.createNamedQuery("CarRentalCompany.carsByType",
				Car.class)
				.setParameter("companyName", crcName)
				.setParameter("carType", carType)
				.getResultList();
		return cars;

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