package at.ridgo8.moreoverlays;

import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsHandler;
import at.ridgo8.moreoverlays.lightoverlay.LightOverlayHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

public class KeyBindings {

    public static void init() {

		// Create a custom category for your mod's keybindings
		KeyMapping.Category category = new KeyMapping.Category(
				Identifier.fromNamespaceAndPath(MoreOverlays.MOD_ID, "category")
		);

		KeyMapping lightOverlayKeyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key." + MoreOverlays.MOD_ID + ".lightoverlay.desc", // The translation key of the keybinding's name
			InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
			GLFW.GLFW_KEY_F7, // The keycode of the key
			category // The translation key of the keybinding's category.
		));
	
		KeyMapping chunkBoundsKeyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key." + MoreOverlays.MOD_ID + ".chunkbounds.desc", // The translation key of the keybinding's name
			InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
			GLFW.GLFW_KEY_F9, // The keycode of the key
			category // The translation key of the keybinding's category.
		));


		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (chunkBoundsKeyMapping.consumeClick()) {
				ChunkBoundsHandler.toggleMode();
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (lightOverlayKeyMapping.consumeClick()) {
				LightOverlayHandler.setEnabled(!LightOverlayHandler.isEnabled());
			}
		});
    }
}
