package ds.gae.entities;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import com.google.appengine.api.datastore.Key;

@NamedQueries({
	@NamedQuery(name = "CarType.namesByCompany", 
				query = "SELECT carType.name " 
						+ "FROM CarType carType "
						+ "WHERE carType.companyName = :companyName"),
	@NamedQuery(name = "CarType.byCompany", 
				query = "SELECT carType " 
						+ "FROM CarType carType "
						+ "WHERE carType.companyName = :companyName"),
	@NamedQuery(name = "CarType.carsByCompany", 
				query = "SELECT cars " 
							+ "FROM CarType carType "
							+ "WHERE carType.companyName = :companyName AND carType.name = :carTypeName")					
	})
@Entity
public class CarType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;
	
	private String companyName;

	@OneToMany(cascade = CascadeType.ALL)
	private Set<Car> cars;
	
	private String	name;
	private int		nbOfSeats;
	private boolean	smokingAllowed;
	private double	rentalPricePerDay;
	// trunk space in liters
	private float	trunkSpace;

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public CarType() {

	}

	public CarType(
					String name,
					int nbOfSeats,
					float trunkSpace,
					double rentalPricePerDay,
					boolean smokingAllowed,
					Set<Car> cars,
					String companyName) {
		this.name = name;
		this.nbOfSeats = nbOfSeats;
		this.trunkSpace = trunkSpace;
		this.rentalPricePerDay = rentalPricePerDay;
		this.smokingAllowed = smokingAllowed;
		this.cars = cars;
		this.companyName = companyName;
	}

	public String getName() {
		return name;
	}

	public int getNbOfSeats() {
		return nbOfSeats;
	}

	public boolean isSmokingAllowed() {
		return smokingAllowed;
	}

	public double getRentalPricePerDay() {
		return rentalPricePerDay;
	}

	public float getTrunkSpace() {
		return trunkSpace;
	}
	
	public Set<Car> getCars() {
		return cars;
	}

	public String getCompany() {
		return companyName;
	}
	
	/*************
	 * TO STRING *
	 *************/

	@Override
	public String toString() {
		return String
				.format(
						"Car type: %s \t[seats: %d, price: %.2f, smoking: %b, trunk: %.0fl] NumCars: %d Company: %s",
						getName(),
						getNbOfSeats(),
						getRentalPricePerDay(),
						isSmokingAllowed(),
						getTrunkSpace(),
						getCars().size(),
						getCompany());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CarType other = (CarType) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}