package es.deusto.ssdd.tracker.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

public class RedundancyManager  implements Runnable {

	private List<Observer> observers;
	
	public RedundancyManager ()
	{
		observers = new ArrayList<Observer>();
	}

	/*** OBSERVABLE PATTERN IMPLEMENTATION **/
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

	/***[END] OBSERVABLE PATTERN IMPLEMENTATION **/
	
	public void desconectar() {
		this.notifyObservers(null);
	}
	
	@Override
	public void run() {
	}
}
