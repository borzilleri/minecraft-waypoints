/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jonathan
 */
public class Structure {
	private static int getX(Location loc) {
		return (int)Math.floor(loc.getZ());
	}
	private static int getY(Location loc) {
		return (int)Math.floor(loc.getX());
	}
	private static int getZ(Location loc) {
		// Subtract one, as we always use the block beneath the player's feet.
		return (int)Math.floor(loc.getY())-1;
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
	public static boolean validate(BlockTypeEnum[][][] pattern, 
					int[] startPosition,
					Location playerLocation) {
		int thisZ, thisX, thisY;
		Location thisLocation = new Location(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());

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
					thisZ = Structure.getZ(playerLocation)+(z-startPosition[0]);
					thisY = Structure.getY(playerLocation)+(y-startPosition[1]);
					thisX = Structure.getX(playerLocation)+(x-startPosition[2]);
					thisLocation.setX(thisY);
					thisLocation.setY(thisZ);
					thisLocation.setZ(thisX);

					if( World.getBlock(thisLocation).getEnum() != pattern[z][y][x] &&
									pattern[z][y][x] != BlockTypeEnum.BEDROCK ) {
						
						//Waypoints.player.sendChat(String.format("Expected %s at %d,%d,%d, found %s",
						//	pattern[z][y][x].toString(), z, y, x,
						//	World.getBlock(thisLocation).getEnum().toString()));
						return false;
					}
				}
			}
		}
		return true;		
	}

}
