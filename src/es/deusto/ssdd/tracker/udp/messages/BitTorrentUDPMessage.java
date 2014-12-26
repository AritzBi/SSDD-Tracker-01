package es.deusto.ssdd.tracker.udp.messages;

/**
 * 
 * 	Size                Name			Value
 *	32-bit integer  	action        	1 // announce
 *  32-bit integer  	transaction_id
 *
 */

public abstract class BitTorrentUDPMessage {

	public enum Action {
		CONNECT(0),
		ANNOUNCE(1),
		SCRAPE(2),
		ERROR(3);
		
		private int value;
		
		private Action(int value) {
			this.value = value;
		}
		
		public int value() {
			return this.value;
		}
		
		public static Action valueOf(int value) {
			for (Action a : Action.values()) {
				if (a.value == value) {
					return a;
				}
			}
			
			return null;
		}
	}
	
	private Action action;
	private int transactionId;
	
	public BitTorrentUDPMessage(Action action) {
		this.action = action;
	}
	
	public Action getAction() {
		return action;
	}
	
	public void setAction(Action action) {
		this.action = action;
	}
	
	public int getTransactionId() {
		return transactionId;
	}
	
	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}
	
	public abstract byte[] getBytes();
}