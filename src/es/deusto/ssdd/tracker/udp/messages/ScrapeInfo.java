package es.deusto.ssdd.tracker.udp.messages;

/**
 * 
 * 	Size				Name
 * 	32-bit integer  	seeders	
 * 	32-bit integer  	completed
 * 	32-bit integer  	leechers
 * 
 */
public class ScrapeInfo {

	private int leechers;
	private int completed;
	private int seeders;
	
	public int getLeechers() {
		return leechers;
	}
	
	public void setLeechers(int leechers) {
		this.leechers = leechers;
	}
	
	public int getCompleted() {
		return completed;
	}

	public void setCompleted(int completed) {
		this.completed = completed;
	}

	public int getSeeders() {
		return seeders;
	}
	
	public void setSeeders(int seeders) {
		this.seeders = seeders;
	}
}