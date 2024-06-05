package de.srendi.advancedperipherals;

import net.fabricmc.api.ModInitializer;

import de.srendi.advancedperipherals.common.addons.APAddons;
import de.srendi.advancedperipherals.common.configuration.APConfig;
import de.srendi.advancedperipherals.common.setup.Registration;
import de.srendi.advancedperipherals.network.APNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class AdvancedPeripherals implements ModInitializer {

    public static final String MOD_ID = "advancedperipherals";
    public static final String NAME = "Advanced Peripherals";
    public static final Logger LOGGER = LogManager.getLogger(NAME);
    public static final Random RANDOM = new Random();
    private static MinecraftServer SERVER = null;

    public AdvancedPeripherals() {
        LOGGER.info("AdvancedPeripherals says hello!");

        APConfig.register(ModLoadingContext.get());

        Registration.register();
    }

    public static void debug(String message) {
        if (APConfig.GENERAL_CONFIG.enableDebugMode.get())
            LOGGER.debug("[DEBUG] {}", message);
    }

    public static void debug(String message, Level level) {
        if (APConfig.GENERAL_CONFIG.enableDebugMode.get())
            LOGGER.log(level, "[DEBUG] {}", message);
    }

    public static ResourceLocation getRL(String resource) {
        return new ResourceLocation(MOD_ID, resource);
    }

    public static MinecraftServer getServer() {
        return SERVER;
    }

    @Override
    public void onInitialize() {
        APAddons.commonSetup();
        APNetworking.registerServerSide();
        ServerTickEvents.START_SERVER_TICK.register((server) -> {
            if (server != null && SERVER == null) {
                SERVER = server;
            }
        });
    }

    public String getVersion() {
        return FabricLoader.getInstance().getModContainer(MOD_ID).get().getModInfo().getVersion().toString();
    }
}
