package com.asylumsw.bukkit.waypoints;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;

/**
 *
 * @author jonathan
 */
public class GateData {

	public final static String GATE_TABLE = "CREATE TABLE `gateList` ("
					+ "`id` INTEGER PRIMARY KEY,"
					+ "`name` varchar(255) NOT NULL UNIQUE,"
					+ "`x` int NOT NULL, `y` int NOT NULL, `z` int NOT NULL,"
					+ "`pitch` smallint NOT NULL,"
					+ "`yaw` smallint NOT NULL,"
					+ "`world` varchar(255) NOT NULL, "
					+ "`owner` varchar(255) NOT NULL "
					+ " )";
	public final static String GATE_ACTIVATION_TABLE = "CREATE TABLE `gateActivations` ( "
					+ "`player` varchar(255) NOT NULL, "
					+ "`gate_id` INTEGER NOT NULL, "
					+ "FOREIGN KEY(gate_id) REFERENCES gateList(id) "
					+ ")";

	public static void initTable() {
		if( !tableExists("gateList") ) {
			createTable(GATE_TABLE);
		}

		if( !tableExists("gateActivations") ) {
			createTable(GATE_ACTIVATION_TABLE);
		}
	}

	public static HashMap<String, HashSet<String>> getGateActivations() {
		HashMap<String, HashSet<String>> gateAccess = new HashMap<String, HashSet<String>>();

		Connection conn = null;
		Statement statement = null;
		ResultSet set = null;
		Logger log = Logger.getLogger("Minecraft");

		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Waypoints.DATABASE);

