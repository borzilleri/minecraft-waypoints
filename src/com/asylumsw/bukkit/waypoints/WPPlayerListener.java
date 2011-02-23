package com.asylumsw.bukkit.waypoints;

import org.bukkit.ChatColor;
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
	 * gates
	 * track <mark>
	 * marks
	 * setmark
	 * @param event
	 */
	@Override
	public void onPlayerCommand(PlayerChatEvent event) {
		if (event.isCancelled()) {
			return;
		}

		String[] split = event.getMessage().split(" ");
		String command = split[0];
		Player player = event.getPlayer();

		if (command.equalsIgnoreCase("/home")) {
			Homes.sendPlayerHome(player, false, false);
			event.setCancelled(true);
		}
		else if (command.equalsIgnoreCase("/sethome")) {
			Homes.setHomePoint(player);
			event.setCancelled(true);
		}
		else if (command.equalsIgnoreCase("/unsethome")) {
			Homes.unsetHomePoint(player);
			event.setCancelled(true);
		}
		else if (command.equalsIgnoreCase("/gates")) {
			Gates.listPlayerGates(player);
			event.setCancelled(true);
		}
		else if (command.equalsIgnoreCase("/gate")) {
			if (split.length < 2) {
				player.sendMessage(ChatColor.RED + "Error: Must supply gate name.");
			}
			else {
				Gates.debug = split.length >= 3 && player.isOp();
				Gates.override = split.length >= 4 && player.isOp();
				Gates.sendPlayerToGate(player, split[1]);
			}
			event.setCancelled(true);
		}
		else if (command.equalsIgnoreCase("/activategate")) {
			if (split.length < 2) {
				player.sendMessage(ChatColor.RED + "Error: Must supply gate name.");
			}
			else {
				Gates.debug = split.length >= 3 && player.isOp();
				Gates.override = split.length >= 4 && player.isOp();
				Gates.activateGate(player, split[1]);
			}
			event.setCancelled(true);
		}
		else if (command.equalsIgnoreCase("/deactivategate")) {
			player.sendMessage(ChatColor.RED + "Error: command not yet implemented.");
		}
	}
}
