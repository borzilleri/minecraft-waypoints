package com.asylumsw.bukkit.waypoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author jonathan
 */
public class Gates {

	public final static int WARP_DELAY = 5;
	public final static int GATE_DISTANCE_MINIMUM = 1000;
	public static boolean debug = false;
	public static boolean override = false;
	private static HashMap<String, Warp> gateList;
	private static HashMap<String, HashSet<String>> gateAccess;
	private static HashSet<String> reservedGateNames = new HashSet<String>();
	private final static int[] gatePointStart = new int[]{0, 0, 3};
	private final static Material[][][] gatePointPattern = new Material[][][]{
		{
			{Material.BEDROCK, Material.BEDROCK, Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.IRON_BLOCK, Material.BEDROCK, Material.BEDROCK}
		},
		{
			{Material.BEDROCK, Material.IRON_BLOCK, Material.AIR, Material.AIR, Material.AIR, Material.IRON_BLOCK, Material.BEDROCK}
		},
		{
			{Material.IRON_BLOCK, Material.GLASS, Material.AIR, Material.AIR, Material.AIR, Material.GLASS, Material.IRON_BLOCK}
		},
		{
			{Material.IRON_BLOCK, Material.GLASS, Material.GLASS, Material.GLASS, Material.GLASS, Material.GLASS, Material.IRON_BLOCK}
		},
		{
			{Material.BEDROCK, Material.IRON_BLOCK, Material.GLASS, Material.GLASS, Material.GLASS, Material.IRON_BLOCK, Material.BEDROCK}
		},
		{
			{Material.BEDROCK, Material.BEDROCK, Material.IRON_BLOCK, Material.DIAMOND_BLOCK, Material.IRON_BLOCK, Material.BEDROCK, Material.BEDROCK}
		}
	};
	private final static int[] gatePointStart2 = new int[]{0, 3, 0};
	private final static Material[][][] gatePointPattern2 = new Material[][][]{
		{
			{Material.BEDROCK}, {Material.BEDROCK}, {Material.IRON_BLOCK}, {Material.GOLD_BLOCK}, {Material.IRON_BLOCK}, {Material.BEDROCK}, {Material.BEDROCK}
		},
		{
			{Material.BEDROCK}, {Material.IRON_BLOCK}, {Material.AIR}, {Material.AIR}, {Material.AIR}, {Material.IRON_BLOCK}, {Material.BEDROCK}
		},
		{
			{Material.IRON_BLOCK}, {Material.GLASS}, {Material.AIR}, {Material.AIR}, {Material.AIR}, {Material.GLASS}, {Material.IRON_BLOCK}
		},
		{
			{Material.IRON_BLOCK}, {Material.GLASS}, {Material.GLASS}, {Material.GLASS}, {Material.GLASS}, {Material.GLASS}, {Material.IRON_BLOCK}
		},
		{
			{Material.BEDROCK}, {Material.IRON_BLOCK}, {Material.GLASS}, {Material.GLASS}, {Material.GLASS}, {Material.IRON_BLOCK}, {Material.BEDROCK}
		},
		{
			{Material.BEDROCK}, {Material.BEDROCK}, {Material.IRON_BLOCK}, {Material.DIAMOND_BLOCK}, {Material.IRON_BLOCK}, {Material.BEDROCK}, {Material.BEDROCK}
		}
	};

	public static void load() {
		GateData.initTable();
		reload();

		reservedGateNames.add("home");
		reservedGateNames.add("reset");
		reservedGateNames.add("activate");
		reservedGateNames.add("remove");
		reservedGateNames.add("rename");
		reservedGateNames.add("reload");
	}

	public static void reload() {
		gateList = GateData.getGates();
		gateAccess = GateData.getGateActivations();
	}

