package com.bringholm.naturalsignshop.type;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

public abstract class SellType implements ConfigurationSerializable {
    public abstract void givePlayer(Player player, int amount);

    public abstract int getAvailableSpace(Player player);

    public abstract String getDisplayName();

    public abstract String getIdentifierName();

    public abstract String getShortIdentifierName();

    public abstract boolean shouldSell(Player player);

    public abstract int removeAllAndGetCount(Player player);

    public abstract int removeOneBatchAndGetCount(Player player, int originalAmount);

    public static SellType parseType(String string) {
        return ItemNameUtils.parseType(string);
        /* We can't use the experience type until we've good a good way to figure out whether it should sell or buy.
        if (string.toLowerCase().startsWith("xp") || string.toLowerCase().startsWith("experience")) {
            return ExperienceType.INSTANCE;
        } else {
            return ItemNameUtils.parseType(string);
        }
        */
    }
}
