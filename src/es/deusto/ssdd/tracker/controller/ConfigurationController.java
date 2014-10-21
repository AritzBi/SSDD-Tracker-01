package es.deusto.ssdd.tracker.controller;

import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.deusto.ssdd.tracker.model.UDPManager;

public class ConfigurationController {
	private UDPManager udpManager;

	private Pattern pattern;
	private Matcher matcher;
	
	private static final String IPADDRESS_PATTERN = 
			"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	
	public ConfigurationController ( UDPManager udpManager ) {
		this.udpManager = udpManager;
		pattern = Pattern.compile(IPADDRESS_PATTERN);
	}
	
	public void addObserver ( Observer o )
	{
		udpManager.addObserver(o);
	}
	
	public void deleteObserver ( Observer o )
	{
		udpManager.deleteObserver(o);
	}
	
	/**
	 * Method used to connect the tracker with a specified IP, port and id
	 * @param ipAddress
	 * @param port
	 * @param id
	 */
	public void connect( String ipAddress, String port , String id ) {
		udpManager.connect ( ipAddress, port , id );
	}
	
	/**
	 * Method used to validate if an IP address matches a pattern
	 * @param ip
	 * @return true: matches the IP address pattern
	 */
	public boolean checkIpAddress ( String ip )
	{
		matcher = pattern.matcher(ip);
		return matcher.matches();
	}
	
	public void disconnect() {
	}
}
