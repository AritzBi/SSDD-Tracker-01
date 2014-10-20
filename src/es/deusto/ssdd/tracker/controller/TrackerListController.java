package es.deusto.ssdd.tracker.controller;

import java.util.Observer;

import es.deusto.ssdd.tracker.model.RedundancyManager;

public class TrackerListController {

	private RedundancyManager redundancyManager;

	public TrackerListController(RedundancyManager redundancyManager ) {
		this.redundancyManager = redundancyManager;
	}

	public void addObserver(Observer o) {
		redundancyManager.addObserver(o);
	}

	public void deleteObserver(Observer o) {
		redundancyManager.addObserver(o);
	}

}
