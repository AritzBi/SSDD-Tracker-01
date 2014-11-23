package es.deusto.ssdd.tracker.controller;

import java.util.Observer;

import es.deusto.ssdd.tracker.model.DataManager;

public class PeerListController {

	private DataManager dataManager;

	public PeerListController(DataManager dataManager) {
		this.dataManager = dataManager;
	}

	public void addObserver(Observer o) {
		dataManager.addObserver(o);
	}

	public void deleteObserver(Observer o) {
		dataManager.deleteObserver(o);
	}
}
