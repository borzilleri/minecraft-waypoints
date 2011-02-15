package com.asylumsw.bukkit.waypoints;

import java.io.File;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author jonathan
 */
public class Waypoints extends JavaPlugin {
	public final static int TELEPORT_DELAY = 15;
	private final WPPlayerListener playerListener = new WPPlayerListener(this);

	public static Server serverInstance;

	public final static String DATABASE = "jdbc:sqlite:waypoints.db";

	public Waypoints(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
		this.serverInstance = instance;
	}

	@Override
	public void onEnable() {
		Homes.loadHomes();

		// Register our events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Event.Priority.Normal, this);

		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
	}

	@Override
	public void onDisable() {
		System.out.println("Waypoints Disabled.");
	}

	public static void warpPlayerTo(Player player, Location loc) {
		int sleepTime = 0;
		while (sleepTime < TELEPORT_DELAY) {
			try {
				Thread.sleep(1000);
				player.sendMessage(ChatColor.GRAY+"* Teleport in " + (TELEPORT_DELAY - sleepTime));
				sleepTime += 1;
			}
			catch (InterruptedException ex) {
			}
		}

		player.teleportTo(loc);
	}

}
