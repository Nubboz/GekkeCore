package me.nubb.gekkecore.files;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import me.nubb.gekkecore.GekkeCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

public class Config {

    private static YamlDocument config;

    public static void load(GekkeCore plugin) throws IOException {

        Reader defaultConfigStream = new InputStreamReader( plugin.getResource("config.yml"));
        YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigStream);
        String jarVersion = defaultConfig.getString("config-version", "1"); // version in JAR


        UpdaterSettings updaterSettings = UpdaterSettings.builder()
                .setVersioning(new BasicVersioning("config-version"))
                .addIgnoredRoute(jarVersion, Route.fromString("Settings.VillagerTrades.AddedTrades", '.'))
                .setAutoSave(true)
                .build();

        config = YamlDocument.create(
                new File(plugin.getDataFolder(), "config.yml"),
                plugin.getResource("config.yml"),
                GeneralSettings.builder().setUseDefaults(true).build(),
                updaterSettings,
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT
        );

    }

    public static YamlDocument get() {
        return config;
    }

    public static void save() throws IOException {
        config.save();
    }

    public static void reload() throws IOException {
        config.reload();   // Re-reads + re-merges defaults
    }
}
