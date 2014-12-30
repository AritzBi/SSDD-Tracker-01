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

import es.deusto.ssdd.tracker.udp.messages.AnnounceRequest;
import es.deusto.ssdd.tracker.udp.messages.AnnounceResponse;
import es.deusto.ssdd.tracker.udp.messages.BitTorrentUDPMessage;
import es.deusto.ssdd.tracker.udp.messages.BitTorrentUDPMessage.Action;
import es.deusto.ssdd.tracker.udp.messages.ConnectRequest;
import es.deusto.ssdd.tracker.udp.messages.ConnectResponse;
import es.deusto.ssdd.tracker.udp.messages.PeerInfo;
import es.deusto.ssdd.tracker.vo.Peer;

public class UDPManager implements Runnable {

	private List<Observer> observers;
	private GlobalManager globalManager;
	private DataManager dataManager;
	private TopicManager topicManager;

	private MulticastSocket socket;
	private InetAddress inetAddress;
	private boolean stopListeningPackets = false;
	private boolean stopThreadAnnounceTests = false;

	public UDPManager() {
		observers = new ArrayList<Observer>();
		globalManager = GlobalManager.getInstance();
		dataManager = DataManager.getInstance();
		topicManager = TopicManager.getInstance();
	}

	@Override
	public void run() {
		topicManager = TopicManager.getInstance();
		createSocket();
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
			System.out.println("# Error creating socket " + e.getMessage());
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
					processConnectRequestMessage(packet.getData(),packet.getAddress(), packet.getPort());
				} else if (isAnnounceRequestMessage(packet)) {
					if ( processAnnounceRequestMessageAndSendResponseMessage(packet.getData(), packet.getAddress(), packet.getPort() ) )
					{
						AnnounceRequest msgAnnounceRequest = AnnounceRequest.parse(packet.getData());
						topicManager.publishReadyToStoreMessage( msgAnnounceRequest.getConnectionId() );
					}
				}
				String messageReceived = new String(packet.getData());
				System.out.println("Received message: " + messageReceived);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processConnectRequestMessage(byte[] data, InetAddress address,
			int port) {

		ConnectRequest msgConnectRequest = ConnectRequest.parse(data);
		
		// store data over memory...
		Peer peer = new Peer();
		peer.setIpAddress(address.getHostAddress());
		peer.setPort(port);
		String response = dataManager.addPeerToMemory(peer, msgConnectRequest.getConnectionId());
		
		if ( response.contains("OK") ) {
			ConnectResponse connectResponse = new ConnectResponse();
			
			connectResponse.setConnectionId(msgConnectRequest.getConnectionId());
			connectResponse.setTransactionId(msgConnectRequest.getTransactionId());
			
			sendResponseMessage(connectResponse, address, port);
		}
		else
		{
			sendErrorMessage ( response , msgConnectRequest.getTransactionId(), address, port);
		}

	}
	
	/**
	 * Method used to send an error message from the part of the tracker
	 * @param message
	 * @param transactionId
	 * @param address (for the socket)
	 * @param port (for the socket)
	 */
	private void sendErrorMessage ( String message, int transactionId, InetAddress address, int port )
	{
		es.deusto.ssdd.tracker.udp.messages.Error error = new es.deusto.ssdd.tracker.udp.messages.Error();
		error.setMessage(message);
		error.setTransactionId(transactionId);
		sendResponseMessage(error, address, port);
	}

	private boolean processAnnounceRequestMessageAndSendResponseMessage(byte[] data,
			InetAddress address, int port) {
		AnnounceRequest msgAnnounceRequest = AnnounceRequest.parse(data);
		// store data over memory..
		Peer peer = new Peer();
		peer.setDownloaded(msgAnnounceRequest.getDownloaded());
		peer.setUploaded(msgAnnounceRequest.getUploaded());
		peer.setId(msgAnnounceRequest.getPeerId());
		peer.setIpAddress(address.getHostAddress());
		peer.setPort(port);

		String response = dataManager.updatePeerMemory(peer, msgAnnounceRequest.getConnectionId() );
		if ( response.contains("OK") )
		{
			sendAnnounceResponseMessage(msgAnnounceRequest, address, port );
			return true;
		}
		
		return false;
	}
	
