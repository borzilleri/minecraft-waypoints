/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asylumsw.bukkit.waypoints;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Arrow;
import org.bukkit.Block;
import org.bukkit.Boat;
import org.bukkit.Chunk;
import org.bukkit.ItemDrop;
import org.bukkit.ItemStack;
import org.bukkit.Location;
import org.bukkit.Minecart;
import org.bukkit.PoweredMinecart;
import org.bukkit.StorageMinecart;
import org.bukkit.Vector;
import org.bukkit.World;

/**
 *
 * @author jonathan
 */
public class HomeData {

	public final static String HOME_TABLE = "CREATE TABLE `homeList` ("
					+ "`id` INTEGER PRIMARY KEY,"
					+ "`player` varchar(255) NOT NULL UNIQUE,"
					+ "x int NOT NULL, y int NOT NULL, z int NOT NULL"
					+ "pitch smallint NOT NULL DEFAULT '0',"
					+ "yaw smallint NOT NULL DEFAULT '0'";

	public static void initTable() {
		if (!tableExists()) {
			createTable();
		}
	}

	public static HashMap<String, Location> getHomes() {
		HashMap<String, Location> homeList = new HashMap<String, Location>();
		Connection conn = null;
		Statement statement = null;
		ResultSet set = null;
		Logger log = Logger.getLogger("Minecraft");
		
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Waypoints.DATABASE);

			statement = conn.createStatement();
			set = statement.executeQuery("SELECT player,x,y,z,pitch,yaw FROM homeList");
			int size = 0;
			Location thisLoc = null;

			while (set.next()) {
				thisLoc = new Location(null, set.getInt("x"), set.getInt("y"), set.getInt("z"), set.getInt("yaw"), set.getInt("pitch"));

				size++;
				int index = set.getInt("id");
				String name = set.getString("name");
				String creator = set.getString("creator");
				int world = set.getInt("world");
				int x = set.getInt("x");
				int y = set.getInt("y");
				int z = set.getInt("z");
				int yaw = set.getInt("yaw");
				int pitch = set.getInt("pitch");
				boolean publicAll = set.getBoolean("publicAll");
				String permissions = set.getString("permissions");
				String welcomeMessage = set.getString("welcomeMessage");
				Warp warp = new Warp(index, name, creator, world, x, y, z, yaw, pitch, publicAll, permissions, welcomeMessage);
				ret.put(name, warp);
			}
			log.info("[MYWARP]: " + size + " warps loaded");
		}
		catch (SQLException ex) {
			log.log(Level.SEVERE, "[MYWARP]: Warp Load Exception");
		}
		catch (ClassNotFoundException e) {
			log.log(Level.SEVERE, "[MYWARP]: Error loading org.sqlite.JDBC");
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
				log.log(Level.SEVERE, "[MYWARP]: Warp Load Exception (on close)");
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
			log.log(Level.SEVERE, "[WAYPOINTS:HOMES]: Table Check Exception");
			return false;
		}
		catch (ClassNotFoundException ex2) {
			Logger log = Logger.getLogger("Minecraft");
			log.log(Level.SEVERE, "[WAYPOINTS]: Table Check Exception");
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
				log.log(Level.SEVERE, "[WAYPOINTS]: Table Check Exception (on closing)");
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
			log.log(Level.SEVERE, "[WAYPOINTS:HOMES]: Create Table Exception", e);
		}
		catch (ClassNotFoundException e) {
			Logger log = Logger.getLogger("Minecraft");
			log.log(Level.SEVERE, "[WAYPOINTS]: Error loading org.sqlite.JDBC");
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
				log.log(Level.SEVERE, "[WAYPOINTS]: Could not create the table (on close)");
			}
		}
	}

	public static void addHome(String playerName, Location loc) {
		Connection conn = null;
		PreparedStatement ps = null;
		Logger log = Logger.getLogger("Minecraft");
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Waypoints.DATABASE);
			ps = conn.prepareStatement("INSERT INTO homeList (player, x, y, z, pitch, yaw) "
							+ "VALUES (?,?,?,?,?,?)");
			ps.setString(1, playerName);
			ps.setInt(2, loc.getBlockX());
			ps.setInt(3, loc.getBlockY());
			ps.setInt(4, loc.getBlockZ());
			ps.setInt(5, (int) loc.getPitch());
			ps.setInt(6, (int) loc.getYaw());

			ps.executeUpdate();

		}
		catch (SQLException ex) {
			log.log(Level.SEVERE, "[WAYPOINTS:HOME]: Warp Insert Exception", ex);
		}
		catch (ClassNotFoundException ex2) {
			log.log(Level.SEVERE, "[WAYPOINTS]: Error loading org.sqlite.JDBC");
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
				log.log(Level.SEVERE, "[WAYPOINTS]: Warp Insert Exception (on close)", ex);
			}
		}
	}

	public static void deleteHome(String playerName) {
		Connection conn = null;
		PreparedStatement ps = null;
		Logger log = Logger.getLogger("Minecraft");
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Waypoints.DATABASE);
			ps = conn.prepareStatement("DELETE FROM homeList WHERE player = ?");
			ps.setString(1, playerName);
			ps.executeUpdate();
		}
		catch (SQLException ex) {
			log.log(Level.SEVERE, "[WAYPOINTS:HOME]: Warp Insert Exception", ex);
		}
		catch (ClassNotFoundException ex2) {
			log.log(Level.SEVERE, "[WAYPOINTS]: Error loading org.sqlite.JDBC");
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
				log.log(Level.SEVERE, "[WAYPOINTS]: Warp Insert Exception (on close)", ex);
			}
		}

	}
}
