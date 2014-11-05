package es.deusto.ssdd.tracker.model;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import es.deusto.ssdd.tracker.vo.ActiveTracker;

//TODO: Proceso de elección del master
//TODO: Enviar keep alive
//TODO: Ready para guardar información
//TODO: Ok, poodeis guardar información
//TODO Cuando quitar un tracker
//TODO  Recibir mensaje de los demás trackers 

public class RedundancyManager implements Runnable {

	private List<Observer> observers;
	private GlobalManager globalManager;

	private MulticastSocket socket;
	private InetAddress inetAddress;
	private boolean stopListeningPackets = false;
	private boolean stopThreadKeepAlive = false;
	private boolean stopThreadCheckerKeepAlive = false;
	private ConcurrentHashMap<String,Boolean> readyToStoreTrackers;
	private static String TYPE_KEEP_ALIVE_MESSAGE = "KeepAlive";
	private static String READY_TO_STORE_MESSAGE = "ReadyToStore";

	public RedundancyManager() {
		observers = new ArrayList<Observer>();
		globalManager = GlobalManager.getInstance();
		readyToStoreTrackers=new ConcurrentHashMap<String,Boolean>();
		
	}

	@Override
	public void run() {
		createSocket();
		generateThreadToSendKeepAliveMessages();
		socketListeningPackets();
		generateThreadToCheckActiveTrackers();
	}

	private void createSocket() {
		try {
			socket = new MulticastSocket(globalManager.getTracker().getPort());
			inetAddress = InetAddress.getByName(globalManager.getTracker()
					.getIpAddress());
			socket.joinGroup(inetAddress);
		} catch (IOException e) {
			System.out.println("Error creating socket " + e.getMessage());
		}
	}

