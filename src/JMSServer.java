import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.naming.NamingException;

public class JMSServer {
	private JMS jmsHandler;
	private MessageConsumer queueReader;
	private MessageProducer topicSender;
	
	public JMSServer() throws NamingException, JMSException {
		jmsHandler = new JMS();
	}
	public void startService() throws JMSException {
		queueReader = jmsHandler.createQueueReceiver();
		topicSender = jmsHandler.createTopicSender();
	}
	public TransactionInfo receiveMessage() throws JMSException {
		Message jmsMessage = queueReader.receive();
		//System.out.println("Receving message...");
		TransactionInfo transaction = (TransactionInfo)((ObjectMessage)jmsMessage).getObject();
		return transaction;
	}
	public void broadcastMessage(Message jmsMessage) throws JMSException {
		topicSender.send(jmsMessage);
	}
	public Message convertMsg(Serializable obj) throws JMSException {
		return jmsHandler.createMessage(obj);
	}
}
