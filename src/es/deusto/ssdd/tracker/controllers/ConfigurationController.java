package es.deusto.ssdd.tracker.controllers;

import java.util.Observer;

import es.deusto.ssdd.tracker.models.RedundancyManager;

public class ConfigurationController {
	private RedundancyManager redundancyManager;
	
	public void desconectar(){
		redundancyManager.desconectar();
	}
	
	public void addObserver ( Observer observer )
	{
		
	}
	
	public void deleteObserver ( Observer observer )
	{
		
	}
}
