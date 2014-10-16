package es.deusto.ssdd.tracker.controllers;

import es.deusto.ssdd.tracker.models.RedundancyManager;

public class ConfigurationController {
	private RedundancyManager redundancyManager;
	
	public void desconectar(){
		redundancyManager.desconectar();
	}
}
