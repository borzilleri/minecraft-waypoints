package com.asylumsw.bukkit.waypoints;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author jonathan
 */
public class Tracker {


	public static boolean trackLocation(Player player, String name) {
		World world = player.getWorld();
		Server server = player.getServer();

		// Check the static "reset" target.
		if( name.equalsIgnoreCase("reset") ) {
			player.sendMessage(ChatColor.GOLD+"*** Resetting Compass to Spawn Point ***");
			player.setCompassTarget(world.getSpawnLocation());
			return true;
		}
		// Check the static "home" target.
		if( name.equalsIgnoreCase("home") ) {
			return Homes.trackHomePoint(player);
		}

		// Check to see if we're tracking another player.
		Player target = server.getPlayer(name);
		if( target instanceof Player ) {
			player.sendMessage(ChatColor.GOLD+"*** Setting Compass to location of '"+name+"'. ***");
			player.setCompassTarget(target.getLocation());
			return true;
		}

		// Check to see if we're tracking a Gate.
		// We can only track gates we've activated.
		if( Gates.trackGate(player, name) ) {
			return true;
		}

		if( Markers.track(player, name) ) {
			return true;
		}

		player.sendMessage(ChatColor.RED+"ERROR: Unknown marker: "+name);
		return false;
	}
}
