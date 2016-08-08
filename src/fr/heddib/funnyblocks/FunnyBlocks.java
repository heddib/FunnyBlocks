package fr.heddib.funnyblocks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.heddib.funnyblocks.command.FunnyCommand;
import fr.heddib.funnyblocks.database.ConfigLoader;
import fr.heddib.funnyblocks.listener.PlayerListener;
import fr.heddib.updater.HeddiBUpdater;

/**
 * FunnyBlocks
 * @version 1.0.1
 * @author heddib
 * 
 */
public class FunnyBlocks extends JavaPlugin {
	
	// Instance du plugin
	private static FunnyBlocks i;
	public static FunnyBlocks getInstance() { return i; };
	
	public static String version = "FunnyBlock - Version ";
	
	// Joueurs avec l'effet
	private List<Player> players = new ArrayList<Player>();
	
	@Override
	public void onEnable() {
		i = this;	
		version += getDescription().getVersion();
		
		new ConfigLoader().load();
		
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), i);
		getCommand("funnyblocks").setExecutor(new FunnyCommand());
		Bukkit.getConsoleSender().sendMessage(Util.colorize("&aFunnyBlocks enabled"));
		try {
			new HeddiBUpdater(i, "funnyblocks", getDataFolder(), true, true);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDisable() {
		players.clear();
		Bukkit.getConsoleSender().sendMessage(Util.colorize("&cFunnyBlocks disabled"));
	}
	
	/**
	 * Active l'effet pour le joueur
	 * @param player Le joueur
	 * @return {@code true} Si l'effet a été activé
	 */
	public boolean activateEffect(Player player) {
		if(!canUseEffect(player))
			return false;
		return players.add(player);
	}
	
	/**
	 * Désactive l'effet pour le joueur
	 * @param player Le joueur
	 * @return {@code true} Si l'effet a été désactivé
	 */
	public boolean desactivateEffect(Player player) {
		return players.remove(player);
	}
	
	/**
	 * Vérifie si le joueur utilise l'effet
	 * @param player Le joueur
	 * @return {@code true} Si le joueur utilise l'effet
	 */
	public boolean hasEffect(Player player) {
		return players.contains(player);
	}
	
	/**
	 * Vérifie si le joueur peut utiliser l'effet
	 * @param player Le joueur
	 * @return {@code true} Si le joueur peut utiliser l'effet
	 */
	public boolean canUseEffect(Player player) {
		return player.hasPermission("funnyblocks.use") || player.isOp();
	}
	
	/**
	 * Retourne une liste des joueurs avec l'effet
	 * @return La liste
	 */
	public List<Player> getPlayers() {
		return this.players;
	}

}
