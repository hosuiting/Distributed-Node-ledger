
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerAPI extends Remote {
	//int CreateAccount(String AccountName) throws RemoteException;
	int SendPayment(String SourceAccount,String DestAccount, float balance) throws RemoteException;
	//int Query(String AccountName) throws RemoteException;
}
