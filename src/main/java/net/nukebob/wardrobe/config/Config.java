package net.nukebob.wardrobe.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import net.nukebob.wardrobe.Wardrobe;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(MinecraftClient.getInstance().runDirectory + "/wardrobe/config.json");
    private static Config config;

    public String skinsDirectory = "/wardrobe/skins/";

    public static Config loadConfig() {
        if (!CONFIG_FILE.exists()) {
            config = new Config();
            saveConfig();
        } else {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, Config.class);
            } catch (IOException e) {
                Wardrobe.LOGGER.error("Could not load config file", e);
            }
        }
        if (config == null) {
            config = new Config();
            saveConfig();
        }
        return config;
    }

    public static void saveConfig() {
        if (!CONFIG_FILE.getParentFile().exists()) {
            CONFIG_FILE.getParentFile().mkdirs();
        }

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            Wardrobe.LOGGER.error("Could not save config file", e);
        }
    }
}
