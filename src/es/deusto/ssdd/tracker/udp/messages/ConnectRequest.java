package es.deusto.ssdd.tracker.udp.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * Offset  Size            	Name            	Value
 * 0       64-bit integer  	connection_id   	0x41727101980 (default value)
 * 8       32-bit integer  	action          	0 // connect
 * 12      32-bit integer  	transaction_id
 * 16
 * 
 */

public class ConnectRequest extends BitTorrentUDPRequestMessage {
	
	public ConnectRequest() {
		super(Action.CONNECT);
		super.setConnectionId(Long.decode("0x41727101980"));
	}
	
	@Override
	public byte[] getBytes() {
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(16);

		byteBuffer.order(ByteOrder.BIG_ENDIAN);

		byteBuffer.putLong(0, getConnectionId() );
		byteBuffer.putInt(8, getAction().value() );
		byteBuffer.putInt(12, getTransactionId() );
		
		byteBuffer.flip();
		
		return byteBuffer.array();
	}
	
	public static ConnectRequest parse(byte[] byteArray) {
		ByteBuffer bufferReceive = ByteBuffer.wrap(byteArray);
		ConnectRequest connectRequest = new ConnectRequest();
		connectRequest.setConnectionId(bufferReceive.getLong(0));
		connectRequest.setAction(Action.valueOf(bufferReceive.getInt(8)));
		connectRequest.setTransactionId(bufferReceive.getInt(12));
		return connectRequest;
	}
}