package es.deusto.ssdd.tracker.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.codec.binary.Base64;

import es.deusto.ssdd.tracker.vo.ActiveTracker;
import es.deusto.ssdd.tracker.vo.Constants;
import es.deusto.ssdd.tracker.vo.Peer;
import es.deusto.ssdd.tracker.vo.Tracker;

public class RedundancyManager implements Runnable, MessageListener {

	private List<Observer> observers;
	private GlobalManager globalManager;
	private DataManager dataManager;
	private TopicManager topicManager;
	private QueueManager queueManager;

	private boolean stopListeningPackets = false;
	private boolean stopThreadKeepAlive = false;
	private boolean stopThreadCheckerKeepAlive = false;
	private ConcurrentHashMap<String, Boolean> readyToStoreTrackers;

	private static String PATH_BASE_SQLITE_FILE = "db/base_database.db";

	private boolean sentKeepAlive;
	private boolean waitingToHaveID = true;
	private boolean choosingMaster = false;

	public RedundancyManager() {
		observers = new ArrayList<Observer>();
		readyToStoreTrackers = new ConcurrentHashMap<String, Boolean>();

		globalManager = GlobalManager.getInstance();
		dataManager = DataManager.getInstance();
		topicManager = TopicManager.getInstance();
		queueManager = QueueManager.getInstance();
	}

	@Override
	public void run() {
		topicManager = TopicManager.getInstance();
		queueManager = QueueManager.getInstance();

		topicManager.subscribeTopicKeepAliveMessages(this);
		topicManager.subscribeTopicConfirmToStoreMessages(this);
		topicManager.subscribeTopicReadyToStoreMessages(this);
		topicManager.subscribeTopicIncorrectIdMessages(this);
		topicManager.subscribeTopicCorrectIdMessages(this);

		generateThreadToSendKeepAliveMessages();
		generateThreadToCheckActiveTrackers();
		socketListeningPackets();
	}

	private void generateThreadToSendKeepAliveMessages() {
		Thread threadSendKeepAliveMessages = new Thread() {
			public void run() {
				while (!stopThreadKeepAlive) {
					try {
						Thread.sleep(4000);
						sentKeepAlive = true;
						System.out
								.println("Method generateThreadToSendKeepAliveMessages: "
										+ "publishKeepAliveMessage()");
						topicManager.publishKeepAliveMessage();
					} catch (InterruptedException e) {
						System.err.println("**INTERRUPTED EXCEPTION..."
								+ e.getMessage());
						e.printStackTrace();
					}
				}
			}
		};
		threadSendKeepAliveMessages.start();
	}

	private void socketListeningPackets() {
		try {
			while (!stopListeningPackets) {
				if (!isChoosingMaster()) {
					topicManager.start();
					queueManager.start();
				}
			}
		} catch (JMSException e) {
			System.err
					.println("** IO EXCEPTION: Error while listening packets from the socket... "
							+ e.getMessage());
		}
	}

