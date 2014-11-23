package es.deusto.ssdd.tracker.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import es.deusto.ssdd.tracker.vo.Constants;
import es.deusto.ssdd.tracker.vo.Tracker;

public class QueueManager {

	private String connectionFactoryName = "QueueConnectionFactory";
	String queueTrackersManagementJNDIName = "jndi.ssdd.trackersmanagement";
	private static QueueManager queueManager;

	private Context ctx;
	private QueueConnectionFactory queueConnectionFactory;
	private QueueConnection queueConnection;
	private QueueSession queueSession;

	private Queue queueTrackersManagement;
	private QueueSender queueSender;
	private GlobalManager globalManager;
	private QueueReceiver queueReceiver;

	private QueueManager() {
		globalManager = GlobalManager.getInstance();

		try {
			ctx = new InitialContext();

			// Connection Factory
			queueConnectionFactory = (QueueConnectionFactory) ctx
					.lookup(connectionFactoryName);

			queueConnection = queueConnectionFactory.createQueueConnection();

			queueSession = queueConnection.createQueueSession(false,
					Session.AUTO_ACKNOWLEDGE);

			queueTrackersManagement = (Queue) ctx
					.lookup(queueTrackersManagementJNDIName);

			queueSender = queueSession.createSender(queueTrackersManagement);

		} catch (NamingException e) {
			System.err
					.println("# Name Exception Error (constructor QueueManager) "
							+ e.getMessage());
		} catch (JMSException e) {
			System.err
					.println("# JMS Exception Error (constructor QueueManager) "
							+ e.getMessage());
		}

	}

	public static QueueManager getInstance() {
		if (queueManager == null) {
			queueManager = new QueueManager();
		}
		return queueManager;
	}

	public void receiveMessagesForMySpecificId(
			RedundancyManager redundancyManager) {
		try {
			if (queueManager != null) {
				queueReceiver = queueSession.createReceiver(
						queueTrackersManagement, "DestinationId = '"
								+ getTracker().getId() + "'");
				queueReceiver.setMessageListener(redundancyManager);
			}
		} catch (JMSException e) {
			System.err
					.println("# JMS Exception Error (receiveMessagesForMySpecificId) "
							+ e.getMessage());
		}
	}

	public void sendBackUpMessage(String destinationId) {
		if (queueManager != null) {
			File file = new File("db/info_" + getTracker().getId() + ".db");
			byte[] bytes = null;
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				System.err.println("# File " + "db/info_"
						+ getTracker().getId() + ".db" + " Not Found "
						+ e.getMessage());
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			try {
				for (int readNum; (readNum = fis.read(buf)) != -1;) {
					bos.write(buf, 0, readNum);
				}
				bytes = bos.toByteArray();
				fis.close();
			} catch (IOException e) {
				System.err.println("# IO Exception Error Reading The File "
						+ e.getMessage());
			}

			MapMessage mapMessage;
			try {
				mapMessage = queueSession.createMapMessage();
				// Message Properties
				mapMessage.setStringProperty("TypeMessage",
						Constants.TYPE_BACKUP_MESSAGE);
				mapMessage.setStringProperty("DestinationId", destinationId);
				// Message Body
				mapMessage.setString("Id", destinationId);
				mapMessage.setBytes("file", bytes);

				// Send the Messages
				queueSender.send(mapMessage);
				System.out.println("- MapMessage sent to the Queue! "
						+ mapMessage);
			} catch (JMSException e) {
				System.err.println("# JMS Exception Error (sendBackUpMessage) "
						+ e.getMessage());
			}
		}
	}

	public void start() throws JMSException {
		queueConnection.start();
	}

	public void close() {
		try {
			if (queueSender != null)
				queueSender.close();
			if (queueReceiver != null)
				queueReceiver.close();
			if (queueSession != null)
				queueSession.close();
			if (queueConnection != null)
				queueConnection.close();
			queueManager = null;

		} catch (JMSException e) {
			System.err.println("# JMS Exception Error (close) "
					+ e.getMessage());
		}
	}

	public void closeWindow() {

	}

	private Tracker getTracker() {
		return globalManager.getTracker();
	}
}
