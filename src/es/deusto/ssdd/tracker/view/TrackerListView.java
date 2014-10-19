package es.deusto.ssdd.tracker.view;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import es.deusto.ssdd.tracker.controller.TrackerListController;

public class TrackerListView extends JPanel implements Observer{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3494245647851905237L;
	private TrackerListController controller;

	public TrackerListView ( TrackerListController trackerListController )
	{
		controller = trackerListController;
		controller.addObserver(this);
	}
	
	@Override
	public void update(Observable o, Object arg) {
	}

}
