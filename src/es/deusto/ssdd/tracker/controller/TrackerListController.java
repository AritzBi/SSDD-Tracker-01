package es.deusto.ssdd.tracker.controller;

import java.util.List;
import java.util.Observer;

import es.deusto.ssdd.tracker.model.GlobalManager;
import es.deusto.ssdd.tracker.model.RedundancyManager;
import es.deusto.ssdd.tracker.vo.ActiveTracker;

public class TrackerListController {

	private RedundancyManager redundancyManager;
	private GlobalManager globalManager;

	public TrackerListController(RedundancyManager redundancyManager ) {
		this.redundancyManager = redundancyManager;
		this.globalManager = GlobalManager.getInstance();
	}

	public void addObserver(Observer o) {
		redundancyManager.addObserver(o);
	}

	public void deleteObserver(Observer o) {
		redundancyManager.addObserver(o);
	}
	
	public List<ActiveTracker> getActiveTrackers() {
		return globalManager.getActiveTrackers();
	}

}
