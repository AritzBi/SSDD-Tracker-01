package es.deusto.ssdd.tracker.vo;

import java.util.Date;

public class Peer {

	private String id;
	private String ipAddress;
	private int port;
	private float downloaded;
	private float uploaded;
	private float left;
	private long lastConnection;

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

	public float getLeft() {
		return left;
	}

	public void setLeft(float left) {
		this.left = left;
	}

	public long getLastConnection() {
		return lastConnection;
	}
	
	public String getLastConnectionFormateada() {
		return new Date(lastConnection).toString();
	}

	public void setLastConnection(long lastConnection) {
		this.lastConnection = lastConnection;
	}
	
	
}
