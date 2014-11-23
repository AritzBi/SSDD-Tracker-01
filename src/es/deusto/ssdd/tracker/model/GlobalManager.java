package es.deusto.ssdd.tracker.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import es.deusto.ssdd.tracker.vo.ActiveTracker;
import es.deusto.ssdd.tracker.vo.Tracker;

public class GlobalManager {

	private Tracker tracker;
	private static GlobalManager instance;

	private RedundancyManager redundancyManager;
	private UDPManager udpManager;
	private TopicManager topicManager;
	private QueueManager queueManager;

	private GlobalManager() {
		tracker = new Tracker();

	}

	/**
	 * Method used to start a Thread for Redundancy Manager and UDP Manager
	 */
	public void start() {
		topicManager = TopicManager.getInstance();
		queueManager = QueueManager.getInstance();

		new Thread(redundancyManager).start();
		new Thread(udpManager).start();
	}

	/**
	 * Method used to get a instance of the object Constructor is set as private
	 * 
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
	 * Method used to connect a new tracker with the ip, port and id and start
	 * the associated threads
	 * 
	 * @param ip
	 * @param port
	 * @param portForPeers
	 * @param id
	 */
	public void connect(String ipAddress, int port, int portForPeers, String id) {
		if (id != null && !id.equals("")) {
			redundancyManager.setStopListeningPackets(false);
			redundancyManager.setStopThreadKeepAlive(false);
			redundancyManager.setStopThreadCheckerKeepAlive(false);

			udpManager.setStopListeningPackets(false);
			udpManager.setStopThreadAnnounceTests(false);
		}
		tracker.setId(id);
		tracker.setPort(port);
		tracker.setPortForPeers(portForPeers);
		tracker.setIpAddress(ipAddress);
		tracker.setMaster(false);
		start();
	}

	/**
	 * Method used to disconnect the tracker
	 */
	public void disconnect() {
		redundancyManager.setStopListeningPackets(true);
		redundancyManager.setStopThreadKeepAlive(true);
		redundancyManager.setStopThreadCheckerKeepAlive(true);
		redundancyManager.setWaitingToHaveID(true);
		udpManager.setStopListeningPackets(true);
		udpManager.setStopThreadAnnounceTests(true);
		getTracker().getTrackersActivos().clear();
		redundancyManager.notifyObservers("DisconnectTracker");

		topicManager.close();
		queueManager.close();

	}

	public List<ActiveTracker> getActiveTrackers() {
		if (getTracker().getTrackersActivos() != null) {
			List<ActiveTracker> listActiveTrackers = new ArrayList<ActiveTracker>();
			Collection<ActiveTracker> activeTrackers = getTracker()
					.getTrackersActivos().values();
			if (activeTrackers != null) {
				listActiveTrackers.addAll(activeTrackers);
			}
			return listActiveTrackers;
		} else
			return new ArrayList<ActiveTracker>();
	}

	public RedundancyManager getRedundancyManager() {
		return redundancyManager;
	}

	public void setRedundancyManager(RedundancyManager redundancyManager) {
		this.redundancyManager = redundancyManager;
	}

	public UDPManager getUdpManager() {
		return udpManager;
	}

	public void setUdpManager(UDPManager udpManager) {
		this.udpManager = udpManager;
	}
	
}
