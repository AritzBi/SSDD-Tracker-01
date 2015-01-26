package es.deusto.ssdd.tracker.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import es.deusto.ssdd.tracker.model.DataManager;
import es.deusto.ssdd.tracker.model.UDPManager;
import es.deusto.ssdd.tracker.udp.messages.PeerInfo;
import es.deusto.ssdd.tracker.vo.Peer;

public class PeerListController {
	private UDPManager udpManager;

	public PeerListController(UDPManager udpManager) {
		this.udpManager=udpManager;
	}
	public void addObserver(Observer o) {
		udpManager.addObserver(o);
	}

	public void deleteObserver(Observer o) {
		udpManager.addObserver(o);
	}

	public List<Peer> getPeerList() {
		return new ArrayList<Peer>(DataManager.getSessionsForPeers().values());
	}
	
	public  Map<String, List<PeerInfo>>  getSeedersList() {
		return DataManager.getSeeders();
	}
	
	public  Map<String, List<PeerInfo>>  getLeechersList() {
		return DataManager.getLeechers();
	}
	
	
}
