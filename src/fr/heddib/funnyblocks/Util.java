package fr.heddib.funnyblocks;

import org.bukkit.ChatColor;

/**
 * Divers utils pour un plugin
 * @author heddib
 *
 */
public class Util {
	
	/**
	 * Transforme les & en § pour les codes couleurs
	 * @param string Le message sans couleurs
	 * @return Le message avec les couleurs
	 */
	public static String colorize(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

}
