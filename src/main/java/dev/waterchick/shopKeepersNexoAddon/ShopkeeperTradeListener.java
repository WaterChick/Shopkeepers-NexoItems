package dev.waterchick.shopKeepersNexoAddon;

import com.nexomc.nexo.api.NexoItems;
import com.nisovin.shopkeepers.api.events.ShopkeeperTradeEvent;
import com.nisovin.shopkeepers.api.shopkeeper.TradingRecipe;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ShopkeeperTradeListener implements Listener {

    private final ShopKeepersNexoAddon plugin;

    public ShopkeeperTradeListener(ShopKeepersNexoAddon plugin) {
        this.plugin = plugin;
    }

    /**
     * Listens at HIGH priority so we run after Shopkeepers' own validation.
     * If a trade was already cancelled, we skip.
     * If the trade involves Nexo items that match by ID but differ in metadata,
     * we update the shopkeeper's offers so future trades work correctly.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShopkeeperTrade(ShopkeeperTradeEvent event) {
        TradingRecipe recipe = event.getTradingRecipe();

        ItemStack offered1 = event.getOfferedItem1().copy();
        ItemStack expected1 = recipe.getItem1().copy();

        // Check slot 1: if both are Nexo items with the same ID but different metadata,
        // trigger a sync so future trades match correctly
        if (!offered1.isSimilar(expected1) && NexoItems.isSameId(offered1, expected1)) {
            triggerSync();
            return;
        }

        // Check slot 2 (optional - may not exist)
        if (event.hasOfferedItem2() && recipe.getItem2() != null) {
            ItemStack offered2 = event.getOfferedItem2().copy();
            ItemStack expected2 = recipe.getItem2().copy();
            if (!offered2.isSimilar(expected2) && NexoItems.isSameId(offered2, expected2)) {
                triggerSync();
            }
        }
    }

    private void triggerSync() {
        plugin.getLogger().info("Detected outdated Nexo item in trade, triggering sync...");
        plugin.getServer().getScheduler().runTask(plugin, () ->
                NexoShopkeeperUtil.updateAllShopkeepers(plugin.getLogger()));
    }
}
