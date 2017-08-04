package com.bringholm.naturalsignshop.bukkitutils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigurationHandler {
    private String configName;
    private Plugin plugin;
    private FileConfiguration config = null;
    private File configFile = null;

    public ConfigurationHandler(String name, Plugin plugin) {
        this.configName = name;
        this.plugin = plugin;
    }

    public ConfigurationHandler(File file, Plugin plugin) {
        this.configFile = file;
        this.plugin = plugin;
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    private void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), configName);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig() {
        if (configFile == null || config == null) {
            return;
        }
        try {
            getConfig().save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + configFile, e);
        }
    }
}
