package me.nubb.gekkecore.files;

import me.nubb.gekkecore.GekkeCore;
import me.nubb.gekkecore.Util.ConfigUpdater;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MessagesConfig {

    // ----------------------------------------------------------
    // This Plugin was made by Nubb__
    // for further help my discord is nubb__#0
    // verison: BETA 1.0
    // Please do not try and edit or republish this
    // ----------------------------------------------------------

    private static File file;
    private static FileConfiguration fileconfig;
    private static GekkeCore plugin = GekkeCore.getPlugin(GekkeCore.class);

    public static FileConfiguration getConfig() {
        return fileconfig;
    }

    public void saveConfig() throws InvalidConfigurationException {
        try {
            fileconfig.save(file);
            Bukkit.getConsoleSender().sendMessage("Configuration has been saved!");
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage("We can't reload your configuration!");
            e.printStackTrace();
        }
    }

    public static void updateConfig() {
        file = new File(plugin.getDataFolder(), "messages.yml");
        try {
            ConfigUpdater.update(plugin, "messages.yml", file, Arrays.asList());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void createConfig() {

        file = new File(plugin.getDataFolder(), "messages.yml");

        if (!file.exists()) {
            try {

                file.getParentFile().mkdir();
                plugin.saveResource("messages.yml", false);

            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage("");
                e.printStackTrace();
            }
        }

        fileconfig = new YamlConfiguration();
        try {
            fileconfig.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getConsoleSender().sendMessage("Error in trying to load the configuration!");
            e.printStackTrace();
        }

    }
    public static void reloadConfig() {
        fileconfig = new YamlConfiguration();
        try {
            fileconfig.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getConsoleSender().sendMessage("§c[Config] Your config is incorrect!");
            e.printStackTrace();
        }

    }


}
