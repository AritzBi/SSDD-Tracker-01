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
import java.util.Random;

import es.deusto.ssdd.tracker.udp.messages.AnnounceRequest;
import es.deusto.ssdd.tracker.udp.messages.AnnounceResponse;
import es.deusto.ssdd.tracker.udp.messages.BitTorrentUDPMessage;
import es.deusto.ssdd.tracker.udp.messages.BitTorrentUDPMessage.Action;
import es.deusto.ssdd.tracker.udp.messages.ConnectRequest;
import es.deusto.ssdd.tracker.udp.messages.ConnectResponse;
import es.deusto.ssdd.tracker.udp.messages.PeerInfo;
import es.deusto.ssdd.tracker.udp.messages.ScrapeRequest;
import es.deusto.ssdd.tracker.vo.Peer;
import es.deusto.ssdd.tracker.vo.Utils;

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
					processConnectRequestMessageAndSendResponseMessage(packet.getData(),packet.getAddress(), packet.getPort());
				} else if (isAnnounceRequestMessage(packet)) {
					if ( processAnnounceRequestMessageAndSendResponseMessage(packet.getData(), packet.getAddress(), packet.getPort() ) )
					{
						AnnounceRequest msgAnnounceRequest = AnnounceRequest.parse(packet.getData());
						topicManager.publishReadyToStoreMessage( msgAnnounceRequest.getConnectionId() );
					}
				}
				else if ( isScrapeRequestMessage(packet)) {
					
				}
				String messageReceived = new String(packet.getData());
				System.out.println("Received message: " + messageReceived);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processConnectRequestMessageAndSendResponseMessage(byte[] data, InetAddress address,
			int port) {

		ConnectRequest msgConnectRequest = ConnectRequest.parse(data);
		
		// store data over memory...
		Peer peer = new Peer();
		peer.setIpAddress(address.getHostAddress());
		peer.setPort(port);
		
		String response = null;
		//check if the connectionId has been initialized correctly by the peer
		if ( msgConnectRequest.getConnectionId() != Long.decode("0x41727101980") )
		{
			response = "The connection id must be initialized to 0x41727101980";
		}
		
		if ( response == null )
		{
			//Calculate an unique connection id number that identifies the peer
			long connectionId = new Random().nextLong();
			
			response = dataManager.addPeerToMemory(peer, msgConnectRequest.getConnectionId());
			
			if ( response.contains("OK") )
			{
					ConnectResponse connectResponse = new ConnectResponse();
					connectResponse.setTransactionId(msgConnectRequest.getTransactionId());
					connectResponse.setConnectionId( connectionId );
					sendResponseMessage(connectResponse, address, port);
			}
		}
		
		if ( response != null && !response.contains("OK") )
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
		peer.setLeft(msgAnnounceRequest.getLeft());
		peer.setId(msgAnnounceRequest.getPeerId());
		
		String response = null;
		
		//Update the ip and the port by the data coming from the AnnounceRequest
		PeerInfo peerInfo = msgAnnounceRequest.getPeerInfo();
		//Set to 0 if you want the tracker to use the sender of this udp packet.
		if ( peerInfo != null && peerInfo.equals(0) )
			peer.setIpAddress(address.getHostAddress());
		else if ( peerInfo != null )
			peer.setIpAddress( PeerInfo.toStringIpAddress( peerInfo.getIpAddress() ) );
		else
			response = "Specify the IP address";
		
		peer.setPort(peerInfo.getPort());
		response = dataManager.updatePeerMemory(peer, msgAnnounceRequest.getConnectionId() );
		
		if ( response != null && response.contains("OK") )
		{
			sendAnnounceResponseMessage(msgAnnounceRequest, address, port );
			return true;
		}
		else
		{
			sendErrorMessage(response, msgAnnounceRequest.getTransactionId(), address, port);
			return false;
		}
		
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
		announceResponse.setTransactionId(msgAnnounceRequest.getTransactionId());
		announceResponse.setLeechers(leechers.size());
		announceResponse.setSeeders(seeders.size());
		//We calculate the total swarn for the infohash
		seeders.addAll(leechers);
		
		//numWant: The maximum number of peers you want in the reply. Use -1 for default.
		List<PeerInfo> peersToSend = null;
		int maxPeers = calculateNumberPeersAllowedForPeer(msgAnnounceRequest.getPeerId());
		if ( msgAnnounceRequest.getNumWant() == -1 )
		{
			if ( seeders.size() > maxPeers )
			{
				peersToSend = seeders.subList(0, maxPeers);
			}
			else
			{
				peersToSend = seeders;
			}
			
		}
		else
		{
			if ( msgAnnounceRequest.getNumWant() > maxPeers )
			{
				peersToSend = seeders.subList(0, maxPeers);
			}
			else
			{
				peersToSend = seeders;
			}
		}
		announceResponse.setPeers(peersToSend);
		
		announceResponse.setInterval(calculateIntervalForPeer ( msgAnnounceRequest.getPeerId()) );
		
		sendResponseMessage(announceResponse, address, port);
	}
	
	private int calculateNumberPeersAllowedForPeer ( String peerId )
	{
		return 10;
	}
	
	/**
	 * Method to calculate the interval per peer
	 * @param peerId
	 * @return
	 */
	private int calculateIntervalForPeer ( String peerId )
	{
		return 10;
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
	
	private boolean isScrapeRequestMessage ( DatagramPacket packet )
	{
		boolean isScrapeRequest = false;
		
		if ( packet.getLength() >= 16 )
		{
			isScrapeRequest = true;
		}
		if ( isScrapeRequest)
		{
			ScrapeRequest scrapeRequest = ScrapeRequest.parse(Utils.parsearArrayBytes(packet.getData(), packet.getLength()) );
			
			if ( !scrapeRequest.getAction().equals(Action.SCRAPE) )
			{
				isScrapeRequest = false;
			}
		}
		return isScrapeRequest;
	}
	
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
