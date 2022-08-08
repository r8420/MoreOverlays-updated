package at.ridgo8.moreoverlays;

import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsHandler;
import at.ridgo8.moreoverlays.lightoverlay.LightOverlayHandler;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MoreOverlays.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindings {

    static InputConstants.Key mappedKey(int key) {
        return InputConstants.Type.KEYSYM.getOrCreate(key);
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new KeyBindings());
    }

    public static RegisterKeyMappingsEvent currentEvent;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onKeyMapping(RegisterKeyMappingsEvent event) {
        currentEvent = event;
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> KeyBindings::registerKeyMappings);
    }

    private static void registerKeyMappings() {
        currentEvent.register(ClientRegistrationHandler.lightOverlayKeyMapping);
        currentEvent.register(ClientRegistrationHandler.chunkBoundsKeyMapping);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(receiveCanceled = true)
    public void onKeyEvent(InputEvent.Key event) {
        if (ClientRegistrationHandler.lightOverlayKeyMapping.isDown()) {
            LightOverlayHandler.setEnabled(!LightOverlayHandler.isEnabled());
        }

        if (ClientRegistrationHandler.chunkBoundsKeyMapping.isDown()) {
            ChunkBoundsHandler.toggleMode();
        }
    }
}
