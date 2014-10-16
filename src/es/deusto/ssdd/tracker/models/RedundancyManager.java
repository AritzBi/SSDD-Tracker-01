package es.deusto.ssdd.tracker.models;

import java.util.List;
import java.util.Observer;

public class RedundancyManager  implements Runnable {

	private List<Observer> observers;

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

	public void desconectar() {
		this.notifyObservers(null);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
