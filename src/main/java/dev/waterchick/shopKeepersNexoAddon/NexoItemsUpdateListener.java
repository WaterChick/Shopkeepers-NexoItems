package dev.waterchick.shopKeepersNexoAddon;

import com.nexomc.nexo.api.events.NexoItemsLoadedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NexoItemsUpdateListener implements Listener {

    private final ShopKeepersNexoAddon plugin;

    public NexoItemsUpdateListener(ShopKeepersNexoAddon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNexoItemsLoaded(NexoItemsLoadedEvent event) {
        // Run on next tick to ensure all Nexo items are fully registered
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getLogger().info("Nexo items loaded, synchronizing shopkeeper offers...");
            int updated = NexoShopkeeperUtil.updateAllShopkeepers(plugin.getLogger());
            if (updated > 0) {
                plugin.getLogger().info("Updated Nexo items in " + updated + " shopkeeper(s).");
            } else {
                plugin.getLogger().info("All shopkeeper Nexo items are up to date.");
            }
        });
    }
}
