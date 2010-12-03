/**
 * NOTE:
 * Latitude: X
 * Longitude: Z
 * Altitude: Y
 */

/**
 *
 * @author jonathan
 */

public class Waypoints extends Mod {
	protected int[] homePointStart = new int[] {0,1,1};
	protected BlockTypeEnum[][][] homePointPattern = new BlockTypeEnum[][][] {
		{
			{BlockTypeEnum.OBSIDIAN, BlockTypeEnum.OBSIDIAN, BlockTypeEnum.OBSIDIAN},
			{BlockTypeEnum.OBSIDIAN, BlockTypeEnum.DIAMOND_BLOCK, BlockTypeEnum.OBSIDIAN},
			{BlockTypeEnum.OBSIDIAN, BlockTypeEnum.OBSIDIAN, BlockTypeEnum.OBSIDIAN},
		}
	};

	protected int[] wayPointStart = new int[] {0, 1, 1};
	protected BlockTypeEnum[][][] wayPointPattern = new BlockTypeEnum[][][] {
		{
			{BlockTypeEnum.OBSIDIAN, BlockTypeEnum.IRON_BLOCK, BlockTypeEnum.OBSIDIAN},
			{BlockTypeEnum.IRON_BLOCK, BlockTypeEnum.GOLD_BLOCK, BlockTypeEnum.IRON_BLOCK},
			{BlockTypeEnum.OBSIDIAN, BlockTypeEnum.IRON_BLOCK, BlockTypeEnum.OBSIDIAN}
		},
		{
			{BlockTypeEnum.AIR, BlockTypeEnum.AIR, BlockTypeEnum.AIR},
			{BlockTypeEnum.IRON_BLOCK, BlockTypeEnum.AIR, BlockTypeEnum.IRON_BLOCK},
			{BlockTypeEnum.AIR, BlockTypeEnum.AIR, BlockTypeEnum.AIR}
		},
		{
			{BlockTypeEnum.AIR, BlockTypeEnum.AIR, BlockTypeEnum.AIR},
			{BlockTypeEnum.IRON_BLOCK, BlockTypeEnum.AIR, BlockTypeEnum.IRON_BLOCK},
			{BlockTypeEnum.AIR, BlockTypeEnum.AIR, BlockTypeEnum.AIR}
		},
		{
			{BlockTypeEnum.AIR, BlockTypeEnum.AIR, BlockTypeEnum.AIR},
			{BlockTypeEnum.IRON_BLOCK, BlockTypeEnum.DIAMOND_BLOCK, BlockTypeEnum.IRON_BLOCK},
			{BlockTypeEnum.AIR, BlockTypeEnum.AIR, BlockTypeEnum.AIR}
		}
	};
	
	public static Player player;


	protected boolean parseCommand(Player player, String[] tokens) {
		Waypoints.player = player;
		String command = tokens[0].substring(1);

		if( command.equalsIgnoreCase("home") ) {
			player.sendChat("Returned Home");
			return true;
		} else if( command.equalsIgnoreCase("h") ) {
			if( StructureParser.validate(homePointPattern, homePointStart, player.getLocation()) ) {
				player.sendChat("VALID home point");
			} else {
				player.sendChat("INVALID home point");
			}
			return true;
		} else if( command.equalsIgnoreCase("a") ) {
			if( StructureParser.validate(wayPointPattern, wayPointStart, player.getLocation()) ) {
				player.sendChat("VALID waypoint");
			} else {
				player.sendChat("INVALID waypoint");
			}
			return true;
		}

		return false;
	}

	@Override
	public boolean onPlayerChat(Player player, String chat) {
		String[] tokens = chat.split(" ");
		return this.parseCommand(player, tokens);
	}
	
	@Override
	public boolean onPlayerCommand(Player player, String[] command) {
		return this.parseCommand(player, command);
	}

	
	protected boolean isValidWaygate(Location loc, Player player) {
		Location startLoc = new Location(loc.getX(), loc.getY()-1, loc.getZ());
		player.sendChat(World.getBlock(startLoc).getEnum().toString());
		
		
		return true;
	}
}
