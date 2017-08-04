package com.bringholm.naturalsignshop.type;

import com.bringholm.naturalsignshop.bukkitutils.ReflectUtil;
import com.google.common.collect.Maps;
import org.bukkit.Material;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * This is an attempt at getting both the localized names (IE 'Block of Iron') and the string IDs (IE 'iron_block')
 * using Reflection.
 */
public class ItemNameUtils {
    private static final Map<String, Material> NAME_TO_MATERIAL = Maps.newHashMap();
    private static final Map<Material, String> MATERIAL_TO_NAME = Maps.newHashMap();
    private static final Map<ItemType, String> ITEM_TYPE_TO_LOCALIZED_NAME = Maps.newHashMap();
    private static final Map<String, ItemType> LOCALIZED_NAME_TO_ITEM_TYPE = Maps.newHashMap();

    private static boolean reflectionFailed = false;

    static {
        try {
            Class<?> itemClass = ReflectUtil.getNMSClass("Item").getOrThrow();
            Class<?> itemStackClass = ReflectUtil.getNMSClass("ItemStack").getOrThrow();
            Class<?> registryMaterialsClass = ReflectUtil.getNMSClass("RegistryMaterials").getOrThrow();
            Class<?> craftMagicNumbersClass = ReflectUtil.getCBClass("util.CraftMagicNumbers").getOrThrow();
            Class<?> minecraftKeyClass = ReflectUtil.getNMSClass("MinecraftKey").getOrThrow();
            Field itemRegistryField = ReflectUtil.getFieldByType(itemClass, registryMaterialsClass
                    , 0).getOrThrow();
            Method getLocalizedNameMethod = ReflectUtil.getMethodByPredicate(itemClass, new ReflectUtil.MethodPredicate()
                    .withReturnType(String.class).withName("b").withParams(itemStackClass).withModifiers(Modifier.PUBLIC), 0).getOrThrow();
            Method getItemMethod = ReflectUtil.getMethodByPredicate(craftMagicNumbersClass, new ReflectUtil.MethodPredicate()
                    .withReturnType(itemClass).withParams(Material.class).withModifiers(Modifier.STATIC, Modifier.PUBLIC), 0).getOrThrow();
            Method getByKeyMethod = ReflectUtil.getMethodByPredicate(registryMaterialsClass, new ReflectUtil.MethodPredicate()
                    .withName("b").withReturnType(Object.class).withParams(Object.class).withModifiers(Modifier.PUBLIC), 0).getOrThrow();
            Method getKeyMethod = ReflectUtil.getMethod(minecraftKeyClass, "getKey").getOrThrow();
            Constructor<?> itemStackConstructor = ReflectUtil.getConstructor(itemStackClass, itemClass, int.class, int.class).getOrThrow();

            for (Material material : Material.values()) {
                Object item = ReflectUtil.invokeMethod(null, getItemMethod, material).getOrThrow();
                if (item == null) {
                    continue;
                }
                Object minecraftKey = ReflectUtil.invokeMethod(ReflectUtil.getFieldValue(null, itemRegistryField).getOrThrow(),
                        getByKeyMethod, item).getOrThrow();
                if (minecraftKey != null) {
                    String name = (String) ReflectUtil.invokeMethod(minecraftKey, getKeyMethod).getOrThrow();
                    NAME_TO_MATERIAL.put(name.replace("_", ""), material);
                    MATERIAL_TO_NAME.put(material, name);
                }
                // It's useless doing this on tools that use durability
                if (material.getMaxDurability() == 0) {
                    for (int i = 0; i < 16; i++) {
                        String localizedName = (String) ReflectUtil.invokeMethod(item, getLocalizedNameMethod,
                                ReflectUtil.invokeConstructor(itemStackConstructor, item, 1, i).getOrThrow()).getOrThrow();
                        if (localizedName != null && !ITEM_TYPE_TO_LOCALIZED_NAME.containsValue(localizedName)) {
                            ItemType itemType = new ItemType(material, (short) i);
                            ITEM_TYPE_TO_LOCALIZED_NAME.put(itemType, localizedName);
                            LOCALIZED_NAME_TO_ITEM_TYPE.put(localizedName.toLowerCase().replace(" ", ""), itemType);
                        }
                    }
                }
            }
        } catch (ReflectUtil.ReflectionException e) {
            // This could be handled better using the reflection responses, but I'm lazy.
            reflectionFailed = true;
            throw e;
        }
    }

    public static void init() {

    }

    public static ItemType parseType(String string) {
        if (string.startsWith("minecraft:")) {
            string = string.replace("minecraft:", "");
        }
        short damage = 0;
        if (string.contains(":")) {
            String[] split = string.split(":");
            try {
                damage = Short.parseShort(split[1]);
            } catch (NumberFormatException e) {
                damage = 0;
            }
            string = split[0];
        }
        if (reflectionFailed) {
            return new ItemType(Material.matchMaterial(string), damage);
        }
        ItemType itemType = LOCALIZED_NAME_TO_ITEM_TYPE.get(string.toLowerCase().replaceAll("(\\W| )", ""));
        if (itemType != null) {
            return itemType;
        }
        Material material = Material.matchMaterial(string);
        if (material == null) {
            material = NAME_TO_MATERIAL.get(string.toLowerCase().replaceAll("(\\W| )", ""));
        }
        if (material != null) {
            return new ItemType(material, damage);
        }
        return null;
    }

    public static String getLocalizedName(ItemType itemType) {
        if (reflectionFailed) {
            return itemType.getMaterial().name().toLowerCase() + (itemType.getDamage() != 0 ? ":" + itemType.getDamage() : "");
        }
        String displayName = ITEM_TYPE_TO_LOCALIZED_NAME.get(itemType);
        if (displayName == null) {
            return getName(itemType.getMaterial()) + (itemType.getDamage() != 0 ? ":" + itemType.getDamage() : "");
        }
        return displayName;
    }

    public static String getName(Material material) {
        if (reflectionFailed) {
            return material.name().toLowerCase();
        }
        String name = MATERIAL_TO_NAME.get(material);
        if (name == null) {
            return material.name().toLowerCase();
        }
        return name;
    }
}
