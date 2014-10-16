package es.deusto.ssdd.tracker.controllers;

import java.util.Observer;

import es.deusto.ssdd.tracker.models.RedundancyManager;
import es.deusto.ssdd.tracker.models.UDPManager;

public class ConfigurationController {
	private UDPManager udpManager;
	
	public void connect( String ipAddress, String port , String id ) {
		udpManager.connect ( ipAddress, port , id );
	}
	
	public void disconnect() {
	}
	
	public void addObserver ( Observer o )
	{
		udpManager.addObserver(o);
	}
	
	public void deleteObserver ( Observer o )
	{
		udpManager.deleteObserver(o);
	}
}
