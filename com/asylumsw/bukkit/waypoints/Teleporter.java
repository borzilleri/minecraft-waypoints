package com.asylumsw.bukkit.waypoints;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author jonathan
 */
public class Teleporter implements Runnable {
	private static HashMap<String,TeleportJob> runningJobs = new HashMap<String, TeleportJob>();
	private Waypoints plugin;

	public static class TeleportJob {
		private Player player;
		private Location destination;
		private int remainingDelay;

		public TeleportJob(Player player, Location loc, int delay) {
			this.player = player;
			this.destination = loc;
			this.remainingDelay = delay;
		}
		
	}
	
	public Teleporter(Waypoints instance) {
		plugin = instance;
	}

	public static boolean scheduleTeleport(TeleportJob newJob) {
		if( runningJobs.containsKey(newJob.player.getName())) {
			return false;
		}
		runningJobs.put(newJob.player.getName(), newJob);
		return true;
	}


	@Override
	public void run() {
		for( Map.Entry<String,TeleportJob> job : runningJobs.entrySet() ) {
			TeleportJob tpJob = job.getValue();
			if( tpJob.remainingDelay > 0 ) {
				tpJob.player.sendMessage(ChatColor.GRAY+"* Warping in "+tpJob.remainingDelay);
				tpJob.remainingDelay -= 1;
			}
			else {
				tpJob.player.teleport(tpJob.destination);
				runningJobs.remove(job.getKey());
			}
		}
	}
}
