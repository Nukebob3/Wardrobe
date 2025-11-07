package net.nukebob.wardrobe;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Wardrobe implements ClientModInitializer {
	public static final String MOD_ID = "wardrobe";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static float TICKS = 0;

	@Override
	public void onInitializeClient() {

		LOGGER.info("Loaded Wardrobe!");
	}
}