package es.deusto.ssdd.tracker.udp.messages;

/**
 * 
 * 	Name    			Value
 *	64-bit integer  	connection_id
 *
 */

public abstract class BitTorrentUDPRequestMessage extends BitTorrentUDPMessage {
	
	private long connectionId;
	
	public BitTorrentUDPRequestMessage(Action action) {
		super(action);
	}

	public long getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(long connectionId) {
		this.connectionId = connectionId;
	}
}