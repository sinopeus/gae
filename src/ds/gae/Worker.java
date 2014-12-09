package ds.gae;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ds.gae.entities.Quote;

public class Worker extends HttpServlet {
	private static final long serialVersionUID = -7058685883212377590L;
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// Deserialize quotes
		ArrayList<Quote> quotes = SerializationUtils.deserialize(req.getInputStream());

		try {
			// Try to confirm quotes
			CarRentalModel.get().confirmQuotes(quotes);
			// Success
		} catch (ReservationException e) {
			// Failure
			e.printStackTrace();
		}
	}
}
