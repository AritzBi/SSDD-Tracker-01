package es.deusto.ssdd.tracker.main;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import es.deusto.ssdd.tracker.controller.ConfigurationController;
import es.deusto.ssdd.tracker.controller.PeerListController;
import es.deusto.ssdd.tracker.controller.TrackerListController;
import es.deusto.ssdd.tracker.model.DataManager;
import es.deusto.ssdd.tracker.model.RedundancyManager;
import es.deusto.ssdd.tracker.model.UDPManager;
import es.deusto.ssdd.tracker.view.ConfigurationView;
import es.deusto.ssdd.tracker.view.MainView;
import es.deusto.ssdd.tracker.view.PeerListView;
import es.deusto.ssdd.tracker.view.TrackerListView;

public class MainProgram {

	public static void main ( String [] args )  {
		UDPManager udpManager = new UDPManager();
		ConfigurationController configurationController = new ConfigurationController(udpManager);
		ConfigurationView configurationView = new ConfigurationView(configurationController);
		
		DataManager dataManager = new DataManager();
		PeerListController peerListController = new PeerListController( dataManager );
		PeerListView peerListView = new PeerListView(peerListController);
		
		RedundancyManager redundancyManager = new RedundancyManager();
		TrackerListController trackerListController = new TrackerListController( redundancyManager );
		TrackerListView trackerListView = new TrackerListView(trackerListController);
		
		Map<String,JPanel> panels = new HashMap<String,JPanel>();
		panels.put ("Configuration" , configurationView );
		panels.put("Trackers List", trackerListView);
		panels.put("Peers List", peerListView);
		
		MainView mainWindow = new MainView ( panels );
		
		mainWindow.setVisible(true);
	}
}
