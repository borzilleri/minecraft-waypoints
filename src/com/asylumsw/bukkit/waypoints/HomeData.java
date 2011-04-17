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
import org.bukkit.Location;

/**
 *
 * @author jonathan
 */
public class HomeData {

	public final static String HOME_TABLE = "CREATE TABLE `homeList` ("
					+ "`id` INTEGER PRIMARY KEY,"
					+ "`player` varchar(255) NOT NULL UNIQUE,"
					+ "`x` int NOT NULL, `y` int NOT NULL, `z` int NOT NULL,"
					+ "`pitch` smallint NOT NULL,"
					+ "`yaw` smallint NOT NULL,"
					+ "`world` varchar(255) NOT NULL )";

	public static void initTable() {
		if (!tableExists()) {
			createTable();
		}
	}

	public static HashMap<String, Warp> getHomes() {
		HashMap<String, Warp> homeList = new HashMap<String, Warp>();
		Connection conn = null;
		Statement statement = null;
		ResultSet set = null;
		Logger log = Logger.getLogger("Minecraft");
		
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Waypoints.DATABASE);

			statement = conn.createStatement();
			set = statement.executeQuery("SELECT player,x,y,z,pitch,yaw,world FROM homeList");
			while (set.next()) {
				String player = set.getString("player");
				Warp home = new Warp(player, set.getInt("x"), set.getInt("y"),
								set.getInt("z"), set.getInt("yaw"), set.getInt("pitch"),
								set.getString("world"), Waypoint.HOME);
				homeList.put(player, home);
			}
		}
		catch (SQLException ex) {
			log.log(Level.SEVERE, "[WP:HOMES]: Load exception.");
			log.log(Level.INFO, ex.getMessage());
		}
		catch (ClassNotFoundException e) {
			log.log(Level.SEVERE, "[WP:HOMES]: Error loading org.sqlite.JDBC");
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
				log.log(Level.SEVERE, "[WP:HOMES]: Load Exception (on close)");
			}
		}
		return homeList;
	}

	private static boolean tableExists() {
		Connection conn = null;
		ResultSet rs = null;

		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Waypoints.DATABASE);
			DatabaseMetaData dbm = conn.getMetaData();
			rs = dbm.getTables(null, null, "homeList", null);
			return (!rs.next() ? false : true);
		}
		catch (SQLException ex) {
			Logger log = Logger.getLogger("Minecraft");
			log.log(Level.SEVERE, "[WP:HOMES]: Table check exception");
			return false;
		}
		catch (ClassNotFoundException ex2) {
			Logger log = Logger.getLogger("Minecraft");
			log.log(Level.SEVERE, "[WP:HOMES]: Class Not Found Exception");
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
				log.log(Level.SEVERE, "[WP:HOMES]: Table Check Exception (on closing)");
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
			st.executeUpdate(HOME_TABLE);
		}
		catch (SQLException e) {
			Logger log = Logger.getLogger("Minecraft");
			log.log(Level.SEVERE, "[WP:HOMES]: Create Table Exception", e);
		}
		catch (ClassNotFoundException e) {
			Logger log = Logger.getLogger("Minecraft");
			log.log(Level.SEVERE, "[WP:HOMES]: Error loading org.sqlite.JDBC");
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
				log.log(Level.SEVERE, "[WP:HOMES]: Could not create the table (on close)");
			}
		}
	}

	public static boolean addHome(Warp loc) {
		Connection conn = null;
		PreparedStatement ps = null;
		Logger log = Logger.getLogger("Minecraft");
		boolean success = false;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Waypoints.DATABASE);
			ps = conn.prepareStatement("INSERT INTO homeList (player, x, y, z, pitch, yaw, world) "
							+ "VALUES (?,?,?,?,?,?,?)");
			ps.setString(1, loc.getName());
			ps.setInt(2, loc.getX());
			ps.setInt(3, loc.getY());
			ps.setInt(4, loc.getZ());
			ps.setInt(5, (int) loc.getPitch());
			ps.setInt(6, (int) loc.getYaw());
			ps.setString(7, loc.getWorldName());

			ps.executeUpdate();
			success = true;
		}
		catch (SQLException ex) {
			log.log(Level.SEVERE, "[WP:HOMES]: Warp Insert Exception", ex);
		}
		catch (ClassNotFoundException ex2) {
			log.log(Level.SEVERE, "[WP:HOMES]: Error loading org.sqlite.JDBC");
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
				log.log(Level.SEVERE, "[WP:HOMES]: Warp Insert Exception (on close)", ex);

			}
		}

		return success;
	}

	public static boolean deleteHome(String playerName) {
		Connection conn = null;
		PreparedStatement ps = null;
		Logger log = Logger.getLogger("Minecraft");
		boolean success = false;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Waypoints.DATABASE);
			ps = conn.prepareStatement("DELETE FROM homeList WHERE player = ?");
			ps.setString(1, playerName);
			ps.executeUpdate();
			success = true;
		}
		catch (SQLException ex) {
			log.log(Level.SEVERE, "[WP:HOMES]: Warp Delete Exception", ex);
		}
		catch (ClassNotFoundException ex2) {
			log.log(Level.SEVERE, "[WP:HOMES]: Error loading org.sqlite.JDBC");
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
				log.log(Level.SEVERE, "[WP:HOMES]: Warp Insert Exception (on close)", ex);
			}
		}
		return success;
	}
}
