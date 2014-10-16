package es.deusto.ssdd.tracker.models;

import es.deusto.ssdd.tracker.vo.Tracker;

public class GlobalManager {

	private Tracker tracker;
	private static GlobalManager instance;

	public GlobalManager() {
		tracker = new Tracker();
	}

	public static GlobalManager getInstance() {
		if (instance == null) {
			instance = new GlobalManager();
		}
		return instance;
	}

	public Tracker getTracker() {
		return tracker;
	}

	public void setTracker(Tracker tracker) {
		this.tracker = tracker;
	}
}
