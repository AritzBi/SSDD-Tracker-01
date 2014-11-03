package es.deusto.ssdd.tracker.model;

import es.deusto.ssdd.tracker.vo.Tracker;

public class GlobalManager {

	private Tracker tracker;
	private static GlobalManager instance;
	
	private RedundancyManager redundancyManager;
	private UDPManager udpManager;
	private GlobalManager() {
		tracker = new Tracker();
	}
	
	/**
	 * Method used to start a Thread for Redundancy Manager and UDP Manager
	 */
	public void start() 
	{
		redundancyManager = new RedundancyManager();
		udpManager = new UDPManager();
		new Thread(redundancyManager).start();
		new Thread(udpManager).start();
	}

	/**
	 * Method used to get a instance of the object
	 * Constructor is set as private
	 * @return
	 */
	public static GlobalManager getInstance() {
		if (instance == null) {
			instance = new GlobalManager();
		}
		return instance;
	}

	public Tracker getTracker() {
		return tracker;
	}

	public void setTracker(Tracker tracker) {
		this.tracker = tracker;
	}
	
	/**
	 * Method used to connect a new tracker with the ip, port and id and start the associated threads
	 * @param ip
	 * @param port
	 * @param portForPeers
	 * @param id
	 */
	public void connect ( String ipAddress, int port , int portForPeers, String id )
	{
		tracker.setId(id);
		tracker.setPort(port);
		tracker.setPortForPeers(portForPeers);
		tracker.setIpAddress(ipAddress);
		start();
	}
}
