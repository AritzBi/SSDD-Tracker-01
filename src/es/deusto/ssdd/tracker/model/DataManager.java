package es.deusto.ssdd.tracker.model;

import java.sql.Connection;
import java.util.List;
import java.util.Observer;

import es.deusto.ssdd.tracker.vo.Peer;

public class DataManager implements Runnable {

	private List<Observer> observers;

	private Connection connection;
	private static DataManager instance;

	protected DataManager() {

	}

	public void connect( ) {

	}

	public void disconnect() {

	}

	public void insertNewPeer( Peer peer ) {

	}

	public void insertNewTorrent( String infoHash ) {

	}

	public void addObserver(Observer o) {
		if (o != null && !this.observers.contains(o)) {
			this.observers.add(o);
		}
	}

	public void deleteObserver(Observer o) {
		this.observers.remove(o);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
