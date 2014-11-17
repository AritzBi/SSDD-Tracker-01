package es.deusto.ssdd.tracker.model;

import java.util.Calendar;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class QueueManager {

	private String connectionFactoryName = "QueueConnectionFactory";
	private static QueueManager queueManager;
	
	private Context ctx;
	private QueueConnectionFactory queueConnectionFactory;
	private QueueConnection queueConnection;
	private QueueSession queueSession;
	
	private QueueManager() {
		//JNDI Initial Context
		try {
			ctx = new InitialContext();
			
			//Connection Factory
			queueConnectionFactory = (QueueConnectionFactory) ctx.lookup(connectionFactoryName);			
			
			queueConnection = queueConnectionFactory.createQueueConnection();
			
			queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			
		} catch (NamingException e) {
			System.err.println("** NAMING EXCEPTION: " + e.getMessage() );
		} catch (JMSException e) {
			System.err.println("** JMS EXCEPTION: " + e.getMessage() );
		}
		
	}
	
	public static QueueManager getInstance() {
		if ( queueManager == null )
		{
			queueManager = new QueueManager();
		}
		return queueManager;
	}
	
	public void sendCorrectIdMessage () {
		
//		//Map Message			
//		MapMessage mapMessage = queueSession.createMapMessage();
//		//Message Properties
//		mapMessage.setStringProperty("Filter", "2");				
//		//Message Body
//		mapMessage.setString("Text", "Hello World!");
//		mapMessage.setLong("Timestamp", Calendar.getInstance().getTimeInMillis());
//		mapMessage.setBoolean("ACK_required", true);
//					
//		//Send the Messages
//		//queueSender.send(textMessage);
	}
}
