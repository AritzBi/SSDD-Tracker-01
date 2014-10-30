package es.deusto.ssdd.tracker.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import es.deusto.ssdd.tracker.vo.Peer;
//TODO  store JSON information
//TODO JSON dump
//TODO Save peer
//TODO 
@SuppressWarnings("unused")
public class DataManager implements Runnable {

	private List<Observer> observers;

	
	private Connection connection;
	private static DataManager instance;

	public DataManager() {
		observers = new ArrayList<Observer>();
	}
	
	/*** OBSERVABLE PATTERN IMPLEMENTACION ***/
	public void addObserver(Observer o) {
		if (o != null && !this.observers.contains(o)) {
			this.observers.add(o);
		}
	}

	public void deleteObserver(Observer o) {
		this.observers.remove(o);
	}
	
	private void notifyObservers(Object param) {
		for (Observer observer : this.observers) {
			if (observer != null) {
				observer.update(null, param);
			}
		}
	}

	/*** [END] OBSERVABLE PATTERN IMPLEMENTACION ***/
	
	public void connect( ) {

	}

	public void disconnect() {

	}

	public void insertNewPeer( Peer peer ) {

	}

	public void insertNewTorrent( String infoHash ) {

	}

	@Override
	public void run() {
		System.out.println("Llamo Data");
	}
}
