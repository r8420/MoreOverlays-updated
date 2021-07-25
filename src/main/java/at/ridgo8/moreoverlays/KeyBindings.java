package at.ridgo8.moreoverlays;

import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsHandler;
import at.ridgo8.moreoverlays.lightoverlay.LightOverlayHandler;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static KeyMapping lightOverlay = new KeyMapping("key." + MoreOverlays.MOD_ID + ".lightoverlay.desc", KeyConflictContext.IN_GAME, mappedKey(GLFW.GLFW_KEY_F7), "key." + MoreOverlays.MOD_ID + ".category");
    public static KeyMapping chunkBounds = new KeyMapping("key." + MoreOverlays.MOD_ID + ".chunkbounds.desc", KeyConflictContext.IN_GAME, mappedKey(GLFW.GLFW_KEY_F9), "key." + MoreOverlays.MOD_ID + ".category");

    private static InputConstants.Key mappedKey(int key) {
        return InputConstants.Type.KEYSYM.getOrCreate(key);
    }

    public static void init() {
        ClientRegistry.registerKeyBinding(lightOverlay);
        ClientRegistry.registerKeyBinding(chunkBounds);

        MinecraftForge.EVENT_BUS.register(new KeyBindings());
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(receiveCanceled = true)
    public void onKeyEvent(InputEvent.KeyInputEvent event) {
        if (lightOverlay.isDown()) {
            LightOverlayHandler.setEnabled(!LightOverlayHandler.isEnabled());
        }

        if (chunkBounds.isDown()) {
            ChunkBoundsHandler.toggleMode();
        }
    }
}
