package com.asylumsw.bukkit.waypoints;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.Player;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.ChatColor;

/**
 *
 * @author jonathan
 */
public class WaypointPlayerListener extends PlayerListener {
	private final Waypoints plugin;

	public WaypointPlayerListener(Waypoints instance) {
		plugin = instance;
	}

	/**
	 * home
	 * gateto <gate>
	 * gates
	 * track <mark>
	 * marks
	 * setmark
	 * sethome
	 * unsethome
	 * setgate
	 * @param event
	 */
	@Override
	public void onPlayerCommand(PlayerChatEvent event) {
		String[] split = event.getMessage().split(" ");
		String command = split[0];
		Player player = event.getPlayer();

		if( command.equalsIgnoreCase("/home")) {
			event.setCancelled(true);
		}
		else if( command.equalsIgnoreCase("/sethome") ) {
			event.setCancelled(true);
		}

	}
	

}
