package me.nubb.gekkecore.files;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import me.nubb.gekkecore.GekkeCore;

import java.io.File;
import java.io.IOException;

public class Config {

    private static YamlDocument config;

    public static void load(GekkeCore plugin) throws IOException {

        config = YamlDocument.create(
                new File(plugin.getDataFolder(), "config.yml"),   // File on disk
                plugin.getResource("config.yml"),                 // Default in JAR
                GeneralSettings.builder().setUseDefaults(true).build(),
                UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build(),
                LoaderSettings.builder().build(),
                DumperSettings.builder().build(),
                UpdaterSettings.builder().setAutoSave(true).build() // <-- auto-merge + save
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
