
import java.util.Date;
import java.util.logging.Logger;

import io.etcd.jetcd.KV;

public class testJetcdClient {
	private static Logger LOGGER = null;
	static {
	      System.setProperty("java.util.logging.SimpleFormatter.format",
	              "%1$tF %1$tT %4$-7s: %5$s %n");
	      LOGGER = Logger.getLogger(testJetcdClient.class.getName());
	}
	public static void main(String[] args) {
		//Logger logger = Logger.getGlobal();
		// TODO Auto-generated method stub
		tradingJetcdAPI tool = new tradingJetcdAPI();
		KV kvClient = tool.getKVClient();
		
    	try {
        	Date now = new Date();
        	long msSend = now.getTime();
        	String account = "newHosuiting";
    		String result = tool.CreateAccount(kvClient, account);
            now = new Date();
            long msReceived = now.getTime();
            long latency= msReceived - msSend;
    		if (result.equals("Success")) {
                LOGGER.info("CreateAccount(*) success, latency: " + latency + "ms, account:" + account );
    		}else {
    			LOGGER.warning("CreateAccount(*) fail,"+ result + ", account:" + account );
    		}
    	}catch (Exception e){
    		LOGGER.warning("Error");
    	}
    	
    	
    	try {
        	Date now = new Date();
        	long msSend = now.getTime();
        	String sourceAccount = "hosuiting";
        	String destAccount = "newHosuiting";
    		String result = tool.SendPayment(kvClient, sourceAccount,destAccount, 100);
            now = new Date();
            long msReceived = now.getTime();
            long latency= msReceived - msSend;
    		if (result.equals("Success")) {
                LOGGER.info("SendPayment(*) success, latency: " + latency + "ms");
    		}else {
    			LOGGER.warning("SendPayment(*) fail ," + result);
    		}
    	}catch (Exception e){
    		LOGGER.warning("Error");
    	} 		
    	try {
        	Date now = new Date();
        	long msSend = now.getTime();
        	String account = "newHosuiting";
    		String result = tool.Query(kvClient, account);
            now = new Date();
            long msReceived = now.getTime();
            long latency= msReceived - msSend;
    		if (!result.equals("No value found")) {
                LOGGER.info("Query(*) success, latency: " + latency + "ms, account:" + account + ", balance:" + result);
    		}else {
    			LOGGER.warning("Query(*) fail,"+ result + ", account:" + account );
    		}
    	}catch (Exception e){
    		LOGGER.warning("Error");
    	}

	}

}
