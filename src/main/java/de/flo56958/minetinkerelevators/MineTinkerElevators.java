package de.flo56958.minetinkerelevators;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class MineTinkerElevators extends JavaPlugin implements Listener {

    //TODO: Make Text appear when using them

    @Override
    public void onEnable() {
        loadConfig();
        registerElevatorMotor();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @EventHandler(ignoreCancelled = true)
    public void onSneak (PlayerToggleSneakEvent e) {
        FileConfiguration config = getPlugin().getConfig();
        if (config.getStringList("BannedWorlds").contains(e.getPlayer().getWorld().getName())) return;
        if (!e.isSneaking()) return;

        Player p = e.getPlayer();

        if (!p.hasPermission("minetinker.elevator.use")) return;

        Location l = p.getLocation();

        Block b = p.getWorld().getBlockAt(l.add(0, -2, 0));
        if (!(b.getState() instanceof Hopper)) return;

        Hopper h1 = (Hopper) b.getState();
        if (h1.getCustomName() == null) return; //name could be NULL
        if (!h1.getCustomName().equals(ChatColor.GRAY + config.getString("ItemName"))) return;

        for (int i = l.getBlockY() - 1; i >= -64; i--) {
            if (p.getWorld().getBlockAt(l.getBlockX(), i, l.getBlockZ()).getState() instanceof Hopper) {
                Hopper h2 = (Hopper) p.getWorld().getBlockAt(l.getBlockX(), i, l.getBlockZ()).getState();

                if (h2.getCustomName() == null) { continue; } //name could be NULL

                if (h2.getCustomName().equals(ChatColor.GRAY + config.getString("ItemName"))) {
                    l.add(0, i - l.getBlockY() + 2, 0);
                    p.teleport(l);

                    if (config.getBoolean("HasSound")) {
                        p.playSound(p.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.5F, 0.5F);
                    }

                    break;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onJump (PlayerMoveEvent e) {
        FileConfiguration config = getPlugin().getConfig();
        if (config.getStringList("BannedWorlds").contains(e.getPlayer().getWorld().getName())) return;

        Player p = e.getPlayer();

        if (!p.hasPermission("minetinker.elevator.use")) return;
        if (e.getTo() == null) return;

        Location l = p.getLocation();

        if (!(e.getTo().getY() > e.getFrom().getY() && e.getTo().getX() == e.getFrom().getX() && e.getTo().getZ() == e.getFrom().getZ())) return;

        Block b = p.getWorld().getBlockAt(l.add(0, -2, 0));
        if (!(b.getState() instanceof Hopper)) return;

        Hopper h1 = (Hopper) b.getState();
        if (h1.getCustomName() == null) return;

        if (h1.getCustomName().equals(ChatColor.GRAY + config.getString("ItemName"))) {
            for (int i = l.getBlockY() + 1; i <= 320; i++) {
                if (p.getWorld().getBlockAt(l.getBlockX(), i, l.getBlockZ()).getState() instanceof Hopper) {
                    Hopper h2 = (Hopper) p.getWorld().getBlockAt(l.getBlockX(), i, l.getBlockZ()).getState();

                    if (h2.getCustomName() == null) { continue; }

                    if (h2.getCustomName().equals(ChatColor.GRAY + config.getString("ItemName"))) {
                        l.add(0, i - l.getBlockY() + 2, 0);
                        p.teleport(l);

                        if (config.getBoolean("HasSound")) {
                            p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5F, 0.5F);
                        }

                        break;
                    }
                }
            }
        }
    }

    public static Plugin getPlugin() { // necessary to do getConfig() in other classes
        return Bukkit.getPluginManager().getPlugin("MineTinker-Elevators");
    }

    /**
     * tries to register the recipe for the elevator motor
     */
    public static void registerElevatorMotor() {
        FileConfiguration config = getPlugin().getConfig();
        try {
            NamespacedKey nkey = new NamespacedKey(getPlugin(), "Elevator_Motor");
            ItemStack item = new ItemStack(Material.HOPPER, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GRAY + config.getString("ItemName"));
            item.setItemMeta(meta);
            ShapedRecipe newRecipe = new ShapedRecipe(nkey, item); //init recipe
            String top = config.getString("Recipe.Top");
            String middle = config.getString("Recipe.Middle");
            String bottom = config.getString("Recipe.Bottom");
            ConfigurationSection materials = config.getConfigurationSection("Recipe.Materials");

            // TODO: Make safe
            newRecipe.shape(top, middle, bottom); //makes recipe

            for (String key : materials.getKeys(false)) {
                newRecipe.setIngredient(key.charAt(0), Material.getMaterial(materials.getString(key)));
            }

            getPlugin().getServer().addRecipe(newRecipe); //adds recipe
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