	private void generateThreadToSendKeepAliveMessages() {
		Thread threadSendKeepAliveMessages = new Thread() {
			public void run() {
				while (!stopThreadKeepAlive) {
					try {
						Thread.sleep(2000);
						sendKeepAliveMessage();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		threadSendKeepAliveMessages.start();
	}

	private void sendKeepAliveMessage() {

		String message = generateKeepAliveMessage();
		byte[] messageBytes = message.getBytes();
		DatagramPacket datagramPacket = new DatagramPacket(messageBytes,
				messageBytes.length, inetAddress, globalManager.getTracker()
						.getPort());
		writeSocket(datagramPacket);
	}
	
	private String generateKeepAliveMessage() {
		return globalManager.getTracker().getId() + ":" + TYPE_KEEP_ALIVE_MESSAGE;
	}

	private void socketListeningPackets() {
		try {
			DatagramPacket packet;
			while (!stopListeningPackets) {
				byte[] buf = new byte[256];
				packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);

				if ( isKeepAliveMessage(packet) )
				{
					saveActiveTracker ( packet ); //TODO:POSSIBLE THREAD?
					//Notify to the observers to change the list..
				}else if(isReadyToStore(packet)){
					if(globalManager.getTracker().isMaster())
						checkIfAllAreReadyToStore(packet);
				}
				String messageReceived = new String(packet.getData());
				System.out.println("Received info..." + messageReceived);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void checkIfAllAreReadyToStore(DatagramPacket packet){
		int num=globalManager.getTracker().getTrackersActivos().size();
		String[] messageReceived = new String ( packet.getData() ).split(":");
		String id = messageReceived[0];
		readyToStoreTrackers.put(id, true);
		int numReady=0;
		for ( Boolean bool : readyToStoreTrackers.values() ){
			if(bool)
				numReady++;
		}
		if(num == numReady){
			readyToStoreTrackers=new ConcurrentHashMap<String,Boolean>();
			//TODO SEND the data to the rest of trackers
		}
		
	}
	private boolean isReadyToStore(DatagramPacket packet){
		String [] message = new String(packet.getData()).split(":");
		return message[1].equals(READY_TO_STORE_MESSAGE);
	}
	private void saveActiveTracker ( DatagramPacket packet )
	{
		String[] messageReceived = new String ( packet.getData() ).split(":");
		String id = messageReceived[0];
		//Check if the id is not equals to the master tracker ID
		if ( !id.equals(globalManager.getTracker().getId() ) )
		{
			ConcurrentHashMap<String,ActiveTracker> activeTrackers = globalManager.getTracker().getTrackersActivos();
			if ( activeTrackers.contains(id ) )
			{
				ActiveTracker activeTracker = activeTrackers.get(id);
				activeTracker.setLastKeepAlive(new Date());
			}
			else
			{
				if ( globalManager.getTracker().isMaster()) //If the tracker is master tracker
				{
					List<String> idsActiveTrackers = getListActiveTrackers();
					if ( id.compareTo(globalManager.getTracker().getId()) == 1 && !idsActiveTrackers.contains(id) )
					{
						ActiveTracker activeTracker = new ActiveTracker();
						activeTracker.setActive(true);
						activeTracker.setId(id);
						activeTracker.setLastKeepAlive(new Date() );
						activeTracker.setMaster(false);
						//Add the new active tracker to the list
						globalManager.getTracker().addActiveTracker(activeTracker);
						//Notify to the observers to update the UI
						this.notifyObservers( new String("New Active Tracker") );
					}
				}
			}
		}
		
	}
	
	private List<String> getListActiveTrackers()
	{
		List<String> activeTrackers = new ArrayList<String>();
	
		for ( ActiveTracker activeTracker : globalManager.getTracker().getTrackersActivos().values() )
		{
			activeTrackers.add(activeTracker.getId());
		}
		return activeTrackers;
		
	}
	
	private boolean isKeepAliveMessage ( DatagramPacket packet )
	{
		String [] message = new String(packet.getData()).split(":");
		return message[1].equals(TYPE_KEEP_ALIVE_MESSAGE);
	}
	private void generateThreadToCheckActiveTrackers ( )
	{
		Thread threadCheckKeepAliveMessages = new Thread() {
			
			public void run() {
				try {
					Thread.sleep(4000);
					electMasterInitiating();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				while (!stopThreadCheckerKeepAlive) {
					try {
						Thread.sleep(4000);
						checkActiveTrackers();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		threadCheckKeepAliveMessages.start();
	}
	
	private void electMasterInitiating()
	{
		ConcurrentHashMap<String, ActiveTracker> mapActiveTrackers = globalManager.getTracker().getTrackersActivos();
		if ( mapActiveTrackers.size() == 0 )
		{
			//Actual one is the master tracker
			globalManager.getTracker().setMaster(true);
		}
		else
		{
			boolean enc = false;
			Integer i = 0;
			List<String> keysMapActiveTrackers = new ArrayList<String>(mapActiveTrackers.keySet());
			while ( !enc && i < mapActiveTrackers.values().size() )
			{
				ActiveTracker activeTracker = (ActiveTracker) mapActiveTrackers.get(keysMapActiveTrackers.get(i));
				if ( activeTracker.getId().compareTo(globalManager.getTracker().getId()) == -1 )
				{
					//Actual one is no the mastee tracker
					globalManager.getTracker().setMaster(false);
				}
			}
		}
	}
	
	private void checkActiveTrackers() {
		for ( ActiveTracker activeTracker : globalManager.getTracker().getTrackersActivos().values() )
		{
			long time = activeTracker.getLastKeepAlive().getTime();
			long actualTime = new Date().getTime();
			if ( actualTime - time >= 4000 )
			{
				
				System.out.println("Borrando tracker " + activeTracker.getId() + " ...");
				if(activeTracker.isMaster()){
					System.out.println("The Master is going to be removedS");
					//TODO Token ring process
				}
				globalManager.getTracker().getTrackersActivos().remove(activeTracker);
			}
		}
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

	private void notifyObservers(Object param) {
		for (Observer observer : this.observers) {
			if (observer != null) {
				observer.update(null, param);
			}
		}
	}

	/*** [END] OBSERVABLE PATTERN IMPLEMENTATION **/

	public void desconectar() {
		this.notifyObservers(null);
	}

	public boolean isStopListeningPackets() {
		return stopListeningPackets;
	}

	public void setStopListeningPackets(boolean stopListeningPackets) {
		this.stopListeningPackets = stopListeningPackets;
	}

	public boolean isStopThreadKeepAlive() {
		return stopThreadKeepAlive;
	}

	public void setStopThreadKeepAlive(boolean stopThreadKeepAlive) {
		this.stopThreadKeepAlive = stopThreadKeepAlive;
	}
}
