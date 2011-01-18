package com.asylumsw.bukkit.waypoints;

import org.bukkit.Block;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/**
 *
 * @author jonathan
 */
public class Structure {
	private static int getX(Location loc) {
		return loc.getBlockZ();
	}
	private static int getY(Location loc) {
		return loc.getBlockX();
	}
	private static int getZ(Location loc) {
		return loc.getBlockY()-1;
	}
	public static Material nonEvalBlock = Material.BEDROCK;

	public interface Actor {
		public boolean doBlockAction(Material structureBlockMaterial,
						Block worldBlock, boolean debug);
		public boolean doBlockAction(Material structureBlockMaterial,
						Block worldBlock);

	}

	public static class Validator implements Actor {
		public int invalidBlockCount;
		public Validator() {
			invalidBlockCount = 0;
		}
		public boolean doBlockAction(Material structureBlockType,
							Block worldBlock, boolean debug) {
			if( worldBlock.getType() != structureBlockType &&
					structureBlockType != Structure.nonEvalBlock ) {
				if( debug ) {
					Server.log(String.format("Invalid Home Block: Found %s, Expecting %s.",
									worldBlock.getBlockType().getName(), structureBlockType.getName())
									);
				}
				invalidBlockCount += 1;
				return false;
			}
			return true;
		}
		public boolean doBlockAction(Material structureBlockType, Block worldBlock) {
			return this.doBlockAction(structureBlockType, worldBlock, false);
		}
	}

	/**
	 * NOTE:
	 * Altitude -> Y
	 * Latitude -> X
	 * Longitude -> Z
	 * 
	 *
	 * @param pattern
	 * @param startPosition
	 * @param playerLocation
	 * @return
	 */
	public static void parse(Material[][][] pattern,
					int[] startPosition,
					Location loc, Actor callBack, boolean debug) {
		int thisZ, thisX, thisY;
		Location thisLocation = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

		Waypoints.loadChunkAtLocation(thisLocation);

		/**
		 * This ugly loop iterates over every value of the pattern.
		 * We'll use the startPosition to calculate it's relative location from the
		 * player's location. We grab the block at that location, and compare it to
		 * the block we're expecting in the pattern, if it's false, we return out
		 * early, otherwise we keep looping.
		 *
		 * If we make it through the loop, we've got a valid pattern.
		 *
		 * There probably is a WAY smarter way to do this, I'm just too dumb to
		 * figure it out.
		 */
		for( int z = 0; z < pattern.length; z++ ){
			for( int y = 0; y < pattern[z].length; y++ ) {
				for( int x = 0; x < pattern[z][y].length; x++ ) {
					thisZ = Structure.getZ(loc)+(z-startPosition[0]);
					thisY = Structure.getY(loc)+(y-startPosition[1]);
					thisX = Structure.getX(loc)+(x-startPosition[2]);
					thisLocation.setX(thisY);
					thisLocation.setY(thisZ);
					thisLocation.setZ(thisX);

					thisLocation.getWorld().getChunkAt(
									thisLocation.getWorld().getBlockAt(thisLocation.getBlockX(),
									thisLocation.getBlockY(), thisLocation.getBlockZ()));
					//World.loadChunk(thisLocation);
					boolean continueLoop = (null == callBack) ? true :
						callBack.doBlockAction(pattern[z][y][x], 
						thisLocation.getWorld().getBlockAt(thisLocation.getBlockX(), thisLocation.getBlockY(), thisLocation.getBlockZ()),
						debug);
					
					if( !continueLoop ) return;
				}
			}
		}
	}

	public static void parse(Material[][][] pattern,
					int[] startPosition,
					Location playerLocation, Actor callBack) {
		Structure.parse(pattern, startPosition, playerLocation, callBack, false);
	}


}
