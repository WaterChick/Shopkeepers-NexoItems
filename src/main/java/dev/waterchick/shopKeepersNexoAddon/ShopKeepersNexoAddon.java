package dev.waterchick.shopKeepersNexoAddon;

import org.bukkit.plugin.java.JavaPlugin;

public final class ShopKeepersNexoAddon extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new NexoItemsUpdateListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopkeeperTradeListener(this), this);
        getLogger().info("ShopKeepers-NexoAddon enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ShopKeepers-NexoAddon disabled.");
    }
}
