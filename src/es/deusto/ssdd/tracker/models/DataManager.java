package es.deusto.ssdd.tracker.models;

import java.sql.Connection;
import java.util.Observable;

public class DataManager extends Observable{
	private Connection connection;
	private static DataManager instance;
	protected DataManager() {

	}
	public void connect(){
	
	}
	public void disconnect(){
		
	}
	
	public void insertNewPeer(){
		
	}
	
	public void insertNewTorrent(){
		
	}
}
