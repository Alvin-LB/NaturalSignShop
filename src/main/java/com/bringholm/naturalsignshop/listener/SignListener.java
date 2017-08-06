package com.bringholm.naturalsignshop.listener;

import com.bringholm.naturalsignshop.NaturalSignShop;
import com.bringholm.naturalsignshop.constants.Permissions;
import com.bringholm.naturalsignshop.data.SignShopProperties;
import com.bringholm.naturalsignshop.bukkitutils.Scroller;
import com.bringholm.naturalsignshop.type.SellType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.EnumSet;

import static com.bringholm.naturalsignshop.NaturalSignShop.addPrefix;

public class SignListener implements Listener {

    private final MathContext mathContext;
    private NaturalSignShop plugin;

    public SignListener(NaturalSignShop plugin) {
        this.plugin = plugin;
        mathContext = new MathContext(plugin.getEconomyHandler().getCurrencyScale(), RoundingMode.HALF_UP);
    }


    @EventHandler
    public void onSignPlace(SignChangeEvent e) {
        if (!e.getLine(0).equalsIgnoreCase("[NaturalSShop]") && !e.getLine(0).equalsIgnoreCase("[NSS]")) {
            return;
        }
        String originalFirstLine = e.getLine(0);
        e.setLine(0, ChatColor.RED + "[NaturalSShop]");
        SellType sellType = SellType.parseType(e.getLine(1));
        if (!e.getPlayer().hasPermission(Permissions.CREATE_SIGN_PERMISSION)
                && !e.getPlayer().hasPermission(Permissions.CREATE_SIGN_PERMISSION.getName() + ".*")) {
            if (sellType == null) {
                // Reset first line to original
                e.setLine(0, originalFirstLine);
                e.getPlayer().sendMessage(addPrefix(ChatColor.RED + "You don't have permission to do this!"));
                return;
            }
            // Check for type-specific permissions
            if (!e.getPlayer().hasPermission(Permissions.CREATE_SIGN_PERMISSION.getName() + "." + sellType.getIdentifierName())
                    && !e.getPlayer().hasPermission(Permissions.CREATE_SIGN_PERMISSION.getName() + "." + sellType.getShortIdentifierName())) {
                // Reset first line to original
                e.setLine(0, originalFirstLine);
                e.getPlayer().sendMessage(addPrefix(ChatColor.RED + "You don't have permission to do this!"));
                return;
            }
        }
        if (sellType == null) {
            e.getPlayer().sendMessage(addPrefix(ChatColor.RED + "Invalid type!"));
            return;
        }
        String name = sellType.getIdentifierName();
        if (name.length() > 16) {
            name = name.replace("_", "");
        }
        if (name.length() > 16) {
            name = sellType.getShortIdentifierName();
        }
        e.setLine(1, name);
        String thirdLine = e.getLine(2);
        // Remove all whitespace and dashes
        thirdLine = thirdLine.replaceAll("( |-)", "");
        String[] split = thirdLine.split("\\$");
        int amount;
        try {
            amount = Integer.parseInt(split[0]);
        } catch (NumberFormatException ex) {
            e.getPlayer().sendMessage(addPrefix(ChatColor.RED + "'" + split[0] + "' is not a valid number!"));
            return;
        }
        if (amount < 1) {
            e.getPlayer().sendMessage(addPrefix(ChatColor.RED + "'" + amount + "' is not a valid amount!"));
            return;
        }
        double buyPrice;
        try {
            buyPrice = Double.parseDouble(split[1]);
        } catch (NumberFormatException ex) {
            e.getPlayer().sendMessage(addPrefix(ChatColor.RED + "'" + split[1] + "' is not a valid number!"));
            return;
        }
        if (buyPrice < 0) {
            e.getPlayer().sendMessage(addPrefix(ChatColor.RED + "'" + buyPrice + "' is not a valid buy price!"));
            return;
        }
        double sellPrice = -1;
        if (split.length == 3) {
            try {
                sellPrice = Double.parseDouble(split[2]);
            } catch (NumberFormatException ex) {
                e.getPlayer().sendMessage(addPrefix(ChatColor.RED + "'" + split[2] + "' is not a valid number!"));
                return;
            }
            if (sellPrice < 0) {
                e.getPlayer().sendMessage(addPrefix(ChatColor.RED + "'" + sellPrice + "' is not a valid number!"));
                return;
            }
        } else if (split.length != 2) {
            e.getPlayer().sendMessage(addPrefix(ChatColor.RED + "Malformed third line!"));
            return;
        }
        // TODO: this is stupid, implement some sort of while-loop
        // If the string is too long, try to shorten it
        String newThirdLine = amount + " B $" + formatDouble(buyPrice) + (sellPrice == -1 ? "" : " : S $" + formatDouble(sellPrice));
        if (newThirdLine.length() > 16) {
            newThirdLine = newThirdLine.replace("$", "");
        }
        if (newThirdLine.length() > 16) {
            newThirdLine = amount + " B" + formatDouble(buyPrice) + (sellPrice == -1 ? "" : " : S" + formatDouble(sellPrice));
        }
        if (newThirdLine.length() > 16) {
            newThirdLine = amount + " B" + formatDouble(buyPrice) + (sellPrice == -1 ? "" : ":S" + formatDouble(sellPrice));
        }
        if (newThirdLine.length() > 16) {
            newThirdLine = amount + "B" + formatDouble(buyPrice) + (sellPrice == -1 ? "" : ":S" + formatDouble(sellPrice));
        }
        if (newThirdLine.length() > 16) {
            // if all other attempts fail: round the prices so they're at maximum 5 characters long;
            String buyPriceString = formatDouble(buyPrice);
            while (buyPriceString.length() > 5) {
                buyPriceString = buyPriceString.substring(0, buyPriceString.length() - 1);
            }
            String sellPriceString = formatDouble(sellPrice);
            while (sellPriceString.length() > 5) {
                sellPriceString = sellPriceString.substring(0, sellPriceString.length() - 1);
            }
            newThirdLine = amount + " B" + buyPriceString + (sellPrice == -1 ? "" : ":S" + sellPriceString);
            e.getPlayer().sendMessage(addPrefix(ChatColor.GOLD + "One or more of the prices were too long to fit on the sign, so they were rounded to fit."));
        }
        e.setLine(2, newThirdLine);
        if (plugin.linkSameOptionSigns()) {
            SignShopProperties properties = parseProperties(e.getLines());
            // cast isn't redundant according to maven!
            //noinspection RedundantCast
            e.setLine(3, String.valueOf((int) (plugin.getLinkedStock().containsKey(properties) ? plugin.getLinkedStock().get(properties) : 0)));
        } else {
            e.setLine(3, "0");
        }
        // Do this last to make sure sign only turns blue if it was correctly setup.
        e.setLine(0, ChatColor.BLUE + "[NaturalSShop]");
        e.getPlayer().sendMessage(addPrefix(ChatColor.GREEN + "Successfully created sign shop for " + sellType.getDisplayName() + "!"));
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.HAND) {
            if (EnumSet.of(Action.RIGHT_CLICK_BLOCK, Action.LEFT_CLICK_BLOCK).contains(e.getAction())) {
                if (EnumSet.of(Material.SIGN_POST, Material.WALL_SIGN).contains(e.getClickedBlock().getType())) {
                    Sign sign = (Sign) e.getClickedBlock().getState();
                    if (sign.getLine(0).equals(ChatColor.BLUE + "[NaturalSShop]")) {
                        // Prevent interactions with sign if it has an active scroller. Avoids displaying the wrong
                        // stock if an item is sold or bought when the scroller is active.
                        if (Scroller.SignScroller.hasScroller(sign, e.getPlayer())) {
                            return;
                        }
                        SignShopProperties properties = parseProperties(sign.getLines());
                        if (properties == null) {
                            this.plugin.getLogger().warning("Failed to parse properties for sign at " + formatLocation(sign.getLocation()) + " (" + Arrays.toString(sign.getLines()) + ")");
                            sign.setLine(0, ChatColor.RED + "[NaturalSShop]");
                            sign.update();
                            e.getPlayer().sendMessage(addPrefix(ChatColor.RED + "Malformed sign!"));
                            return;
                        }
                        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            int newAmount;
                            int signAmount;
                            try {
                                signAmount = Integer.parseInt(sign.getLine(3));
                            } catch (NumberFormatException ex) {
                                this.plugin.getLogger().warning("Failed to parse amount for sign at " + formatLocation(sign.getLocation()) + " (" + ex + ")");
                                sign.setLine(0, ChatColor.RED + "[NaturalSShop]");
                                sign.update();
                                e.getPlayer().sendMessage(addPrefix(ChatColor.RED + "Malformed sign!"));
                                return;
                            }
                            if (properties.getSellType().shouldSell(e.getPlayer())) {
                                if (!e.getPlayer().hasPermission(Permissions.SELL_PERMISSION)) {
                                    e.getPlayer().sendMessage(addPrefix(ChatColor.RED + "You don't have permission to do this!"));
                                    return;
                                }
                                if (properties.getSellPrice() == -1) {
                                    e.getPlayer().sendMessage(addPrefix(ChatColor.RED + "You cannot sell items to this sign!"));
                                    return;
                                }
                                if (e.getPlayer().isSneaking()) {
                                    newAmount = sellAllAvailable(e.getPlayer(), properties, signAmount);
                                } else {
                                    newAmount = sellOneBatch(e.getPlayer(), properties, signAmount);
                                }
                            } else {
                                if (!e.getPlayer().hasPermission(Permissions.BUY_PERMISSION)) {
                                    e.getPlayer().sendMessage(addPrefix(ChatColor.RED + "You don't have permission to do this!"));
                                    return;
                                }
                                if (e.getPlayer().isSneaking()) {
                                    newAmount = buyAllPossible(e.getPlayer(), properties, signAmount);
                                } else {
                                    newAmount = buyOneBatch(e.getPlayer(), properties, signAmount);
                                }
                            }
                            sign.setLine(3, String.valueOf(newAmount));
                            sign.update();
                        } else {
                            // Take every chance we can to update the linked stock
                            if (plugin.linkSameOptionSigns()) {
                                if (plugin.getLinkedStock().containsKey(properties) && !plugin.getLinkedStock().get(properties).toString().equals(sign.getLine(3))) {
                                    sign.setLine(3, plugin.getLinkedStock().get(properties).toString());
                                    sign.update();
                                }
                            }
                            if (!e.getPlayer().hasPermission(Permissions.USE_SCROLLER_PERMISSION)) {
                                e.getPlayer().sendMessage(addPrefix(ChatColor.RED + "You don't have permission to do this!"));
                                return;
                            }
                            if (Scroller.SignScroller.hasScroller(sign, e.getPlayer())) {
                                return;
                            }
                            String[] strings = new String[2];
                            strings[0] = properties.getSellType().getDisplayName();
                            strings[1] = "Amount: " + properties.getAmount() + " Buy Price: " + formatDouble(properties.getBuyPrice()) + (properties.getSellPrice() != -1 ? " Sell Price: " + formatDouble(properties.getSellPrice()) : "");
                            new Scroller.SignScroller(plugin, sign, e.getPlayer(), new int[] {1, 2}, strings, 3);
                        }
                    }
                }
            }
        }
    }

    /*
     * Update the linked stock as well as cancel any currently running scrollers for broken signs to avoid client desync.
     */
    @EventHandler
    public void onBlockDestroy(BlockBreakEvent e) {
        if (EnumSet.of(Material.SIGN_POST, Material.WALL_SIGN).contains(e.getBlock().getType())) {
            Sign sign = (Sign) e.getBlock().getState();
            if (sign.getLine(0).equals(ChatColor.BLUE + "[NaturalSShop]")) {
                SignShopProperties properties = parseProperties(sign.getLines());
                if (properties != null && plugin.getLinkedStock().containsKey(properties)) {
                    int amount;
                    try {
                        amount = Integer.parseInt(sign.getLine(3));
                    } catch (NumberFormatException ex) {
                        plugin.getLogger().warning("Failed to parse amount for removed sign at " + formatLocation(sign.getLocation()) + " due to " + ex + "!");
                        return;
                    }
                    int linkedStockAmount = plugin.getLinkedStock().get(properties);
                    linkedStockAmount = linkedStockAmount - amount;
                    if (linkedStockAmount < 0) {
                        linkedStockAmount = 0;
                    }
                    plugin.getLinkedStock().put(properties, linkedStockAmount);
                }
                if (Scroller.SignScroller.hasScroller(sign)) {
                    Scroller.SignScroller.cancelScroller(sign);
                }
            }
        }
    }

    /*
     * Remove scrollers for players who have left. Prevents a small memory leak.
     */
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Scroller.SignScroller.cancelScroller(e.getPlayer());
    }

    private String formatLocation(Location location) {
        return "x " + location.getX() + " y " + location.getY() + " z " + location.getZ() + " in world " + location.getWorld().getName();
    }

    private int buyAllPossible(Player player, SignShopProperties signShopProperties, int oldAmount) {
        int amount;
        if (plugin.linkSameOptionSigns()) {
            amount = plugin.getLinkedStock().containsKey(signShopProperties) ? plugin.getLinkedStock().get(signShopProperties) : 0;
        } else {
            amount = oldAmount;
        }
        if (amount == 0) {
            player.sendMessage(addPrefix(ChatColor.RED + "This sign is out of stock!"));
            if (plugin.linkSameOptionSigns()) {
                return plugin.getLinkedStock().containsKey(signShopProperties) ? plugin.getLinkedStock().get(signShopProperties) : 0;
            } else {
                return oldAmount;
            }
        }
        BigDecimal priceDivisor = new BigDecimal(signShopProperties.getAmount(), mathContext).divide(new BigDecimal(amount, mathContext), mathContext);
        BigDecimal price = new BigDecimal(signShopProperties.getBuyPrice(), mathContext).divide(priceDivisor, mathContext);
        int availableSpace = signShopProperties.getSellType().getAvailableSpace(player);
        if (availableSpace == 0) {
            player.sendMessage(addPrefix(ChatColor.RED + "You don't have enough space in your inventory!"));
            if (plugin.linkSameOptionSigns()) {
                return plugin.getLinkedStock().containsKey(signShopProperties) ? plugin.getLinkedStock().get(signShopProperties) : 0;
            } else {
                return oldAmount;
            }
        }
        if (availableSpace < amount) {
            amount = availableSpace;
            priceDivisor = new BigDecimal(signShopProperties.getAmount(), mathContext).divide(new BigDecimal(amount, mathContext), mathContext);
            price = new BigDecimal(signShopProperties.getBuyPrice(), mathContext).divide(priceDivisor, mathContext);
        }
        if (!plugin.getEconomyHandler().doesPlayerHave(player, price.doubleValue())) {
            amount = new BigDecimal(plugin.getEconomyHandler().getBalance(player), mathContext).divide(price, mathContext).intValue();
            if (amount == 0) {
                player.sendMessage(addPrefix(ChatColor.RED + "You do not have enough money to buy this!"));
                if (plugin.linkSameOptionSigns()) {
                    return plugin.getLinkedStock().containsKey(signShopProperties) ? plugin.getLinkedStock().get(signShopProperties) : 0;
                } else {
                    return oldAmount;
                }
            } else {
                priceDivisor = new BigDecimal(signShopProperties.getAmount(), mathContext).divide(new BigDecimal(amount, mathContext), mathContext);
                price = new BigDecimal(signShopProperties.getBuyPrice(), mathContext).divide(priceDivisor, mathContext);
            }
        }
        plugin.getEconomyHandler().withdrawBalance(player, price.doubleValue());
        signShopProperties.getSellType().givePlayer(player, amount);
        player.sendMessage(addPrefix(ChatColor.GREEN + "You have bought " + amount + " " + signShopProperties.getSellType().getDisplayName() + " for $" + formatDouble(price.doubleValue()) + "!"));
        int linkedStockAmount = plugin.getLinkedStock().containsKey(signShopProperties) ? plugin.getLinkedStock().get(signShopProperties) : 0;
        plugin.getLinkedStock().put(signShopProperties, linkedStockAmount - amount);
        if (plugin.linkSameOptionSigns()) {
            oldAmount = linkedStockAmount;
        }
        return oldAmount - amount;
    }

    private int buyOneBatch(Player player, SignShopProperties signShopProperties, int oldAmount) {
        int amount = signShopProperties.getAmount();
        BigDecimal price = new BigDecimal(signShopProperties.getBuyPrice(), mathContext);
        if (!plugin.getEconomyHandler().doesPlayerHave(player, price.doubleValue())) {
            amount = new BigDecimal(plugin.getEconomyHandler().getBalance(player), mathContext).divide(price, mathContext).intValue();
            if (amount == 0) {
                player.sendMessage(addPrefix(ChatColor.RED + "You do not have enough money to buy this!"));
                if (plugin.linkSameOptionSigns()) {
                    return plugin.getLinkedStock().containsKey(signShopProperties) ? plugin.getLinkedStock().get(signShopProperties) : 0;
                } else {
                    return oldAmount;
                }
            } else {
                BigDecimal priceDivisor = new BigDecimal(signShopProperties.getAmount(), mathContext).divide(new BigDecimal(amount, mathContext), mathContext);
                price = new BigDecimal(signShopProperties.getBuyPrice(), mathContext).divide(priceDivisor, mathContext);
            }
        }
        int stock;
        if (plugin.linkSameOptionSigns()) {
            stock = plugin.getLinkedStock().containsKey(signShopProperties) ? plugin.getLinkedStock().get(signShopProperties) : 0;
        } else {
            stock = oldAmount;
        }
        if (stock == 0) {
            player.sendMessage(addPrefix(ChatColor.RED + "This sign is out of stock!"));
            if (plugin.linkSameOptionSigns()) {
                return plugin.getLinkedStock().containsKey(signShopProperties) ? plugin.getLinkedStock().get(signShopProperties) : 0;
            } else {
                return oldAmount;
            }
        }
        if (stock < amount) {
            amount = stock;
            BigDecimal priceDivisor = new BigDecimal(signShopProperties.getAmount(), mathContext).divide(new BigDecimal(amount, mathContext), mathContext);
            price = new BigDecimal(signShopProperties.getBuyPrice(), mathContext).divide(priceDivisor, mathContext);
        }
        int availableSpace = signShopProperties.getSellType().getAvailableSpace(player);
        if (availableSpace == 0) {
            player.sendMessage(addPrefix(ChatColor.RED + "You don't have enough space in your inventory!"));
            if (plugin.linkSameOptionSigns()) {
                return plugin.getLinkedStock().containsKey(signShopProperties) ? plugin.getLinkedStock().get(signShopProperties) : 0;
            } else {
                return oldAmount;
            }
        }
        if (availableSpace < amount) {
            amount = availableSpace;
            BigDecimal priceDivisor = new BigDecimal(signShopProperties.getAmount(), mathContext).divide(new BigDecimal(amount, mathContext), mathContext);
            price = new BigDecimal(signShopProperties.getBuyPrice(), mathContext).divide(priceDivisor, mathContext);
        }
        plugin.getEconomyHandler().withdrawBalance(player, price.doubleValue());
        signShopProperties.getSellType().givePlayer(player, amount);
        player.sendMessage(addPrefix(ChatColor.GREEN + "You have bought " + amount + " " + signShopProperties.getSellType().getDisplayName() + " for $" + formatDouble(price.doubleValue()) + "!"));
        int linkedStockAmount = plugin.getLinkedStock().containsKey(signShopProperties) ? plugin.getLinkedStock().get(signShopProperties) : 0;
        plugin.getLinkedStock().put(signShopProperties, linkedStockAmount - amount);
        if (plugin.linkSameOptionSigns()) {
            oldAmount = linkedStockAmount;
        }
        return oldAmount - amount;
    }

    private int sellAllAvailable(Player player, SignShopProperties signShopProperties, int oldAmount) {
        int inventoryAmount = signShopProperties.getSellType().removeAllAndGetCount(player);
        if (inventoryAmount == 0) {
            player.sendMessage(addPrefix(ChatColor.RED + "You don't have anything to sell!"));
            if (plugin.linkSameOptionSigns()) {
                return plugin.getLinkedStock().containsKey(signShopProperties) ? plugin.getLinkedStock().get(signShopProperties) : 0;
            } else {
                return oldAmount;
            }
        }
        BigDecimal priceDivisor = new BigDecimal(signShopProperties.getAmount(), mathContext).divide(new BigDecimal(inventoryAmount, mathContext), mathContext);
        BigDecimal price = new BigDecimal(signShopProperties.getSellPrice(), mathContext).divide(priceDivisor, mathContext);
        plugin.getEconomyHandler().addBalance(player, price.doubleValue());
        player.sendMessage(addPrefix(ChatColor.GREEN + "You have sold " + inventoryAmount + " " + signShopProperties.getSellType().getDisplayName()
                + " for $" + formatDouble(price.doubleValue()) + "!"));
        int linkedStockAmount = plugin.getLinkedStock().containsKey(signShopProperties) ? plugin.getLinkedStock().get(signShopProperties) : 0;
        plugin.getLinkedStock().put(signShopProperties, linkedStockAmount + inventoryAmount);
        if (plugin.linkSameOptionSigns()) {
            oldAmount = linkedStockAmount;
        }
        return oldAmount + inventoryAmount;
    }

    private int sellOneBatch(Player player, SignShopProperties signShopProperties, int oldAmount) {
        // amount is the amount specified on the sign, or the amount in hand, if the player has less than the
        // sign specifies
        int amount = signShopProperties.getSellType().removeOneBatchAndGetCount(player, signShopProperties.getAmount());
        if (amount == 0) {
            player.sendMessage(addPrefix(ChatColor.RED + "You don't have anything to sell!"));
            if (plugin.linkSameOptionSigns()) {
                return plugin.getLinkedStock().containsKey(signShopProperties) ? plugin.getLinkedStock().get(signShopProperties) : 0;
            } else {
                return oldAmount;
            }
        }
        // Number to divide the price by
        // if sell amount is 10 and amount is 5, this equals 2, and
        // price / 2 will give the price for 5 items.
        BigDecimal priceDivisor = new BigDecimal(signShopProperties.getAmount(), mathContext).divide(new BigDecimal(amount, mathContext), mathContext);
        BigDecimal price = new BigDecimal(signShopProperties.getSellPrice(), mathContext).divide(priceDivisor, mathContext);
        plugin.getEconomyHandler().addBalance(player, price.doubleValue());
        player.sendMessage(addPrefix(ChatColor.GREEN + "You have sold " + amount + " " + signShopProperties.getSellType().getDisplayName()
                + " for $" + formatDouble(price.doubleValue()) + "!"));
        int linkedStockAmount = plugin.getLinkedStock().containsKey(signShopProperties) ? plugin.getLinkedStock().get(signShopProperties) : 0;
        plugin.getLinkedStock().put(signShopProperties, linkedStockAmount + amount);
        if (plugin.linkSameOptionSigns()) {
            oldAmount = linkedStockAmount;
        }
        return oldAmount + amount;
    }

    private String formatDouble(double d) {
        String string = String.valueOf(d);
        if (string.endsWith(".0")) {
            string = string.replace(".0", "");
        }
        return string;
    }

    private SignShopProperties parseProperties(String[] lines) {
        SellType sellType = SellType.parseType(lines[1]);
        if (sellType == null) {
            return null;
        }
        String thirdLine = lines[2].replaceAll("( |\\$|:)", "");
        String[] split = thirdLine.split("B");
        int amount;
        double buyPrice;
        double sellPrice = -1;
        try {
            amount = Integer.parseInt(split[0]);
            if (split[1].contains("S")) {
                split = split[1].split("S");
                buyPrice = Double.parseDouble(split[0]);
                sellPrice = Double.parseDouble(split[1]);
            } else {
                buyPrice = Double.parseDouble(split[1]);
            }
        } catch (NumberFormatException e) {
            // TODO: handle this properly
            return null;
        }
        return new SignShopProperties(sellType, amount, buyPrice, sellPrice);
    }
}
