package es.deusto.ssdd.tracker.views;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import es.deusto.ssdd.tracker.controllers.TrackerListController;

public class TrackerListView extends JPanel implements Observer{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3494245647851905237L;
	private TrackerListController controller;

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

}
