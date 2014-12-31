package es.deusto.ssdd.tracker.vo;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import es.deusto.ssdd.tracker.metainfo.handler.MetainfoHandler;

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

	
	public static void main ( String [] args )
	{
		String hex = "B415C913643E5FF49FE37D304BBB5E6E11AD5101";
		BigInteger value = new BigInteger(hex.substring(0, hex.length()),16); 
		System.out.println(MetainfoHandler.toHexString( Arrays.copyOfRange(value.toByteArray(), 1, value.toByteArray().length ) ));
		
	}
	

	
}
