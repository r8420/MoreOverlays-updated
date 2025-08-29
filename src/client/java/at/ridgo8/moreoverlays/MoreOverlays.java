package at.ridgo8.moreoverlays;

import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsHandler;
import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsHandler.RenderMode;
import at.ridgo8.moreoverlays.config.ConfigManager;
import at.ridgo8.moreoverlays.lightoverlay.LightOverlayHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;



public class MoreOverlays implements ClientModInitializer {

	public static final String MOD_ID = "moreoverlays";
    public static final String NAME = "MoreOverlays";

	public static Logger logger = LogManager.getLogger(NAME);

	@Override
	public void onInitializeClient() {

		// Correctly register the event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return; // Ensures the player is not null
			ChunkBoundsHandler.updateRegionInfo();

            LightOverlayHandler.onClientTick();
        });

		ClientLifecycleEvents.CLIENT_STOPPING.register(client1 -> {
            LightOverlayHandler.setEnabled(false);
            ChunkBoundsHandler.setMode(RenderMode.NONE);
        });


		ConfigManager.CONFIG.load();
        ClientRegistrationHandler.setupClient();
	}
}