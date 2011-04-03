package com.asylumsw.bukkit.waypoints;

import org.bukkit.Location;
import org.bukkit.Material;

/**
 *
 * @author jonathan
 */
public class Portals {
	public final static int WARP_DELAY = 0;

	protected static int[] portalStartPoint = new int[] {0,1,1};
	protected static Material[][][] portalPattern = new Material[][][] {
		{
			{Material.GLASS, Material.GLASS, Material.GLASS},
			{Material.GLASS, Material.STEP, Material.GLASS},
			{Material.GLASS, Material.GLASS, Material.GLASS},
		}
	};

	public static void load() {
	}


	public static boolean isValidPortal(Location loc) {
		return false;
	}


}
