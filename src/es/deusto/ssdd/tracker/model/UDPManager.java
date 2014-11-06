package es.deusto.ssdd.tracker.model;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

public class UDPManager implements Runnable {

	private List<Observer> observers;
	private GlobalManager globalManager;

	private MulticastSocket socket;
	private InetAddress inetAddress;
	private boolean stopListeningPackets = false;

	public UDPManager() {
		observers = new ArrayList<Observer>();
		globalManager = GlobalManager.getInstance();
	}

	@Override
	public void run() {
		createSocket();
		socketListeningPackets();
	}

	private void createSocket() {
		try {
			socket = new MulticastSocket(globalManager.getTracker()
					.getPortForPeers());
			inetAddress = InetAddress.getByName(globalManager.getTracker()
					.getIpAddress());
			socket.joinGroup(inetAddress);
		} catch (IOException e) {
			System.out.println("Error creating socket " + e.getMessage());
		}
	}

	private void socketListeningPackets() {
		try {
			DatagramPacket packet;
			while (!stopListeningPackets) {
				byte[] buf = new byte[256];
				packet = new DatagramPacket(buf, buf.length);
				System.out.println("Before socket");
				socket.receive(packet);
				System.out.println("Post socket");
				if (isConnectRequestMessage(packet))
				{
					//New thread to send a response
				}
				else if (isAnnounceRequestMessage (packet ))
				{
					//New thread to send the associated response message
				}
				
				String messageReceived = new String(packet.getData());
				System.out.println("Received message: " + messageReceived);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method used to know if the received UDP packet is a connect request
	 * message
	 * 
	 * @param packet
	 * @return
	 */
	private boolean isConnectRequestMessage(DatagramPacket packet) {
		return true;
	}

	/**
	 * Method used to know if the received UDP packet is an announce request
	 * message
	 * 
	 * @param packet
	 * @return
	 */
	private boolean isAnnounceRequestMessage(DatagramPacket packet) {
		return true;
	}

	/**
	 * Method used to write over the socket
	 * 
	 * @param datagramPacket
	 */
	private synchronized void writeSocket(DatagramPacket datagramPacket) {
		try {
			socket.send(datagramPacket);
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

	/*** [END] OBSERVABLE PATTERN IMPLEMENTATION **/

	// TODO
	public void receiveConnectionRequest(DatagramPacket packet) {
	}

	// TODO
	public void receiveAnnounceRequest(DatagramPacket packet) {
	}

	// TODO
	public void sendConnectionResponse(DatagramPacket packet) {
	}

	// TODO
	public void sendAnnounceResponse(DatagramPacket packet) {
	}

	public boolean isStopListeningPackets() {
		return stopListeningPackets;
	}

	public void setStopListeningPackets(boolean stopListeningPackets) {
		this.stopListeningPackets = stopListeningPackets;
	}
}
