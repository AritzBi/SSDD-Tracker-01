package es.deusto.ssdd.tracker.udp.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Offset      	Size            	Name            Value
 * 0           	32-bit integer  	action          2 // scrape
 * 4           	32-bit integer  	transaction_id
 * 8 + 12 * n  	32-bit integer  	seeders
 * 12 + 12 * n 	32-bit integer  	completed
 * 16 + 12 * n 	32-bit integer  	leechers
 * 8 + 12 * N
 * 
 */

public class ScrapeResponse extends BitTorrentUDPMessage {
	
	private List<ScrapeInfo> scrapeInfos;

	public ScrapeResponse() {
		super(Action.SCRAPE);		
		this.scrapeInfos = new ArrayList<>();
	}
	
	@Override
	public byte[] getBytes() {
		int tamanyo = 8 + 12 * scrapeInfos.size();
		ByteBuffer byteBuffer = ByteBuffer.allocate(tamanyo);

		byteBuffer.order(ByteOrder.BIG_ENDIAN);

		byteBuffer.putInt(0, getAction().value() );
		byteBuffer.putInt(4, getTransactionId() );
		int inicio = 8;
		for ( ScrapeInfo scrapeInfo : scrapeInfos )
		{
			byteBuffer.putInt(inicio, scrapeInfo.getSeeders());
			inicio += 4;
			byteBuffer.putInt (inicio, scrapeInfo.getCompleted());
			inicio += 4;
			byteBuffer.putInt(inicio,scrapeInfo.getLeechers());
		}
		
		return byteBuffer.array();
	}
	
	public static ScrapeResponse parse(byte[] byteArray) {
		ByteBuffer bufferReceive = ByteBuffer.wrap(byteArray);
		ScrapeResponse scrapeResponse = new ScrapeResponse();
		scrapeResponse.setAction(Action.valueOf(bufferReceive.getInt(0)));
		scrapeResponse.setTransactionId( bufferReceive.getInt(4));
		
		List<ScrapeInfo> scrapes = new ArrayList<ScrapeInfo>();
		int inicio;
		for ( inicio = 8; inicio < byteArray.length ; inicio += 12 )
		{
			int seeders = bufferReceive.getInt(inicio);
			int completed = bufferReceive.getInt(inicio+4);
			int leechers = bufferReceive.getInt(inicio+8);
			ScrapeInfo scrapeInfo = new ScrapeInfo();
			scrapeInfo.setSeeders(seeders);
			scrapeInfo.setCompleted(completed);
			scrapeInfo.setLeechers(leechers);
			scrapes.add(scrapeInfo);
		}
		
		return scrapeResponse;
		
	}
	
	public List<ScrapeInfo> getScrapeInfos() {
		return scrapeInfos;
	}

	public void addScrapeInfo(ScrapeInfo scrapeInfo) {
		if (scrapeInfo != null && !this.scrapeInfos.contains(scrapeInfo)) {
			this.scrapeInfos.add(scrapeInfo);
		}
	}
}