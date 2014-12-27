package es.deusto.ssdd.tracker.udp.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 
 * Offset  Size            	Name            	Value
 * 0       32-bit integer  	action          	0 // connect
 * 4       32-bit integer  	transaction_id
 * 8       64-bit integer  	connection_id
 * 16
 * 
 */

public class ConnectResponse extends BitTorrentUDPRequestMessage {
	
	public ConnectResponse() {
		super(Action.CONNECT);
	}
	
	@Override
	public byte[] getBytes() {
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(16);

		byteBuffer.order(ByteOrder.BIG_ENDIAN);

		byteBuffer.putInt(0, getAction().value() );
		byteBuffer.putInt(4, getTransactionId() );
		byteBuffer.putLong(8, getConnectionId() );
		
		byteBuffer.flip();
		
		return byteBuffer.array();
	}
	
	public static ConnectResponse parse(byte[] byteArray) {
		
		ByteBuffer bufferReceive = ByteBuffer.wrap(byteArray);
		ConnectResponse connectResponse = new ConnectResponse();
		connectResponse.setAction(Action.valueOf(bufferReceive.getInt(0)));
		connectResponse.setTransactionId( bufferReceive.getInt(4));
		connectResponse.setConnectionId(bufferReceive.getLong(8));
		
		return connectResponse;
	}
}