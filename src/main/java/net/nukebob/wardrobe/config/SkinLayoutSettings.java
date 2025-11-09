package net.nukebob.wardrobe.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.nukebob.wardrobe.Wardrobe;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SkinLayoutSettings {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File SKIN_SETTINGS_FILE = new File(Config.loadConfig().getSkinsDirectory() + "/wardrobe_layout.json");
    private static SkinLayoutSettings skinLayoutSettings;

    public SkinFolder root = new SkinFolder();

    public static SkinLayoutSettings loadSettings() {
        if (!SKIN_SETTINGS_FILE.exists()) {
            skinLayoutSettings = new SkinLayoutSettings();

            File rootDir = Config.loadConfig().getSkinsDirectory();
            skinLayoutSettings.root = SkinFolder.buildFromDirectory(rootDir);
            saveSettings();
        } else {
            try (FileReader reader = new FileReader(SKIN_SETTINGS_FILE)) {
                skinLayoutSettings = GSON.fromJson(reader, SkinLayoutSettings.class);
            } catch (IOException e) {
                Wardrobe.LOGGER.error("Could not load config file", e);
            }
        }
        if (skinLayoutSettings == null) {
            skinLayoutSettings = new SkinLayoutSettings();
            skinLayoutSettings.root.name = "root";
            saveSettings();
        }
        return skinLayoutSettings;
    }

    public static void saveSettings() {
        if (!SKIN_SETTINGS_FILE.getParentFile().exists()) {
            SKIN_SETTINGS_FILE.getParentFile().mkdirs();
        }

        try (FileWriter writer = new FileWriter(SKIN_SETTINGS_FILE)) {
            GSON.toJson(skinLayoutSettings, writer);
        } catch (IOException e) {
            Wardrobe.LOGGER.error("Could not save skin settings file", e);
        }
    }
}
