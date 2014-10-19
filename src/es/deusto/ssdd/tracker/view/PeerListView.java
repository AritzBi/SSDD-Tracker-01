package es.deusto.ssdd.tracker.view;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import es.deusto.ssdd.tracker.controller.PeerListController;

public class PeerListView extends JPanel implements Observer{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3290055577494365102L;
	private PeerListController controller;

	public PeerListView ( PeerListController peerListController)
	{
		controller = peerListController;
		controller.addObserver(this);
	}
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

}
