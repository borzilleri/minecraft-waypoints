import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * @author jonathan
 */
public class GateList extends FileLoader {
	protected LinkedHashMap<String,Location> gates;

	protected int[] gatePointStart = new int[] {0,0,3};
	protected BlockType[][][] gatePointPattern = new BlockType[][][] {
		{
			{BlockType.BEDROCK, BlockType.BEDROCK, BlockType.IRON_BLOCK, BlockType.GOLD_BLOCK, BlockType.IRON_BLOCK, BlockType.BEDROCK, BlockType.BEDROCK}
		},
		{
			{BlockType.BEDROCK, BlockType.IRON_BLOCK, BlockType.GLASS, BlockType.AIR, BlockType.GLASS, BlockType.IRON_BLOCK, BlockType.BEDROCK}
		},
		{
			{BlockType.IRON_BLOCK, BlockType.GLASS, BlockType.GLASS, BlockType.AIR, BlockType.GLASS, BlockType.GLASS, BlockType.IRON_BLOCK}
		},
		{
			{BlockType.IRON_BLOCK, BlockType.GLASS, BlockType.GLASS, BlockType.GLASS, BlockType.GLASS, BlockType.GLASS, BlockType.IRON_BLOCK}
		},
		{
			{BlockType.BEDROCK, BlockType.IRON_BLOCK, BlockType.GLASS, BlockType.GLASS, BlockType.GLASS, BlockType.IRON_BLOCK, BlockType.BEDROCK}
		},
		{
			{BlockType.BEDROCK, BlockType.BEDROCK, BlockType.IRON_BLOCK, BlockType.DIAMOND_BLOCK, BlockType.IRON_BLOCK, BlockType.BEDROCK, BlockType.BEDROCK}
		}
	};
	
	public GateList() {
		filename = "gates.txt";
		gates = new LinkedHashMap<String,Location>();
	}

	public void listGates(Player player) {
		String gateList = Color.LightGray.getFormat() + "Gates: ";
		
		for(Map.Entry<String,Location> gate : gates.entrySet() ) {
			gateList += Color.Gold.getFormat() + gate.getKey() +
							Color.LightGray.getFormat() + ", ";			
		}
		player.sendChat(gateList);
	}

	public void sendPlayerToGate(Player player, String gateName) {
		if( !gates.containsKey(gateName) ) {
			player.sendChat("ERROR: Unknown gate", Color.Red);
			return;
		}

		Location gateLocation = gates.get(gateName);

		Structure.Validator validator = new Structure.Validator();
		Structure.parse(gatePointPattern, gatePointStart, gateLocation, validator);

		if( 0 >= validator.invalidBlockCount ) {
			player.setLocation(gateLocation);
		}
		else {
			player.sendChat("ERROR: Gate '"+gateName+"' is invalid.", Color.Red);
			return;
		}		
	}
	
	protected void loadLocationChunk(Location loc) {
		if( !World.isChunkLoaded(loc) ) {
			World.loadChunk(loc);
		}
	}

	public void setGate(String gateName, Player player) {
		if( gates.containsKey(gateName) ) {
			player.sendChat("ERROR: Gate '"+gateName+"' already active.", Color.Red);
			return;
		}

		Structure.Validator validator = new Structure.Validator();
		Structure.parse(gatePointPattern, gatePointStart, player.getLocation(), validator);

		if( 0 >= validator.invalidBlockCount ) {
			addGate(gateName,
				player.getLocation().getX(),
				player.getLocation().getY(),
				player.getLocation().getZ()
			);
			save();
			player.sendChat("*** Activating Gate ***", Color.Gold);
		}
		else {
			player.sendChat("ERROR: Gate is invalid.", Color.Red);
		}

	}	
	
	protected void addGate(String name, double xLoc, double yLoc, double zLoc) {
		gates.put(name, new Location(xLoc, yLoc, zLoc));
	}

	@Override
	public void beforeLoad() {
		gates.clear();
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

		addGate(tokens[0], Double.parseDouble(locArray[0]),
			Double.parseDouble(locArray[1]),
			Double.parseDouble(locArray[2])
		);
	}

	@Override
	public String saveString() {
		String line = "";
		Location thisLoc;
		
		for( Map.Entry<String,Location> homePoint: gates.entrySet() ) {
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