	public static void listPlayerGates(CommandSender sender) {
		String playerName = "";
		if (sender instanceof Player) {
			playerName = ((Player) sender).getName();
		}

		ArrayList<String> gates = new ArrayList<String>();
		for (Map.Entry<String, Warp> gate : gateList.entrySet()) {
			if (playerHasGateAccess(playerName, gate.getKey())) {
				gates.add(gate.getKey());
			}
			else if (sender.isOp()) {
				gates.add(gate.getKey() + "*");
			}
		}

		if (0 >= gates.size()) {
			sender.sendMessage(ChatColor.DARK_GRAY + "[wp] "
							+ ChatColor.RED + "You cannot access any gates.");
			return;
		}

		Collections.sort(gates);
		String msg = ChatColor.GRAY + "Available Gates: ";
		for (String gate : gates) {
			if (msg.length() >= 40) {
				sender.sendMessage(ChatColor.DARK_GRAY + "[wp] " + msg);
				msg = "";
			}
			msg += ChatColor.AQUA + gate + ChatColor.GRAY + ", ";
		}
		sender.sendMessage(ChatColor.DARK_GRAY + "[wp] " + msg.substring(0, msg.length() - 2));
	}

	public static boolean playerHasGateAccess(String player, String gateName) {
		if (!gateList.containsKey(gateName)) return false;
		if (gateList.get(gateName).getOwnerName().equalsIgnoreCase(player))
			return true;
		if (!gateAccess.containsKey(player)) return false;
		return gateAccess.get(player).contains(gateName);
	}

