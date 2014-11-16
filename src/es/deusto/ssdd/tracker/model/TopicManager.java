package es.deusto.ssdd.tracker.model;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import es.deusto.ssdd.tracker.vo.Constants;
import es.deusto.ssdd.tracker.vo.Tracker;

public class TopicManager {

	private String connectionFactoryName = "TopicConnectionFactory";
	private String topicKeepAliveMessagesJNDIName = "jndi.ssdd.keepalivemessages";
	private String topicReadyToStoreMessagesJNDIName = "jndi.ssdd.readytostoremessages";
	private String topicConfirmToStoreMessagesJNDIName = "jndi.ssdd.confirmtostoremessages";

	private TopicConnection topicConnection = null;
	private TopicSession topicSession = null;
	private TopicSubscriber topicSubscriber = null;
	private TopicConnectionFactory topicConnectionFactory = null;

	private GlobalManager globalManager;
	private Context ctx = null;
	private String clientID = "";
	private static TopicManager instance = null;

	private TopicManager() {
		globalManager = GlobalManager.getInstance();

		try {
			ctx = new InitialContext();
			topicConnectionFactory = (TopicConnectionFactory) ctx
					.lookup(connectionFactoryName);

			topicConnection = topicConnectionFactory.createTopicConnection();
			clientID = "TrackerSubscriber_"
					+ globalManager.getTracker().getId();
			topicConnection.setClientID(clientID);

			topicSession = topicConnection.createTopicSession(false,
					Session.AUTO_ACKNOWLEDGE);

		} catch (NamingException e) {
			System.err.println("** NAMING EXCEPTION " + e.getMessage());
		} catch (JMSException e) {
			System.err.println("** JMS EXCEPTION " + e.getMessage());
		}

	}

	public static TopicManager getInstance() {
		if (instance == null) {
			instance = new TopicManager();
		}
		return instance;
	}

	public void publishKeepAliveMessage() {
		try {
			Topic topicKeepAliveMessages = (Topic) ctx
					.lookup(topicKeepAliveMessagesJNDIName);

			TopicPublisher topicPublisher = topicSession
					.createPublisher(topicKeepAliveMessages);
			// Map Message
			MapMessage mapMessage = topicSession.createMapMessage();

			// Message Properties
			mapMessage.setStringProperty("TypeMessage", "KeepAlive");

			// Message Body
			mapMessage.setString("Id", getTracker().getId());
			mapMessage.setBoolean("Master", getTracker().isMaster());

			topicPublisher.publish(mapMessage);
			System.out.println("- MapMessage sent to the Topic!");
		} catch (JMSException e) {
			System.err.println("# JMS Exception Error " + e.getMessage());
		} catch (NamingException e) {
			System.err.println("# Name Exception Error " + e.getMessage());
		}

	}

	public void publishReadyToStoreMessage() {
		try {
			Topic topicReadyToStoreMessages = (Topic) ctx
					.lookup(topicReadyToStoreMessagesJNDIName);

			TopicPublisher topicPublisher = topicSession
					.createPublisher(topicReadyToStoreMessages);
			// Map Message
			MapMessage mapMessage = topicSession.createMapMessage();

			// Message Properties
			mapMessage.setStringProperty("TypeMessage",
					Constants.TYPE_READY_TO_STORE_MESSAGE);

			// Message Body
			mapMessage.setString("Id", getTracker().getId());

			topicPublisher.publish(mapMessage);
			System.out.println("- MapMessage sent to the Topic!");

		} catch (JMSException e) {
			System.err.println("# JMS Exception Error " + e.getMessage());
		} catch (NamingException e) {
			System.err.println("# Name Exception Error " + e.getMessage());
		}
	}

	public void publishConfirmToStoreMessage() {
		try {
			Topic topicConfirmToStoreMessages = (Topic) ctx
					.lookup(topicConfirmToStoreMessagesJNDIName);

			TopicPublisher topicPublisher = topicSession
					.createPublisher(topicConfirmToStoreMessages);
			// Map Message
			MapMessage mapMessage = topicSession.createMapMessage();

			// Message Properties
			mapMessage.setStringProperty("TypeMessage",
					Constants.TYPE_CONFIRM_TO_STORE_MESSAGE);

			// Message Body
			mapMessage.setString("Id", getTracker().getId());

			topicPublisher.publish(mapMessage);
			System.out.println("- MapMessage sent to the Topic!");

		} catch (JMSException e) {
			System.err.println("# JMS Exception Error " + e.getMessage());
		} catch (NamingException e) {
			System.err.println("# Name Exception Error " + e.getMessage());
		}
	}

	public void start() throws JMSException {
		topicConnection.start();
	}

	public void subscribeTopicKeepAliveMessages(
			RedundancyManager redundancyManager) {
		try {
			Topic topicKeepAliveMessages = (Topic) ctx
					.lookup(topicKeepAliveMessagesJNDIName);

			TopicSubscriber topicSubscriber = topicSession
					.createSubscriber(topicKeepAliveMessages);
			topicSubscriber.setMessageListener(redundancyManager);
		} catch (NamingException | JMSException e) {
			e.printStackTrace();
		}
	}

	public void subscribeTopicReadyToStoreMessages(
			RedundancyManager redundancyManager) {
		try {
			Topic topicReadyToStoreMessages = (Topic) ctx
					.lookup(topicReadyToStoreMessagesJNDIName);
			TopicSubscriber topicSubscriber = topicSession
					.createSubscriber(topicReadyToStoreMessages);
			topicSubscriber.setMessageListener(redundancyManager);
		} catch (NamingException | JMSException e) {
			e.printStackTrace();
		}
	}

	public void subscribeTopicConfirmToStoreMessages(
			RedundancyManager redundancyManager) {
		try {
			Topic topicConfirmToStoreMessages = (Topic) ctx
					.lookup(topicConfirmToStoreMessagesJNDIName);
			TopicSubscriber topicSubscriber = topicSession
					.createSubscriber(topicConfirmToStoreMessages);
			topicSubscriber.setMessageListener(redundancyManager);
		} catch (NamingException | JMSException e) {
			e.printStackTrace();
		}
	}

	public void close() {

		try {
			topicSubscriber.close();
			topicSession.unsubscribe(clientID);
			topicSession.close();
			topicConnection.close();

		} catch (JMSException e) {
			System.err.println("* TopicManager Error: " + e.getMessage());
		}

	}

	private Tracker getTracker() {
		return globalManager.getTracker();
	}
}
