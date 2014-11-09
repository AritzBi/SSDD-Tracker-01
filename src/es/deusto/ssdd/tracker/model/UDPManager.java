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
	private boolean stopThreadAnnounceTests=false;
	private static String READY_TO_STORE_MESSAGE = "ReadyToStore";


	public UDPManager() {
		observers = new ArrayList<Observer>();
		globalManager = GlobalManager.getInstance();
	}

	@Override
	public void run() {
		createSocket();
		socketListeningPackets();
		//generateAnnounceTests();
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
					this.sendReadyToStoreMessage();
				}
				
				String messageReceived = new String(packet.getData());
				System.out.println("Received message: " + messageReceived);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void generateAnnounceTests() {
		try {
			
			final MulticastSocket testSocket = new MulticastSocket(globalManager.getTracker().getPort());
			inetAddress = InetAddress.getByName(globalManager.getTracker()
					.getIpAddress());
			testSocket.joinGroup(inetAddress);
			Thread threadSendAnnounceTests = new Thread() {
				public void run() {
					while (!stopThreadAnnounceTests) {
						try {
							Thread.sleep(5000);
							sendTestAnnouceRequest(testSocket);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			};
			threadSendAnnounceTests.start();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void sendTestAnnouceRequest(MulticastSocket testSocket){
		String message = "ANNOUNCE:";
		byte[] messageBytes = message.getBytes();
		DatagramPacket datagramPacket = new DatagramPacket(messageBytes,
				messageBytes.length, inetAddress, globalManager.getTracker()
						.getPort());
		try {
			socket.send(datagramPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void sendReadyToStoreMessage() {

		String message = generateReadyToStoreMessage();
		byte[] messageBytes = message.getBytes();
		DatagramPacket datagramPacket = new DatagramPacket(messageBytes,
				messageBytes.length, inetAddress, globalManager.getTracker()
						.getPort());
		writeSocket(datagramPacket);
	}
	
	private String generateReadyToStoreMessage() {
		return globalManager.getTracker().getId() + ":" + READY_TO_STORE_MESSAGE + ":";
	}
	/**
	 * Method used to know if the received UDP packet is a connect request
	 * message
	 * 
	 * @param packet
	 * @return
	 */
	private boolean isConnectRequestMessage(DatagramPacket packet) {
		String [] message = new String(packet.getData()).split(":");
		return message[1].equals("ANNOUNCE");
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
	@SuppressWarnings("unused")
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
	
	public boolean isStopThreadAnnounceTests() {
		return stopThreadAnnounceTests;
	}

	public void setStopThreadAnnounceTests(boolean stopThreadAnnounceTests) {
		this.stopThreadAnnounceTests = stopThreadAnnounceTests;
	}
}
