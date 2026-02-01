package dev.waterchick.shopKeepersNexoAddon;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import com.nisovin.shopkeepers.api.ShopkeepersAPI;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.admin.regular.RegularAdminShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.offers.PriceOffer;
import com.nisovin.shopkeepers.api.shopkeeper.offers.TradeOffer;
import com.nisovin.shopkeepers.api.shopkeeper.player.buy.BuyingPlayerShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.sell.SellingPlayerShopkeeper;
import com.nisovin.shopkeepers.api.shopkeeper.player.trade.TradingPlayerShopkeeper;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class NexoShopkeeperUtil {

    private NexoShopkeeperUtil() {}

    /**
     * If the given ItemStack is a Nexo item, returns a fresh copy built from the current
     * Nexo definition (preserving the original amount). Returns null if not a Nexo item
     * or if the Nexo ID no longer exists.
     */
    public static ItemStack getUpdatedNexoItem(ItemStack item) {
        if (item == null) return null;
        String nexoId = NexoItems.idFromItem(item);
        if (nexoId == null) return null;
        ItemBuilder builder = NexoItems.itemFromId(nexoId);
        if (builder == null) return null;
        ItemStack updated = builder.build();
        updated.setAmount(item.getAmount());
        return updated;
    }

    /**
     * Synchronizes all Nexo items across every shopkeeper's offers.
     * Returns the number of shopkeepers that were updated.
     */
    public static int updateAllShopkeepers(Logger logger) {
        int updatedCount = 0;
        for (Shopkeeper shopkeeper : ShopkeepersAPI.getPlugin().getShopkeeperRegistry().getAllShopkeepers()) {
            boolean changed = false;

            if (shopkeeper instanceof SellingPlayerShopkeeper sell) {
                changed = updatePriceOffers(sell.getOffers(), sell, logger);
            } else if (shopkeeper instanceof BuyingPlayerShopkeeper buy) {
                changed = updatePriceOffers(buy.getOffers(), buy, logger);
            } else if (shopkeeper instanceof TradingPlayerShopkeeper trade) {
                changed = updateTradeOffers(trade.getOffers(), trade, logger);
            } else if (shopkeeper instanceof RegularAdminShopkeeper admin) {
                changed = updateTradeOffers(admin.getOffers(), admin, logger);
            }

            if (changed) {
                shopkeeper.save();
                updatedCount++;
            }
        }
        return updatedCount;
    }

    private static boolean updatePriceOffers(List<? extends PriceOffer> offers, Object shopkeeper, Logger logger) {
        List<PriceOffer> newOffers = new ArrayList<>();
        boolean changed = false;

        for (PriceOffer offer : offers) {
            ItemStack item = offer.getItem().copy();
            ItemStack updated = getUpdatedNexoItem(item);
            if (updated != null && !updated.isSimilar(item)) {
                String nexoId = NexoItems.idFromItem(item);
                logger.fine("Updating Nexo item '" + nexoId + "' in shopkeeper");
                newOffers.add(PriceOffer.create(updated, offer.getPrice()));
                changed = true;
            } else {
                newOffers.add(offer);
            }
        }

        if (changed) {
            if (shopkeeper instanceof SellingPlayerShopkeeper sell) {
                sell.setOffers(newOffers);
            } else if (shopkeeper instanceof BuyingPlayerShopkeeper buy) {
                buy.setOffers(newOffers);
            }
        }
        return changed;
    }

    private static boolean updateTradeOffers(List<? extends TradeOffer> offers, Object shopkeeper, Logger logger) {
        List<TradeOffer> newOffers = new ArrayList<>();
        boolean changed = false;

        for (TradeOffer offer : offers) {
            ItemStack result = offer.getResultItem().copy();
            ItemStack item1 = offer.getItem1().copy();
            ItemStack item2 = offer.hasItem2() ? offer.getItem2().copy() : null;

            ItemStack updatedResult = getUpdatedNexoItem(result);
            ItemStack updatedItem1 = getUpdatedNexoItem(item1);
            ItemStack updatedItem2 = item2 != null ? getUpdatedNexoItem(item2) : null;

            boolean offerChanged = false;
            if (updatedResult != null && !updatedResult.isSimilar(result)) {
                result = updatedResult;
                offerChanged = true;
            }
            if (updatedItem1 != null && !updatedItem1.isSimilar(item1)) {
                item1 = updatedItem1;
                offerChanged = true;
            }
            if (updatedItem2 != null && !updatedItem2.isSimilar(item2)) {
                item2 = updatedItem2;
                offerChanged = true;
            }

            if (offerChanged) {
                String ids = formatNexoIds(result, item1, item2);
                logger.fine("Updating Nexo trade offer [" + ids + "] in shopkeeper");
                newOffers.add(TradeOffer.create(result, item1, item2));
                changed = true;
            } else {
                newOffers.add(offer);
            }
        }

        if (changed) {
            if (shopkeeper instanceof TradingPlayerShopkeeper trade) {
                trade.setOffers(newOffers);
            } else if (shopkeeper instanceof RegularAdminShopkeeper admin) {
                admin.setOffers(newOffers);
            }
        }
        return changed;
    }

    private static String formatNexoIds(ItemStack result, ItemStack item1, ItemStack item2) {
        StringBuilder sb = new StringBuilder();
        appendNexoId(sb, item1, "in1");
        appendNexoId(sb, item2, "in2");
        appendNexoId(sb, result, "out");
        return sb.toString();
    }

    private static void appendNexoId(StringBuilder sb, ItemStack item, String label) {
        if (item == null) return;
        String id = NexoItems.idFromItem(item);
        if (id != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(label).append("=").append(id);
        }
    }
}
