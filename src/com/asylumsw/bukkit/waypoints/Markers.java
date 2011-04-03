package com.asylumsw.bukkit.waypoints;

import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 *
 * @author jonathan
 */
public class Markers {
	private static HashMap<String,Warp> markerList;
	protected static int[][] markerStartPoints = new int[][] {
		{-1,1,0}, // North of Player
		{-1,-1,0}, // South of Player
		{-1,0,1}, // East of Player
		{-1,0,-1}  // West of Player
	};
	protected static Material[][][] markerPattern = new Material[][][] {
		{
			{Material.COBBLESTONE}
		},
		{
			{Material.COBBLESTONE}
		},
		{
			{Material.REDSTONE_TORCH_ON}
		}
	};

	public static void loadMarkers() {
		MarkerData.initTable();
		markerList = MarkerData.getMarkers();
	}

	public static boolean setMark(Player player, String markerName) {
		if (markerName.equalsIgnoreCase("home")) {
			player.sendMessage(ChatColor.RED + "Error: Name 'home' is reserved.");
			return false;
		}
		if (markerName.equalsIgnoreCase("reset")) {
			player.sendMessage(ChatColor.RED + "Error: Name 'reset' is reserved.");
			return false;
		}
		if( Gates.gateExists(markerName) || markerList.containsKey(markerName) ) {
			player.sendMessage(ChatColor.RED+"Error: Name '"+markerName+"' is in use.");
			return false;
		}

		boolean isValidMark = false;
		for( int[] startPoint: markerStartPoints ) {
			Structure.Validator validator = new Structure.Validator();
			Structure.parse(markerPattern, startPoint, player.getLocation(), validator,true);

			if( 0 >= validator.invalidBlockCount ) {
				isValidMark = true;
				break;
			}
		}

		if( isValidMark ) {
			Warp mark = new Warp(markerName, player.getLocation(), Waypoint.MARKER);
			mark.setOwner(player);

			//if( MarkerData.addMarker(mark) ) {
			if( true ) {
				//markerList.put(markerName, mark);
				player.sendMessage(ChatColor.GOLD+"*** Setting Marker: '"+markerName+"' ***");
				return true;
			}
			else {
				player.sendMessage(ChatColor.RED+"ERROR: Error occured saving marker.");
			}
		}
		else {
			player.sendMessage(ChatColor.RED+"ERROR: Marker is invalid.");
		}

		return false;
	}

	public static boolean unsetMark(Player player, String markerName) {
		if( !markerList.containsKey(markerName) ) {
			player.sendMessage(ChatColor.RED+"ERROR: No active marker named '"+markerName+"'.");
			return false;
		}

		Warp mark = markerList.get(markerName);

		if( !MarkerData.deleteMarker(mark.getName()) ) {
			player.sendMessage(ChatColor.RED+"ERROR: An error occured removing marker '"+markerName+"'.");
			return false;
		}
		
		player.sendMessage(ChatColor.GOLD+"*** Removing marker '"+markerName+"'. ***");
		return true;
	}
}
