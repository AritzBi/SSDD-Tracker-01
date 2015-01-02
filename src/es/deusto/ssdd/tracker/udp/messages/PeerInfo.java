package es.deusto.ssdd.tracker.udp.messages;

import java.util.regex.Pattern;

/**
 * 
 * 	Size				Name
 * 	32-bit integer  	IP address
 * 	16-bit integer  	TCP port
 *
 */

public class PeerInfo implements Comparable{
	private int ipAddress;
	private int port;
	
	public int getIpAddress() {
		return ipAddress;
	}
	
	public void setIpAddress(int ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public static String toStringIpAddress(int address) {
		StringBuffer ipBuffer = new StringBuffer();
		
		ipBuffer.append(((address >> 24 ) & 0xFF));
		ipBuffer.append(".");
		ipBuffer.append(((address >> 16 ) & 0xFF));
		ipBuffer.append(".");
		ipBuffer.append(((address >> 8 ) & 0xFF));
		ipBuffer.append(".");
		ipBuffer.append(address & 0xFF);
		
		return  ipBuffer.toString();
	}
	
	public static int parseIp(String address) {
	    int result = 0;

	    // iterate over each octet
	    for(String part : address.split(Pattern.quote("."))) {
	        // shift the previously parsed bits over by 1 byte
	        result = result << 8;
	        // set the low order bits to the current octet
	        result |= Integer.parseInt(part);
	    }
	    return result;
	}
	
	public static void main ( String [] args ) {
		int numero = PeerInfo.parseIp("127.0.0.1");
		System.out.println(numero);
	}

	@Override
	public int compareTo(Object o) {
		PeerInfo peerinfo=(PeerInfo) o;
		if(this.ipAddress==peerinfo.getIpAddress() && this.port==peerinfo.getPort()){
			return 0;
		}
		return -1;
	}
	
	
}