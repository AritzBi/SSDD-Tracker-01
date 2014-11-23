package es.deusto.ssdd.tracker.model;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Observer;

import es.deusto.ssdd.tracker.vo.Tracker;

public class UDPManager implements Runnable {

	private List<Observer> observers;
	private GlobalManager globalManager;
	private TopicManager topicManager;

	private MulticastSocket socket;
	private InetAddress inetAddress;
	private boolean stopListeningPackets = false;
	private boolean stopThreadAnnounceTests = false;
	

	public UDPManager() {
		observers = new ArrayList<Observer>();
		globalManager = GlobalManager.getInstance();
		topicManager = TopicManager.getInstance();
	}

	@Override
	public void run() {
		topicManager = TopicManager.getInstance();
		createSocket();
		generateAnnounceTests();
		socketListeningPackets();

	}

	private void createSocket() {
		try {
			socket = new MulticastSocket(globalManager.getTracker()
					.getPortForPeers());
			inetAddress = InetAddress.getByName(globalManager.getTracker()
					.getIpAddress());
			autoSetNetworkInterface(socket);
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
				if (isConnectRequestMessage(packet)) {
					// New thread to send a response
				} else if (isAnnounceRequestMessage(packet)) {
						topicManager.publishReadyToStoreMessage();
					
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

			final MulticastSocket testSocket = new MulticastSocket(
					globalManager.getTracker().getPort());
			inetAddress = InetAddress.getByName(globalManager.getTracker()
					.getIpAddress());
			autoSetNetworkInterface(testSocket);
			testSocket.joinGroup(inetAddress);
			Thread threadSendAnnounceTests = new Thread() {
				public void run() {
					while (!stopThreadAnnounceTests) {
						if (getTracker().isMaster()) {
							try {
								Thread.sleep(5000);
								sendTestAnnouceRequest(testSocket);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
			};
			threadSendAnnounceTests.start();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void sendTestAnnouceRequest(MulticastSocket testSocket) {
		String message = "ANNOUNCE:";
		byte[] messageBytes = message.getBytes();
		DatagramPacket datagramPacket = new DatagramPacket(messageBytes,
				messageBytes.length, inetAddress, globalManager.getTracker()
						.getPortForPeers());
		try {
			socket.send(datagramPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}/**

	private void sendReadyToStoreMessage() {
		String message = generateReadyToStoreMessage();
		byte[] messageBytes = message.getBytes();
		DatagramPacket datagramPacket = new DatagramPacket(messageBytes,
				messageBytes.length, inetAddress, globalManager.getTracker()
						.getPort());
		writeSocket(datagramPacket);
	}

	private String generateReadyToStoreMessage() {
		return globalManager.getTracker().getId() + ":"
				+ READY_TO_STORE_MESSAGE + ":";
	}
	**/
	/**
	 * Method used to know if the received UDP packet is a connect request
	 * message
	 * 
	 * @param packet
	 * @return
	 */
	private boolean isConnectRequestMessage(DatagramPacket packet) {
		return false;
	}

	/**
	 * Method used to know if the received UDP packet is an announce request
	 * message
	 * 
	 * @param packet
	 * @return
	 */
	private boolean isAnnounceRequestMessage(DatagramPacket packet) {
		String[] message = new String(packet.getData()).split(":");
		return message[0].equals("ANNOUNCE");
	}

	/**
	 * Method used to write over the socket
	 * 
	 * @param datagramPacket
	 */
//	private synchronized void writeSocket(DatagramPacket datagramPacket) {
//		try {
//			socket.send(datagramPacket);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

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

	private Tracker getTracker() {
		return globalManager.getTracker();
	}
	private void autoSetNetworkInterface(MulticastSocket vSocket) {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			Enumeration<InetAddress> addresses;
			InetAddress inetAddress;
			NetworkInterface interfaceAux;
			boolean interfaceSelected = false;
			
			//Iterate over all network interfaces
			while (interfaces.hasMoreElements() && !interfaceSelected) {
				interfaceAux = interfaces.nextElement();
				
				//Check wether the interface is up
				if (interfaceAux.isUp()) {					
					addresses = interfaceAux.getInetAddresses();
					
					while (addresses.hasMoreElements()) {
						inetAddress = addresses.nextElement();
						
						//Check that the InetAddres is no IPv6
						if (!(inetAddress instanceof Inet6Address)) {						
							vSocket.setNetworkInterface(interfaceAux);						
							interfaceSelected = true;
							
							System.out.println("- Selected network interface: " + interfaceAux + " - " + inetAddress);
							
							break;
						}
					}
				}
			}
		} catch (Exception ex) {
			
		}
	}

}
