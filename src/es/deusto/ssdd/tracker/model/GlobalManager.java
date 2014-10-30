package es.deusto.ssdd.tracker.model;

import es.deusto.ssdd.tracker.vo.Tracker;

public class GlobalManager {

	private static Tracker tracker;
	private static GlobalManager instance;
	
	private DataManager dataManager;
	private RedundancyManager redundancyManager;
	private UDPManager udpManager;
	
	private GlobalManager() {
		tracker = new Tracker( );
	}
	
	public void start () 
	{
		dataManager = new DataManager();
		redundancyManager = new RedundancyManager();
		udpManager = new UDPManager();
		new Thread(dataManager).start();
		new Thread(redundancyManager).start();
		new Thread(udpManager).start();
	}

	public static GlobalManager getInstance() {
		if (instance == null) {
			instance = new GlobalManager( );
		}
		return instance;
	}

	public Tracker getTracker() {
		return tracker;
	}

	public void setTracker(Tracker tracker) {
		this.tracker = tracker;
	}
	
	public void connect ( String ip, int port , String id )
	{
		tracker.setId(id);
		tracker.setPort(port);
		tracker.setIpAddress(ip);
		start();
	}
}
