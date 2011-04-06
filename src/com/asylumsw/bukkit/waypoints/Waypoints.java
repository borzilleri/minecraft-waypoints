package com.asylumsw.bukkit.waypoints;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author jonathan
 */
public class Waypoints extends JavaPlugin {

	public static Server serverInstance;
	public final static String DATABASE = "jdbc:sqlite:waypoints.db";

	@Override
	public void onEnable() {
		serverInstance = this.getServer();
		Homes.load();
		Gates.load();
		Markers.load();

		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
	}

	@Override
	public void onDisable() {
		System.out.println("Waypoints Disabled.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Is this necessary?
		if (!cmd.getName().equalsIgnoreCase("wp")) return false;
		if (1 > args.length) return false;

		String action = args[0];

		if (action.equalsIgnoreCase("home")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.GRAY + "[wp] "
								+ ChatColor.RED + "ERROR: This command is available only to players.");
			}
			else if (2 <= args.length) {
				if (args[1].equalsIgnoreCase("activate")) {
					Homes.setHomePoint((Player) sender);
					return true;
				}
				else if (args[1].equalsIgnoreCase("remove")) {
					Homes.unsetHomePoint((Player) sender);
					return true;
				}
				return false;
			}
			else {
				Homes.sendPlayerHome((Player) sender, false, false);
			}
			return true;
		}
		else if (action.equalsIgnoreCase("gate") && 2 <= args.length) {
			String gateAction = args[1];
			if( !(sender instanceof Player) && !gateAction.equalsIgnoreCase("list") ) {
				sender.sendMessage(ChatColor.GRAY + "[wp] "
								+ ChatColor.RED + "ERROR: This command is available only to players.");
				return true;
			}

			if (gateAction.equalsIgnoreCase("list")) {
				Gates.listPlayerGates(sender);
			}
			else if (gateAction.equalsIgnoreCase("remove")) {
				sender.sendMessage(ChatColor.RED + "Error: Removeing gates is not yet implemented.");
			}
			else if (gateAction.equalsIgnoreCase("activate")) {
				if (3 > args.length) {
					sender.sendMessage(ChatColor.RED + "Error: Must supply gate name.");
					return false;
				}
				Gates.activateGate((Player) sender, args[2]);
			}
			else if (gateAction.equalsIgnoreCase("check") ) {
				if( !(sender instanceof Player) ) {
					sender.sendMessage(ChatColor.DARK_GRAY+"[wp] "+
									ChatColor.RED+"ERROR: This command only available to players.");
				}
				else if(!Gates.isValidGateLocation(((Player) sender).getLocation())) {
					sender.sendMessage(ChatColor.DARK_GRAY+"[wp] "+
									ChatColor.RED+"ERROR: Gates must be "+
									Gates.GATE_DISTANCE_MINIMUM+" blocks from any other gate.");
				}
				else {
					sender.sendMessage(ChatColor.DARK_GRAY+"[wp] "+
									ChatColor.GOLD+"* Valid Gate Location.");
				}
			}
			else {
				Gates.sendPlayerToGate((Player) sender, gateAction);
			}
			return true;
		}
		else if (action.equalsIgnoreCase("track") && 2 <= args.length) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.GRAY + "[wp] "
								+ ChatColor.RED + "ERROR: This command is available only to players.");
			}
			else {
				Tracker.trackLocation((Player) sender, args[1]);
			}
			return true;
		}
		else if (action.equalsIgnoreCase("marker") && 2 <= args.length) {
			String markAction = args[1];

			if( markAction.equalsIgnoreCase("list") ) {
				String playerName = null;
				if( !(sender instanceof Player) && !sender.isOp() ) {
					sender.sendMessage(ChatColor.DARK_GRAY+"[wp] "+
									ChatColor.RED+"ERROR: This command is only available to players and admins");
					return true;
				}

				if( sender.isOp() && 3 <= args.length ) {
					playerName = args[2];
				}
				else if( sender.isOp() && !(sender instanceof Player) ) {
					playerName = null;
				}
				else {
					playerName = ((Player)sender).getName();
				}
				
				Markers.list(sender, playerName);
			}
			else if(markAction.equalsIgnoreCase("set")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.GRAY + "[wp] "
									+ ChatColor.RED + "ERROR: This command is available only to players.");
				}
				else if( 3 > args.length ) {
					sender.sendMessage(ChatColor.DARK_GRAY+"[wp] "+
									ChatColor.RED+"ERROR: You must specify a marker name.");
				}
				else {
					Markers.setMark((Player) sender, args[2]);
				}
				return true;
			}
			else if (markAction.equalsIgnoreCase("remove")) {
				if( 3 > args.length ) {
					sender.sendMessage(ChatColor.DARK_GRAY+"[wp] "+
									ChatColor.RED+"ERROR: You must specify a marker name.");
				}
				else {
					Markers.unsetMark(sender, args[2]);
				}
				return true;
			}
		}

		return false;
	}

	public static void warpPlayerTo(Player player, Location loc, int delay) {
		int sleepTime = 0;
		while (sleepTime < delay) {
			try {
				Thread.sleep(1000);
				player.sendMessage(ChatColor.GRAY + "* Teleport in " + (delay - sleepTime));
				sleepTime += 1;
			}
			catch (InterruptedException ex) {
			}
		}

		player.teleport(loc);
	}
}
