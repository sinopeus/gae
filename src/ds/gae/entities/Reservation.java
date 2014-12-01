package ds.gae.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.google.appengine.api.datastore.Key;

@Entity
@NamedQueries({ 
	@NamedQuery(name = "Reservation.reservationsByRenter", 
				query = "SELECT res FROM Reservation res WHERE res.carRenter = :clientName") 
	})
public class Reservation extends Quote {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;
  	private int	carId;

	/***************
	 * CONSTRUCTOR *
	 ***************/

	Reservation() {

	}

	Reservation(Quote quote, int carId) {
		super(
				quote.getCarRenter(),
				quote.getStartDate(),
				quote.getEndDate(),
				quote.getRentalCompany(),
				quote.getCarType(),
				quote.getRentalPrice());
		this.carId = carId;
	}

	/******
	 * ID *
	 ******/

	public int getCarId() {
		return carId;
	}
	
	public Key getKey() {
		return key;
	}

	/*************
	 * TO STRING *
	 *************/

	@Override
	public String toString() {
		return String
				.format(
						"Reservation for %s from %s to %s at %s\nCar type: %s\tCar: %s\nTotal price: %.2f",
						getCarRenter(),
						getStartDate(),
						getEndDate(),
						getRentalCompany(),
						getCarType(),
						getCarId(),
						getRentalPrice());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + carId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		Reservation other = (Reservation) obj;
		if (carId != other.carId)
			return false;
		return true;
	}
}