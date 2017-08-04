package com.bringholm.naturalsignshop.type;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ExperienceType extends SellType {

    public static final ExperienceType INSTANCE = new ExperienceType();

    public static ExperienceType deserialize(Map<String, Object> args) {
        return INSTANCE;
    }

    @Override
    public void givePlayer(Player player, int amount) {
        player.setTotalExperience(player.getTotalExperience() + amount);
    }

    @Override
    public int getAvailableSpace(Player player) {
        return Integer.MAX_VALUE;
    }

    @Override
    public String getDisplayName() {
        return "Experience";
    }

    @Override
    public String getIdentifierName() {
        return "experience";
    }

    @Override
    public String getShortIdentifierName() {
        return "xp";
    }

    @Override
    public boolean shouldSell(Player player) {
        return false; // TODO: How should this be done?
    }

    @Override
    public int removeAllAndGetCount(Player player) {
        int amount = player.getTotalExperience();
        player.setTotalExperience(0);
        return amount;
    }

    @Override
    public int removeOneBatchAndGetCount(Player player, int originalAmount) {
        int amount = Math.min(originalAmount, player.getTotalExperience());
        player.setTotalExperience(player.getTotalExperience() - amount);
        return amount;
    }

    @Override
    public Map<String, Object> serialize() {
        //noinspection unchecked
        return Collections.EMPTY_MAP;
    }
}
