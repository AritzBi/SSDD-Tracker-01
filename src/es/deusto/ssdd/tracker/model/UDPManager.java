package es.deusto.ssdd.tracker.model;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import es.deusto.ssdd.tracker.vo.Tracker;

public class UDPManager implements Runnable {

	private List<Observer> observers;
	private GlobalManager globalManager;
	
	public UDPManager () {
		observers = new ArrayList<Observer>();
		globalManager = GlobalManager.getInstance();
		try {
			MulticastSocket socket = new MulticastSocket(4878);
			InetAddress inetAddress = InetAddress.getByName( globalManager.MULTICAST_IP_ADDRESS);
			socket.joinGroup(inetAddress);
			
			DatagramPacket packet;
			while ( true )
			{
				byte[] buf = new byte[256];
				packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				
				String messageReceived = new String( packet.getData() );
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

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
	//TODO
	public void receiveConnectionRequest(DatagramPacket packet) {
	}
	//TODO
	public void receiveAnnounceRequest(DatagramPacket packet) {
	}
	//TODO
	public void sendConnectionResponse(DatagramPacket packet) {
	}
	//TODO
	public void sendAnnounceResponse(DatagramPacket packet) {
	}

	public void connect(String ipAddress, String port, String id) {
		System.out.println("The tracker is now started!");
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
