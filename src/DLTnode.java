

//import java.rmi.*;
//import java.rmi.server.*;
import java.sql.Connection;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;  // Import the File class
import java.io.FileReader;
import java.io.FileWriter;   // Import the FileWriter class
import java.io.IOException;  // Import the IOException class to handle errors
import java.io.PrintWriter;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.jms.JMSException;
import javax.jms.Message;

import io.etcd.jetcd.KV;

public class DLTnode{
//public class DLTnode extends UnicastRemoteObject implements ServerAPI {
	private JMSServer jmsServer;
	private int previousBlockHash = 0;
	private int transactionID = 1;
	private int transactionCount = 0;
	public LinkedList<block> localLedger;   //local ledger which stores blocks
	private int blockid = 1;
	
	//-Djava.security.policy="${workspace_loc:Final/security.policy}"
	private tradingJetcdAPI tool = new tradingJetcdAPI();  //API to call Raft cluster
	private Logger LOGGER = Logger.getLogger(HandleTransaction.class.getName()); 
	private FileHandler fh = null;
	private block newBlock;
	//private ArrayList unHandledTransactionlist = new ArrayList<TransactionInfo>();
	
	static {
	      System.setProperty("java.util.logging.SimpleFormatter.format",
	              "%1$tF %1$tT %4$-7s: %5$s %n");
	}
	
	public static void main(String[] args) {
		try {
			DLTnode tradingServer = new DLTnode();
			JMSServer jmsServer = new JMSServer();
			//System.setSecurityManager(new SecurityManager());
			//Naming.rebind("TradingServer", tradingServer);
			//System.out.println("RMI Service registered");
			System.out.println("JMS registered");
			tradingServer.run(jmsServer);
		}catch(Exception e) {
			System.err.println("Exception thrown: "+e);
		}
	}
	protected DLTnode(){
	//protected DLTnode() throws RemoteException {
		// TODO Auto-generated constructor stub
		super();
		this.newBlock = new block();
		this.localLedger = new LinkedList<block>();
		/*
        SimpleDateFormat format = new SimpleDateFormat("M-d_HHmmss");
        try {
            fh = new FileHandler("C:/Users/hosuiting/Desktop/comp3358a5/DLTnode.log");
        } catch (Exception e) {
            e.printStackTrace();
        }
        fh.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(fh);
        */
	}
	private void run(JMSServer jmsServer)throws Exception {
		this.jmsServer = jmsServer;
		jmsServer.startService();
		try {
			System.out.println("Opened server, waiting transaction...");
	    	while(true){
				TransactionInfo transactionInfo = jmsServer.receiveMessage(); //block until receive message
				//System.out.println(transactionInfo.getSourceAccount());
				//System.out.println(transactionInfo.getDestAccount());
				//System.out.println(transactionInfo.getAmount());
				System.out.println("Client send a transaction message..");
				new Thread(new HandleTransaction(transactionInfo,transactionID)).start();
				transactionID++;
				//unHandledTransactionlist.add(transactionID);
	    	}
		} catch (JMSException e) {
			System.out.println("Thread error: "+e);
		}
		//new Thread(new waitTransaction()).start();
	}
	/*
	class waitTransaction implements Runnable {   //main thread
    	public waitTransaction() throws JMSException{
    		jmsServer.startService();
    	}
    	public void run() {
			try {
				System.out.println("Opened server, waiting transaction...");
		    	while(true){
					TransactionInfo transactionInfo = jmsServer.receiveMessage(); //block until receive message
					System.out.println(transactionInfo.getSourceAccount());
					System.out.println(transactionInfo.getDestAccount());
					System.out.println(transactionInfo.getAmount());
					System.out.println("Client send a transaction message..");
					new Thread(new HandleTransaction(transactionInfo,transactionID)).start();
					transactionID++;
					//unHandledTransactionlist.add(transactionID);
		    	}
			} catch (JMSException e) {
				System.out.println("Thread error: "+e);
			}
		}
	}*/
	class HandleTransaction implements Runnable {
    	private TransactionInfo transactionInfo;
    	private KV kvClient = tool.getKVClient();
    	private String sourceAccount;
    	private String destAccount;
    	private float amount;
    	private int transactionID;
    	public HandleTransaction(TransactionInfo transactionInfo,int transactionID){
    		this.transactionInfo = transactionInfo;
    		this.transactionID = transactionID;
    	}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
	    	try {
	        	Date now = new Date();
	        	long msSend = now.getTime();
	        	this.sourceAccount = transactionInfo.getSourceAccount();
	        	this.destAccount = transactionInfo.getDestAccount();
	        	this.amount = transactionInfo.getAmount();
	        	transactionInfo.setTransactionID(transactionID);
	        	//System.out.println("Amount = " + amount);
	    		String result = tool.SendPayment(this.kvClient, this.sourceAccount,this.destAccount, amount); //sending payment between account
	            now = new Date();
	            long msReceived = now.getTime();
	            long latency= msReceived - msSend;
	    		if (result.equals("Success")) {
	    			//System.out.println("SendPayment(*) success, latency: " + latency + "ms" + ", id:" + transactionID);
	                LOGGER.info("SendPayment(*) success, latency: " + latency + "ms" + ", id:" + transactionID);
	                UpdateBlock(transactionInfo);
	    		}else {
	    			//System.out.println("SendPayment(*) fail ," + result + ", id:" + transactionID);
	    			LOGGER.warning("SendPayment(*) fail ," + result + ", id:" + transactionID);
	    		}
	    	}catch (Exception e){
	    		//LOGGER.warning("Error");
	    	}     	
		}		
	}
	public synchronized void UpdateBlock(TransactionInfo transactionInfo) {  //only 1 thread is allowed to enter this function and update the block each time
		newBlock.updateTransactionList(transactionInfo); //add the transaction to a block
		if(newBlock.getTransactionListSize() < 100) {
			transactionCount++;
		}else {  // the block is full
			System.out.println("100 Transaction obtained, start storing in local ledger.");
			transactionCount = transactionCount % 100;
			Date now = new Date();
			long timestamp = now.getTime();
			System.out.println("Current time stamp: " + timestamp);
			newBlock.setTimestamp(timestamp);
			newBlock.setPreviousBlockHash(previousBlockHash);
			System.out.println("PreviousBlockHash: " + previousBlockHash);
			previousBlockHash = newBlock.hashCode();
			System.out.println("BlockSize = " + newBlock.getTransactionListSize());
			localLedger.addFirst(newBlock);  //adding block to linked list 
			System.out.println("Block "+ blockid + " recorded in local ledger");
			Message m;
			try {
				m = jmsServer.convertMsg(newBlock);
				jmsServer.broadcastMessage(m);
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			blockid ++;
			System.out.println("Broadcasting to clients for successfully add transactions");
			this.newBlock = new block();  //create a new block	
		}
	}

	//@Override
	/*
	public int SendPayment(String SourceAccount, String DestAccount, float balance) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}*/
}
