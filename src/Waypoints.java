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
	protected static HomeList homelist;
	
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

	@Override
	public void activate() {
		Waypoints.homelist = new HomeList();
		Waypoints.homelist.load();
	}

	protected boolean parseCommand(Player player, String[] tokens) {
		Waypoints.player = player;
		String command = tokens[0].substring(1);

		if(command.equalsIgnoreCase("home")) {
			Waypoints.homelist.sendPlayerHome(player);
			return true;
		}
		else if( command.equalsIgnoreCase("sethome") ) {
			Waypoints.homelist.setUserHomePoint(player);
			return true;
		}
		else if( command.equalsIgnoreCase("unsethome") ) {
			Waypoints.homelist.unsetUserHomePoint(player);
			return true;
		}

		return false;
	}

	@Override
	public String toString() {
		return "!home, !sethome, !unsethome";
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
