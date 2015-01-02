package es.deusto.ssdd.tracker.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.deusto.ssdd.tracker.udp.messages.PeerInfo;
import es.deusto.ssdd.tracker.vo.Peer;

public class DataManager {

	private Connection con;
	private static DataManager instance;
	
	public static Map<Long,Peer> peers;
	public static Map<String,List<PeerInfo>> seeders;
	public static Map<String,List<PeerInfo>> leechers;
	public DataManager() {
		peers = new HashMap<Long,Peer> ();
		seeders= new HashMap<String,List<PeerInfo>>();
		leechers=new HashMap<String,List<PeerInfo>>();
		initializeLists();
	}

	public static DataManager getInstance() {
		if (instance == null) {
			instance = new DataManager();
		}

		return instance;
	}

	public void connectDB(String dbname) {
		con = null;

		try {
			Class.forName("org.sqlite.JDBC");
			con = DriverManager.getConnection("jdbc:sqlite:" + dbname);
			con.setAutoCommit(false);

			System.out.println(" - Db connection was opened");
		} catch (Exception ex) {
			System.err.println(" # Unable to create SQLiteDBManager: "
					+ ex.getMessage());
		}
	}

	public void insertNewPeer(Peer peer) {

		if (peer.getId() != null && !peer.getId().isEmpty()
				&& peer.getIpAddress() != null
				&& !peer.getIpAddress().isEmpty() ) {

			String sqlString = "INSERT INTO PEER ('id', 'ip', 'port', 'downloaded','uploaded') VALUES (?,?,?,?,?)";

			try (PreparedStatement stmt = con.prepareStatement(sqlString)) {
				stmt.setString(1, peer.getId());
				stmt.setString(2, peer.getIpAddress());
				stmt.setInt(3, peer.getPort());
				stmt.setFloat(4, peer.getDownloaded());
				stmt.setFloat(5, peer.getUploaded());

				if (stmt.executeUpdate() == 1) {
					System.out.println("\n - A new peer was inserted. :)");
					con.commit();
				} else {
					System.err.println("\n - A new peer wasn't inserted. :(");
					con.rollback();
				}
			} catch (Exception ex) {
				System.err.println("\n # Error storing data in the db: "
						+ ex.getMessage());
			}
		} else {
			System.err
					.println("\n # Error inserting a new Peer: some parameters are 'null' or 'empty'.");
		}

	}
	public void initializeLists(){
		List<String>infoHashes=findAllInfoHashes();
		for(String infohash: infoHashes){
			List<PeerInfo>lSeeders=this.findPeersByInfoHash(infohash, true, false);
			List<PeerInfo>lLeechers=this.findPeersByInfoHash(infohash, false, true);
			seeders.put(infohash, lSeeders);
			leechers.put(infohash, lLeechers);
		}
		
	}
	public List<String> findAllInfoHashes(){
		String sqlString="Select info_hash from torrent";
		List<String>infoHashes=new ArrayList<String>();
		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {			
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()) {
			
				infoHashes.add(rs.getString(0));
			
			}				
		} catch (Exception ex) {
			System.err.println("\n # Error loading data in the db: " + ex.getMessage());
		}
		return infoHashes;
	}
	/**
	 * Method to find the peers associated to one info_hash
	 * @param infoHash
	 * @return
	 */
	public List<PeerInfo> findPeersByInfoHash ( String infoHash, boolean seeders, boolean leechers )
	{
		List<PeerInfo> peers = null;
		if ( infoHash != null && !infoHash.isEmpty() )
		{
			peers = new ArrayList<PeerInfo>();
			
			String sqlString = "Select ip,port from PEER,TORRENT,TORRENT_PEER where PEER.id = TORRENT_PEER.peer_id "
					+ "and TORRENT.id = TORRENT_PEER.torrent_id and TORRENT.Info_Hash = '" + infoHash + "'";
			
			if ( seeders )
			{
				sqlString += " and TORRENT_PEER.state = '1' ";
			}
			if ( leechers )
			{
				sqlString += " and TORRENT_PEER.state = '0' ";
			}
			
			try (PreparedStatement stmt = con.prepareStatement(sqlString)) {			
				ResultSet rs = stmt.executeQuery();
				
				while(rs.next()) {
				
					PeerInfo peerInfo = new PeerInfo();
					peerInfo.setIpAddress( rs.getInt("ip") );
					peerInfo.setPort(rs.getInt("port"));
					peers.add(peerInfo);
				
				}				
			} catch (Exception ex) {
				System.err.println("\n # Error loading data in the db: " + ex.getMessage());
			}
		}
		return peers;
	}

	public void insertNewTorrent(String infoHash) {

	}

	public void closeConnection() {
		try {
			con.close();
			System.out.println("\n - Db connection was closed :)");
		} catch (Exception ex) {
			System.err.println("\n # Error closing db connection: "
					+ ex.getMessage());
		}
	}
	
	public String addPeerToMemory ( Peer peer , long connectionId )
	{
		String response = "";
		if ( peers.containsKey(connectionId ) )
		{
			response = "500 ALREADY EXISTS THIS CONNECTION ID";
		}
		else
		{
			peers.put(connectionId, peer );
			response = "200 OK";
		}
		return response;
	}
	
	public String updatePeerMemory ( Peer peer, long connectionId )
	{
		String response = "";
		if ( peers.containsKey(connectionId ) )
		{
			Peer updtPeer = peers.get(connectionId);
			updtPeer.setDownloaded(peer.getDownloaded());
			updtPeer.setId(peer.getId());
			updtPeer.setIpAddress(peer.getIpAddress());
			updtPeer.setPort(peer.getPort());
			updtPeer.setUploaded(peer.getUploaded());
			response = "200 OK";
		}
		else
		{
			response = "500 ERROR";
		}
		return response;
	}
}
