# ShopKeepers-NexoAddon

A bridge plugin that adds [Nexo](https://nexomc.com/) custom item support to [Shopkeepers](https://github.com/Shopkeepers/Shopkeepers).

## Problem

Shopkeepers stores trade items as full Bukkit `ItemStack` objects and compares them using `isSimilar()`. When Nexo updates an item's definition (texture, lore, custom model data, etc.), the version stored in a shopkeeper's offers becomes outdated and no longer matches what players have in their inventory. This causes trades to silently fail.

## Solution

This addon keeps Shopkeepers in sync with Nexo item definitions by:

1. **Automatic synchronization on load/reload** - Listens to `NexoItemsLoadedEvent` (fired on server start and `/nexo reload`). When triggered, it iterates over every shopkeeper, checks each offer for Nexo items (via `NexoItems.idFromItem()`), and replaces outdated ItemStacks with the current version from Nexo while preserving the item amount.

2. **Trade-time detection** - Listens to `ShopkeeperTradeEvent` as a safety net. If a player attempts a trade where the offered Nexo item matches the expected item by Nexo ID but differs in metadata, it triggers a full sync to fix all affected shopkeepers.

## Supported shop types

- Selling player shops (`PriceOffer`)
- Buying player shops (`PriceOffer`)
- Trading player shops (`TradeOffer`)
- Regular admin shops (`TradeOffer`)

Book shops are not affected (they trade by book title, not by item).

## Requirements

- Java 21+
- Spigot/Paper 1.20+
- [Shopkeepers](https://www.spigotmc.org/resources/shopkeepers.80756/) 2.15.1+
- [Nexo](https://nexomc.com/) 1.18.0+

## Installation

1. Build the plugin with `./gradlew build`
2. Copy the JAR from `build/libs/` to your server's `plugins/` folder
3. Make sure both Shopkeepers and Nexo are installed
4. Start/restart the server

## Building

```bash
./gradlew build
```

The output JAR will be in `build/libs/ShopKeepers-NexoAddon-1.0.0.jar`.
