package net.nukebob.wardrobe.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.nukebob.wardrobe.Wardrobe;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SkinSettings {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File SKIN_SETTINGS_FILE = new File(Config.loadConfig().getSkinsDirectory() + "/wardrobe.json");
    private static SkinSettings skinSettings;

    public SkinFolder root = new SkinFolder();

    public static SkinSettings loadSettings() {
        if (!SKIN_SETTINGS_FILE.exists()) {
            skinSettings = new SkinSettings();

            File rootDir = Config.loadConfig().getSkinsDirectory();
            skinSettings.root = SkinFolder.buildFromDirectory(rootDir);
            saveSettings();
        } else {
            try (FileReader reader = new FileReader(SKIN_SETTINGS_FILE)) {
                skinSettings = GSON.fromJson(reader, SkinSettings.class);
            } catch (IOException e) {
                Wardrobe.LOGGER.error("Could not load config file", e);
            }
        }
        if (skinSettings == null) {
            skinSettings = new SkinSettings();
            skinSettings.root.name = "root";
            saveSettings();
        }
        return skinSettings;
    }

    public static void saveSettings() {
        if (!SKIN_SETTINGS_FILE.getParentFile().exists()) {
            SKIN_SETTINGS_FILE.getParentFile().mkdirs();
        }

        try (FileWriter writer = new FileWriter(SKIN_SETTINGS_FILE)) {
            GSON.toJson(skinSettings, writer);
        } catch (IOException e) {
            Wardrobe.LOGGER.error("Could not save skin settings file", e);
        }
    }
}
