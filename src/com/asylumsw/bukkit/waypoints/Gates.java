package com.asylumsw.bukkit.waypoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author jonathan
 */
public class Gates {
	public final static int WARP_DELAY = 5;
	private final static int GATE_DISTANCE_MINIMUM = 1000;

	public static boolean debug = false;
	public static boolean override = false;

	private static HashMap<String, Warp> gateList;
	private static HashMap<String, HashSet<String>> gateAccess;

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
		gateList = GateData.getGates();
		gateAccess = GateData.getGateActivations();
	}

	public static void listPlayerGates(CommandSender sender) {
		String playerName = "";
		if( sender instanceof Player ) {
			playerName = ((Player)sender).getName();
		}
		
		ArrayList<String> gates = new ArrayList<String>();
		for( Map.Entry<String,Warp> gate : gateList.entrySet() ) {
			if( gateAccess.containsKey(playerName) && gateAccess.get(playerName).contains(gate.getKey()) ) {
				gates.add(gate.getKey());
			}
			else if( sender.isOp() ) {
				gates.add(gate.getKey()+"*");
			}
		}
		
		if( 0 >= gates.size() ) {
			sender.sendMessage(ChatColor.DARK_GRAY+"[wp] "+
							ChatColor.RED+"You cannot access any gates.");
			return;
		}

		Collections.sort(gates);
		String msg = ChatColor.GRAY+"Available Gates: ";
		for( String gate : gates ) {
			if( msg.length() >= 40 ) {
				sender.sendMessage(ChatColor.DARK_GRAY+"[wp] "+msg);
				msg = "";
			}
			msg += ChatColor.AQUA + gate + ChatColor.GRAY + ", ";
		}
		sender.sendMessage(ChatColor.DARK_GRAY+"[wp] "+msg.substring(0, msg.length()-2));
	}

	public static boolean playerHasGateAccess(Player player, String gateName) {
		if( !gateList.containsKey(gateName) ) return false;
		if( gateList.get(gateName).getOwnerName().equalsIgnoreCase(player.getName()) ) return true;
		if( !gateAccess.containsKey(player.getName()) ) return false;
		return gateAccess.get(player.getName()).contains(gateName);
	}
	public static boolean trackGate(Player player, String gateName) {
		if( !playerHasGateAccess(player, gateName) ) return false;

		player.sendMessage(ChatColor.GOLD+"*** Setting compass to track Gate: "+gateName+" ***");
		player.setCompassTarget(gateList.get(gateName).getLocation());
		return true;
	}

	public static void sendPlayerToGate(Player player, String gateName) {
		// The gate name "Home" is reserved as an alias for /home
		if( gateName.equalsIgnoreCase("home") ) {
			Homes.sendPlayerHome(player, debug, override);
			return;
		}

		// The player must be standing on a valid gate to gate anywhere.
		if( !isValidGate(player.getLocation()) && !override) {
			player.sendMessage(ChatColor.RED+"ERROR: You must gate from an existing activated gate.");
			return;
		}
		
		if( !playerHasGateAccess(player, gateName) ) {
			player.sendMessage(ChatColor.RED+"ERROR: You may not gate there.");
			return;
		}
		
		Warp gate = gateList.get(gateName);

		if( !isValidGate(gate.getLocation()) && !override) {
			player.sendMessage(ChatColor.RED+"ERROR: '"+gateName+"' structure is invalid.");
			return;
		}
		
		player.sendMessage(ChatColor.GOLD + "*** Gating to "+gateName+" ***");
		if( !debug ) {
			gate.warp(player);
		}
	}

	public static boolean isValidGate(Location loc) {
		Structure.Validator validator = new Structure.Validator();
		Structure.parse(gatePointPattern, gatePointStart, loc, validator, debug);

		Structure.Validator validator2 = new Structure.Validator();
		Structure.parse(gatePointPattern2, gatePointStart2, loc, validator2, debug);

		return 0 >= validator.invalidBlockCount ||
						0 >= validator2.invalidBlockCount;
	}

	public static boolean addGateAccess(Player player, String gateName) {
		if (gateName.equalsIgnoreCase("home")) {
			player.sendMessage(ChatColor.RED + "Error: Gate name 'home' may not be activated.");
			return false;
		}
		if (!gateList.containsKey(gateName)) {
			player.sendMessage(ChatColor.RED + "Error: Gate '" + gateName + "' does not exist.");
			return false;
		}

		if (!gateAccess.containsKey(player.getName())) {
			gateAccess.put(player.getName(), new HashSet<String>());
		}

		if (GateData.addGateActivation(player, gateName)) {
			gateAccess.get(player.getName()).add(gateName);
			player.sendMessage(ChatColor.GOLD + "*** Activating Gate '" + gateName + "' ***");
			return true;
		}
		else {
			player.sendMessage(ChatColor.RED + "ERROR: Error occured saving gate activation.");
			return false;
		}
	}

	public static boolean activateGate(Player player, String gateName) {
		if (gateName.equalsIgnoreCase("home")) {
			player.sendMessage(ChatColor.RED + "Error: Gate name 'home' is reserved.");
			return false;
		}
		if (gateName.equalsIgnoreCase("reset")) {
			player.sendMessage(ChatColor.RED + "Error: Gate name 'reset' is reserved.");
			return false;
		}

		Location loc;
		Warp gate = gateList.containsKey(gateName) ? gateList.get(gateName) : null;
		HashSet<String> access = gateAccess.containsKey(player.getName()) ?
			gateAccess.get(player.getName()) :  null;

		// The gate has already been activated by SOMEONE.
		if (null != gate) {
			// The gate was activated by this player, so alert them.
			if (gate.getOwnerName().equalsIgnoreCase(player.getName())
							|| (null != access && access.contains(gateName))) {
				player.sendMessage(ChatColor.RED + "Error: You have already activated this gate.");
				return false;
			}
		}
		// The gate exists, but has not been activated by this player.

		loc = (null == gate) ? player.getLocation() : gate.getLocation();
		if (!isValidGate(loc)) {
			player.sendMessage(ChatColor.RED + "ERROR: Gate structure is invalid.");
			return false;
		}
		if (null != gate && 1 < gate.distanceFromLocation(player.getLocation())) {
			player.sendMessage(ChatColor.RED + "Error: You must be standing at the gate.");
			return false;
		}

		if (null != gate) {
			// The gate exists, so just add this player's access to it.
			return addGateAccess(player, gateName);
		}

		gate = new Warp(gateName, player, Waypoint.GATE);
		gate.setOwner(player);

		for( Map.Entry<String,Warp> gatePoint : gateList.entrySet() ) {
			if( GATE_DISTANCE_MINIMUM > gate.distanceFromLocation(gatePoint.getValue().getLocation()) ) {
				player.sendMessage(ChatColor.RED+"ERROR: Gates must be "+GATE_DISTANCE_MINIMUM
								+" blocks from any other gate.");
				return false;
			}
		}

		if (GateData.addGate(gate)) {
			gateList.put(gateName, gate);
			player.sendMessage(ChatColor.GOLD + "*** Activating Gate '" + gateName + "' ***");
			return true;
		}
		else {
			player.sendMessage(ChatColor.RED + "ERROR: Error occured saving gate.");
			return false;
		}
	}

	public static boolean deactivateGate(Player player, String gateName) {
		if (!gateList.containsKey(player.getName())) {
			// Player has no active home point.
			player.sendMessage(ChatColor.RED + "ERROR: No active home point.");
			return false;
		}

		Warp home = gateList.get(player.getName());

		Structure.Validator validator = new Structure.Validator();
		Structure.parse(gatePointPattern, gatePointStart, home.getLocation(), validator);

		if (0 < validator.invalidBlockCount) {
			// Player's home point is invalid
			player.sendMessage(ChatColor.RED + "ERROR: Home point is invalid.");
			return false;
		}
		if (1 <= Math.floor(player.getLocation().toVector().distance(home.getLocation().toVector()))) {
			// Player is not located at his home point.
			player.sendMessage(ChatColor.RED + "ERROR: Must be standing at your home point.");
			return false;
		}
		if (!HomeData.deleteHome(home.getName())) {
			player.sendMessage(ChatColor.RED + "ERROR: An error occured removing home point.");
			return false;
		}

		// Remove the blocks from the world.
		Structure.Actor remover = new Structure.Actor() {

			public boolean doBlockAction(Material structureMaterial,
																	 Block worldBlock) {
				worldBlock.setType(Material.AIR);
				return true;
			}

			public boolean doBlockAction(Material structureMaterial,
																	 Block worldBlock, boolean debug) {
				return doBlockAction(structureMaterial, worldBlock);
			}
		};
		Structure.parse(gatePointPattern, gatePointStart, gateList.get(player.getName()).getLocation(), remover);

		// Remove home point from the list, and save the list.
		gateList.remove(player.getName());

		player.sendMessage(ChatColor.GOLD + "*** Deactivating Home Point ***");
		return true;
	}

	public static boolean gateExists(String name) {
		return gateList.containsKey(name);
	}
}
