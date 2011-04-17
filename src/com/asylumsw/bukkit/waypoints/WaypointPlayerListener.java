package com.asylumsw.bukkit.waypoints;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

/**
 *
 * @author jonathan
 */
public class WaypointPlayerListener extends PlayerListener {
	private final Waypoints plugin;

	public WaypointPlayerListener(Waypoints plugin) {
		this.plugin = plugin;
	}

	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if( event.isCancelled() ) return;

		if( Homes.blockIsPartOfHomepoint(event.getClickedBlock()) ) {
			event.setCancelled(true);
		}		
	}
	
}
