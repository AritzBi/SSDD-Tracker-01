package es.deusto.ssdd.tracker.vo;
import java.util.concurrent.ConcurrentHashMap;


public class Tracker {

	private String id;
	private String ipAddress;
	//Port --> port for communication between trackers
	private int port;
	//PortForPeers --> port for communication between peer to tracker
	private int portForPeers;
	private ConcurrentHashMap<String,ActiveTracker> trackersActivos;
	private boolean master;
	
	public Tracker () {
	}
	
	public Tracker ( String id, String ipAddress, int port, int portForPeers )
	{
		this.id = id;
		this.ipAddress = ipAddress;
		this.port = port;
		this.portForPeers = portForPeers;
		
	}
	
	public Tracker ( String id , ConcurrentHashMap<String,ActiveTracker> trackersActivos )
	{
		this.id = id;
		this.trackersActivos = trackersActivos;
	}
	
	public void addActiveTracker ( ActiveTracker activeTracker )
	{
		trackersActivos.put ( activeTracker.getId() , activeTracker );
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

	public ConcurrentHashMap<String,ActiveTracker> getTrackersActivos() {
		if (trackersActivos == null )
		{
			trackersActivos = new ConcurrentHashMap<String,ActiveTracker>();
		}
		return trackersActivos;
	}

	public void setTrackersActivos(ConcurrentHashMap<String,ActiveTracker> trackersActivos) {
		this.trackersActivos = trackersActivos;
	}

	public boolean isMaster() {
		return master;
	}

	public void setMaster(boolean master) {
		this.master = master;
	}
	
}
