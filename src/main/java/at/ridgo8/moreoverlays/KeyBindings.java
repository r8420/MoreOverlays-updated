package at.ridgo8.moreoverlays;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;


@EventBusSubscriber(value = Dist.CLIENT, modid = MoreOverlays.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class KeyBindings {

    // Lazy initialization for key mappings
    public static final Lazy<KeyMapping> lightOverlayKeyMapping = Lazy.of(() ->
        new KeyMapping("key." + MoreOverlays.MOD_ID + ".lightoverlay.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F7,
            "key." + MoreOverlays.MOD_ID + ".category"));

    public static final Lazy<KeyMapping> chunkBoundsKeyMapping = Lazy.of(() ->
        new KeyMapping("key." + MoreOverlays.MOD_ID + ".chunkbounds.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            "key." + MoreOverlays.MOD_ID + ".category"));

    // Remove any manual event bus registration from this class
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(lightOverlayKeyMapping.get());
        event.register(chunkBoundsKeyMapping.get());
    }
}