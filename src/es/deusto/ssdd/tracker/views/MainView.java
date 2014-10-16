package es.deusto.ssdd.tracker.views;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import es.deusto.ssdd.tracker.controllers.ConfigurationController;
import es.deusto.ssdd.tracker.controllers.PeerListController;
import es.deusto.ssdd.tracker.controllers.TrackerListController;

public class MainView extends JFrame{
	
	private static final long serialVersionUID = -5839974224728234589L;
	
	private TrackerListView trackerListView;
	private ConfigurationView configurationView;
	private PeerListView peerListView;
	
	public MainView ()
	{
		JTabbedPane tabbedPane = new JTabbedPane();
		configurationView = new ConfigurationView( new ConfigurationController() );
		tabbedPane.addTab("Configuration", configurationView);
		trackerListView = new TrackerListView ( new TrackerListController() );
		tabbedPane.addTab("Trackers List", trackerListView);
		peerListView = new PeerListView( new PeerListController() );
		tabbedPane.addTab("Peers List", peerListView);
		add(tabbedPane);
		
	}
	
	public static void main ( String [] args )
	{
		MainView main = new MainView();
	}

}
