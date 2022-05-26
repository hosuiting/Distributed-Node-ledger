import java.io.Serializable;
public class TransactionInfo implements Serializable {
	private String SourceAccount;
	private String DestAccount;
	private float amount;
	private int transactionID;
	
	public TransactionInfo(String SourceAccount,String DestAccount, float amount){
		this.SourceAccount = SourceAccount;
		this.DestAccount = DestAccount;
		this.amount = amount;
	}
	public String getSourceAccount() {
		return SourceAccount;
	}
	public String getDestAccount() {
		return DestAccount;
	}
	public float getAmount() {
		return amount;
	}
	public int getTransactionID() {
		return transactionID;
	}
	public void setTransactionID( int transactionID) {
		this.transactionID = transactionID;
	}
}
