package es.deusto.ssdd.tracker.vo;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class Utils {

	private static Map<String,byte[]> infoHashes = new HashMap<String,byte[]> ();
	
	public static byte[] getInfoHash ( String infoHash )
	{
		return infoHashes.get(infoHash);
	}
	public static void putInfoHash ( String infoHash, byte[] infoHashB )
	{
		if ( !infoHashes.containsKey(infoHash) )
			infoHashes.put(infoHash, infoHashB);
	}
	public static byte[] parsearArrayBytes ( byte [] bytes, int length )
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(length);
		ByteBuffer.wrap(bytes);
		return byteBuffer.array();
	}
	
}
