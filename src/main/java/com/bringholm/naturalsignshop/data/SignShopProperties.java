package com.bringholm.naturalsignshop.data;

import com.bringholm.naturalsignshop.type.SellType;
import com.google.common.collect.Maps;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Map;
import java.util.Objects;

public class SignShopProperties implements ConfigurationSerializable {
    private SellType sellType;
    private int amount;
    private double buyPrice;
    private double sellPrice;

    public SignShopProperties(SellType sellType, int amount, double buyPrice, double sellPrice) {
        this.sellType = sellType;
        this.amount = amount;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    private SignShopProperties(Map<String, Object> args) {
        this.sellType = (SellType) args.get("sellType");
        this.amount = (int) args.get("amount");
        this.buyPrice = (double) args.get("buyPrice");
        this.sellPrice = (double) args.get("sellPrice");
    }

    public static SignShopProperties deserialize(Map<String, Object> args) {
        return new SignShopProperties(args);
    }

    public SellType getSellType() {
        return sellType;
    }

    public int getAmount() {
        return amount;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof SignShopProperties)) {
            return false;
        } else {
            SignShopProperties properties = (SignShopProperties) obj;
            return properties.sellType.equals(sellType) && properties.amount == amount && properties.buyPrice == buyPrice && properties.sellPrice == sellPrice;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(sellType, amount, buyPrice, sellPrice);
    }

    @Override
    public String toString() {
        return "SignShopProperties{sellType=" + sellType + ",amount=" + amount + ",buyPrice=" + buyPrice + ",sellPrice=" + sellPrice + "}";
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("sellType", sellType);
        map.put("amount", amount);
        map.put("buyPrice", buyPrice);
        map.put("sellPrice", sellPrice);
        return map;
    }
}
