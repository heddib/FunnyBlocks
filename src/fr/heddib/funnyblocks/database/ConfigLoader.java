package fr.heddib.funnyblocks.database;

import org.bukkit.Material;
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
				"Les mises à jour sont obligatoires (pour votre bien). Bon... Vous pouvez les désactiver dans /HeddiBUpdater/HeddiBUpdater.yml (non recommandé)" + 
				"\n" + 
				"enable: Autoriser l'utilisation de l'effet (défaut: true)" + 
				"\n" + 
				"block: Nom du block (défaut: STAINED_CLAY) Une liste des blocs est disponible ici: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html"
				);
		
		c.addDefault("enable", true);
		c.addDefault("block", Material.STAINED_CLAY.toString());
		
		c.options().copyDefaults(true);
		FunnyBlocks.getInstance().saveConfig();
	}

}
