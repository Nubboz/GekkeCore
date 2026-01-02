package me.nubb.anticore.files;

import me.nubb.anticore.AntiCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class Config {

    private static final AntiCore plugin = AntiCore.getPlugin(AntiCore.class);

    private static File configFile;
    private static FileConfiguration config;

    /** Get the loaded config */
    public static FileConfiguration getConfig() {
        return config;
    }

    /** Create or load config with defaults merged */
    public static void createConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");

        // Save default resource if missing
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = new YamlConfiguration();
        try {
            config.load(configFile); // load existing config
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getConsoleSender().sendMessage("§c[Config] Failed to load config!");
            e.printStackTrace();
        }

        // Merge defaults from default config.yml recursively
        mergeDefaults();

        saveCf(); // Save to file so missing keys appear
    }

    /** Reload config and merge defaults */
    public static void reloadConfig() {
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getConsoleSender().sendMessage("§c[Config] Failed to reload config!");
            e.printStackTrace();
        }

        mergeDefaults();
        saveCf();
    }

    /** Save config */
    public static boolean saveCf() {
        try {
            config.save(configFile);
            return true;
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage("§c[Config] Could not save config!");
            e.printStackTrace();
            return false;
        }
    }

    /** Merge defaults from plugin resource recursively */
    private static void mergeDefaults() {
        try (Reader defStream = new InputStreamReader(plugin.getResource("config.yml"))) {
            if (defStream == null) return;
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defStream);
            mergeSections(defConfig, (YamlConfiguration) config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Recursive merge helper */
    private static void mergeSections(YamlConfiguration defaults, YamlConfiguration target) {
        for (String key : defaults.getKeys(true)) {
            if (!target.contains(key)) {
                target.set(key, defaults.get(key));
            }
        }
    }
}
