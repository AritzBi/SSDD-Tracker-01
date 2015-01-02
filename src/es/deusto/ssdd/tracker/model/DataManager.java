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

	private DataManager() {
		peers = new HashMap<Long,Peer> ();
		seeders= new HashMap<String,List<PeerInfo>>();
		leechers=new HashMap<String,List<PeerInfo>>();
		
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
			initializeLists();
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
			List<PeerInfo>lSeeders = findPeersByInfoHash(infohash, true, false);
			List<PeerInfo>lLeechers = findPeersByInfoHash(infohash, false, true);
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
			
				infoHashes.add(rs.getString("info_hash"));
			
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
					peerInfo.setIpAddress( PeerInfo.parseIp( rs.getString("ip") ) );
					peerInfo.setPort(rs.getInt("port"));
					peers.add(peerInfo);
				
				}				
			} catch (Exception ex) {
				System.err.println("\n # Error loading data in the db: " + ex.getMessage());
			}
		}
		return peers;
	}

	public void insertNewTorrent(String infoHash, List<PeerInfo>seeders, List<PeerInfo>leechers) {

			String sqlString = "INSERT INTO TORRENT ('info_hash') VALUES (?)";

			try (PreparedStatement stmt = con.prepareStatement(sqlString)) {
				stmt.setString(1, infoHash);

				if (stmt.executeUpdate() == 1) {
					System.out.println("\n - A new torrent was inserted. :)");
					sqlString = "SELECT ID FROM TORRENT WHERE ID=?";
					int torrentId=0;
					try (PreparedStatement stmt3 = con.prepareStatement(sqlString)) {	
						stmt3.setString(0, infoHash);
						ResultSet rs = stmt3.executeQuery();
						torrentId=rs.getInt("id");
					} catch (Exception ex) {
						System.err.println("\n # Error loading data in the db: " + ex.getMessage());
					}	
					sqlString="INSERT INTO TORRENT_PEER ('torrent_id','peer_id','state') VALUES(?,?,?)";
					for(PeerInfo peerInfo:seeders){
						try (PreparedStatement stmt2 = con.prepareStatement(sqlString)) {
						stmt2.setInt(1,torrentId);
						stmt2.setString(2, peerInfo.getId());
						stmt2.setInt(3, 1);
						stmt2.executeUpdate();
						}catch(Exception e){
							System.err.println("\n - Error inserting seeders:(");
							e.printStackTrace();
						}
					}
					for(PeerInfo peerInfo:leechers){
						try (PreparedStatement stmt2 = con.prepareStatement(sqlString)) {
							stmt2.setInt(1,torrentId);
							stmt2.setString(2, peerInfo.getId());
							stmt2.setInt(3, 1);
							stmt2.executeUpdate();
							}catch(Exception e){
								System.err.println("\n - Error inserting leechers:(");
								e.printStackTrace();
							}
					}
					
					con.commit();
				
				} else {
					System.err.println("\n - A new torrent wasn't inserted. :(");
					con.rollback();
				}
			} catch (Exception ex) {
				System.err.println("\n # Error storing data in the db: "
						+ ex.getMessage());
			}
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
	
	public boolean existsInfoHashInMemory(String infohash){
		if(seeders.containsKey(infohash)||leechers.containsKey(infohash)){
			return true;
		}
		return false;
	}
	//TODO Pasarlo a un hilo
	public void insertLeechersAndSeeders (){
		String sqlString = "Delete from TORRENT_PEER ";
		executeQuery(sqlString);
		sqlString = "Delete from TORRENT ";
		executeQuery(sqlString);
	}
	
	public void executeQuery(String query){
		try (PreparedStatement stmt = con.prepareStatement(query)) {			
			stmt.executeQuery();	
		} catch (Exception ex) {
			System.err.println("\n # Error deleting data in the db: " + ex.getMessage());
		}
	}
	
	public int numberOfTorrentInWhichIsSeeder(String id){
		String sqlString="SELECT COUNT(TORRENT_ID) FROM TORRENT_PEER WHERE STATE='0' AND PEER_ID=?";
		int number=0;
		try (PreparedStatement stmt = con.prepareStatement(sqlString)) {	
			stmt.setString(0, id);
			ResultSet rs = stmt.executeQuery();
			number=rs.getInt(0);
			System.out.println(number);
		} catch (Exception ex) {
			System.err.println("\n # Error querying number of torrents in which the user is seeder:" + ex.getMessage());
		}
		return number;
	}
}