			statement = conn.createStatement();
			set = statement.executeQuery("SELECT player,name FROM gateActivations "
							+ "LEFT OUTER JOIN gateList ON (gate_id=id)");
			while (set.next()) {
				String player = set.getString("player");
				String gate = set.getString("name");
				if( !gateAccess.containsKey(player) ) {
					gateAccess.put(player, new HashSet<String>());
				}
				gateAccess.get(player).add(gate);
			}
		}
		catch (SQLException ex) {
			log.log(Level.SEVERE, 
							String.format("[WAYPOINTS:GATES]: Activation Load Exception: %s", ex.getMessage()),
							ex.getCause());
		}
		catch (ClassNotFoundException e) {
			log.log(Level.SEVERE, "[WAYPOINTS:GATES]: Error loading org.sqlite.JDBC");
		}
		finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (set != null) {
					set.close();
				}
				if (conn != null) {
					conn.close();
				}
			}
			catch (SQLException ex) {
				log.log(Level.SEVERE, "[WAYPOINTS:GATES]: Activation Load Exception (on close)");
			}
		}

		return gateAccess;
	}
	public static HashMap<String, Warp> getGates() {
		HashMap<String, Warp> gateList = new HashMap<String, Warp>();
		Connection conn = null;
		Statement statement = null;
		ResultSet set = null;
		Logger log = Logger.getLogger("Minecraft");
		
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Waypoints.DATABASE);

			statement = conn.createStatement();
			set = statement.executeQuery("SELECT name,x,y,z,pitch,yaw,world,owner FROM gateList");
			while (set.next()) {
				String name = set.getString("name");
				Warp gate = new Warp(name, set.getInt("x"), set.getInt("y"),
								set.getInt("z"), set.getInt("yaw"), set.getInt("pitch"),
								set.getString("world"), Waypoint.GATE);
				gate.setOwner(set.getString("owner"));
				gateList.put(name, gate);
			}
		}
		catch (SQLException ex) {
			log.log(Level.SEVERE,
							String.format("[WAYPOINTS:GATES]: Gate Load Exception: %s", ex.getMessage()),
							ex.getCause());		}
		catch (ClassNotFoundException e) {
			log.log(Level.SEVERE, "[WAYPOINTS:GATES]: Error loading org.sqlite.JDBC");
		}
		finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (set != null) {
					set.close();
				}
				if (conn != null) {
					conn.close();
				}
			}
			catch (SQLException ex) {
				log.log(Level.SEVERE, "[WAYPOINTS:GATES]: Gate Load Exception (on close)");
			}
		}
		return gateList;
	}

	private static boolean tableExists(String tableName) {
		Connection conn = null;
		ResultSet rs = null;

		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Waypoints.DATABASE);
			DatabaseMetaData dbm = conn.getMetaData();
			rs = dbm.getTables(null, null, tableName, null);
			return (!rs.next() ? false : true);
		}
		catch (SQLException ex) {
			Logger log = Logger.getLogger("Minecraft");
			log.log(Level.SEVERE, "[WAYPOINTS:GATES]: Table Check Exception: {0}", tableName);
			return false;
		}
		catch (ClassNotFoundException ex2) {
			Logger log = Logger.getLogger("Minecraft");
			log.log(Level.SEVERE, "[WAYPOINTS:GATES]: Class Not Found Exception");
			return false;
		}
		finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (conn != null) {
					conn.close();
				}
			}
			catch (SQLException ex) {
				Logger log = Logger.getLogger("Minecraft");
				log.log(Level.SEVERE, "[WAYPOINTS:GATES]: Table Check Exception (on closing)");
			}
		}
	}

	private static void createTable(String createString) {
		Connection conn = null;
		Statement st = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Waypoints.DATABASE);
			st = conn.createStatement();
			st.executeUpdate(createString);
		}
		catch (SQLException e) {
			Logger log = Logger.getLogger("Minecraft");
			log.log(Level.SEVERE, "[WAYPOINTS:GATES]: Create Table Exception", e);
		}
		catch (ClassNotFoundException e) {
			Logger log = Logger.getLogger("Minecraft");
			log.log(Level.SEVERE, "[WAYPOINTS:GATES]: Error loading org.sqlite.JDBC");
		}
		finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (st != null) {
					st.close();
				}
			}
			catch (SQLException e) {
				Logger log = Logger.getLogger("Minecraft");
				log.log(Level.SEVERE, "[WAYPOINTS:GATES]: Could not create the table (on close)");
			}
		}
	}

	public static boolean addGateActivation(Player player, String gate) {
		Connection conn = null;
		PreparedStatement ps = null;
		Logger log = Logger.getLogger("Minecraft");
		boolean success = false;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Waypoints.DATABASE);
			ps = conn.prepareStatement("INSERT INTO gateActivations (player, gate_id) "
							+ "SELECT ?, id FROM gateList WHERE name=?");
			ps.setString(1, player.getName());
			ps.setString(2, gate);
			ps.executeUpdate();
			success = true;
		}
		catch (SQLException ex) {
			log.log(Level.SEVERE, "[WAYPOINTS:GATES]: Warp Insert Exception: {0}", ex.getMessage());
		}
		catch (ClassNotFoundException ex2) {
			log.log(Level.SEVERE, "[WAYPOINTS:GATES]: Error loading org.sqlite.JDBC");
		}
		finally {
			try {
				if (ps != null) ps.close();
				if (conn != null) conn.close();
			}
			catch (SQLException ex) {
				log.log(Level.SEVERE, "[WAYPOINTS:GATES]: Warp Insert Exception (on close)", ex);
			}
		}
		return success;
	}

	public static boolean addGate(Warp loc) {
		Connection conn = null;
		PreparedStatement ps = null;
		Logger log = Logger.getLogger("Minecraft");
		boolean success = false;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Waypoints.DATABASE);
			ps = conn.prepareStatement("INSERT INTO gateList (name, x, y, z, pitch, yaw, world, owner) "
							+ "VALUES (?,?,?,?,?,?,?,?)");
			ps.setString(1, loc.getName());
			ps.setInt(2, loc.getX());
			ps.setInt(3, loc.getY());
			ps.setInt(4, loc.getZ());
			ps.setInt(5, (int) loc.getPitch());
			ps.setInt(6, (int) loc.getYaw());
			ps.setString(7, loc.getWorldName());
			ps.setString(8, loc.getOwnerName());

			ps.executeUpdate();
			success = true;
		}
		catch (SQLException ex) {
			log.log(Level.SEVERE, "[WAYPOINTS:GATES]: Warp Insert Exception", ex);
		}
		catch (ClassNotFoundException ex2) {
			log.log(Level.SEVERE, "[WAYPOINTS:GATES]: Error loading org.sqlite.JDBC");
		}
		finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (conn != null) {
					conn.close();
				}
			}
			catch (SQLException ex) {
				log.log(Level.SEVERE, "[WAYPOINTS:GATES]: Warp Insert Exception (on close)", ex);

			}
		}

		return success;
	}

	public static boolean deleteGate(String playerName) {
		Connection conn = null;
		PreparedStatement ps = null;
		Logger log = Logger.getLogger("Minecraft");
		boolean success = false;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Waypoints.DATABASE);
			ps = conn.prepareStatement("DELETE FROM gateList WHERE player = ?");
			ps.setString(1, playerName);
			ps.executeUpdate();
			success = true;
		}
		catch (SQLException ex) {
			log.log(Level.SEVERE, "[WAYPOINTS:GATES]: Warp Delete Exception", ex);
		}
		catch (ClassNotFoundException ex2) {
			log.log(Level.SEVERE, "[WAYPOINTS:GATES]: Error loading org.sqlite.JDBC");
		}
		finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (conn != null) {
					conn.close();
				}
			}
			catch (SQLException ex) {
				log.log(Level.SEVERE, "[WAYPOINTS:GATES]: Warp Delete Exception (on close)", ex);
			}
		}
		return success;
	}
}
