package es.deusto.ssdd.tracker.vo;

public class Peer {

	private String id;
	private String ipAddress;
	private int port;
	private float downloaded;
	private float uploaded;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public float getDownloaded() {
		return downloaded;
	}

	public void setDownloaded(float downloaded) {
		this.downloaded = downloaded;
	}

	public float getUploaded() {
		return uploaded;
	}

	public void setUploaded(float uploaded) {
		this.uploaded = uploaded;
	}
	
}
