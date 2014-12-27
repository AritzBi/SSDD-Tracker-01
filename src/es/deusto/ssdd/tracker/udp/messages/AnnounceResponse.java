package es.deusto.ssdd.tracker.udp.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Offset      Size            	Name            Value
 * 0           32-bit integer  	action          1 // announce
 * 4           32-bit integer  	transaction_id
 * 8           32-bit integer  	interval
 * 12          32-bit integer  	leechers
 * 16          32-bit integer  	seeders
 * 20 + 6 * n  32-bit integer  	IP address
 * 24 + 6 * n  16-bit integer  	TCP port		(this is an unsigned int, so we use a Java char)
 * 20 + 6 * N
 * 
 */

public class AnnounceResponse extends BitTorrentUDPMessage {
	
	private int interval;
	private int leechers;
	private int seeders;
	
	private List<PeerInfo> peers;
	
	public AnnounceResponse() {
		super(Action.ANNOUNCE);
		this.peers = new ArrayList<PeerInfo>();
	}
	
	@Override
	public byte[] getBytes() {
		int tamanyo = 20 + 6 * peers.size();
				
		ByteBuffer byteBuffer = ByteBuffer.allocate(tamanyo);

		byteBuffer.order(ByteOrder.BIG_ENDIAN);

		byteBuffer.putInt(0, getAction().value());
		byteBuffer.putInt(4, getTransactionId());
		byteBuffer.putInt(8, getInterval());
		byteBuffer.putInt(12, getLeechers());
		byteBuffer.putInt(16, getSeeders());
		int inicio = 20;
		for (PeerInfo peerInfo : peers) {
			byteBuffer.putInt(inicio, peerInfo.getIpAddress());
			inicio += 4;
			byteBuffer.putChar(inicio, (char) peerInfo.getPort());
			inicio += 2;
		}
		
		byteBuffer.flip();
		
		return byteBuffer.array();
	}
	
	public static AnnounceResponse parse(byte[] byteArray) {
		ByteBuffer bufferReceive = ByteBuffer.wrap(byteArray);
		AnnounceResponse announceResponse = new AnnounceResponse();
		announceResponse.setAction( Action.valueOf(bufferReceive.getInt(0) ) );
		announceResponse.setTransactionId(bufferReceive.getInt(4));
		announceResponse.setInterval(bufferReceive.getInt(8));
		announceResponse.setLeechers(bufferReceive.getInt(12));
		announceResponse.setSeeders(bufferReceive.getInt(16));
		List<PeerInfo> peers = new ArrayList<PeerInfo>();
		
		int inicio;
		for ( inicio = 20; inicio < byteArray.length ; inicio += 6 )
		{
			int ipAddress = bufferReceive.getInt(inicio);
			char port = bufferReceive.getChar(inicio+4);
			PeerInfo peerInfo = new PeerInfo();
			peerInfo.setIpAddress(ipAddress);
			peerInfo.setPort(port);
			peers.add(peerInfo);
		}
		announceResponse.setPeers(peers);
		
		return announceResponse;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public int getLeechers() {
		return leechers;
	}

	public void setLeechers(int leechers) {
		this.leechers = leechers;
	}

	public int getSeeders() {
		return seeders;
	}

	public void setSeeders(int seeders) {
		this.seeders = seeders;
	}

	public List<PeerInfo> getPeers() {
		return peers;
	}

	public void setPeers(List<PeerInfo> peers) {
		this.peers = peers;
	}
}