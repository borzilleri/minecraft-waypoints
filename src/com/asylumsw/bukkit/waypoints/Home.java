package com.asylumsw.bukkit.waypoints;

import org.bukkit.Location;
import org.bukkit.Player;
import org.bukkit.World;

/**
 *
 * @author jonathan
 */
public class Home {
	private String name;
	private int x;
	private int y;
	private int z;
	private int pitch;
	private int yaw;

	public Home(String player, int x, int y, int z, int pitch, int yaw) {
		this.name = player;
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = pitch;
		this.yaw = yaw;
	}

	public Home(Player player) {
		this.name = player.getName();
		this.x = player.getLocation().getBlockX();
		this.y = player.getLocation().getBlockY();
		this.z = player.getLocation().getBlockZ();
		this.pitch = (int)player.getLocation().getPitch();
		this.yaw = (int)player.getLocation().getYaw();		
	}

	public Home(String player, Location loc) {
		this.name = player;
		this.x = loc.getBlockX();
		this.y = loc.getBlockY();
		this.z = loc.getBlockZ();
		this.pitch = (int)loc.getPitch();
		this.yaw = (int)loc.getYaw();
	}

	public void warp(Player player) {
		World world = player.getWorld();
		Location loc = new Location(world, x, y, z, yaw, pitch);
		player.teleportTo(loc);
	}

}
