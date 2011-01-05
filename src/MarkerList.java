
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author jonathan
 */
public class MarkerList extends FileLoader {
	protected static LinkedHashMap<String,Location> markers;

	protected final static int[] markerPointStart = new int[] {1,0,0};
	protected final static BlockType[][][] markerPattern = new BlockType[][][] {
		{
			{BlockType.COBBLESTONE}
		},
		{
			{BlockType.COBBLESTONE}
		},
		{
			{BlockType.REDSTONE_TORCH_ON}
		}
	};

	public MarkerList() {
		filename = "markers.txt";
		markers = new LinkedHashMap<String, Location>();
	}

	protected void addMarker(String name, double xLoc, double yLoc, double zLoc) {
		markers.put(name, new Location(xLoc, yLoc, zLoc));
	}

	public void listMarkers(Player player) {
		String markerList = Color.LightGray.getFormat() + "Markers: "
						+ Color.Rose.getFormat() + "reset" + Color.LightGray.getFormat() + ", "
						+ Color.Rose.getFormat() + "home" + Color.LightGray.getFormat() + ", ";

		for(Map.Entry<String,Location> mark : markers.entrySet() ) {
			markerList += Color.Rose.getFormat() + mark.getKey() +
							Color.LightGray.getFormat() + ", ";
		}
		player.sendChat(markerList);
	}

	public void setTracking(String markerName, Player player) {
		if( markerName.equalsIgnoreCase("reset") ) {
			player.setCompassLocation(World.getSpawnLocation());
			player.sendChat("*** Tracking reset to Spawn Point ***", Color.Gold);
			return;
		}

		if( markerName.equalsIgnoreCase("home") ) {
			if( HomeList.hasHomePoint(player) ) {
				player.setCompassLocation(HomeList.getHomeLocation(player));
				player.sendChat("*** Tracking Home Point ***", Color.Gold);
			}
			else {
				player.sendChat("ERROR: You have no active Home Point.", Color.Red);
			}
			return;
		}

		if( GateList.gateExists(markerName) ) {
			player.setCompassLocation(GateList.gateLocation(markerName));
			player.sendChat("*** Tracking Gate: "+markerName+" ***", Color.Gold);
			return;
		}

		if( markers.containsKey(markerName) ) {
			player.setCompassLocation(markers.get(markerName));
			player.sendChat("*** Tracking Marker: "+markerName+" ***", Color.Gold);
			return;
		}

		player.sendChat("ERROR: Unknown gate or marker '"+markerName+"'.", Color.Red);
	}

	public void setMarker(String markName, Player player) {		
		if( GateList.gateExists(markName) ) {
			player.sendChat("ERROR: Gate '"+markName+"' already active.", Color.Red);
			return;
		}
		if( markers.containsKey(markName) ) {
			player.sendChat("ERROR: Marker "+markName+" already active.", Color.Red);
			return;
		}

		Structure.Validator validator = new Structure.Validator();
		Structure.parse(markerPattern, markerPointStart, player.getLocation(), validator);

		if( 0 >= validator.invalidBlockCount ) {
			addMarker(markName,
				player.getLocation().getX(),
				player.getLocation().getY(),
				player.getLocation().getZ()
			);
			save();
			player.sendChat("*** Activating Marker "+markName+" ***", Color.Gold);
		}
		else {
			player.sendChat("ERROR: Marker is invalid.", Color.Red);
		}
	}


	@Override
	public String saveString() {
		String line = "";
		Location thisLoc;
		for( Map.Entry<String,Location> mark: markers.entrySet() ) {
			thisLoc = mark.getValue();
			line += String.format("%s:%f;%f;%f;\r\n", mark.getKey(),
				thisLoc.getX(),
				thisLoc.getY(),
				thisLoc.getZ()
			);
		}
		return line;
	}
	
	@Override
	public void beforeLoad() {
		markers.clear();
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

		addMarker(tokens[0], Double.parseDouble(locArray[0]),
			Double.parseDouble(locArray[1]),
			Double.parseDouble(locArray[2])
		);
	}
}
