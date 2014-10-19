package es.deusto.ssdd.tracker.controller;

import java.util.Observer;

import es.deusto.ssdd.tracker.model.UDPManager;

public class ConfigurationController {
	private UDPManager udpManager;
	
	/** Duda: pondrias asi o se lo pasamos como parámetro ?? **/
	
	public ConfigurationController () {
		udpManager = new UDPManager();
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
	
	public void disconnect() {
	}
}
