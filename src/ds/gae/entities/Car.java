package ds.gae.entities;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import com.google.appengine.api.datastore.Key;

@NamedQueries({
	@NamedQuery(name = "Car.byType", 
				query = "SELECT car " 
							+ "FROM Car car "
							+ "WHERE car.type = :carType")					
	})
@Entity
public class Car {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;
	private int id;
	@OneToMany(cascade = CascadeType.ALL)
	private Set<Reservation> reservations;
	
	@ManyToOne(fetch = FetchType.LAZY)
    private CarType type;

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public Car() {

	}

	public Car(int uid) {
		this.id = uid;
		this.reservations = new HashSet<Reservation>();
	}

	/******
	 * ID *
	 ******/

	public int getId() {
		return id;
	}
	
	public Key getKey() {
		return key;
	}

	/****************
	 * RESERVATIONS *
	 ****************/

	public Set<Reservation> getReservations() {
		return reservations;
	}

	public boolean isAvailable(Date start, Date end) {
		if (!start.before(end))
			throw new IllegalArgumentException("Illegal given period");

		for (Reservation reservation : reservations) {
			if (reservation.getEndDate().before(start)
					|| reservation.getStartDate().after(end))
				continue;
			return false;
		}
		return true;
	}

	public void addReservation(Reservation res) {
		reservations.add(res);
	}

	public void removeReservation(Reservation reservation) {
		// equals-method for Reservation is required!
		reservations.remove(reservation);
	}

}