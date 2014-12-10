package ds.gae;

import java.io.Serializable;
import java.util.ArrayList;

import ds.gae.entities.Quote;

public class ConfirmQuotesTask implements Serializable {

	private static final long serialVersionUID = -4482776502460530766L;
	
	private final ArrayList<Quote> quotes;
	private final String renter;

	public ConfirmQuotesTask(ArrayList<Quote> quotes, String renter) {
		this.quotes = quotes;
		this.renter = renter;
	}

	public ArrayList<Quote> getQuotes() {
		return quotes;
	}

	public String getRenter() {
		return renter;
	}
}
