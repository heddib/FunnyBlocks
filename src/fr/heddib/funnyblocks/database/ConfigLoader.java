package fr.heddib.funnyblocks.database;

import org.bukkit.configuration.file.FileConfiguration;

import fr.heddib.funnyblocks.FunnyBlocks;

public class ConfigLoader {
	
	public void load() {
		reloadConfig();
	}

	public static void reloadConfig() {
		FunnyBlocks.getInstance().reloadConfig();
		FileConfiguration c = FunnyBlocks.getInstance().getConfig();
		
		c.options().header(
				"Configuration de FunnyBlocks" +
				"\n" +
				"Par heddib" +
				"\n" +
				"auto-updater: Autoriser l'updater à chercher des mises à jour (défaut: true)" + 
				"\n" + 
				"enable: Autoriser l'utilisation de l'effet"
				);
		
		c.addDefault("auto-updater", true);
		c.addDefault("enable", true);
		
		c.options().copyDefaults(true);
		FunnyBlocks.getInstance().saveConfig();
	}

}
