package fr.heddib.funnyblocks.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.heddib.funnyblocks.FunnyBlocks;
import fr.heddib.funnyblocks.Util;

/**
 * Commande /funnyblocks
 * @author heddib
 *
 */
public class FunnyCommand implements CommandExecutor {
	
	private String authors;
	
	public FunnyCommand() {		
		this.authors = " &7&m--&r &ePlugin développé par ";
		List<String> authors = FunnyBlocks.getInstance().getDescription().getAuthors();
		for (int i = 0; i < authors.size(); i++) {			
		    this.authors += (String)authors.get(i);
		    if (i != authors.size() - 1) {	    	
		        this.authors += " et ";
		    }
		}
		this.authors = Util.colorize(this.authors);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("funnyblocks")) {
			if(args.length == 0) {
				sender.sendMessage(Util.colorize("&7[&6FunnyBlocks&7] ") + "Version " + FunnyBlocks.getInstance().getDescription().getVersion() + this.authors);
				sender.sendMessage(Util.colorize("&8/&6funnyblocks toggle §7- §e Toggle l'effet !"));
				return true;
			} else {
				if(args.length == 1) {
					if(args[0].equalsIgnoreCase("toggle")) {
						if(sender instanceof Player) {
							Player player = (Player) sender;
							if(!FunnyBlocks.getInstance().getConfig().getBoolean("enable")) {
								player.sendMessage(Util.colorize("&cErreur: Cet effet est désactivé."));
								return true;
							}
							if(!FunnyBlocks.getInstance().canUseEffect(player)) {
								player.sendMessage(Util.colorize("&cErreur: Vous n'avez pas la permission d'utiliser cet effet."));
								return true;
							}
							if(FunnyBlocks.getInstance().hasEffect(player)) {
								FunnyBlocks.getInstance().desactivateEffect(player);
								player.sendMessage(Util.colorize("&7[&6FunnyBlocks&7] &cEffet désactivé."));
							} else {
								FunnyBlocks.getInstance().activateEffect(player);
								player.sendMessage(Util.colorize("&7[&6FunnyBlocks&7] &aEffet activé."));
							}
							return true;
						} else {
							sender.sendMessage(Util.colorize("&cErreur: Vous devez etre un joueur pour faire cette commande."));
							return true;
						}
					} else {
						sender.sendMessage(Util.colorize("&7[&6FunnyBlocks&7] ") + "Version " + FunnyBlocks.getInstance().getDescription().getVersion() + this.authors);
						sender.sendMessage(Util.colorize("&8/&6funnyblocks toggle §7- §e Toggle l'effet !"));
						return true;
					}
				} else {
					sender.sendMessage(Util.colorize("&7[&6FunnyBlocks&7] ") + "Version " + FunnyBlocks.getInstance().getDescription().getVersion() + this.authors);
					sender.sendMessage(Util.colorize("&8/&6funnyblocks toggle §7- §e Toggle l'effet !"));
					return true;
				}
			}
		}
		
		return true;
	}

}
