package ds.gae;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
		ConfirmQuotesTask task = SerializationUtils.deserialize(req.getInputStream());

		try {
			// Try to confirm quotes
			CarRentalModel.get().confirmQuotes(task.getQuotes());
			// Success
			String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
			StringBuilder message = new StringBuilder();
			message.append(String.format("<strong>%s</strong> | quote(s) successfully confirmed", timeStamp));
			message.append("<br>");
			for (Quote quote : task.getQuotes()) {
				message.append("*  " + quote);
				message.append("<br>");
			}
			CarRentalModel.get().addNotification(task.getRenter(), message.toString());
		} catch (ReservationException e) {
			// Failure
			// e.printStackTrace();
			String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
			StringBuilder message = new StringBuilder();
			message.append(String.format("<strong>%s</strong> | Could not confirm all quote(s).", timeStamp));
			message.append("<br>");
			message.append(e.getMessage());
			CarRentalModel.get().addNotification(task.getRenter(), message.toString());
		}
	}
}
