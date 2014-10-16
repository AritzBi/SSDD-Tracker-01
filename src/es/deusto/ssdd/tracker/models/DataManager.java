package es.deusto.ssdd.tracker.models;

import java.sql.Connection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class DataManager extends Observable {

	private List<Observer> observers;

	private Connection connection;
	private static DataManager instance;

	protected DataManager() {

	}

	public void connect() {

	}

	public void disconnect() {

	}

	public void insertNewPeer() {

	}

	public void insertNewTorrent() {

	}

	public void addObserver(Observer o) {
		if (o != null && !this.observers.contains(o)) {
			this.observers.add(o);
		}
	}

	public void deleteObserver(Observer o) {
		this.observers.remove(o);
	}
}
