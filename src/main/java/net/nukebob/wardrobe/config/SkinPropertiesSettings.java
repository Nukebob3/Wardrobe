package net.nukebob.wardrobe.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.nukebob.wardrobe.Wardrobe;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SkinPropertiesSettings {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File SKIN_SETTINGS_FILE = new File(Config.loadConfig().getSkinsDirectory() + "/wardrobe_properties.json");
    private static SkinPropertiesSettings skinPropertiesSettings;

    public Map<String, SkinData> properties = new HashMap<>();

    public static SkinPropertiesSettings loadSettings() {
        if (!SKIN_SETTINGS_FILE.exists()) {
            skinPropertiesSettings = new SkinPropertiesSettings();

            File rootDir = Config.loadConfig().getSkinsDirectory();
            //skinPropertiesSettings.properties = SkinFolder.buildFromDirectory(rootDir);
            saveSettings();
        } else {
            try (FileReader reader = new FileReader(SKIN_SETTINGS_FILE)) {
                skinPropertiesSettings = GSON.fromJson(reader, SkinPropertiesSettings.class);
            } catch (IOException e) {
                Wardrobe.LOGGER.error("Could not load config file", e);
            }
        }
        if (skinPropertiesSettings == null) {
            skinPropertiesSettings = new SkinPropertiesSettings();
            saveSettings();
        }
        return skinPropertiesSettings;
    }

    public static void saveSettings() {
        if (!SKIN_SETTINGS_FILE.getParentFile().exists()) {
            SKIN_SETTINGS_FILE.getParentFile().mkdirs();
        }

        try (FileWriter writer = new FileWriter(SKIN_SETTINGS_FILE)) {
            GSON.toJson(skinPropertiesSettings, writer);
        } catch (IOException e) {
            Wardrobe.LOGGER.error("Could not save skin settings file", e);
        }
    }
}
