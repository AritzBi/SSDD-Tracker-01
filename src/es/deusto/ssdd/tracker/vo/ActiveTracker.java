package es.deusto.ssdd.tracker.vo;

import java.util.Date;

public class ActiveTracker {

	private String id;
	private boolean active;
	private Date lastKeepAlive;
	private boolean master;
	public static final int numColumns = 4;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public Date getLastKeepAlive() {
		return lastKeepAlive;
	}
	public void setLastKeepAlive(Date lastKeepAlive) {
		this.lastKeepAlive = lastKeepAlive;
	}
	public boolean isMaster() {
		return master;
	}
	public void setMaster(boolean master) {
		this.master = master;
	}
	@Override
	public String toString() {
		return "Id: " + id + "master: " + master;
	}
}
