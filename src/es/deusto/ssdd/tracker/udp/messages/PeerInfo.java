package es.deusto.ssdd.tracker.udp.messages;

/**
 * 
 * 	Size				Name
 * 	32-bit integer  	IP address
 * 	16-bit integer  	TCP port
 *
 */

public class PeerInfo {
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
}