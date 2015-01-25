package es.deusto.ssdd.tracker.udp.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 
 * Offset  Size            	Name            	Value
 * 0       32-bit integer  	action          	3 // error
 * 4       32-bit integer  	transaction_id
 * 8       string  message
 * 
 */

public class Error extends BitTorrentUDPMessage {

	private String message;

	public Error() {
		super(Action.ERROR);
	}
	
	@Override
	public byte[] getBytes() {
		
		int size = 8 + message.getBytes().length;
		ByteBuffer byteBuffer = ByteBuffer.allocate(size);

		byteBuffer.order(ByteOrder.BIG_ENDIAN);

		byteBuffer.putInt(0, getAction().value() );
		byteBuffer.putInt(4, getTransactionId() );
		byteBuffer.position(8);
		byteBuffer.put( message.getBytes() );

		byteBuffer.flip();
		
		return byteBuffer.array();
	}
	
	public static Error parse(byte[] byteArray) {
		
		ByteBuffer bufferReceive = ByteBuffer.wrap(byteArray);
		Error error = new Error();
		error.setAction(Action.valueOf(bufferReceive.getInt(0) ));
		error.setTransactionId(bufferReceive.getInt(4));
		byte [] message = new byte[byteArray.length-8];
		bufferReceive.position(8);
		bufferReceive.get ( message );
		error.setMessage(message.toString());
		return error;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public static void main ( String [] args )
	{
		String error = "Prueba tottoot gfgdgfdfghg";
		System.out.println( error.getBytes().length );
	}
}