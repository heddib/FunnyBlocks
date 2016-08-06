package fr.heddib.funnyblocks.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import fr.heddib.funnyblocks.FunnyBlocks;

/**
 * Listener pour les joueurs
 * La partie du code pour restaurer les blocs vient de UltraCosmetics par iSach
 * @author heddib
 *
 */
public class PlayerListener implements Listener {
	
    /**
     * Liste de tous les blocs à restorer
     */
    public static Map<Location, String> blocksToRestore = new HashMap<Location, String>();
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {		
		 if(FunnyBlocks.getInstance().hasEffect(e.getPlayer())) {
			 Random r = new Random();
	         byte b = (byte) r.nextInt(15);
	         // Restore le bloc sous les pieds du joueurs en hard clay avec une couleur random en 2 secondes
	         setToRestore(e.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN), Material.STAINED_CLAY, b, 20 * 2);
		 }
	}
	
    /**
     * Forcer la restauration des blocs
     */
    @SuppressWarnings("deprecation")
	public static void forceRestore() {
        for (Location loc : blocksToRestore.keySet()) {
            Block b = loc.getBlock();
            String s = blocksToRestore.get(loc);
            Material m = Material.valueOf(s.split(",")[0]);
            byte d = Byte.valueOf(s.split(",")[1]);
            b.setType(m);
            b.setData(d);
        }
    }
    

    /**
     * Retore le bloc à la location "loc".
     *
     * @param location La location du bloc à restorer.
     */
    public static void restoreBlockAt(final Location location) {
        Bukkit.getScheduler().runTaskAsynchronously(FunnyBlocks.getInstance(), new Runnable() {
            @SuppressWarnings("deprecation")
			@Override
            public void run() {
                if (!blocksToRestore.containsKey(location)) return;
                Block b = location.getBlock();
                String s = blocksToRestore.get(location);
                Material m = Material.valueOf(s.split(",")[0]);
                byte d = Byte.valueOf(s.split(",")[1]);
                for (Player player : b.getLocation().getWorld().getPlayers())
                    player.sendBlockChange(location, m, d);
                blocksToRestore.remove(location);
            }
        });
    }

    /**
     * Replace un bloc avec un nouveau material et une nouvelle data, et après le delai, le restore.
     *
     * @param block     The block.
     * @param type   	The new material.
     * @param data   	The new data.
     * @param delay 	The delay after which the block is restored.
     */
    public static void setToRestoreIgnoring(final Block block, final Material NEW_TYPE, final byte NEW_DATA, final int TICK_DELAY) {
        Bukkit.getScheduler().runTaskAsynchronously(FunnyBlocks.getInstance(), new Runnable() {
            @SuppressWarnings("deprecation")
			@Override
            public void run() {
                if (blocksToRestore.containsKey(block.getLocation())) return;
                if (!blocksToRestore.containsKey(block.getLocation())) {
                    blocksToRestore.put(block.getLocation(), block.getType().toString() + "," + block.getData());
                    for (Player player : block.getLocation().getWorld().getPlayers())
                        player.sendBlockChange(block.getLocation(), NEW_TYPE, NEW_DATA);
                    Bukkit.getScheduler().runTaskLater(FunnyBlocks.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            restoreBlockAt(block.getLocation());

                        }
                    }, TICK_DELAY);
                }
            }
        });
    }

    /**
     * Replace un bloc avec un nouveau material et une nouvelle data, et après le delai, le restore.
     *
     * @param block     The block.
     * @param type   	The new material.
     * @param data   	The new data.
     * @param delay 	The delay after which the block is restored.
     */
    public static void setToRestore(final Block block, final Material type, final byte data, final int delay) {
        Bukkit.getScheduler().runTaskAsynchronously(FunnyBlocks.getInstance(), new Runnable() {
            @SuppressWarnings("deprecation")
			@Override
            public void run() {
                if (blocksToRestore.containsKey(block.getLocation())) return;
                Block bUp = block.getRelative(BlockFace.UP);
                if (block.getType() != Material.AIR
                        && block.getType() != Material.SIGN_POST
                        && block.getType() != Material.CHEST
                        && block.getType() != Material.STONE_PLATE
                        && block.getType() != Material.WOOD_PLATE
                        && block.getType() != Material.WALL_SIGN
                        && block.getType() != Material.WALL_BANNER
                        && block.getType() != Material.STANDING_BANNER
                        && block.getType() != Material.CROPS
                        && block.getType() != Material.LONG_GRASS
                        && block.getType() != Material.SAPLING
                        && block.getType() != Material.DEAD_BUSH
                        && block.getType() != Material.RED_ROSE
                        && block.getType() != Material.RED_MUSHROOM
                        && block.getType() != Material.BROWN_MUSHROOM
                        && block.getType() != Material.TORCH
                        && block.getType() != Material.LADDER
                        && block.getType() != Material.VINE
                        && block.getType() != Material.DOUBLE_PLANT
                        && block.getType() != Material.PORTAL
                        && block.getType() != Material.CACTUS
                        && block.getType() != Material.WATER
                        && block.getType() != Material.STATIONARY_WATER
                        && block.getType() != Material.LAVA
                        && block.getType() != Material.STATIONARY_LAVA
                        && block.getType() != Material.PORTAL
                        && block.getType() != Material.ENDER_PORTAL
                        && block.getType() != Material.SOIL
                        && block.getType() != Material.BARRIER
                        && block.getType() != Material.COMMAND
                        && block.getType() != Material.DROPPER
                        && block.getType() != Material.DISPENSER                     
                        && !block.getType().toString().toLowerCase().contains("door")
                        && block.getType() != Material.BED
                        && block.getType() != Material.BED_BLOCK
                        && !isPortalBlock(block)
                        && !blocksToRestore.containsKey(block.getLocation())
                        && block.getType().isSolid()
                        && a(bUp)
                        && block.getType().getId() != 43
                        && block.getType().getId() != 44) {
                    blocksToRestore.put(block.getLocation(), block.getType().toString() + "," + block.getData());
                    for (Player player : block.getLocation().getWorld().getPlayers())
                        player.sendBlockChange(block.getLocation(), type, data);
                    Bukkit.getScheduler().runTaskLater(FunnyBlocks.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            restoreBlockAt(block.getLocation());
                        }
                    }, delay);
                }

            }
        });
    }


    private static boolean a(Block b) {
        return b.getType() == Material.AIR
                || b.getType().isSolid();
    }

    /**
     * Checks if a block is part of a Nether Portal.
     *
     * @param b The block to check
     * @return {@code true} if a block is part of a Nether Portal, otherwise {@code false}.
     */
    public static boolean isPortalBlock(Block b) {
        for (BlockFace face : BlockFace.values())
            if (b.getRelative(face).getType() == Material.PORTAL)
                return true;
        return false;
    }
	
}
