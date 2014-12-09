package ds.gae.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.google.appengine.api.datastore.Key;

@NamedQueries({
	@NamedQuery(name = "Notification.byRenter", 
				query = "SELECT notification.msg " 
							+ "FROM Notification notification "
							+ "WHERE notification.renter = :renter")					
	})
@Entity
public class Notification {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;
	private String renter;
	private String msg;
	
	public Notification() {}
	
	public Notification(String renter, String msg) {
		this.setRenter(renter);
		this.setMsg(msg);
	}

	public String getRenter() {
		return renter;
	}

	public void setRenter(String renter) {
		this.renter = renter;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
