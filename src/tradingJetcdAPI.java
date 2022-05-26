import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import static com.google.common.base.Charsets.UTF_8;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class tradingJetcdAPI {
	public KV getKVClient() {
		String endpoints = "http://127.0.0.1:2380,http://127.0.0.1:2381,http://127.0.0.1:2382";
		Client client = Client.builder().endpoints(endpoints.split(",")).build();
		KV kvClient = client.getKVClient();
		return kvClient;
		//return client.getKVClient();
	}
	private static ByteSequence bytesOf(String variable) {
		return ByteSequence.from(variable,UTF_8);
		//return ByteSequence.from(variable.getBytes());
	}
	
	public String CreateAccount(KV kvClient, String account)throws ExecutionException, InterruptedException{
        GetResponse response = kvClient.get(bytesOf(account)).get();
        if (response.getKvs().isEmpty()) {  // check if the key contains any value
            String value = Integer.toString(0); // store 0 into value, all balance is 0 when it is created
            PutResponse putresponse = kvClient.put(bytesOf(account), bytesOf(value)).get(); //store balance = 0 into account
            //LOGGER.info("CreateAccount(*) success, latency: " + latency + "account:" + account );
            return "Success";
        }else {
        	//LOGGER.warn("CreateAccount(*) fail, account:" + account );
        	return "Account Exists";
        }
	}
	
	public String SendPayment(KV kvClient, String sourceAccount,String destAccount, float amount)throws ExecutionException, InterruptedException{
    	//Check whether two accounts exist or not
        GetResponse response = kvClient.get(bytesOf(sourceAccount)).get();
        GetResponse response2 = kvClient.get(bytesOf(destAccount)).get();
        if (response.getKvs().isEmpty()) {  //sourceAccount not exists 
        	//LOGGER.warn("SendPayment(*) fail, sourceAccount:" + sourceAccount);
        	return "Source Account Not Exists";
        }
        if (response2.getKvs().isEmpty()) {  //destAccount not exists 
        	//LOGGER.warn("SendPayment(*) fail, destAccount:" + destAccount);
        	return "Destination Account Not Exists";
        }
        String sourceAccountBalance = response.getKvs().get(0).getValue().toString(UTF_8); 
        float sourceBalance = Float.parseFloat(sourceAccountBalance);  //get source account balance
        if (sourceBalance >= amount) {
        	//update sourceAccount balance
        	float newBalance = sourceBalance - amount;
        	String value = Float.toString(newBalance);
        	PutResponse putresponse = kvClient.put(bytesOf(sourceAccount), bytesOf(value)).get();
        	
        	//update destAccount balance
            String destAccountBalance = response2.getKvs().get(0).getValue().toString(UTF_8); 
            float destBalance = Float.parseFloat(destAccountBalance);
            destBalance = destBalance + amount;
            String newValue = Float.toString(destBalance);
        	PutResponse putresponse2 = kvClient.put(bytesOf(destAccount), bytesOf(newValue)).get();
        	//LOGGER.info("SendPayment(*) success, latency: " + latency + "sourceAccount:" + sourceAccount + " balance: " + sourceBalance);
            return "Success";
        }else {  //not enough money in source Account
        	//LOGGER.warn("SendPayment(*) fail, sourceAccount:" + sourceAccount + " balance: " + sourceBalance + " amount: " + amount);
        	return "Source Account Not Enough Money";
        }

	}
	public String Query(KV kvClient, String account)throws ExecutionException, InterruptedException{
        GetResponse response = kvClient.get(bytesOf(account)).get();
        if (response.getKvs().isEmpty()) {  // check if the key contains any value
            return "No value found";
        }else { //account contains value
        	String value = response.getKvs().get(0).getValue().toString(UTF_8);
        	//LOGGER.warn("CreateAccount(*) fail, account:" + account );
        	return value; //return the balance in the 
        }
	}
	
}
