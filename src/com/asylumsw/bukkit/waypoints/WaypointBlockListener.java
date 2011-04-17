package com.asylumsw.bukkit.waypoints;

import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;

/**
 *
 * @author jonathan
 */
public class WaypointBlockListener extends BlockListener {
	private Waypoints plugin;

	public WaypointBlockListener(Waypoints plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onBlockDamage(BlockDamageEvent event) {
		if( event.isCancelled() ) return;
		
		if( Homes.blockIsPartOfHomepoint(event.getBlock()) ) {
			event.setCancelled(true);
		}
	}

}
