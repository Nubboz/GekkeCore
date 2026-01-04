package me.nubb.gekkecore.files;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import me.nubb.gekkecore.GekkeCore;

import java.io.File;
import java.io.IOException;

public class MessagesConfig {

    // ----------------------------------------------------------
    // This Plugin was made by Nubb__
    // for further help my discord is nubb__#0
    // verison: BETA 1.0
    // Please do not try and edit or republish this
    // ----------------------------------------------------------
    private static YamlDocument messagesconfig;

    public static void load(GekkeCore plugin) throws IOException {

        messagesconfig = YamlDocument.create(
                new File(plugin.getDataFolder(), "messages.yml"),   // File on disk
                plugin.getResource("messages.yml"),                 // Default in JAR


                GeneralSettings.builder()
                        .setUseDefaults(true)
                        .build(),
                DumperSettings.DEFAULT,
                LoaderSettings.builder()
                        .setAutoUpdate(true)
                        .build(),
                DumperSettings.builder().build(),

                UpdaterSettings.builder()
                        .setAutoSave(true)       // <-- Save after updating
                        .build()
        );
    }

    public static YamlDocument get() {
        return messagesconfig;
    }

    public static void save() throws IOException {
        messagesconfig.save();
    }

    public static void reload() throws IOException {
        messagesconfig.reload();   // Re-reads + re-merges defaults
    }


}
