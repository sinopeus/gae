package ds.gae.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import ds.gae.CarRentalModel;
import ds.gae.SerializationUtils;
import ds.gae.entities.Quote;
import ds.gae.view.JSPSite;

@SuppressWarnings("serial")
public class ConfirmQuotesServlet extends HttpServlet {
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		HttpSession session = req.getSession();
		@SuppressWarnings("unchecked")
		HashMap<String, ArrayList<Quote>> allQuotes = (HashMap<String, ArrayList<Quote>>) session
				.getAttribute("quotes");

		// Collect quotes from session
		ArrayList<Quote> qs = new ArrayList<Quote>();
		for (String crcName : allQuotes.keySet()) {
			qs.addAll(allQuotes.get(crcName));
		}
		
		// Clear quotes in session
		session.setAttribute("quotes", new HashMap<String, ArrayList<Quote>>());
		
		// Construct serialized payload
		byte[] serializedTask = SerializationUtils.serialize(qs);

		// Create task and add it to the (default) queue
		Queue queue = QueueFactory.getDefaultQueue();
		TaskOptions options = TaskOptions.Builder.withUrl("/worker").payload(serializedTask);
		queue.add(options);
		
		// Create notification
		String msg = String.format("%d quote(s) are being processed", qs.size());
		CarRentalModel.get().addNotification(qs.get(0).getCarRenter(), msg);

		// Redirect to notifications page
		resp.sendRedirect(JSPSite.CONFIRM_QUOTES_RESPONSE.url());
	}
}
