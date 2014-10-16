package es.deusto.ssdd.tracker.vo;
import java.util.ArrayList;
import java.util.List;


public class Tracker {

	private String id;
	private String ipAddress;
	private String port;
	private List<Tracker> trackersActivos;
	
	public Tracker ( String id )
	{
		this.id = id;
		trackersActivos = new ArrayList<Tracker>();
	}
	
	public Tracker ( String id , List<Tracker> trackersActivos )
	{
		this.id = id;
		this.trackersActivos = trackersActivos;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public List<Tracker> getTrackersActivos() {
		return trackersActivos;
	}

	public void setTrackersActivos(List<Tracker> trackersActivos) {
		this.trackersActivos = trackersActivos;
	}
	
}
