package es.deusto.ssdd.tracker.udp.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Offset          Size            	Name            	Value
 * 0               64-bit integer  	connection_id
 * 8               32-bit integer  	action          	2 // scrape
 * 12              32-bit integer  	transaction_id
 * 16 + 20 * n     20-byte string  	info_hash
 * 16 + 20 * N
 *
 */

public class ScrapeRequest extends BitTorrentUDPRequestMessage {

	private List<String> infoHashes;
	
	public ScrapeRequest() {
		super(Action.SCRAPE);
		this.infoHashes = new ArrayList<>();
	}
	
	@Override
	public byte[] getBytes() {
		int tamanyo = 16 + 20 * infoHashes.size();
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(tamanyo);

		byteBuffer.order(ByteOrder.BIG_ENDIAN);

		byteBuffer.putLong(0, getConnectionId() );
		byteBuffer.putInt(8, getAction().value() );
		byteBuffer.putInt(12, getTransactionId());
		int inicio = 20;
		for ( String infoHash : infoHashes )
		{
			byteBuffer.position(inicio);
			byteBuffer.put(infoHash.getBytes());
			inicio += 20;
		}
		
		return byteBuffer.array();
	}
	
	public static ScrapeRequest parse(byte[] byteArray) {
		ByteBuffer bufferReceive = ByteBuffer.wrap(byteArray);
		ScrapeRequest scrapeRequest = new ScrapeRequest();
		scrapeRequest.setConnectionId(bufferReceive.getLong(0));
		scrapeRequest.setAction(Action.valueOf(bufferReceive.getInt(8)));
		scrapeRequest.setTransactionId(bufferReceive.getInt(12));
		int inicio = 16;
		List<String> infoHashes = new ArrayList<String>();
		for ( inicio = 16; inicio < byteArray.length; inicio += 20 )
		{
			byte[] infoHashBytes = new byte [20];
			bufferReceive.position(inicio);
			bufferReceive.get(infoHashBytes);
			infoHashes.add(new String ( infoHashBytes ) );
		}
		return scrapeRequest;
	}
	
	public List<String> getInfoHashes() {
		return infoHashes;
	}

	public void addInfoHash(String infoHash) {
		if (infoHash != null && !infoHash.trim().isEmpty() && !this.infoHashes.contains(infoHash)) {
			this.infoHashes.add(infoHash);
		}
	}
}