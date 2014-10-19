package es.deusto.ssdd.tracker.view;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import es.deusto.ssdd.tracker.controller.ConfigurationController;

public class ConfigurationView extends JPanel implements Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -291548739663122869L;
	private ConfigurationController controller;

	public ConfigurationView(ConfigurationController configurationController) {
		controller = configurationController;
		controller.addObserver(this);
	}

	@Override
	public void update(Observable o, Object arg) {

	}

}
