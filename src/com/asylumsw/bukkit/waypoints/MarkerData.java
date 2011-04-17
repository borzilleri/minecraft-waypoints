package com.asylumsw.bukkit.waypoints;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jonathan
 */
public class MarkerData {

	public final static String MARKER_TABLE = "CREATE TABLE `markerList` ("
					+ "`id` INTEGER PRIMARY KEY,"
					+ "`player` varchar(255) NOT NULL,"
					+ "`name` varchar(255) NOT NULL UNIQUE,"
					+ "`x` int NOT NULL, `y` int NOT NULL, `z` int NOT NULL,"
					+ "`world` varchar(255) NOT NULL )";

	public static void initTable() {
		if (!tableExists()) {
			createTable();
		}
	}

	public static HashMap<String, Warp> getMarkers() {
		HashMap<String, Warp> markerList = new HashMap<String, Warp>();
		Connection conn = null;
		Statement statement = null;
		ResultSet set = null;
		Logger log = Logger.getLogger("Minecraft");
		
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Waypoints.DATABASE);

			statement = conn.createStatement();
			set = statement.executeQuery("SELECT player,name,x,y,z,world FROM markerList");
			while (set.next()) {
				String name = set.getString("name");
				Warp mark = new Warp(name, set.getInt("x"), set.getInt("y"),
								set.getInt("z"), 0, 0, set.getString("world"), Waypoint.MARKER);
				mark.setOwner(set.getString("player"));
				markerList.put(name, mark);
			}
		}
		catch (SQLException ex) {
			log.log(Level.SEVERE, "[WP:MARKER]: Warp Load Exception");
		}
		catch (ClassNotFoundException e) {
			log.log(Level.SEVERE, "[WP:MARKER]: Error loading org.sqlite.JDBC");
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
				log.log(Level.SEVERE, "[WP:MARKER]: Warp Load Exception (on close)");
			}
		}
		return markerList;
	}

	private static boolean tableExists() {
		Connection conn = null;
		ResultSet rs = null;

		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Waypoints.DATABASE);
			DatabaseMetaData dbm = conn.getMetaData();
			rs = dbm.getTables(null, null, "markerList", null);
			return (!rs.next() ? false : true);
		}
		catch (SQLException ex) {
			Logger log = Logger.getLogger("Minecraft");
			log.log(Level.SEVERE, "[WP:MARKER]: Table Check Exception");
			return false;
		}
		catch (ClassNotFoundException ex2) {
			Logger log = Logger.getLogger("Minecraft");
			log.log(Level.SEVERE, "[WP:MARKER]: Class Not Found Exception");
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
				log.log(Level.SEVERE, "[WP:MARKER]: Table Check Exception (on closing)");
			}
		}
	}

	private static void createTable() {
		Connection conn = null;
		Statement st = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Waypoints.DATABASE);
			st = conn.createStatement();
			st.executeUpdate(MARKER_TABLE);
		}
		catch (SQLException e) {
			Logger log = Logger.getLogger("Minecraft");
			log.log(Level.SEVERE, "[WP:MARKER]: Create Table Exception", e);
		}
		catch (ClassNotFoundException e) {
			Logger log = Logger.getLogger("Minecraft");
			log.log(Level.SEVERE, "[WP:MARKER]: Error loading org.sqlite.JDBC");
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
				log.log(Level.SEVERE, "[WP:MARKER]: Could not create the table (on close)");
			}
		}
	}

	public static boolean addMarker(Warp loc) {
		Connection conn = null;
		PreparedStatement ps = null;
		Logger log = Logger.getLogger("Minecraft");
		boolean success = false;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Waypoints.DATABASE);
			ps = conn.prepareStatement("INSERT INTO markerList (player, name, x, y, z, world) "
							+ "VALUES (?,?,?,?,?,?)");
			ps.setString(1, loc.getOwnerName());
			ps.setString(2, loc.getName());
			ps.setInt(3, loc.getX());
			ps.setInt(4, loc.getY());
			ps.setInt(5, loc.getZ());
			ps.setString(6, loc.getWorldName());

			ps.executeUpdate();
			success = true;
		}
		catch (SQLException ex) {
			log.log(Level.SEVERE, "[WP:MARKER]: Warp Insert Exception", ex);
		}
		catch (ClassNotFoundException ex2) {
			log.log(Level.SEVERE, "[WP:MARKER]: Error loading org.sqlite.JDBC");
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
				log.log(Level.SEVERE, "[WP:MARKER]: Warp Insert Exception (on close)", ex);

			}
		}

		return success;
	}

	public static boolean deleteMarker(String markerName) {
		Connection conn = null;
		PreparedStatement ps = null;
		Logger log = Logger.getLogger("Minecraft");
		boolean success = false;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Waypoints.DATABASE);
			ps = conn.prepareStatement("DELETE FROM markerList WHERE name = ?");
			ps.setString(1, markerName);
			ps.executeUpdate();
			success = true;
		}
		catch (SQLException ex) {
			log.log(Level.SEVERE, "[WP:MARKER]: Warp Insert Exception", ex);
		}
		catch (ClassNotFoundException ex2) {
			log.log(Level.SEVERE, "[WP:MARKER]: Error loading org.sqlite.JDBC");
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
				log.log(Level.SEVERE, "[WP:MARKER]: Warp Insert Exception (on close)", ex);
			}
		}
		return success;
	}
}
