package com.asylumsw.bukkit.waypoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
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

	public static void load() {
		MarkerData.initTable();
		markerList = MarkerData.getMarkers();
	}

	public static void list(CommandSender sender, String player) {
		ArrayList<String> markers = new ArrayList<String>();

		for(Map.Entry<String,Warp> mark : markerList.entrySet() ) {
			if( mark.getValue().getOwnerName().equalsIgnoreCase(player) ) {
				markers.add(mark.getKey());
			}
		}

		Collections.sort(markers);
		String msg = ChatColor.GRAY+"Known Markers: ";
		for( String gate : markers ) {
			if( msg.length() >= 40 ) {
				sender.sendMessage(ChatColor.DARK_GRAY+"[wp] "+msg);
				msg = "";
			}
			msg += ChatColor.AQUA + gate + ChatColor.GRAY + ", ";
		}
		sender.sendMessage(ChatColor.DARK_GRAY+"[wp] "+msg.substring(0, msg.length()-2));

	}

	public static boolean track(Player player, String name) {
		if( markerList.containsKey(name) ) {
			player.sendMessage(ChatColor.DARK_GRAY+"[wp] "+
							ChatColor.GOLD+"*** Setting Compass target to marker: "+name+" ***");
			player.setCompassTarget(markerList.get(name).getLocation());
			return true;
		}
		return false;
	}

	public static boolean setMark(Player player, String markerName) {
		if (markerName.equalsIgnoreCase("home")) {
			player.sendMessage(ChatColor.DARK_GRAY+"[wp]"+
							ChatColor.RED + "Error: Name 'home' is reserved.");
			return false;
		}
		if (markerName.equalsIgnoreCase("reset")) {
			player.sendMessage(ChatColor.DARK_GRAY+"[wp]"+
							ChatColor.RED + "Error: Name 'reset' is reserved.");
			return false;
		}
		if( Gates.gateExists(markerName) || markerList.containsKey(markerName) ) {
			player.sendMessage(ChatColor.DARK_GRAY+"[wp]"+
							ChatColor.RED+"Error: Name '"+markerName+"' is in use.");
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
				player.sendMessage(ChatColor.DARK_GRAY+"[wp]"+
								ChatColor.GOLD+"*** Setting Marker: '"+markerName+"' ***");
				return true;
			}
			else {
				player.sendMessage(ChatColor.DARK_GRAY+"[wp]"+
								ChatColor.RED+"ERROR: Error occured saving marker.");
			}
		}
		else {
			player.sendMessage(ChatColor.DARK_GRAY+"[wp]"+
							ChatColor.RED+"ERROR: Marker is invalid.");
		}

		return false;
	}

	public static boolean unsetMark(CommandSender sender, String markerName) {
		if( !markerList.containsKey(markerName) ) {
			sender.sendMessage(ChatColor.DARK_GRAY+"[wp]"+
							ChatColor.RED+"ERROR: No active marker named '"+markerName+"'.");
			return false;
		}
		Warp mark = markerList.get(markerName);

		if( !(sender instanceof Player) && !sender.isOp() ) {
			sender.sendMessage(ChatColor.DARK_GRAY+"[wp]"+
							ChatColor.RED+"ERROR: Only admins or players may remove markers.");
			return false;
		}
		
		Player player = (Player)sender;
		if( !mark.getOwnerName().equalsIgnoreCase(player.getName()) || !player.isOp() ) {
			sender.sendMessage(ChatColor.DARK_GRAY+"[wp]"+
							ChatColor.RED+"ERROR: Only admins or the marker's owner may remove a marker.");
			return false;
		}

		if( !MarkerData.deleteMarker(mark.getName()) ) {
			sender.sendMessage(ChatColor.DARK_GRAY+"[wp]"+
							ChatColor.RED+"ERROR: An error occured removing marker '"+markerName+"'.");
			return false;
		}
		
		sender.sendMessage(ChatColor.DARK_GRAY+"[wp]"+
						ChatColor.GOLD+"*** Removing marker '"+markerName+"'. ***");
		return true;
	}
}
