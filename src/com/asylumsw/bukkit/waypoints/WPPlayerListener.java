package com.asylumsw.bukkit.waypoints;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.entity.Player;

/**
 *
 * @author jonathan
 */
public class WPPlayerListener extends PlayerListener {
	private final Waypoints plugin;

	public WPPlayerListener(Waypoints instance) {
		plugin = instance;
	}

	/**
	 * gateto <gate>
	 * gates
	 * track <mark>
	 * marks
	 * setmark
	 * setgate
	 * @param event
	 */
	@Override
	public void onPlayerCommand(PlayerChatEvent event) {
		if( event.isCancelled() ) return;

		String[] split = event.getMessage().split(" ");
		String command = split[0];
		Player player = event.getPlayer();

		if( command.equalsIgnoreCase("/home")) {
			Homes.sendPlayerHome(player, false, false);
			event.setCancelled(true);
		}
		else if( command.equalsIgnoreCase("/sethome") ) {
			Homes.setHomePoint(player);
			event.setCancelled(true);
		}
		else if( command.equalsIgnoreCase("/unsethome") ) {
			Homes.unsetHomePoint(player);
			event.setCancelled(true);
		}
	}
	

}
