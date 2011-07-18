package com.asylumsw.bukkit.waypoints;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.World;

/**
 *
 * @author jonathan
 */
public class Warp {
	public static Waypoints plugin;
	public final static int DEFAULT_DELAY = 20;

	private Waypoint type;
	private String name;
	private String owner;
	private int x;
	private int y;
	private int z;
	private int pitch;
	private int yaw;
	private String world;

	public Warp(String name, int x, int y, int z, int pitch, int yaw, String world, Waypoint type) {
		if( null == Waypoints.serverInstance.getWorld(world) ) {
			throw new NullPointerException("World '"+world+"' does not exist.");
		}

		this.type = type;
		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = pitch;
		this.yaw = yaw;
		this.world = world;
	}

	public Warp(String name, Player player, Waypoint type) {
		this.type = type;
		this.name = name;
		this.x = player.getLocation().getBlockX();
		this.y = player.getLocation().getBlockY();
		this.z = player.getLocation().getBlockZ();
		this.pitch = (int)player.getLocation().getPitch();
		this.yaw = (int)player.getLocation().getYaw();
		this.world = player.getWorld().getName();
	}

	public Warp(String name, Location loc, Waypoint type) {
		this.type = type;
		this.name = name;
		this.x = loc.getBlockX();
		this.y = loc.getBlockY();
		this.z = loc.getBlockZ();
		this.pitch = (int)loc.getPitch();
		this.yaw = (int)loc.getYaw();
		this.world = loc.getWorld().getName();
	}

	public void setOwner(String owner) { this.owner = owner; }
	public void setOwner(Player owner) { this.owner = owner.getName(); }

	public String getName() { return name; }
	public Waypoint getType() { return type; }
	public int getX() { return x; }
	public int getY() { return y; }
	public int getZ() { return z; }
	public int getPitch() { return pitch; }
	public int getYaw() { return yaw; }
	public String getWorldName() { return world; }
	public World getWorld() { return Waypoints.serverInstance.getWorld(world); }
	public Player getOwner() { return Waypoints.serverInstance.getPlayer(owner); }
	public String getOwnerName() { return owner; }

	public void warp(Player player) {
		int delay;
		switch(type) {
			case HOME:
				delay = Homes.WARP_DELAY;
				break;
			case GATE:
				delay = Gates.WARP_DELAY;
				break;
			default:
				delay = DEFAULT_DELAY;
				break;
		}
		Location loc = new Location(Waypoints.serverInstance.getWorld(world), x, y, z, yaw, pitch);
		Warp.warpPlayerTo(player, loc, delay);
	}

	public static void warpPlayerTo(Player player, Location loc, int delay) {
		Teleporter.TeleportJob tpJob = new Teleporter.TeleportJob(player, loc, delay);
		Teleporter.scheduleTeleport(tpJob);
	}

	public Location getLocation() {
		return new Location(Waypoints.serverInstance.getWorld(world), x, y, z, yaw, pitch);
	}

	public int distanceFromLocation(Location loc) {
		return (int)Math.floor(loc.toVector().distance(getLocation().toVector()));
	}
	
}
