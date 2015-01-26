package es.deusto.ssdd.tracker.controller;

import java.util.List;
import java.util.Map;
import java.util.Observer;

import es.deusto.ssdd.tracker.model.DataManager;
import es.deusto.ssdd.tracker.model.RedundancyManager;
import es.deusto.ssdd.tracker.udp.messages.PeerInfo;
import es.deusto.ssdd.tracker.vo.Peer;

public class PeerListController {
	private RedundancyManager redundancyManager;

	public PeerListController(RedundancyManager redundancyManager) {
		this.redundancyManager=redundancyManager;
	}
	public void addObserver(Observer o) {
		redundancyManager.addObserver(o);
	}

	public void deleteObserver(Observer o) {
		redundancyManager.addObserver(o);
	}

	public List<Peer> getPeerList() {
		return DataManager.getInstance().findAllPeers();
	}
	
	public  Map<String, List<PeerInfo>>  getSeedersList() {
		return DataManager.getSeeders();
	}
	
	public  Map<String, List<PeerInfo>>  getLeechersList() {
		return DataManager.getLeechers();
	}
	
	
}
