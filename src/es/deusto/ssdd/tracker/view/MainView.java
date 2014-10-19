package es.deusto.ssdd.tracker.view;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import es.deusto.ssdd.tracker.controller.ConfigurationController;
import es.deusto.ssdd.tracker.controller.PeerListController;
import es.deusto.ssdd.tracker.controller.TrackerListController;

public class MainView extends JFrame{
	
	private static final long serialVersionUID = -5839974224728234589L;
	
	private TrackerListView trackerListView;
	private ConfigurationView configurationView;
	private PeerListView peerListView;
	
	public MainView ()
	{
		setSize(600,300);
		setLocationRelativeTo(null);
		setTitle("Tracker UI");
		
		//Specify the tabs of the window
		JTabbedPane tabbedPane = new JTabbedPane();
		configurationView = new ConfigurationView( new ConfigurationController() );
		tabbedPane.addTab("Configuration", configurationView);
		trackerListView = new TrackerListView ( new TrackerListController() );
		tabbedPane.addTab("Trackers List", trackerListView);
		peerListView = new PeerListView( new PeerListController() );
		tabbedPane.addTab("Peers List", peerListView);
		add(tabbedPane);
		
	    addWindowListener(new WindowAdapter() {
	         public void windowClosing(WindowEvent windowEvent){
	            System.exit(0);
	         }        
	      });
		
	}
	
	public static void main ( String [] args )
	{
		MainView mainView = new MainView();
		mainView.setVisible(true);
	}

}
