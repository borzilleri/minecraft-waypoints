package com.asylumsw.bukkit.waypoints;

import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 *
 * @author jonathan
 */
public class Waypoints extends JavaPlugin {

	public static Server serverInstance;
	public final static String DATABASE = "jdbc:sqlite:waypoints.db";
	public static Teleporter teleporter;
	private static int teleporterTaskId;

	@Override
	public void onEnable() {
		serverInstance = this.getServer();
		teleporter = new Teleporter(this);
		teleporterTaskId = getServer().getScheduler().scheduleAsyncRepeatingTask(this, teleporter, 0, 20);

		Warp.plugin = this;
		Homes.load();
		Gates.load();
		Markers.load();

		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
	}

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTask(teleporterTaskId);

		System.out.println("Waypoints Disabled.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String action;
		if (label.equalsIgnoreCase("home")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.DARK_GRAY + "[wp] "
								+ ChatColor.RED + "ERROR: /home is only available to players.");
			}
			else if (1 <= args.length) {
				action = args[0];
				if (action.equalsIgnoreCase("activate")) {
					Homes.setHomePoint((Player)sender);
					return true;
				}
				else if (action.equalsIgnoreCase("remove")) {
					Homes.unsetHomePoint((Player)sender);
					return true;
				}
				return false;
			}
			else {
				Homes.sendPlayerHome((Player)sender, false, false);
			}
		}
		else if (label.equalsIgnoreCase("gate")) {
			if (1 > args.length) return false;
			action = args[0];

			if (action.equalsIgnoreCase("list")) {
				Gates.listPlayerGates(sender);
			}
			else if (action.equalsIgnoreCase("reload") ) {
				if( !sender.isOp() ) {
					sender.sendMessage(ChatColor.DARK_GRAY + "[wp] "
									+ ChatColor.RED + "ERROR: Must be an admin to reload gate data.");
				}
				else {
					Gates.reload();
				}
			}
			else if (action.equalsIgnoreCase("check")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.DARK_GRAY + "[wp] "
									+ ChatColor.RED + "ERROR: Must be a logged in player to check gate locations.");
				}
				else if (!Gates.isValidGateLocation(((Player) sender).getLocation())) {
					sender.sendMessage(ChatColor.DARK_GRAY + "[wp] "
									+ ChatColor.RED + "ERROR: Gates must be "
									+ Gates.GATE_DISTANCE_MINIMUM + " blocks from any other gate.");
				}
				else {
					sender.sendMessage(ChatColor.DARK_GRAY + "[wp] "
									+ ChatColor.GOLD + "* Valid Gate Location.");
				}
			}
			else if (action.equalsIgnoreCase("activate")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.DARK_GRAY + "[wp] "
									+ ChatColor.RED + "ERROR: Must be a logged in player to activate gates.");
				}
				else if (args.length < 2) {
					sender.sendMessage(ChatColor.RED + "Error: Must supply gate name.");
				}
				else {
					Gates.activateGate((Player) sender, args[1]);
				}
			}
			else if (action.equalsIgnoreCase("remove")) {
				String gateName = (args.length < 2) ? null : args[1];
				if( !(sender instanceof Player) && !sender.isOp() ) {
					sender.sendMessage(ChatColor.DARK_GRAY+"[wp] "+
									ChatColor.RED+"ERROR: Must be an op or a logged in player to remove gates.");
				}
				else {
					Gates.deactivateGate(sender, gateName);
				}
			}
			else if (action.equalsIgnoreCase("rename")) {
				if( !(sender instanceof Player) && !sender.isOp() ) {
					sender.sendMessage(ChatColor.DARK_GRAY+"[wp] "+
									ChatColor.RED+"ERROR: Must be an op or a logged in player to rename gates.");
				}
				else if( args.length < 3 ) {
					sender.sendMessage(ChatColor.DARK_GRAY+"[wp] "+
									ChatColor.RED+"ERROR: Must supply the existing gate name and the new gate name");
					return false;
				}
				else {
					Gates.renameGate(sender, args[1], args[2]);
				}
			}
			else {
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.DARK_GRAY + "[wp] "
									+ ChatColor.RED + "ERROR: Must be a logged in player to teleport.");
				}
				else {
					Gates.sendPlayerToGate((Player) sender, action);
				}
			}
		}
		else if (label.equalsIgnoreCase("wp")) {
			if (args.length < 1) return false;
			action = args[0];

			if (action.equalsIgnoreCase("track") && args.length < 2) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.GRAY + "[wp] "
									+ ChatColor.RED + "ERROR: Must be a logged in player to track points.");
				}
				else {
					Tracker.trackLocation((Player) sender, args[1]);
				}
				return true;
			}
			else if (action.equalsIgnoreCase("marker") && args.length < 2) {
				String markAction = args[1];

				if (markAction.equalsIgnoreCase("list")) {
					String playerName = null;
					if (!(sender instanceof Player) && !sender.isOp()) {
						sender.sendMessage(ChatColor.DARK_GRAY + "[wp] "
										+ ChatColor.RED + "ERROR: This command is only available to players and admins");
						return true;
					}

					if (sender.isOp() && 3 <= args.length) {
						playerName = args[2];
					}
					else if (sender.isOp() && !(sender instanceof Player)) {
						playerName = null;
					}
					else {
						playerName = ((Player) sender).getName();
					}
					Markers.list(sender, playerName);
					return true;
				}
				else if (markAction.equalsIgnoreCase("set")) {
					if (!(sender instanceof Player)) {
						sender.sendMessage(ChatColor.GRAY + "[wp] "
										+ ChatColor.RED + "ERROR: This command is available only to players.");
					}
					else if (3 > args.length) {
						sender.sendMessage(ChatColor.DARK_GRAY + "[wp] "
										+ ChatColor.RED + "ERROR: You must specify a marker name.");
					}
					else {
						Markers.setMark((Player) sender, args[2]);
					}
					return true;
				}
				else if (markAction.equalsIgnoreCase("remove")) {
					if (3 > args.length) {
						sender.sendMessage(ChatColor.DARK_GRAY + "[wp] "
										+ ChatColor.RED + "ERROR: You must specify a marker name.");
					}
					else {
						Markers.unsetMark(sender, args[2]);
					}
					return true;
				}
			}
			return false;
		}
		
		return true;
	}
}