	@Override
	public void onMessage(Message message) {
		if (message != null) {
			try {
				System.out.println("   - TopicListener: "
						+ message.getClass().getSimpleName() + " received!");

				if (message.getClass().getCanonicalName()
						.equals(ActiveMQTextMessage.class.getCanonicalName())) {
					System.out.println("     - TopicListener: TextMessage '"
							+ ((TextMessage) message).getText());
				} else if (message.getClass().getCanonicalName()
						.equals(ActiveMQMapMessage.class.getCanonicalName())) {
					System.out.println("     - TopicListener: MapMessage");
					MapMessage mapMsg = ((MapMessage) message);

					// We obtain the type of the message
					String typeMessage = getTypeMessage(mapMsg);

					// Iterate over the different data of the message
					@SuppressWarnings("unchecked")
					Enumeration<String> mapKeys = (Enumeration<String>) mapMsg
							.getMapNames();
					String key = null;
					List<Object> data = new ArrayList<Object>();
					System.out.println("TYPE OF MESSAGE: " + typeMessage);
					while (mapKeys.hasMoreElements()) {
						key = mapKeys.nextElement();
						if (key != null & !key.equals("")) {
							data.add(mapMsg.getObject(key));
						}
						System.out.println("       + " + key + ": "
								+ mapMsg.getObject(key));
					}

					if (typeMessage.equals(Constants.TYPE_KEEP_ALIVE_MESSAGE)) {
						saveActiveTracker(data.toArray());
					} else if (typeMessage
							.equals(Constants.TYPE_READY_TO_STORE_MESSAGE)) {
						if (getTracker().isMaster()) {
							checkIfAllAreReadyToStore(data.toArray());
						}
					} else if (typeMessage
							.equals(Constants.TYPE_CONFIRM_TO_STORE_MESSAGE)) {
						storeTemporalData( data.toArray() );
					} else if (typeMessage
							.equals(Constants.TYPE_ERROR_ID_MESSAGE)) {
						checkErrorIDMessage(data.toArray());
					} else if (typeMessage
							.equals(Constants.TYPE_CORRECT_ID_MESSAGE)) {
						checkIfCorrectBelongsToTracker(data.toArray());
					} else if (typeMessage
							.equals(Constants.TYPE_BACKUP_MESSAGE)) {
						generateDatabaseForPeersAndTorrents(data.toArray());
					}

				}

			} catch (Exception ex) {
				System.err.println("# TopicListener error: " + ex.getMessage());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private String getTypeMessage(MapMessage message) {

		Enumeration<String> propertyNames;
		String typeMessage = "";
		try {
			propertyNames = (Enumeration<String>) message.getPropertyNames();
			while (propertyNames.hasMoreElements()) {
				String propertyName = propertyNames.nextElement();
				if (propertyName.equals("TypeMessage")) {
					typeMessage = message.getStringProperty(propertyName);
				}
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return typeMessage;
	}

	private void generateDatabaseForPeersAndTorrents(Object... data) {
		byte[] bytesOfDbFile = (byte[]) data[0];
		String newFileName = "db/info_" + getTracker().getId() + ".db";
		File fileDest = new File(newFileName);
		FileOutputStream file = null;
		try {
			long length = fileDest.length();
			file = new FileOutputStream(fileDest);
			System.out.println("Writing the file...");
			if (length > 0) {
				file.write((new String()).getBytes());
			}
			file.write(bytesOfDbFile);
			file.flush();
			file.close();
		} catch (FileNotFoundException e) {
			System.err.println(" ** FILE NOT FOUND: Not found " + newFileName
					+ " " + e.getMessage());
		} catch (IOException e) {
			System.err.println(" ** IO EXCEPTION: Error writing the file "
					+ newFileName + " " + e.getMessage());
		}
		
		dataManager.connectDB("db/info_" + getTracker().getId() + ".db");
	}

	private void storeTemporalData(Object... data ) {
		long connectionId = (long) data[0];
		
		Map<Long,Peer> peers = DataManager.peers;
		
		Peer peerToStore = peers.get(connectionId);
		if ( dataManager.existsPeer(peerToStore.getIpAddress(), peerToStore.getPort()) )
		{
			dataManager.updatePeer(peerToStore);
		}
		else
		{
			dataManager.insertNewPeer(peerToStore);
		}
		DataManager.peers.remove(connectionId);
		dataManager.insertLeechersAndSeeders();
		this.notifyObservers("NewPeer");
	}

	private void checkIfAllAreReadyToStore(Object... data) {
		int num = getTracker().getTrackersActivos().size();
		long connectionId = (long) data[0];
		String id = (String) data[1];
		
		readyToStoreTrackers.put(id, true);
		int numReady = 0;
		for (Boolean bool : readyToStoreTrackers.values()) {
			if (bool)
				numReady++;
		}

		if (num == numReady) {
			readyToStoreTrackers.clear();
			System.out.println("Method checkIfAllAreReadyToStore: "
					+ "publishConfirmToStoreMessage()");
			topicManager.publishConfirmToStoreMessage( connectionId );
		}
	}

	private void checkIfCorrectBelongsToTracker(Object... data) {
		String originId = (String) data[0];
		if (originId.equals(getTracker().getId()) && waitingToHaveID) {
			waitingToHaveID = false;
			queueManager.receiveMessagesForMySpecificId(this);
		}

	}

	private void checkErrorIDMessage(Object... data) {
		String candidateId = (String) data[0];
		String originId = (String) data[1];

		if (originId.equals(getTracker().getId()) && waitingToHaveID) {
			getTracker().setId(candidateId);
			notifyObservers(new String("NewIdTracker"));
			waitingToHaveID = false;
			queueManager.receiveMessagesForMySpecificId(this);
		}

	}

	private void saveActiveTracker(Object... data) {
		boolean master = (Boolean) data[0];
		String id = (String) data[1];

		ConcurrentHashMap<String, ActiveTracker> activeTrackers = globalManager
				.getTracker().getTrackersActivos();
		System.out.println("For tracker " + getTracker().getId()
				+ " : Current Active Trackers "
				+ getTracker().getTrackersActivos().values().toString());
		if (!waitingToHaveID) {
			// Already exists an active tracker with the coming id
			if (activeTrackers.containsKey(id)) {
				if (id.equals(getTracker().getId()) && sentKeepAlive) {
					sentKeepAlive = false;
				} else if (id.equals(getTracker().getId()) && !sentKeepAlive) {
					calculatePossibleId(id);
				}

				if (getTracker().getId().equals(id)) {
					if (getTracker().isMaster() == master) {
						ActiveTracker activeTracker = activeTrackers.get(id);
						activeTracker.setLastKeepAlive(new Date());
						activeTracker.setMaster(master);
						notifyObservers(new String("EditActiveTracker"));
					}
				}

				if (!getTracker().getId().equals(id)) {
					ActiveTracker activeTracker = activeTrackers.get(id);
					activeTracker.setLastKeepAlive(new Date());
					activeTracker.setMaster(master);
					notifyObservers(new String("EditActiveTracker"));
				}
			} else {
				boolean continuar = true;
				// Process necessary when coming a new id and the tracker is the
				// master tracker
				if (globalManager.getTracker().isMaster()) {
					// No accept ids less than mine and equals than me and is
					// not set as master
					if (id.compareTo(getTracker().getId()) <= -1
							|| (id.equals(getTracker().getId()) && !master)) {
						// not save the new active tracker
						continuar = false;
					} else {
						if (!id.equals(getTracker().getId())) {
							System.out.println("ENVIAMOS BACKUP MESSAGE");
							topicManager.publishCorrectIdMessage(id);
							queueManager.sendBackUpMessage(id);

						}

					}
				}

				if (continuar) {

					ActiveTracker activeTracker = new ActiveTracker();
					activeTracker.setActive(true);
					activeTracker.setId(id);
					activeTracker.setLastKeepAlive(new Date());
					activeTracker.setMaster(master);
					// Add the new active tracker to the list
					getTracker().addActiveTracker(activeTracker);
					System.out
							.println("ADD: New tracker into the Active Trackers "
									+ getTracker().getTrackersActivos()
											.values().toString());
					// Notify to the observers to update the UI
					notifyObservers(new String("NewActiveTracker"));
				} else {
					if (!master) {
						calculatePossibleId(id);
					} else {
						System.err
								.println("ERROR: I am Master, and I am receiving a message with other tracker same id and also master...");
					}
				}
			}
		}

	}

	private void calculatePossibleId(String originId) {
		int candidateID = Integer.parseInt(getTracker().getId()) + 1;
		int tempID;
		List<ActiveTracker> orderedList = globalManager.getActiveTrackers();
		Collections.sort(orderedList, new ActiveTracker());
		for (ActiveTracker activeTracker : orderedList) {
			tempID = Integer.parseInt(activeTracker.getId());
			if (tempID == candidateID) {
				candidateID++;
			} else if (candidateID < tempID) {
				break;
			}
		}
		System.out.println("Method calculatePossibleId: "
				+ "publishIncorrectIdMessage()");
		topicManager.publishIncorrectIdMessage(originId,
				String.valueOf(candidateID));

	}

	private void generateNewDatabaseForTracker() {
		File file = new File(PATH_BASE_SQLITE_FILE);
		byte[] bytes = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];

			for (int readNum; (readNum = fis.read(buf)) != -1;) {
				bos.write(buf, 0, readNum);
			}
			bytes = bos.toByteArray();
			fis.close();

		} catch (FileNotFoundException e) {
			System.err.println("** FILE " + PATH_BASE_SQLITE_FILE
					+ " NOT FOUND ** " + e.getMessage());
		} catch (IOException e) {
			System.err.println("** IO EX: Error reading the file "
					+ e.getMessage());
		}
		String mensaje = Base64.encodeBase64String(bytes);
		File newFile = new File("db/info_" + getTracker().getId() + ".db");
		long lengthFile = newFile.length();
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(newFile);
			if (lengthFile > 0) {
				fileOutputStream.write((new String()).getBytes());
			}
			fileOutputStream.write(Base64.decodeBase64(mensaje));
			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
			System.err.println("** FILE " + newFile.getPath()
					+ " NOT FOUND ** " + e.getMessage());
		} catch (IOException e) {
			System.err.println("** IO EX: Error writing new file "
					+ e.getMessage());
		}
		
		dataManager.connectDB("db/info_" + getTracker().getId() + ".db");

	}

	/*** [END] CHECK THE TYPES OF THE RECEIVED MESSAGES **/

	private void generateThreadToCheckActiveTrackers() {
		Thread threadCheckKeepAliveMessages = new Thread() {

			public void run() {
				try {
					Thread.sleep(8000);
					if (!stopThreadCheckerKeepAlive) {
						electMasterInitiating();
						checkActiveTrackers();
					}
				} catch (InterruptedException e1) {
					System.err.println("** INTERRUPTED EXCEPTION: "
							+ e1.getMessage());
				}

				while (!stopThreadCheckerKeepAlive) {
					try {
						Thread.sleep(8000);
						checkActiveTrackers();
					} catch (InterruptedException e) {
						System.err.println("** INTERRUPTED EXCEPTION: "
								+ e.getMessage());
					}
				}
			}
		};
		threadCheckKeepAliveMessages.start();
	}

	private void electMasterInitiating() {
		System.out.println("Start electing the new master");
		setChoosingMaster(true);
		ConcurrentHashMap<String, ActiveTracker> mapActiveTrackers = getTracker()
				.getTrackersActivos();
		if (mapActiveTrackers.size() == 0
				|| (mapActiveTrackers.size() == 1 && mapActiveTrackers
						.containsKey(getTracker().getId()))) {
			System.out
					.println("Only exists one active tracker and I am this one, so "
							+ getTracker().getId() + "is the new master");
			getTracker().setMaster(true);
			if (waitingToHaveID) {
				waitingToHaveID = false;
				generateNewDatabaseForTracker();
				queueManager.receiveMessagesForMySpecificId(this);
			}

		} else {
			boolean enc = false;
			Integer i = 0;
			List<String> keysMapActiveTrackers = new ArrayList<String>(
					mapActiveTrackers.keySet());
			System.out.println("Active Trackers to compare with "
					+ keysMapActiveTrackers);
			while (!enc && i < mapActiveTrackers.values().size()) {
				ActiveTracker activeTracker = (ActiveTracker) mapActiveTrackers
						.get(keysMapActiveTrackers.get(i));
				System.out.println("Active tracker >> " + activeTracker);
				if (activeTracker != null) {
					System.out.println("Para caso tracker "
							+ activeTracker.getId()
							+ " comparamos "
							+ activeTracker.getId().compareTo(
									getTracker().getId()));
					if (activeTracker.getId().compareTo(getTracker().getId()) <= -1) {
						System.out
								.println("Found an active tracker with a id less than mine ("
										+ getTracker().getId()
										+ ") that is "
										+ activeTracker.getId());
						// Actual one is no the master tracker
						getTracker().setMaster(false);
						enc = true;
					}
				}
				i++;
			}
			if (!enc) {
				getTracker().setMaster(true);
			}
		}
		setChoosingMaster(false);
	}

	private void checkActiveTrackers() {
		for (ActiveTracker activeTracker : getTracker().getTrackersActivos()
				.values()) {
			long time = activeTracker.getLastKeepAlive().getTime();
			long actualTime = new Date().getTime();
			if (actualTime - time >= 8000) {

				System.out.println("Deleting the tracker "
						+ activeTracker.getId() + " ...");
				boolean isMaster = activeTracker.isMaster();
				getTracker().getTrackersActivos().remove(activeTracker.getId());
				if (isMaster) {
					electMasterInitiating();
				} else {
					this.notifyObservers(new String("DeleteActiveTracker"));
				}

			}
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

	public void notifyObservers(Object param) {
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

	public synchronized boolean isChoosingMaster() {
		return choosingMaster;
	}

	public synchronized void setChoosingMaster(boolean choosingMaster) {
		this.choosingMaster = choosingMaster;
	}

	public boolean isWaitingToHaveID() {
		return waitingToHaveID;
	}

	public void setWaitingToHaveID(boolean waitingToHaveID) {
		this.waitingToHaveID = waitingToHaveID;
	}

}
