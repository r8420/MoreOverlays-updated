package at.feldim2425.moreoverlays;

import at.feldim2425.moreoverlays.chunkbounds.ChunkBoundsHandler;
import at.feldim2425.moreoverlays.lightoverlay.LightOverlayHandler;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static KeyBinding lightOverlay = new KeyBinding("key." + MoreOverlays.MOD_ID + ".lightoverlay.desc", KeyConflictContext.IN_GAME, KeyBindings.mappedKey(GLFW.GLFW_KEY_F7), "key." + MoreOverlays.MOD_ID + ".category");
    public static KeyBinding chunkBounds = new KeyBinding("key." + MoreOverlays.MOD_ID + ".chunkbounds.desc", KeyConflictContext.IN_GAME, KeyBindings.mappedKey(GLFW.GLFW_KEY_F9), "key." + MoreOverlays.MOD_ID + ".category");

    private static InputMappings.Input mappedKey(final int key) {
        return InputMappings.Type.KEYSYM.getOrMakeInput(key);
    }

    public static void init() {
        ClientRegistry.registerKeyBinding(KeyBindings.lightOverlay);
        ClientRegistry.registerKeyBinding(KeyBindings.chunkBounds);

        MinecraftForge.EVENT_BUS.register(new KeyBindings());
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(receiveCanceled = true)
    public void onKeyEvent(final InputEvent.KeyInputEvent event) {
        if (KeyBindings.lightOverlay.isPressed()) {
            LightOverlayHandler.setEnabled(!LightOverlayHandler.isEnabled());
        }

        if (KeyBindings.chunkBounds.isPressed()) {
            ChunkBoundsHandler.toggleMode();
        }
    }
}
