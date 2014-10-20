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
	
<<<<<<< HEAD
	/** Duda: pondrias asi o se lo pasamos como par�metro ?? **/
	
=======
>>>>>>> a478f96f405254ed8e9a65dc19984fa492e76382
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
	
	public void connect( String ipAddress, String port , String id ) {
		udpManager.connect ( ipAddress, port , id );
	}
	
	public boolean checkIpAddress ( String ip )
	{
		matcher = pattern.matcher(ip);
		return matcher.matches();
	}
	
	public void disconnect() {
	}
}
