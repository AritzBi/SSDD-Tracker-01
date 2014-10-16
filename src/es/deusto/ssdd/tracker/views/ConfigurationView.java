package es.deusto.ssdd.tracker.views;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import es.deusto.ssdd.tracker.controllers.ConfigurationController;

public class ConfigurationView extends JPanel implements Observer{
	/**
	 * 
	 */
	private static final long serialVersionUID = -291548739663122869L;
	private ConfigurationController controller;

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

}
