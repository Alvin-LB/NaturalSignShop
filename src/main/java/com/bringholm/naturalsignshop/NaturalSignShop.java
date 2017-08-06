package com.bringholm.naturalsignshop;

import com.bringholm.naturalsignshop.bukkitutils.ConfigurationHandler;
import com.bringholm.naturalsignshop.bukkitutils.ReflectUtil;
import com.bringholm.naturalsignshop.constants.Permissions;
import com.bringholm.naturalsignshop.data.SignShopProperties;
import com.bringholm.naturalsignshop.economy.EconomyHandler;
import com.bringholm.naturalsignshop.listener.SignListener;
import com.bringholm.naturalsignshop.type.ExperienceType;
import com.bringholm.naturalsignshop.type.ItemNameUtils;
import com.bringholm.naturalsignshop.type.ItemType;
import com.bringholm.naturalsignshop.type.SellType;
import com.google.common.collect.Maps;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;

public class NaturalSignShop extends JavaPlugin {

    private EconomyHandler economyHandler;
    private Map<SignShopProperties, Integer> linkedStock = Maps.newHashMap();
    private boolean linkSameOptionSigns;

    @Override
    public void onEnable() {
        // IMPORTANT: Any subclasses of SellType must be registered here!
        ConfigurationSerialization.registerClass(ItemType.class);
        ConfigurationSerialization.registerClass(ExperienceType.class);
        ConfigurationSerialization.registerClass(SignShopProperties.class);
        reloadConfigurationValues();
        if (!this.getServer().getPluginManager().isPluginEnabled("Vault")) {
            this.getLogger().severe("Could not find Vault! Please install a working version of Vault, otherwise NaturalSignShop won't work!");
            this.setEnabled(false);
            return;
        }
        RegisteredServiceProvider<Economy> serviceProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (serviceProvider == null || serviceProvider.getProvider() == null) {
            throw new IllegalStateException("Failed to retrieve Economy Service from Vault! Are you sure you have an Economy plugin installed?");
        }
        economyHandler = new EconomyHandler(serviceProvider.getProvider());
        SignListener signListener = new SignListener(this);
        this.getServer().getPluginManager().registerEvents(signListener, this);
        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SimplePie("link-same-option-signs", () -> linkSameOptionSigns ? "enabled" : "disabled"));
        metrics.addCustomChart(new Metrics.SimplePie("economy-plugin", () -> economyHandler.getEconomyPlugin()));
        try {
            ItemNameUtils.init();
        } catch (ReflectUtil.ReflectionException e) {
            this.getLogger().log(Level.WARNING, "Failed to initialize NMS provider for localized names and string ids!", e);
        }
    }

    @Override
    public void onDisable() {
        saveConfigValues();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(addPrefix(ChatColor.RED + "Incorrect arguments!"));
            return true;
        }
        if (!sender.hasPermission(Permissions.RELOAD_PERMISSION)) {
            sender.sendMessage(addPrefix(ChatColor.RED + "You don't have permission to do this!"));
            return true;
        }
        reloadConfigurationValues();
        sender.sendMessage(addPrefix(ChatColor.GREEN + "Reloaded configuration files!"));
        return true;
    }

    private void reloadConfigurationValues() {
        saveDefaultConfig();
        reloadConfig();
        linkSameOptionSigns = getConfig().getBoolean("link-same-option-signs");
        linkedStock.clear();
        // This is kind of a bad system, but YAML does not allow for serialization of Maps with anything other than Strings as keys.
        // Of course, a string parser for keys could be implemented, but that would probably be worse.
        ConfigurationHandler configurationHandler = new ConfigurationHandler("data/linked-sign-stock.yml", this);
        for (String string : configurationHandler.getConfig().getKeys(false)) {
            linkedStock.put((SignShopProperties) configurationHandler.getConfig().get(string + ".properties"), configurationHandler.getConfig().getInt(string + ".amount"));
        }
    }

    private void saveConfigValues() {
        int index = 0; // What this is doesn't really matter, just that it's unique for each entry.
        ConfigurationHandler configurationHandler = new ConfigurationHandler("data/linked-sign-stock.yml", this);
        for (Map.Entry<SignShopProperties, Integer> entry : linkedStock.entrySet()) {
            configurationHandler.getConfig().set(index + ".properties", entry.getKey());
            configurationHandler.getConfig().set(index + ".amount", entry.getValue());
            index++;
        }
        configurationHandler.saveConfig();
    }

    public static String addPrefix(String string) {
        return ChatColor.BLUE + "[" + ChatColor.GOLD + "NaturalSignShop" + ChatColor.BLUE + "] " + string;
    }

    public Map<SignShopProperties, Integer> getLinkedStock() {
        return linkedStock;
    }

    public boolean linkSameOptionSigns() {
        return linkSameOptionSigns;
    }

    public EconomyHandler getEconomyHandler() {
        return economyHandler;
    }
}
