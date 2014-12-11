package ds.gae.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import ds.gae.CarRentalModel;
import ds.gae.entities.Car;
import ds.gae.entities.CarRentalCompany;
import ds.gae.entities.CarType;

public class CarRentalServletContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// This will be invoked as part of a warming request,
		// or the first user request if no warming request was invoked.

		// check if dummy data is available, and add if necessary
		if (!isDummyDataAvailable()) {
			addDummyData();
		}
	}

	private boolean isDummyDataAvailable() {
		// If the Hertz car rental company is in the datastore, we assume the
		// dummy data is available

		// FIXME: can be improved using EXISTS JPQL query, more efficient

		return CarRentalModel
				.get()
				.getAllRentalCompanyNames()
				.contains("Hertz");
	}

	private void addDummyData() {
		loadRental("Hertz", "hertz.csv");
		loadRental("Dockx", "dockx.csv");
	}

	private void loadRental(String name, String datafile) {
		Logger.getLogger(CarRentalServletContextListener.class.getName()).log(
				Level.INFO,
				"loading {0} from file {1}",
				new Object[] { name, datafile });
		try {

			Map<String,CarType> carTypes = loadData(name, datafile);
			CarRentalCompany company = new CarRentalCompany(name, carTypes);
			CarRentalModel.get().addRentalCompany(company);

		} catch (NumberFormatException ex) {
			Logger
					.getLogger(CarRentalServletContextListener.class.getName())
					.log(Level.SEVERE, "bad file", ex);
		} catch (IOException ex) {
			Logger
					.getLogger(CarRentalServletContextListener.class.getName())
					.log(Level.SEVERE, null, ex);
		}
	}

	public static Map<String,CarType> loadData(String name, String datafile)
			throws NumberFormatException, IOException {
		// FIXME: adapt the implementation of this method to your entity
		// structure

		Map<String,CarType> types = new HashMap<String,CarType>();
		int carId = 1;

		// open file from jar
		BufferedReader in = new BufferedReader(new InputStreamReader(
				CarRentalServletContextListener.class
						.getClassLoader()
						.getResourceAsStream(datafile)));
		// while next line exists
		while (in.ready()) {
			// read line
			String line = in.readLine();
			// if comment: skip
			if (line.startsWith("#")) {
				continue;
			}
			// tokenize on ,
			StringTokenizer csvReader = new StringTokenizer(line, ",");
			// create new car type from first 5 fields
			String typeName = csvReader.nextToken();
			int nbOfSeats = Integer.parseInt(csvReader.nextToken());
			float trunkSpace = Float.parseFloat(csvReader.nextToken());
			double rentalPricePerDay = Double.parseDouble(csvReader.nextToken());
			boolean smokingAllowed = Boolean.parseBoolean(csvReader.nextToken());
			// create N new cars with given type, where N is the 5th field
			Set<Car> cars = new HashSet<Car>();
			for (int i = Integer.parseInt(csvReader.nextToken()); i > 0; i--) {
				cars.add(new Car(carId++));
			}
			types.put(typeName, new CarType(typeName,nbOfSeats,trunkSpace,rentalPricePerDay,smokingAllowed,cars,name));
		}
		return types;
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// App Engine does not currently invoke this method.
	}
}