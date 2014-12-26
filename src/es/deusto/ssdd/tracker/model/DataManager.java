package es.deusto.ssdd.tracker.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import es.deusto.ssdd.tracker.vo.Peer;

public class DataManager {

	private Connection con;
	private static DataManager instance;
	
	private Map<Long,Peer> peers;

	public DataManager() {
		peers = new HashMap<Long,Peer> ();
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

			System.out.println(" - Db connection was opened :)");
		} catch (Exception ex) {
			System.err.println(" # Unable to create SQLiteDBManager: "
					+ ex.getMessage());
		}
	}

	public void insertNewPeer(Peer peer) {

		if (peer.getId() != null && !peer.getId().isEmpty()
				&& peer.getIpAddress() != null
				&& !peer.getIpAddress().isEmpty() ) {

			String sqlString = "INSERT INTO STUDENT ('id', 'ip', 'port', 'downloaded','uploaded') VALUES (?,?,?,?,?)";

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
	
	public void addPeerToMemory ( Peer peer , long connectionId )
	{
		if ( peers.containsKey(connectionId ) )
		{
			Peer updtPeer = peers.get(connectionId);
			updtPeer.setDownloaded(peer.getDownloaded());
			updtPeer.setId(peer.getId());
			updtPeer.setIpAddress(peer.getIpAddress());
			updtPeer.setPort(peer.getPort());
			updtPeer.setUploaded(peer.getUploaded());
		}
		else
		{
			peers.put(connectionId, peer );
		}
	}
}