	/**
	 * Method to send the announce response to the peers
	 * It is send as peers both the seeders and the leechers associated to the info_hash
	 * @param msgAnnounceRequest
	 */
	private void sendAnnounceResponseMessage ( AnnounceRequest msgAnnounceRequest, InetAddress address, int port )
	{
		List<PeerInfo> seeders = dataManager.findPeersByInfoHash(msgAnnounceRequest.getInfoHash(), true, false );
		List<PeerInfo> leechers = dataManager.findPeersByInfoHash(msgAnnounceRequest.getInfoHash(), false, true);
		
		AnnounceResponse announceResponse = new AnnounceResponse();
		announceResponse.setLeechers(leechers.size());
		announceResponse.setSeeders(seeders.size());
		seeders.addAll(leechers);
		announceResponse.setPeers(seeders);
		
		sendResponseMessage(announceResponse, address, port);
	}
	
	/**
	 * Global method used for sending a BitTorrentUDPMessage to the socket
	 * @param message
	 * @param address
	 * @param port
	 */
	private void sendResponseMessage ( BitTorrentUDPMessage message , InetAddress address , int port )
	{
		try {
			byte [] bytes = message.getBytes();
			DatagramPacket reply = new DatagramPacket(bytes, bytes.length, address, port);
			socket.send(reply);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**private void generateAnnounceTests() {
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
	}

	 * private void sendReadyToStoreMessage() { String message =
	 * generateReadyToStoreMessage(); byte[] messageBytes = message.getBytes();
	 * DatagramPacket datagramPacket = new DatagramPacket(messageBytes,
	 * messageBytes.length, inetAddress, globalManager.getTracker() .getPort());
	 * writeSocket(datagramPacket); }
	 * 
	 * private String generateReadyToStoreMessage() { return
	 * globalManager.getTracker().getId() + ":" + READY_TO_STORE_MESSAGE + ":";
	 * }
	 **/
	
	/**
	 * Method used to know if the received UDP packet is a connect request
	 * message
	 * 
	 * @param packet
	 * @return
	 */
	private boolean isConnectRequestMessage(DatagramPacket packet) {
		boolean isConnectRequest = true;
		
		if ( packet.getLength() != 16 )
		{
			isConnectRequest = false;
		}
		if ( isConnectRequest )
		{
			ConnectRequest msgConnectRequest = ConnectRequest.parse(packet.getData());
			
			if ( !msgConnectRequest.getAction().equals(Action.CONNECT) )
			{
				isConnectRequest = false;
			}
		}
		
		return isConnectRequest;
	}

	/**
	 * Method used to know if the received UDP packet is an announce request
	 * message
	 * 
	 * @param packet
	 * @return
	 */
	private boolean isAnnounceRequestMessage(DatagramPacket packet) {
		boolean isAnnounceRequest = true;
		
		if ( packet.getLength() != 98 )
		{
			isAnnounceRequest = false;
		}
		if ( isAnnounceRequest ) {
			AnnounceRequest msgAnnounceRequest = AnnounceRequest.parse(packet.getData());
			if ( !msgAnnounceRequest.getAction().equals(Action.ANNOUNCE) )
			{
				isAnnounceRequest = false;
			}
		}

		return isAnnounceRequest;
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

	private void autoSetNetworkInterface(MulticastSocket vSocket) {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface
					.getNetworkInterfaces();
			Enumeration<InetAddress> addresses;
			InetAddress inetAddress;
			NetworkInterface interfaceAux;
			boolean interfaceSelected = false;

			// Iterate over all network interfaces
			while (interfaces.hasMoreElements() && !interfaceSelected) {
				interfaceAux = interfaces.nextElement();

				// Check wether the interface is up
				if (interfaceAux.isUp()) {
					addresses = interfaceAux.getInetAddresses();

					while (addresses.hasMoreElements()) {
						inetAddress = addresses.nextElement();

						// Check that the InetAddres is no IPv6
						if (!(inetAddress instanceof Inet6Address)) {
							vSocket.setNetworkInterface(interfaceAux);
							interfaceSelected = true;

							System.out.println("- Selected network interface: "
									+ interfaceAux + " - " + inetAddress);

							break;
						}
					}
				}
			}
		} catch (Exception ex) {

		}
	}

}
