import java.io.Serializable;
import java.util.ArrayList;
public class block implements Serializable {
	private long timestamp;
	private int previousBlockHash;
	private ArrayList<TransactionInfo> transactionlist = new ArrayList<TransactionInfo>();
	
	public ArrayList<TransactionInfo> getTransactionList() {
		return transactionlist;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public void setPreviousBlockHash(int previousBlockHash) {
		this.previousBlockHash = previousBlockHash;
	}
	public void updateTransactionList(TransactionInfo transactionInfo ) {
		//if(this.transactionlist.size()<100) {
		this.transactionlist.add(transactionInfo);
			//return 1;
		//}else {
			//return 0;
		//}
	}
	public int getTransactionListSize() {
		return this.transactionlist.size();
	}
}
