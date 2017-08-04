package com.bringholm.naturalsignshop.type;

import com.google.common.collect.Maps;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

public class ItemType extends SellType {
    private Material material;
    private short damage;

    public ItemType(Material material, short damage) {
        this.material = material;
        this.damage = damage;
    }

    private ItemType(Map<String, Object> args) {
        this.material = Material.getMaterial((String) args.get("material"));
        this.damage = (short) ((Integer) args.get("damage")).intValue();
    }

    public static ItemType deserialize(Map<String, Object> args) {
        return new ItemType(args);
    }

    public short getDamage() {
        return damage;
    }

    public Material getMaterial() {
        return material;
    }

    public boolean checkType(ItemStack itemStack) {
        return itemStack != null && itemStack.getType() == material && itemStack.getDurability() == damage;
    }

    @Override
    public void givePlayer(Player player, int amount) {
        player.getInventory().addItem(getItemStacks(amount));
    }

    @Override
    public int getAvailableSpace(Player player) {
        return countSpacesInInventory(player);
    }

    @Override
    public String getDisplayName() {
        return ItemNameUtils.getLocalizedName(this);
    }

    @Override
    public String getIdentifierName() {
        return ItemNameUtils.getName(material) + (damage != 0 ? ":" + damage : "");
    }

    @Override
    public String getShortIdentifierName() {
        // Show the numeric ID, we have basically no other option
        //noinspection deprecation
        return material.getId() + (damage != 0 ? ":" + damage : "");
    }

    @Override
    public boolean shouldSell(Player player) {
        return checkType(player.getInventory().getItemInMainHand());
    }

    @Override
    public int removeAllAndGetCount(Player player) {
        int amount = 0;
        ListIterator<ItemStack> iterator = player.getInventory().iterator();
        for (ItemStack itemStack = null; iterator.hasNext(); itemStack = iterator.next()) {
            if (itemStack != null && checkType(itemStack)) {
                amount += itemStack.getAmount();
                iterator.set(null);
            }
        }
        return amount;
    }

    @Override
    public int removeOneBatchAndGetCount(Player player, int originalAmount) {
        int amount = Math.min(originalAmount, player.getInventory().getItemInMainHand().getAmount());
        if (player.getInventory().getItemInMainHand().getAmount() - amount <= 0) {
            player.getInventory().setItemInMainHand(null);
        } else {
            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - amount);
        }
        return amount;
    }

    private int countSpacesInInventory(Player player) {
        int space = 0;
        for (ItemStack itemStack : player.getInventory().getStorageContents()) {
            if (checkType(itemStack)) {
                // Handles full stack and stacks modified to be over their limit.
                if (itemStack.getAmount() < material.getMaxStackSize()) {
                    space += material.getMaxStackSize() - itemStack.getAmount();
                }
            } else if (itemStack == null) {
                space += material.getMaxStackSize();
            }
        }
        return space;
    }

    private ItemStack[] getItemStacks(int amount) {
        // amount % 64 == 0 ? 0 : 1 (add one if the last stack will not be complete)
        ItemStack[] stacks = new ItemStack[amount / material.getMaxStackSize() + (amount % material.getMaxStackSize() == 0 ? 0 : 1)];
        for (int i = 0; i < amount / material.getMaxStackSize(); i++) {
            stacks[i] = createStack(material.getMaxStackSize());
        }
        if (amount % material.getMaxStackSize() != 0) {
            stacks[stacks.length - 1] = createStack(amount % material.getMaxStackSize());
        }
        return stacks;
    }

    private ItemStack createStack(int amount) {
        return new ItemStack(material, amount, damage);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof ItemType)) {
            return false;
        } else {
            ItemType itemType = (ItemType) obj;
            return itemType.damage == this.damage && itemType.material == this.material;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, damage);
    }

    @Override
    public String toString() {
        return "ItemType{material=" + this.material + ",damage=" + this.damage + "}";
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("material", material.name());
        map.put("damage", damage);
        return map;
    }
}
