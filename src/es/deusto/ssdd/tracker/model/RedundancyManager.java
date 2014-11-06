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
import es.deusto.ssdd.tracker.vo.Tracker;

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
		System.out.println("Se genera una nueva instancia");
		observers = new ArrayList<Observer>();
		globalManager = GlobalManager.getInstance();
		readyToStoreTrackers=new ConcurrentHashMap<String,Boolean>();
		
	}

	@Override
	public void run() {
		createSocket();
		generateThreadToSendKeepAliveMessages();
		generateThreadToCheckActiveTrackers();
		socketListeningPackets();
		
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
						Thread.sleep(4000);
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
		return getTracker().getId() + ":" + getTracker().isMaster() + ":" + TYPE_KEEP_ALIVE_MESSAGE + ":";
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
					System.out.println("Me ha venido un keep alive message... " + " Soy tracker " + getTracker().getId());
					saveActiveTracker ( packet );
					
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
			this.sendBackUp();
		}
		
	}
	private boolean isReadyToStore(DatagramPacket packet){
		String [] message = new String(packet.getData()).split(":");
		return message[1].equals(READY_TO_STORE_MESSAGE);
	}
	
	private boolean getBoolean ( String condicion )
	{
		if ( condicion.equals("true") )
			return true;
		else if (condicion.equals("false") )
			return false;
		return false;
	}
	private void saveActiveTracker ( DatagramPacket packet )
	{
		String[] messageReceived = new String ( packet.getData() ).split(":");
		String id = messageReceived[0];
		String master = messageReceived[1];
		System.out.println("Viene id: " + id + " se comprueba con " +  globalManager.getTracker().getId() );
		if ( !id.equals(globalManager.getTracker().getId() ) )
		{
			System.out.println("Id diferente a la del tracker..");
			ConcurrentHashMap<String,ActiveTracker> activeTrackers = globalManager.getTracker().getTrackersActivos();
			System.out.println("Active Trackers..." + getTracker().getTrackersActivos().values().toString() +  " para tracker " + getTracker().getId());
			if ( activeTrackers.containsKey(id ) )
			{
				ActiveTracker activeTracker = activeTrackers.get(id);
				activeTracker.setLastKeepAlive(new Date());
				activeTracker.setMaster(getBoolean(master));
				this.notifyObservers( new String("NewActiveTracker") );
			}
			else
			{
				boolean continuar = true;
				if ( globalManager.getTracker().isMaster()) //If the tracker is master tracker
				{
					List<String> idsActiveTrackers = getListActiveTrackers();
					if ( id.compareTo(globalManager.getTracker().getId()) == -1 )
					{
						continuar = false;
					}
				}
				if ( continuar ) {
					ActiveTracker activeTracker = new ActiveTracker();
					activeTracker.setActive(true);
					activeTracker.setId(id);
					activeTracker.setLastKeepAlive(new Date() );
					System.out.println("Me viene de master... " + master + Boolean.getBoolean(master) + " para tracker " + id);
					activeTracker.setMaster(Boolean.getBoolean(master));
					//Add the new active tracker to the list
					globalManager.getTracker().addActiveTracker(activeTracker);
					System.out.println("Anyadimos a la lisa un nuevo tracker activo");
					//Notify to the observers to update the UI
					this.notifyObservers( new String("NewActiveTracker") );
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
		return message[2].equals(TYPE_KEEP_ALIVE_MESSAGE);
	}
	private void generateThreadToCheckActiveTrackers ( )
	{
		Thread threadCheckKeepAliveMessages = new Thread() {
			
			public void run() {
				try {
					System.out.println("Vamos a esperar 8 segundos");
					Thread.sleep(8000);
					if ( !stopThreadCheckerKeepAlive )
					{
						electMasterInitiating();
						checkActiveTrackers();	
					}
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				while (!stopThreadCheckerKeepAlive) {
					try {
						Thread.sleep(8000);
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
		System.out.println("Entro en la eleccion del master");
		ConcurrentHashMap<String, ActiveTracker> mapActiveTrackers = globalManager.getTracker().getTrackersActivos();
		System.out.println("Miramos el map de Trackers Activos..." + mapActiveTrackers.toString() );
		if ( mapActiveTrackers.size() == 0 )
		{
			System.out.println("Al no existir m�s trackers, el tracker" + getTracker().getId() + "es el MASTER");
			getTracker().setMaster(true);
		}
		else
		{
			boolean enc = false;
			Integer i = 0;
			List<String> keysMapActiveTrackers = new ArrayList<String>(mapActiveTrackers.keySet());
			while ( !enc && i < mapActiveTrackers.values().size() )
			{
				ActiveTracker activeTracker = (ActiveTracker) mapActiveTrackers.get(keysMapActiveTrackers.get(i));
				if ( activeTracker.getId().compareTo( getTracker().getId()) == -1 )
				{
					System.out.println("Encuentro un tracker menor que yo (" + getTracker().getId() + ") que es " +  activeTracker.getId() );
					//Actual one is no the master tracker
					getTracker().setMaster(false);
					enc = true;
				}
			}
			if ( !enc )
			{
				getTracker().setMaster(true);
			}
			
		}
	}
	
	private void checkActiveTrackers() {
		for ( ActiveTracker activeTracker : getTracker().getTrackersActivos().values() )
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
				globalManager.getTracker().getTrackersActivos().remove(activeTracker.getId());
				this.notifyObservers(new String ("DeleteActiveTracker"));
			}
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
	private void sendBackUp(){
		
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
			System.out.println("Se a�ade un nuevo observador");
			this.observers.add(o);
		}
	}

	public void deleteObserver(Observer o) {
		this.observers.remove(o);
	}

	private void notifyObservers(Object param) {
		System.out.println("Lista observers: " + observers);
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

	public boolean isStopThreadCheckerKeepAlive() {
		return stopThreadCheckerKeepAlive;
	}

	public void setStopThreadCheckerKeepAlive(boolean stopThreadCheckerKeepAlive) {
		this.stopThreadCheckerKeepAlive = stopThreadCheckerKeepAlive;
	}
	
	private Tracker getTracker() {
		return globalManager.getTracker();
	}
}
