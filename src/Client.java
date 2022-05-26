

//import java.rmi.RemoteException;
//import java.rmi.registry.LocateRegistry;
//import java.rmi.registry.Registry;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;


public class Client {
	private static int client_number = 100; 
	private static int requests_per_client = 1; 
	private String host;
	private ServerAPI serverHandler;
	private String ClientAccount;
	private JMS jmsHandler;
	private MessageProducer queueSender;
	private MessageConsumer topicReceiver;
	private String sourceAccount = "hosuiting";
	private String destAccount = "zyron";
	public Client(String host) {
	    try {
	    	this.host = host;
	        jmsHandler = new JMS(host);
			queueSender = jmsHandler.createQueueSender();
			topicReceiver = jmsHandler.createTopicReceiver();
			System.out.println("JMS registered");
			for(int i = 0; i<requests_per_client;i++) {
				new Thread(new TransactionRequest()).start();
			}
	    } catch(Exception e) {
	        System.err.println("Failed accessing RMI: "+e);
	    }
	}
	public static void main(String[] args) {
		for(int i = 0; i<client_number;i++) {
			Client client = new Client(args[0]);
		}
	}
/*
	@Override
	public void run() {
		new Thread(new TransactionRequest()).start();
		//new sendPaymentUpdater().execute();
		//CreateAccount();
		
	}*/
	private class TransactionRequest implements Runnable{
		int status; 
		int finish = 0;
		int includedInBlock = 0;
		private float amount;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Random rand = new Random();
			amount = rand.nextInt(3);
			status = SendPayment(amount);
			while(finish == 0) {
				try {
					Message jmsMessage = topicReceiver.receive();
					System.out.println("Received message...");
					Object output = ((ObjectMessage)jmsMessage).getObject();
					block thisBlock = (block) output; 
					ArrayList<TransactionInfo> transactionlist = thisBlock.getTransactionList();
					for(int i =0; i <transactionlist.size(); i++) {
						TransactionInfo transaction = transactionlist.get(i);
						//System.out.println(transaction.getSourceAccount());
						//System.out.println(transaction.getDestAccount());
						//System.out.println(transaction.getAmount());
						//System.out.println(transaction.getSourceAccount().equals(sourceAccount));
						//System.out.println(transaction.getDestAccount().equals(destAccount));
						//System.out.println(transaction.getAmount() == amount);
						if((transaction.getSourceAccount().equals(sourceAccount))&&(transaction.getDestAccount().equals(destAccount))&&(transaction.getAmount() == amount)) {
							includedInBlock = 1;
						}
					}
					if (includedInBlock == 1) {
						System.out.println("Transaction is included in Local ledger");
						finish = 1;
					}else {
						System.out.println("Transaction fail to recorded in Local ledger");
						finish = 1;
					}
				} catch (JMSException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		
	}
	/*
	private class sendPaymentUpdater extends SwingWorker<Void, Void> {

		protected Void doInBackground() throws SQLException {
			status = SendPayment();
			return null;
		}
		protected void done() {
			if (status == 1) {
				System.out.println("Send Payment Successfully");
			}else {
				System.out.println("Failed to send Payment!!");
			}
		}
	}*/
	
	public int SendPayment(float amount) {
		System.out.println("Start Sending Payment");
		TransactionInfo transaction = new TransactionInfo(sourceAccount,destAccount,amount);
		try {
			Message message = null;
			message = jmsHandler.createMessage(transaction);
			if(message != null) {
				queueSender.send(message);
			}
			System.out.println("Message Sent");
			return 1;
		} catch (JMSException e1) {
			System.err.println("Failed to send message");
			return 0;
		}
		/*
	    if(serverHandler != null) {
	        try {
	            status = serverHandler.SendPayment("hosuiting","zyron",1);
	        } catch (RemoteException e) {
	            System.err.println("Failed invoking RMI: ");
	        }
	    }
	    */
	}
	/*
	@Override
	public void onMessage(Message arg0) {
		// TODO Auto-generated method stub
		int includedInBlock = 0;
		try {
			Object output = ((ObjectMessage)arg0).getObject();
			block thisBlock = (block) output; 
			ArrayList<TransactionInfo> transactionlist = thisBlock.getTransactionList();
			for(int i =0; i <transactionlist.size(); i++) {
				TransactionInfo transaction = transactionlist.get(i);
				if(transaction.getSourceAccount().equals(sourceAccount)&&transaction.getDestAccount().equals(destAccount)&&transaction.getAmount() == amount) {
					includedInBlock = 1;
				}
			}
			if (includedInBlock == 1) {
				System.out.println("Transaction inculdied in Local ledger");
			}else {
				System.out.println("Transaction fail to recorded in Local ledger");
			}
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}
