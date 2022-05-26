import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.jms.MessageConsumer;
import javax.jms.Topic;
import javax.jms.ObjectMessage;

public class JMS {
	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_PORT = 3700;
	private static final String JMS_CONNECTION_FACTORY = "jms/tradingConnectionFactory";
	private static final String JMS_TOPIC = "jms/tradingTopic";
	private static final String JMS_QUEUE = "jms/tradingQueue";
	
	private Context jndiContext;
	private ConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
	private Queue queue;
	private Topic topic;
	
	public JMS() throws NamingException, JMSException {
		this(DEFAULT_HOST);
	}
	
	public JMS(String host) throws NamingException, JMSException {
		int port = DEFAULT_PORT;
		System.setProperty("org.omg.CORBA.ORBInitialHost", host);
		System.setProperty("org.omg.CORBA.ORBInitialPort", port+"");
		try {
			jndiContext = new InitialContext();
		} catch (NamingException e) {
			System.err.println("Could not create JNDI API context: " + e);
			throw e;
		}
		try {
			connectionFactory = (ConnectionFactory)jndiContext.lookup(JMS_CONNECTION_FACTORY);
		} catch (NamingException e) {
			System.err.println("JNDI API JMS connection factory lookup failed: " + e);
			throw e;
		}
		
		try {
			queue = (Queue)jndiContext.lookup(JMS_QUEUE);
		} catch (NamingException e) {
			System.err.println("JNDI API JMS queue lookup failed: " + e);
			throw e;
		}
		try {
			topic = (Topic)jndiContext.lookup(JMS_TOPIC);
		} catch (NamingException e) {
			System.err.println("JNDI API JMS topic lookup failed: " + e);
			throw e;
		}
		try {
			connection = connectionFactory.createConnection();
			connection.start();
		} catch (JMSException e) {
			System.err.println("Failed to create connection to JMS provider: " + e);
			throw e;
		}
	}
	public Session createSession() throws JMSException {
		if(session != null) {
			return session;
		} else {
			try {
				return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			} catch (JMSException e) {
				System.err.println("Failed to create session: " + e);
				throw e;
			}
		}		
	}

	public MessageProducer createQueueSender() throws JMSException {
		try {
			return createSession().createProducer(queue);
		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}
	
	public MessageConsumer createQueueReceiver() throws JMSException {
		try {
			return createSession().createConsumer(queue);
		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}
	public MessageProducer createTopicSender() throws JMSException {
		try {
			return createSession().createProducer(topic);
		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}
	public MessageConsumer createTopicReceiver() throws JMSException {
		try {
			return createSession().createConsumer(topic);
		} catch (JMSException e) {
			System.err.println("Failed to create session: " + e);
			throw e;
		}
	}
	public ObjectMessage createMessage(Serializable obj) throws JMSException {
		return createSession().createObjectMessage(obj);
	}
}
