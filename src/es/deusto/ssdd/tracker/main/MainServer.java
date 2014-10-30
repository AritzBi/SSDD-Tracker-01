package es.deusto.ssdd.tracker.main;

import es.deusto.ssdd.tracker.model.GlobalManager;

public class MainServer {

	public static void main(String[] args) {
		GlobalManager globalManager = GlobalManager.getInstance();
		globalManager.start();
		
	}

}
