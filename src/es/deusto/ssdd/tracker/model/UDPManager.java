package es.deusto.ssdd.tracker.model;

import java.net.DatagramPacket;
import java.util.List;
import java.util.Observer;

import es.deusto.ssdd.tracker.vo.Tracker;

public class UDPManager implements Runnable {

	private List<Observer> observers;

	/*** OBSERVABLE PATTERN IMPLEMENTATION **/
	
	public void addObserver(Observer o) {
		if (o != null && !this.observers.contains(o)) {
			this.observers.add(o);
		}
	}

	public void deleteObserver(Observer o) {
		this.observers.remove(o);
	}

	@SuppressWarnings("unused")
	private void notifyObservers(Object param) {
		for (Observer observer : this.observers) {
			if (observer != null) {
				observer.update(null, param);
			}
		}
	}
	
	/***[END] OBSERVABLE PATTERN IMPLEMENTATION **/

	public void receiveConnectionRequest(DatagramPacket packet) {
	}

	public void receiveAnnounceRequest(DatagramPacket packet) {
	}

	public void sendConnectionRequest(DatagramPacket packet) {
	}

	public void sendAnnounceRequest(DatagramPacket packet) {
	}

	public void connect(String ipAddress, String port, String id) {
		Tracker tracker = GlobalManager.getInstance().getTracker();
		tracker.setId(id);
		tracker.setIpAddress(ipAddress);
		tracker.setPort(port);
		GlobalManager.getInstance().setTracker(tracker);
	}

	@Override
	public void run() {

	}
}
