package com.asylumsw.bukkit.waypoints;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author jonathan
 */
public class Homes {

	public final static int WARP_DELAY = 15;
	private static HashMap<String, Warp> homeList;
	protected static int[] homePointStart = new int[]{0, 1, 1};
	protected static Material[][][] homePointPattern = new Material[][][]{
		{
			{Material.GLASS, Material.GLASS, Material.GLASS},
			{Material.GLASS, Material.IRON_BLOCK, Material.GLASS},
			{Material.GLASS, Material.GLASS, Material.GLASS},}
	};

	public static void load() {
		HomeData.initTable();
		homeList = HomeData.getHomes();
	}

	public static boolean hasHomePoint(Player player) {
		return homeList.containsKey(player.getName());
	}

	public static boolean trackHomePoint(Player player) {
		if (!hasHomePoint(player)) {
			player.sendMessage(ChatColor.RED + "ERROR: You do not have an active home point.");
			return false;
		}

		player.sendMessage(ChatColor.GOLD + "*** Setting compass to Home Point");
		player.setCompassTarget(homeList.get(player.getName()).getLocation());
		return true;
	}

	public static void sendPlayerHome(Player player, boolean debug, boolean override) {
		// If the player does not have a home point, send them to the spawn.
		if (!homeList.containsKey(player.getName())) {
			player.sendMessage(ChatColor.GOLD + "*** Returning to Spawn Point ***");
			Warp.warpPlayerTo(player, player.getWorld().getSpawnLocation(), WARP_DELAY);
			return;
		}

		Warp home = homeList.get(player.getName());

		Structure.Validator validator = new Structure.Validator();
		Structure.parse(homePointPattern, homePointStart, home.getLocation(), validator, debug);

		if (0 < validator.invalidBlockCount) {
			player.sendMessage(ChatColor.RED + "ERROR: Home point is invalid.");
			if (debug) {
				player.sendMessage(ChatColor.GRAY + "Invalid Blocks: " + validator.invalidBlockCount);
			}
		}

		if (0 >= validator.invalidBlockCount || override) {
			player.sendMessage(ChatColor.GOLD + "*** Returning Home ***");
			if (!debug || override) {
				home.warp(player);
			}
		}
	}

	public static boolean setHomePoint(Player player) {
		if (homeList.containsKey(player.getName())) {
			player.sendMessage(ChatColor.RED + "Error: Home Point already active.");
			return false;
		}

		Structure.Validator validator = new Structure.Validator();
		Structure.parse(homePointPattern, homePointStart, player.getLocation(), validator, true);

		if (0 >= validator.invalidBlockCount) {
			Warp home = new Warp(player.getName(), player, Waypoint.HOME);
			if (HomeData.addHome(home)) {
				homeList.put(player.getName(), home);
				player.sendMessage(ChatColor.GOLD + "*** Activating Home Point ***");
				return true;
			}
			else {
				player.sendMessage(ChatColor.RED + "ERROR: Error occured saving home point.");
			}
		}
		else {
			player.sendMessage(ChatColor.RED + "ERROR: Home point is invalid.");
		}

		return false;
	}

	public static boolean unsetHomePoint(Player player) {
		if (!homeList.containsKey(player.getName())) {
			// Player has no active home point.
			player.sendMessage(ChatColor.RED + "ERROR: No active home point.");
			return false;
		}

		Warp home = homeList.get(player.getName());

		Structure.Validator validator = new Structure.Validator();
		Structure.parse(homePointPattern, homePointStart, home.getLocation(), validator);

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

			public boolean doBlockAction(Material structureBlockType,
																	 Block worldBlock) {
				worldBlock.setType(Material.AIR);
				return true;
			}

			public boolean doBlockAction(Material structureBlockType,
																	 Block worldBlock, boolean debug) {
				return doBlockAction(structureBlockType, worldBlock);
			}
		};
		Structure.parse(homePointPattern, homePointStart, homeList.get(player.getName()).getLocation(), remover);

		// Remove home point from the list, and save the list.
		homeList.remove(player.getName());

		player.sendMessage(ChatColor.GOLD + "*** Deactivating Home Point ***");
		return true;
	}

	public static class BlockFinder implements Structure.Actor {

		public Block block;
		public boolean blockFound = false;

		public BlockFinder(Block blockToFind) {
			block = blockToFind;
		}

		public boolean doBlockAction(Material structureBlockType,
																 Block worldBlock, boolean debug) {
			if (worldBlock.getX() == block.getX()
							&& worldBlock.getY() == block.getY()
							&& worldBlock.getZ() == block.getZ()) {
				blockFound = true;
			}
			return true;
		}

		public boolean doBlockAction(Material structureBlockType, Block worldBlock) {
			return this.doBlockAction(structureBlockType, worldBlock, false);
		}
	}

	public static boolean blockIsPartOfHomepoint(Block block, Player player) {
		for( Map.Entry<String, Warp> home : homeList.entrySet() ) {
			BlockFinder finder = new BlockFinder(block);
			Structure.parse(homePointPattern, homePointStart, home.getValue().getLocation(), finder);
			if( finder.blockFound ) return true;
			return finder.blockFound;
		}
		return false;
	}
}