	public static boolean trackGate(Player player, String gateName) {
		if (!playerHasGateAccess(player.getName(), gateName)) return false;

		player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
						+ ChatColor.GOLD + "*** Setting compass to track Gate: " + gateName + " ***");
		player.setCompassTarget(gateList.get(gateName).getLocation());
		return true;
	}

	public static void sendPlayerToGate(Player player, String gateName) {
		// The gate name "Home" is reserved as an alias for /home
		if (gateName.equalsIgnoreCase("home")) {
			Homes.sendPlayerHome(player, debug, override);
			return;
		}

		// The player must be standing on a valid gate to gate anywhere.
		if (!isValidGate(player.getLocation()) && !override) {
			player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
							+ ChatColor.RED + "ERROR: You must gate from an existing activated gate.");
			return;
		}

		if (!playerHasGateAccess(player.getName(), gateName)) {
			player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
							+ ChatColor.RED + "ERROR: You may not gate there.");
			return;
		}

		Warp gate = gateList.get(gateName);

		if (!isValidGate(gate.getLocation()) && !override) {
			player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
							+ ChatColor.RED + "ERROR: '" + gateName + "' structure is invalid.");
			return;
		}

		player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
						+ ChatColor.GOLD + "*** Gating to " + gateName + " ***");
		if (!debug) {
			gate.warp(player);
		}
	}

	public static boolean isValidGate(Location loc) {
		Structure.Validator validator = new Structure.Validator();
		Structure.parse(gatePointPattern, gatePointStart, loc, validator, debug);

		Structure.Validator validator2 = new Structure.Validator();
		Structure.parse(gatePointPattern2, gatePointStart2, loc, validator2, debug);

		return 0 >= validator.invalidBlockCount
						|| 0 >= validator2.invalidBlockCount;
	}

	public static boolean isValidGateName(String name) {
		if (reservedGateNames.contains(name)) {
			return false;
		}
		return true;
	}

	public static boolean addGateAccess(Player player, String gateName) {
		if (!gateList.containsKey(gateName)) {
			player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
							+ ChatColor.RED + "Error: Gate '" + gateName + "' does not exist.");
			return false;
		}

		if (!gateAccess.containsKey(player.getName())) {
			gateAccess.put(player.getName(), new HashSet<String>());
		}

		if (GateData.addGateActivation(player, gateName)) {
			gateAccess.get(player.getName()).add(gateName);
			player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
							+ ChatColor.GOLD + "*** Activating Gate '" + gateName + "' ***");
			return true;
		}
		else {
			player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
							+ ChatColor.RED + "ERROR: Error occured saving gate activation.");
			return false;
		}
	}

	public static boolean isValidGateLocation(Location loc) {
		for (Map.Entry<String, Warp> gatePoint : gateList.entrySet()) {
			int distanceFromLoc = (int) Math.floor(loc.toVector().distance(gatePoint.getValue().getLocation().toVector()));
			if (GATE_DISTANCE_MINIMUM > distanceFromLoc) {
				return false;
			}
		}
		return true;
	}

	public static boolean activateGate(Player player, String gateName) {
		if (!isValidGateName(gateName)) {
			player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
							+ ChatColor.RED + "Error: Gate name '" + gateName + "' is reserved.");
			return false;
		}

		Location loc;
		Warp gate = gateList.containsKey(gateName) ? gateList.get(gateName) : null;
		HashSet<String> access = gateAccess.containsKey(player.getName())
						? gateAccess.get(player.getName()) : null;

		// The gate has already been activated by SOMEONE.
		if (null != gate) {
			// The gate was activated by this player, so alert them.
			if (gate.getOwnerName().equalsIgnoreCase(player.getName())
							|| (null != access && access.contains(gateName))) {
				player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
								+ ChatColor.RED + "Error: You have already activated this gate.");
				return false;
			}
		}

		loc = (null == gate) ? player.getLocation() : gate.getLocation();
		if (!isValidGate(loc)) {
			// The player's location is not a valid gate location.
			player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
							+ ChatColor.RED + "ERROR: Gate structure is invalid.");
			return false;
		}
		if (null != gate && 1 < gate.distanceFromLocation(player.getLocation())) {
			/**
			 * TODO: This is potential information leakage.
			 */
			player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
							+ ChatColor.RED + "Error: You must be standing at the gate.");
			return false;
		}

		if (null != gate) {
			// The gate exists, so just add this player's access to it.
			return addGateAccess(player, gateName);
		}

		gate = new Warp(gateName, player, Waypoint.GATE);
		gate.setOwner(player);

		if (!isValidGateLocation(gate.getLocation())) {
			return false;
		}

		if (GateData.addGate(gate)) {
			gateList.put(gateName, gate);
			player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
							+ ChatColor.GOLD + "*** Activating Gate '" + gateName + "' ***");
			return true;
		}
		else {
			player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
							+ ChatColor.RED + "ERROR: Error occured saving gate.");
			return false;
		}
	}

	public static boolean renameGate(CommandSender player, String oldName, String newName) {
		if (!gateList.containsKey(oldName) || (!player.isOp() && !gateList.get(oldName).getOwnerName().equalsIgnoreCase(((Player) player).getName()))) {
			player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
							+ ChatColor.RED + "ERROR: You may only rename gates you own.");
			return false;
		}

		if (!GateData.renameGate(oldName, newName)) {
			player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
							+ ChatColor.RED + "ERROR: An error occured renaming the gate.");
			return false;
		}

		for (Map.Entry<String, HashSet<String>> access : gateAccess.entrySet()) {
			if (access.getValue().contains(oldName)) {
				access.getValue().add(newName);
				access.getValue().remove(oldName);
			}
		}
		gateList.put(newName, gateList.get(oldName));
		gateList.remove(oldName);

		Waypoints.serverInstance.broadcastMessage(ChatColor.DARK_GRAY + "[wp] "
						+ ChatColor.GOLD + "*** Gate '" + oldName + "' renamed to '" + newName + "' ***");
		return true;
	}

	public static boolean deactivateGate(CommandSender player, String gateName) {
		if (null == gateName) {
			// No gate name supplied so we must discern the gate from the player's location.
			// TODO: actually implement that.
			//if( !(player instanceof Player) ) {
			player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
							+ ChatColor.RED + "ERROR: No gate name to deactivate supplied.");
			return false;
			//}
		}

		/**
		 * Check to make sure that:
		 * - the gate exists
		 * - the player is either an OP or Owns the gate.
		 */
		if (!gateList.containsKey(gateName) || (!player.isOp() && !gateList.get(gateName).getOwnerName().equalsIgnoreCase(((Player) player).getName()))) {
			player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
							+ ChatColor.RED + "ERROR: You may only deactivate gates you own.");
			return false;
		}


		if (!GateData.deleteGate(gateName)) {
			player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
							+ ChatColor.RED + "ERROR: An error occured removing the gate.");
			return false;
		}

		for (Map.Entry<String, HashSet<String>> access : gateAccess.entrySet()) {
			if (access.getValue().contains(gateName)) {
				access.getValue().remove(gateName);
			}
		}
		gateList.remove(gateName);
		player.sendMessage(ChatColor.DARK_GRAY + "[wp] "
						+ ChatColor.GOLD + "*** Deactivating Gate: " + gateName + " ***");
		return true;
	}

	public static boolean gateExists(String name) {
		return gateList.containsKey(name);
	}
}
