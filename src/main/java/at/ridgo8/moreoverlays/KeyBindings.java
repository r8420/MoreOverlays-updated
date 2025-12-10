package at.ridgo8.moreoverlays;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;

public class KeyBindings {

    // Lazy initialization for key mappings
    private static final Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MoreOverlays.MOD_ID, "main"));

    public static final Lazy<KeyMapping> lightOverlayKeyMapping = Lazy.of(() ->
        new KeyMapping("key." + MoreOverlays.MOD_ID + ".lightoverlay.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F7,
            CATEGORY));

    public static final Lazy<KeyMapping> chunkBoundsKeyMapping = Lazy.of(() ->
        new KeyMapping("key." + MoreOverlays.MOD_ID + ".chunkbounds.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            CATEGORY));

    // Remove any manual event bus registration from this class
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(lightOverlayKeyMapping.get());
        event.register(chunkBoundsKeyMapping.get());
    }
}