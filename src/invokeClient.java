

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import io.etcd.jetcd.KV;

public class invokeClient {
	private static int client_number = 100; //modify the number of clients here
	private static int request_Number = 1000; //modify the number of requests per client here
	private Logger LOGGER = Logger.getLogger(raftCall.class.getName()); 
	private FileHandler fh = null;
	public invokeClient(){
        SimpleDateFormat format = new SimpleDateFormat("M-d_HHmmss");
        try {
            fh = new FileHandler("C:/Users/hosuiting/Desktop/comp3358a5/ClientNumber_" + client_number + ".log");
        } catch (Exception e) {
            e.printStackTrace();
        }

        fh.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(fh);
	}
	static {
	      System.setProperty("java.util.logging.SimpleFormatter.format",
	              "%1$tF %1$tT %4$-7s: %5$s %n");
	}
	
	public static void main(String[] args) {
		invokeClient client = new invokeClient();
		client.run();
	}
	private void run() {
		for (int i=1 ; i <= client_number; i++) {
			new Thread(new raftCall(i)).start();
		}
	}
	class raftCall implements Runnable {
		//private Logger LOGGER = Logger.getLogger(raftCall.class.getName()); 
		//private FileHandler fh = null;
		
		int Client_id;
		public raftCall(int i) {
			Client_id = i;
			/*
			// each client write to a log file respectively
	        SimpleDateFormat format = new SimpleDateFormat("M-d_HHmmss");
	        try {
	            fh = new FileHandler("C:/Users/hosuiting/Desktop/comp3358a5/Client_" + Client_id + ".log");
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        fh.setFormatter(new SimpleFormatter());
	        LOGGER.addHandler(fh);
	        */
		}
		
		public void run() {
			tradingJetcdAPI tool = new tradingJetcdAPI();
			KV kvClient = tool.getKVClient();
			for (int request_id = 1 ; request_id <= request_Number; request_id++) {
				Random rand = new Random();
				int random_number = rand.nextInt(3);
				//int random_number = 2;
				if(random_number == 0) {  //create account
			    	try {
			        	Date now = new Date();
			        	long msSend = now.getTime();
			        	String account = msSend + "-" + Client_id;
			    		String result = tool.CreateAccount(kvClient, account);
			            now = new Date();
			            long msReceived = now.getTime();
			            long latency= msReceived - msSend;
			    		if (result.equals("Success")) {
			                LOGGER.info("CreateAccount(*) success, latency: " + latency + "ms, id:" + request_id + ", client:" + Client_id );
			    		}else {
			    			LOGGER.warning("CreateAccount(*) fail,"+ result + ", id:" + request_id + ", client:" + Client_id );
			    		}
			    	}catch (Exception e){
			    		LOGGER.warning("Error");
			    	}
				}else if (random_number == 1) { //transfer payment
			    	try {
			        	Date now = new Date();
			        	long msSend = now.getTime();
			        	String sourceAccount = "hosuiting";
			        	String destAccount = "newHosuiting";
			    		String result = tool.SendPayment(kvClient, sourceAccount,destAccount, 0);
			            now = new Date();
			            long msReceived = now.getTime();
			            long latency= msReceived - msSend;
			    		if (result.equals("Success")) {
			                LOGGER.info("SendPayment(*) success, latency: " + latency + "ms" + ", id:" + request_id + ", client:" + Client_id );
			    		}else {
			    			LOGGER.warning("SendPayment(*) fail ," + result + ", id:" + request_id + ", client:" + Client_id );
			    		}
			    	}catch (Exception e){
			    		LOGGER.warning("Error");
			    	} 					
				}else {  //query balance
			    	try {
			        	Date now = new Date();
			        	long msSend = now.getTime();
			        	String account = "newHosuiting";
			    		String result = tool.Query(kvClient, account);
			            now = new Date();
			            long msReceived = now.getTime();
			            long latency= msReceived - msSend;
			    		if (!result.equals("No value found")) {
			                LOGGER.info("Query(*) success, latency: " + latency + "ms, id:" + request_id + ", client:" + Client_id );
			    		}else {
			    			LOGGER.warning("Query(*) fail,"+ result + ", id:" + request_id + ", client:" + Client_id );
			    		}
			    	}catch (Exception e){
			    		LOGGER.warning("Error");
			    	}
				}
			}
		}
	
	}

}
