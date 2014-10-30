package es.deusto.ssdd.tracker.vo;
import java.util.ArrayList;
import java.util.List;


public class Tracker {

	private String id;
	private String ipAddress;
	//Port --> port for communication between trackers
	private int port;
	//PortForPeers --> port for communication between peer to tracker
	private int portForPeers;
	private List<Tracker> trackersActivos;
	
	public Tracker () {
	}
	
	public Tracker ( String id, String ipAddress, int port, int portForPeers )
	{
		this.id = id;
		this.ipAddress = ipAddress;
		this.port = port;
		this.portForPeers = portForPeers;
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

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPortForPeers() {
		return portForPeers;
	}

	public void setPortForPeers(int portForPeers) {
		this.portForPeers = portForPeers;
	}

	public List<Tracker> getTrackersActivos() {
		return trackersActivos;
	}

	public void setTrackersActivos(List<Tracker> trackersActivos) {
		this.trackersActivos = trackersActivos;
	}
	
}
