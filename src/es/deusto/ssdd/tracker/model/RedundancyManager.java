package es.deusto.ssdd.tracker.model;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

//TODO: Proceso de elección del master
//TODO: Enviar keep alive
//TODO: Ready para guardar información
//TODO: Ok, poodeis guardar información
//TODO Cuando quitar un tracker
//TODO  Recibir mensaje de los demás trackers 

public class RedundancyManager implements Runnable {

	private List<Observer> observers;
	private GlobalManager globalManager;
	private MulticastSocket socket;
	private InetAddress inetAddress;
	private boolean stop = false;

	public RedundancyManager() {
		observers = new ArrayList<Observer>();
		globalManager = GlobalManager.getInstance();
	}
	
	private void generateSocket() {
		try {
			socket = new MulticastSocket(globalManager.getTracker().getPort());
			inetAddress = InetAddress
					.getByName(globalManager.getTracker().getIpAddress());
			socket.joinGroup(inetAddress);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void listeningSocket() {
		try {

			DatagramPacket packet;
			while (!stop) {
				byte[] buf = new byte[256];
				packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);

				String messageReceived = new String(packet.getData());
				System.out.println("Received info..." + messageReceived);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	/*** [END] OBSERVABLE PATTERN IMPLEMENTATION **/

	public void desconectar() {
		this.notifyObservers(null);
	}

	private synchronized void writeSocket(DatagramPacket datagramPacket) {
		try {
			socket.send(datagramPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getKeepAliveMessage() {
		return globalManager.getTracker().getId() + "KeepAlive";
	}

	public void sendKeepAlive() {

		String message = getKeepAliveMessage();
		byte[] messageBytes = message.getBytes();
		DatagramPacket datagramPacket = new DatagramPacket(messageBytes,
				messageBytes.length, inetAddress, globalManager.getTracker().getPort());
		writeSocket(datagramPacket);
	}

	public void generateThreadToSendKeepAliveMessages() {
		Thread t = new Thread() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(2000);
						sendKeepAlive();
					} catch (InterruptedException e) {
					}

				}
			}
		};
		t.start();
	}

	@Override
	public void run() {
		generateSocket();
		generateThreadToSendKeepAliveMessages();
		listeningSocket();
		sendKeepAlive();
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}
}
