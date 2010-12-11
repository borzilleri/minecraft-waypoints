import java.util.LinkedHashMap;
import java.util.Map;

/**
 * duaiwe:xx;yy;zz;
 *
 *
 * @author jonathan
 */
public class HomeList extends FileLoader {
	protected LinkedHashMap<String,Location> homes;
	protected int[] homePointStart = new int[] {0,1,1};
	protected BlockTypeEnum[][][] homePointPattern = new BlockTypeEnum[][][] {
		{
			{BlockTypeEnum.GLASS, BlockTypeEnum.GLASS, BlockTypeEnum.GLASS},
			{BlockTypeEnum.GLASS, BlockTypeEnum.IRON_BLOCK, BlockTypeEnum.GLASS},
			{BlockTypeEnum.GLASS, BlockTypeEnum.GLASS, BlockTypeEnum.GLASS},
		}
	};
	
	public HomeList() {
		filename = "homes.txt";
		homes = new LinkedHashMap<String,Location>();		
	}

	public void sendPlayerHome(Player player) {
		if( !hasHomePoint(player) ) {
			player.sendChat("*** Returning to Spawn Point ***", ColorEnum.Gold);
			loadLocationChunk(World.getSpawnLocation());
			player.setLocation(World.getSpawnLocation());
			return;
		}

		Structure.Validator validator = new Structure.Validator();
		Structure.parse(homePointPattern, homePointStart, homes.get(player.getName()), validator);

		if( 0 >= validator.invalidBlockCount ) {
			player.sendChat("*** Returning Home ***", ColorEnum.Gold);
			loadLocationChunk(homes.get(player.getName()));
			player.setLocation(new Location(homes.get(player.getName())));
			return;
		}

		player.sendChat("ERROR: Home point is invalid.", ColorEnum.Red);
	}
	protected void loadLocationChunk(Location loc) {
		if( !World.isChunkLoaded(loc) ) {
			World.loadChunk(loc);
		}
	}

	public void unsetUserHomePoint(Player player) {
		if( !hasHomePoint(player) ) {
			// Player has no active home point.
			player.sendChat("ERROR: No active home point.", ColorEnum.Red);
			return;
		}

		Structure.Validator validator = new Structure.Validator();
		Structure.parse(homePointPattern, homePointStart, homes.get(player.getName()), validator);
		
		if( 0 < validator.invalidBlockCount ) {
			// Player's home point is invalid
			player.sendChat("ERROR: Home point is invalid.", ColorEnum.Red);
			return;
		}
		if( !playerAtHomePoint(player) ) {
			// Player is not located at his home point.
			player.sendChat("ERROR: Must be standing at your home point.", ColorEnum.Red);
			return;
		}
		
		// Remove the blocks from the world.
		Structure.Actor remover = new Structure.Actor() {
			public boolean doBlockAction(BlockTypeEnum structureBlockType,
							Block worldBlock) {
				World.setBlock(worldBlock.getLocation(), BlockTypeEnum.AIR);
				return true;
			}
		};
		Structure.parse(homePointPattern, homePointStart, homes.get(player.getName()), remover);

		// Remove home point from the list, and save the list.
		homes.remove(player.getName());
		save();

		player.sendChat("*** Deactivating Home Point ***", ColorEnum.Gold);
	}
	
	public void setUserHomePoint(Player player) {
		if( homes.containsKey(player.getName()) ) {
			player.sendChat("Error: Home Point already active.", ColorEnum.Red);
			return;
		}
		
		Structure.Validator validator = new Structure.Validator();
		Structure.parse(homePointPattern, homePointStart, player.getLocation(), validator);

		if( 0 >= validator.invalidBlockCount ) {
			addHomePoint(player.getName(),
				player.getLocation().getX(),
				player.getLocation().getY(),
				player.getLocation().getZ()
			);
			save();
			player.sendChat("*** Activating Home Point ***", ColorEnum.Gold);
		}
		else {
			player.sendChat("ERROR: Home point is invalid.", ColorEnum.Red);
		}		
	}

	protected boolean playerAtHomePoint(Player player) {
		if( !hasHomePoint(player) ) return false;

		Location homeLoc = homes.get(player.getName());
		Location pLoc = player.getLocation();

		return Math.abs(pLoc.getX()-homeLoc.getX()) < 1 &&
					 Math.abs(pLoc.getY()-homeLoc.getY()) < 1 &&
					 Math.abs(pLoc.getZ()-homeLoc.getZ()) < 1;
	}
	
	protected boolean hasHomePoint(Player player) {
		return homes.containsKey(player.getName());
	}
	protected void addHomePoint(String name, double xLoc, double yLoc, double zLoc) {
		homes.put(name, new Location(xLoc, yLoc, zLoc));
	}

	@Override
	public void beforeLoad() {
		homes.clear();
	}

	@Override
	public void loadLine(String line) {
		// Split our line into the name & location.
		String[] tokens = line.split(":");

		// If we dont have a name AND a location, this is an invalid line.
		if( tokens.length < 2 ) return;

		// Split the location into it's coordinate points
		String[] locArray = tokens[1].split(";");

		// We don't have at least three locatio points, so also invalid.
		if( 3 > locArray.length ) return;

		addHomePoint(tokens[0], Double.parseDouble(locArray[0]),
			Double.parseDouble(locArray[1]),
			Double.parseDouble(locArray[2])
		);
	}

	@Override
	public String saveString() {
		String line = "";
		Location thisLoc;
		
		for( Map.Entry<String,Location> homePoint: homes.entrySet() ) {
			thisLoc = homePoint.getValue();
			line += String.format("%s:%f;%f;%f;\r\n", homePoint.getKey(),
				thisLoc.getX(),
				thisLoc.getY(),
				thisLoc.getZ()
			);
		}
		
		return line;
	}


	
}
