package com.bringholm.naturalsignshop.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class EconomyHandler {

    private Economy economy;

    public EconomyHandler(Economy economy) {
        this.economy = economy;
    }

    public boolean doesPlayerHave(Player player, double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < 0) {
            throw new IllegalArgumentException("amount is not a valid number (" + amount + ")");
        }
        return economy.has(player, amount);
    }

    public void withdrawBalance(Player player, double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < 0) {
            throw new IllegalArgumentException("amount is not a valid number (" + amount + ")");
        }
        economy.withdrawPlayer(player, amount);
    }

    public void addBalance(Player player, double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < 0) {
            throw new IllegalArgumentException("amount is not a valid number (" + amount + ")");
        }
        economy.depositPlayer(player, amount);
    }

    public int getCurrencyScale() {
        // No use returning anything greater than 11, since that's the maximum our format allows on a sign.
        // 1B1S.00000000001 (the longest number displayable on the signs) has 11 digits.
        int vaultScale = economy.fractionalDigits();
        if (vaultScale == -1) {
            return 11;
        }
        return Math.min(11, vaultScale);
    }

    public double getBalance(Player player) {
        return economy.getBalance(player);
    }

    public String getEconomyPlugin() {
        return economy.getName();
    }
}
