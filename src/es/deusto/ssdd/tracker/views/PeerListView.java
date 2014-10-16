package es.deusto.ssdd.tracker.views;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import es.deusto.ssdd.tracker.controllers.PeerListController;

public class PeerListView extends JPanel implements Observer{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3290055577494365102L;
	private PeerListController controller;

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

}
