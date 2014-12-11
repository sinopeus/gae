package ds.gae.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.google.appengine.api.datastore.Key;

@NamedQueries({
	@NamedQuery(name = "Notification.byRenter", 
				query = "SELECT notification " 
							+ "FROM Notification notification "
							+ "WHERE notification.renter = :renter "
							+ "ORDER BY notification.created DESC")					
	})
@Entity
public class Notification {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key key;
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	private String renter;
	private String msg;
	
	public Notification() {}
	
	public Notification(String renter, String msg) {
		this.setRenter(renter);
		this.setMsg(msg);
	}
	
	@PrePersist
	protected void onCreate() {
		created = new Date();
	}
	
	public Date getTime() {
		return created;
	}

	public String getRenter() {
		return renter;
	}

	protected void setRenter(String renter) {
		this.renter = renter;
	}

	public String getMsg() {
		return msg;
	}

	protected void setMsg(String msg) {
		this.msg = msg;
	}
}
