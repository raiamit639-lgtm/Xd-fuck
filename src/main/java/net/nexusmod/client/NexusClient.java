package net.nexusmod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.nexusmod.client.config.NexusConfig;
import net.nexusmod.client.gui.loader.NexusLoaderScreen;
import net.nexusmod.client.perf.NexusOptionsSync;
import org.lwjgl.glfw.GLFW;

/**
 * Entrypoint for the Nexus client mod. Registers the config, the
 * keybinding used to open the settings dashboard, and hooks into the
 * client tick loop for anything that needs polling (e.g. GUI animation
 * state, live perf sampling).
 */
public class NexusClient implements ClientModInitializer {

    public static final String MOD_ID = "nexus";

    private static KeyBinding openSettingsKey;

    @Override
    public void onInitializeClient() {
        // Load config eagerly so defaults are written to disk on first run,
        // then immediately push the vanilla-equivalent settings (render
        // distance, max FPS, vsync, particles, graphics quality) onto
        // GameOptions so the config takes effect from the very first frame,
        // not just after the dashboard is opened once.
        NexusConfig.get();
        NexusOptionsSync.apply();

        openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nexus.open_settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.nexus.general"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openSettingsKey.wasPressed() && client.currentScreen == null) {
                client.setScreen(new NexusLoaderScreen(null));
            }
        });

        System.out.println("[Nexus] Initialized. Press N in-game to open the settings dashboard.");
    }

    public static MinecraftClient client() {
        return MinecraftClient.getInstance();
    }
}
