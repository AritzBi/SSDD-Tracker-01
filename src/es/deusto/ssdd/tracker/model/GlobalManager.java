package es.deusto.ssdd.tracker.model;

import es.deusto.ssdd.tracker.vo.Tracker;

public class GlobalManager {

	private Tracker tracker;
	private static GlobalManager instance;
	
	private DataManager dataManager;
	private RedundancyManager redundancyManager;
	private UDPManager udpManager;
	
	public static final String MULTICAST_IP_ADDRESS = "228.5.6.7";

	public GlobalManager() {
		tracker = new Tracker();
		
		dataManager = new DataManager();
		redundancyManager = new RedundancyManager();
		udpManager = new UDPManager();
		new Thread(dataManager).start();
		new Thread(redundancyManager).start();
		new Thread(udpManager).start();
	}

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
}
