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

public class RedundancyManager  implements Runnable {

	private List<Observer> observers;
	private GlobalManager globalManager;
	private MulticastSocket socket;
	
	private static final int port = 48900;
	
	public RedundancyManager ()
	{
		observers = new ArrayList<Observer>();
		globalManager = GlobalManager.getInstance();
		
	}
	
	private void socketListening () {
		try {
			socket = new MulticastSocket(port);
			InetAddress inetAddress = InetAddress.getByName( globalManager.MULTICAST_IP_ADDRESS);
			socket.joinGroup(inetAddress);
			
			DatagramPacket packet;
			while ( true )
			{
				byte[] buf = new byte[256];
				packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				
				String messageReceived = new String( packet.getData() );
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

	/***[END] OBSERVABLE PATTERN IMPLEMENTATION **/
	
	public void desconectar() {
		this.notifyObservers(null);
	}
	
	public void sendKeepAlive () {
		InetAddress inetAddress = null;
		try {
			inetAddress = InetAddress.getByName(GlobalManager.MULTICAST_IP_ADDRESS);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		String message = globalManager.getTracker().getId() + "KeepAlive";
		byte[] messageBytes = message.getBytes();
		DatagramPacket datagramPacket = new DatagramPacket(messageBytes, messageBytes.length, inetAddress, port);
		try {
			socket.send(datagramPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		socketListening();
		sendKeepAlive();
	}
}
