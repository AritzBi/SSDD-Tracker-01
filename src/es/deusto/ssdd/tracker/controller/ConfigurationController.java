package es.deusto.ssdd.tracker.controller;

import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.deusto.ssdd.tracker.model.GlobalManager;
import es.deusto.ssdd.tracker.model.RedundancyManager;
import es.deusto.ssdd.tracker.model.UDPManager;
import es.deusto.ssdd.tracker.vo.Tracker;

public class ConfigurationController {
	private UDPManager udpManager;
	private GlobalManager globalManager;
	private RedundancyManager redundancyManager;

	private Pattern pattern;
	private Matcher matcher;

	private static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	public ConfigurationController(UDPManager udpManager,
			RedundancyManager redundancyManager) {
		this.udpManager = udpManager;
		this.redundancyManager = redundancyManager;
		globalManager = GlobalManager.getInstance();
		pattern = Pattern.compile(IPADDRESS_PATTERN);
	}

	public void addObserver(Observer o) {
		udpManager.addObserver(o);
	}

	public void addObserverRedundandcy(Observer o) {
		redundancyManager.addObserver(o);
	}

	public void deleteObserver(Observer o) {
		udpManager.deleteObserver(o);
	}

	public void deleteObserverRedundancy(Observer o) {
		redundancyManager.deleteObserver(o);
	}

	/**
	 * Method used to connect the tracker with a specified IP, port and id
	 * 
	 * @param ipAddress
	 * @param port
	 * @param id
	 */
	public void connect(String ipAddress, int port, int portForPeers, String id) {
		globalManager.connect(ipAddress, port, portForPeers, id);
	}

	/**
	 * Method used to validate if an IP address matches a pattern
	 * 
	 * @param ip
	 * @return true: matches the IP address pattern
	 */
	public boolean checkIpAddress(String ip) {
		matcher = pattern.matcher(ip);
		return matcher.matches();
	}

	/**
	 * Method used to disconnect the tracker
	 */
	public void disconnect() {
		// We need to stop all the running threads on the tracker
		globalManager.disconnect();
	}

	public Tracker getTracker() {
		return globalManager.getTracker();
	}
}
