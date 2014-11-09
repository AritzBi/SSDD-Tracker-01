package es.deusto.ssdd.tracker.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import es.deusto.ssdd.tracker.vo.ActiveTracker;
import es.deusto.ssdd.tracker.vo.Tracker;

//TODO: Proceso de elección del master
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
	private static String CONFIRM_TO_STORE_MESSAGE = "ConfirmToStore";
	private static String BACKUP_MESSAGE = "BackUpMessage";
	private static String ERROR_ID_MESSAGE="IncorrectId";
	private static String CORRECT_ID_MESSAGE="CorrectId";
	
	private static String PATH_SQLITE_FILE = "src/info_master.db";
	private static String INICIO = "I";
	private static String FIN = "F";
	private byte[] ficheroDB = null;
	private int sizeActual = 0;
	private boolean sentKeepAlive;
	private boolean waitingToHaveID=true;
	
	public RedundancyManager() {
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

	private void socketListeningPackets() {
		try {
			DatagramPacket packet;
			while (!stopListeningPackets) {
				//byte[] buf = new byte[256];
				byte[] buf = new byte[2048];
				packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				if ( isKeepAliveMessage(packet) )
				{
					System.out.println("Me ha venido un keep alive message... " + " Soy tracker " + getTracker().getId());
					saveActiveTracker ( packet );
				}
				else if(isReadyToStore(packet)){
					if(getTracker().isMaster())
					{
						checkIfAllAreReadyToStore(packet);
					}
				}
				else if(isCorrectIDMessage(packet)){
					checkIfCorrectBelongsToTracker(packet);
				}
				else if(isErrorIDMessage(packet)){
					checkErrorIDMessage(packet);
				}
				else if (isBackUpMessage(packet))
				{
					generateDatabaseForPeersAndTorrents ( packet );
				}
				String messageReceived = new String(packet.getData());
				System.out.println("Received info..." + messageReceived);
			}
		} catch (IOException e) {
			System.err.println("Error while listening packets from the socket..." );
			e.printStackTrace();
		}
	}
	
	private void generateDatabaseForPeersAndTorrents ( DatagramPacket packet )
	{
		System.out.println("Me ha venido un mensaje de backup. �Es para mi? ");
		String [] partsMessage = new String(packet.getData()).split("%:%");
		String idMessage = partsMessage[0];
		String inicioOFin = partsMessage[1];
		String totalBytes = partsMessage[2];
		byte[] bytes = partsMessage[4].getBytes();
		System.out.println("Id viene: " +  idMessage + " mi Id " + getTracker().getId() );
		if ( idMessage.equals(getTracker().getId()))
		{
			if ( ficheroDB == null )
			{
				ficheroDB = new byte [Integer.valueOf(totalBytes)];
			}
			if (inicioOFin.equals(INICIO) )
			{
				addBytesToNewSQliteFile ( bytes );
			}
			else if ( inicioOFin.equals(FIN) )
			{
				addBytesToNewSQliteFile ( bytes );
				String newFileName = "src/info_" + getTracker().getId() + ".db";
				File fileDest = new File ( newFileName );
				FileOutputStream file;
				try {
					file = new FileOutputStream(fileDest);
					System.out.println("Writing the file...");
					file.write(ficheroDB);
					file.flush();
					file.close();
				} catch (FileNotFoundException e) {
					System.err.println(" ** FILE NOT FOUND: Not found " +  newFileName + " " + e.getMessage() );
				} catch (IOException e) {
					System.err.println(" ** IO EXCEPTION: Error writing the file " + newFileName + " " + e.getMessage() );
				}
				ficheroDB = null;
				sizeActual = 0;
			}

		}
	}
	
	private void addBytesToNewSQliteFile ( byte [] bytes )
	{
		for ( byte currentByte: bytes )
		{
			if ( sizeActual < ficheroDB.length )
			{
				ficheroDB[sizeActual] = currentByte;
				sizeActual++;	
			}
		}
		System.out.println("Fichero DB actual: " + new String ( ficheroDB ) );
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
		if(num-1 == numReady){
			readyToStoreTrackers.clear();
			sendConfirmToStoreMessage();
		}
	}
	
	private void checkIfCorrectBelongsToTracker(DatagramPacket packet){
		String[] messageReceived = new String ( packet.getData() ).split(":");
		String originId = messageReceived[0];
		if(originId.equals(getTracker().getId())&&waitingToHaveID)
			waitingToHaveID = false;
		
	}
	private void checkErrorIDMessage(DatagramPacket packet){
		String[] messageReceived = new String ( packet.getData() ).split(":");
		String originId = messageReceived[0];
		String candidateId = messageReceived[2];
		if(originId.equals(getTracker().getId())&&waitingToHaveID)
		{
			getTracker().setId(candidateId);
			this.notifyObservers(new String ("NewIdTracker") );
			waitingToHaveID = false;
		}
			
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
		
		ConcurrentHashMap<String,ActiveTracker> activeTrackers = globalManager.getTracker().getTrackersActivos();
		System.out.println("Active Trackers..." + getTracker().getTrackersActivos().values().toString() +  " para tracker " + getTracker().getId());
		if ( activeTrackers.containsKey(id ) )
		{
			if(id.equals( getTracker().getId())  && sentKeepAlive){
				sentKeepAlive=false;
			}else if(id.equals( getTracker().getId())  && !sentKeepAlive){
				calculatePossibleId(id);
			}
			ActiveTracker activeTracker = activeTrackers.get(id);
			activeTracker.setLastKeepAlive(new Date());
			activeTracker.setMaster(getBoolean(master));
			notifyObservers( new String("EditActiveTracker") );
		}
		else
		{
			boolean continuar = true;
			if ( globalManager.getTracker().isMaster())
			{
				if ( id.compareTo(getTracker().getId()) == -1 || ( id.equals(getTracker().getId()) ) )
				{
					continuar = false;
				}
				else 
				{
					sendBackUpMessage( id );
					sendCorrectIDMessage(id);
				}
			}
			
			if ( continuar ) {
				
				ActiveTracker activeTracker = new ActiveTracker();
				activeTracker.setActive(true);
				activeTracker.setId(id);
				activeTracker.setLastKeepAlive(new Date() );
				System.out.println("Me viene de master... " + master + getBoolean(master) + " para tracker " + id);
				activeTracker.setMaster(getBoolean(master));
				//Add the new active tracker to the list
				getTracker().addActiveTracker(activeTracker);
				System.out.println("Anyadimos a la lista un nuevo tracker activo");
				//Notify to the observers to update the UI
				notifyObservers( new String("NewActiveTracker") );
			}
			else {
				if ( !getBoolean(master) )
				{
					calculatePossibleId(id);
				}
				else
				{
					System.err.println("ERROR: I am Master, and I am receiving a message with other tracker same id and also master...");
				}
			}
		}
		
	}
	
	private void calculatePossibleId(String originId){
		int candidateID=Integer.parseInt(getTracker().getId())+1;
		int tempID;
		List<ActiveTracker>orderedList= globalManager.getActiveTrackers();
		Collections.sort(orderedList, new ActiveTracker());
		for ( ActiveTracker activeTracker :orderedList ){
			tempID=Integer.parseInt(activeTracker.getId());
			if(tempID==candidateID){
				candidateID++;
			}else if(candidateID<tempID){
				break;
			}
		}
		sendErrorIDMessage(Integer.parseInt(originId),candidateID);
		
		
	}
	
	/*** SEND MESSAGES TO OTHER TRACKERS ***/
	
	private void sendConfirmToStoreMessage() {
		String confirmToStoreMessage = generateConfirmToStoreMessage();
		byte[] messageBytes = confirmToStoreMessage.getBytes();
		DatagramPacket datagramPacket = new DatagramPacket(messageBytes,
				messageBytes.length, inetAddress, globalManager.getTracker()
						.getPort());
		writeSocket(datagramPacket);
	}
	
	private void sendKeepAliveMessage() {

		String message = generateKeepAliveMessage();
		byte[] messageBytes = message.getBytes();
		DatagramPacket datagramPacket = new DatagramPacket(messageBytes,
				messageBytes.length, inetAddress, globalManager.getTracker()
						.getPort());
		this.sentKeepAlive=true;
		writeSocket(datagramPacket);
	}
	
	private void sendErrorIDMessage(int originID,int candidateID){
		String message = generateIDErrorMessage(originID,candidateID);
		byte[] messageBytes = message.getBytes();
		DatagramPacket datagramPacket = new DatagramPacket(messageBytes,
				messageBytes.length, inetAddress, globalManager.getTracker()
						.getPort());
		writeSocket(datagramPacket);
	}
	
	private void sendCorrectIDMessage(String originID){
		String message = generateCorrectIDMessage(originID);
		byte[] messageBytes = message.getBytes();
		DatagramPacket datagramPacket = new DatagramPacket(messageBytes,
				messageBytes.length, inetAddress, globalManager.getTracker()
						.getPort());
		writeSocket(datagramPacket);
	}
	
	private String generateKeepAliveMessage() {
		return getTracker().getId() + ":" + getTracker().isMaster() + ":" + TYPE_KEEP_ALIVE_MESSAGE + ":";
	}
	
	private String generateConfirmToStoreMessage() {
		return getTracker().getId() + ":" + CONFIRM_TO_STORE_MESSAGE + ":";
	}
	private String generateIDErrorMessage(int originID,int candidateID) {
		return originID+ ":" + ERROR_ID_MESSAGE+ ":"+candidateID+":";
	}
	private String generateCorrectIDMessage(String originID) {
		return originID+ ":" + CORRECT_ID_MESSAGE+ ":";
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
	
	private void sendBackUpMessage( String idTracker ){
		String [] message = generateBackUpMessage( idTracker );
		for ( String currentMessage : message )
		{
			byte[] messageBytes = currentMessage.getBytes();
			System.out.println("Size of message " + messageBytes.length );

			DatagramPacket datagramPacket = new DatagramPacket(messageBytes,
				messageBytes.length, inetAddress, globalManager.getTracker()
						.getPort());
			writeSocket(datagramPacket);
		}
		
	}

	private String[] generateBackUpMessage( String idTracker ) {
		//Take the .db file of the master
		File file = new File (PATH_SQLITE_FILE);
		byte[] bytes = null;
	    FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			System.err.println("** FILE " + PATH_SQLITE_FILE + " NOT FOUND ** " + e.getMessage() );
		}
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                bos.write(buf, 0, readNum);
            }
            bytes = bos.toByteArray();
            fis.close();
        }
        catch (IOException e) {
        	System.err.println("** IO EX: Error reading the file " + e.getMessage() );
        }
        //Calculate the length of the message
        int messageLength = 0;
        if ( bytes.length % 1024 ==0 )
        {
        	messageLength = bytes.length / 1024;
        }
        else
        {
        	messageLength = bytes.length / 1024 + 1;
        }
        String [] message = new String[messageLength];
        int kMessage = 0;
        for ( int i = 0; i < bytes.length; i = i + 1024 ) 
        {
        	byte [] partMessage = new byte[1024];
        	partMessage = fillArrayBytes(i, partMessage, bytes);
        	System.out.println("SIZE: " + new String ( partMessage ) );
        	if ( i >= (bytes.length - 1024) )
        	{
        		System.out.println("EMPEZAMOS POR AQUI 2");
        		message[kMessage] = idTracker + "%:%" + FIN + "%:%" + bytes.length + "%:%" + BACKUP_MESSAGE + "%:%" + new String ( partMessage ) + "%:%";
        		System.out.println("EMPEZAMOS POR AQUI 2");
        		kMessage++;
        	}
        	else
        	{
        		System.out.println("EMPEZAMOS POR AQUI");
        		//We put INICIO on the fragment in order to know that there are more messages
        		message[kMessage] = idTracker + "%:%" + INICIO + "%:%" + bytes.length + "%:%" + BACKUP_MESSAGE + "%:%" + new String ( partMessage ) + "%:%";
        		System.out.println("EMPEZAMOS POR AQUI");
        		kMessage++;
        	}
        	
        }
        return message;
	}
	
	private byte[] fillArrayBytes ( int j , byte [] tofill, byte[] data )
	{
		for ( int k = 0; k < tofill.length ; k++ )
		{
			tofill[k] = data[j];
			j++;
		}
		return tofill;
	}
	
	/*** [END] SEND MESSAGES TO OTHER TRACKERS ***/
	
	/*** CHECK THE TYPES OF THE RECEIVED MESSAGES **/
	
	private boolean isReadyToStore(DatagramPacket packet){
		String [] message = new String(packet.getData()).split(":");
		return message[1].equals(READY_TO_STORE_MESSAGE);
	}
	
	private boolean isKeepAliveMessage ( DatagramPacket packet )
	{
		String [] message = new String(packet.getData()).split(":");
		return message[2].equals(TYPE_KEEP_ALIVE_MESSAGE);
	}
	
	private boolean isBackUpMessage ( DatagramPacket packet ) {
		String [] message = new String(packet.getData()).split("%:%");
		if ( message.length >= 5 )
			return message[3].equals(BACKUP_MESSAGE);
		else
			return false;
	}
	private boolean isCorrectIDMessage(DatagramPacket packet){
		String [] message = new String(packet.getData()).split(":");
		return message[1].equals(CORRECT_ID_MESSAGE);
	}
	private boolean isErrorIDMessage(DatagramPacket packet){
		String [] message = new String(packet.getData()).split(":");
		return message[1].equals(ERROR_ID_MESSAGE);
	}
	
	
	/*** [END] CHECK THE TYPES OF THE RECEIVED MESSAGES **/
	
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
		if ( mapActiveTrackers.size() == 1 && mapActiveTrackers.containsKey(getTracker().getId()) )
		{
			System.out.println("Al solo existir un activo y ser yo, el tracker" + getTracker().getId() + "es el MASTER");
			getTracker().setMaster(true);
			if ( waitingToHaveID )
				waitingToHaveID = false;
		}
		else
		{
			boolean enc = false;
			Integer i = 0;
			List<String> keysMapActiveTrackers = new ArrayList<String>(mapActiveTrackers.keySet());
			while ( !enc && i < mapActiveTrackers.values().size() )
			{
				ActiveTracker activeTracker = (ActiveTracker) mapActiveTrackers.get(keysMapActiveTrackers.get(i));
				if ( activeTracker != null )
				{
					if ( activeTracker.getId().compareTo( getTracker().getId()) == -1 )
					{
						System.out.println("Encuentro un tracker menor que yo (" + getTracker().getId() + ") que es " +  activeTracker.getId() );
						//Actual one is no the master tracker
						getTracker().setMaster(false);
						enc = true;
					}	
				}
				i++;
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
				boolean isMaster = activeTracker.isMaster();
				globalManager.getTracker().getTrackersActivos().remove(activeTracker.getId());
				if(isMaster){
					electMasterInitiating();
				}
				else
				{
					this.notifyObservers(new String ("DeleteActiveTracker"));
				}
				
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
