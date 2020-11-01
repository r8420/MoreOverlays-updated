package at.feldim2425.moreoverlays.lightoverlay;

import at.feldim2425.moreoverlays.MoreOverlays;
import at.feldim2425.moreoverlays.api.lightoverlay.ILightRenderer;
import at.feldim2425.moreoverlays.api.lightoverlay.ILightScanner;
import at.feldim2425.moreoverlays.api.lightoverlay.LightOverlayReloadHandlerEvent;
import at.feldim2425.moreoverlays.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.message.FormattedMessage;

public class LightOverlayHandler {

    private static boolean enabled;
    private static ILightRenderer renderer;
    private static ILightScanner scanner;

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new LightOverlayHandler());
    }

    public static boolean isEnabled() {
        return LightOverlayHandler.enabled;
    }

    public static void setEnabled(final boolean enabled) {
        if (LightOverlayHandler.enabled == enabled) {
            return;
        }

        if (enabled) {
            LightOverlayHandler.reloadHandlerInternal();
        } else {
            LightOverlayHandler.scanner.clear();
        }
        LightOverlayHandler.enabled = enabled;
    }

    public static void reloadHandler() {
        if (LightOverlayHandler.enabled) {
            MoreOverlays.logger.info("Light overlay handlers reloaded");
            LightOverlayHandler.reloadHandlerInternal();
        }
    }

    private static void reloadHandlerInternal() {
        final LightOverlayReloadHandlerEvent event = new LightOverlayReloadHandlerEvent(Config.light_IgnoreSpawnList.get(), LightOverlayRenderer.class, LightScannerVanilla.class);
        MinecraftForge.EVENT_BUS.post(event);

        if (LightOverlayHandler.renderer == null || LightOverlayHandler.renderer.getClass() != event.getRenderer()) {
            try {
                LightOverlayHandler.renderer = event.getRenderer().newInstance();
            } catch (final IllegalAccessException | InstantiationException e) {
                MoreOverlays.logger.warn(new FormattedMessage("Could not create ILightRenderer from type \"%s\"!", event.getRenderer().getName()), e);
                LightOverlayHandler.renderer = new LightOverlayRenderer();
            }
        }

        if (LightOverlayHandler.scanner == null || LightOverlayHandler.scanner.getClass() != event.getScanner()) {
            if (LightOverlayHandler.scanner != null && LightOverlayHandler.enabled) {
                LightOverlayHandler.scanner.clear();
            }

            try {
                LightOverlayHandler.scanner = event.getScanner().newInstance();
            } catch (final IllegalAccessException | InstantiationException e) {
                MoreOverlays.logger.warn(new FormattedMessage("Could not create ILightScanner from type \"%s\"!", event.getScanner().getName()), e);
                LightOverlayHandler.scanner = new LightScannerVanilla();
            }
        }
    }

    @SubscribeEvent
    public void renderWorldLastEvent(final RenderWorldLastEvent event) {
        if (LightOverlayHandler.enabled) {
            LightOverlayHandler.renderer.renderOverlays(LightOverlayHandler.scanner);

        }
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        if (Minecraft.getInstance().world != null && Minecraft.getInstance().player != null && LightOverlayHandler.enabled && event.phase == TickEvent.Phase.END &&
                (Minecraft.getInstance().currentScreen == null || !Minecraft.getInstance().currentScreen.isPauseScreen())) {
            LightOverlayHandler.scanner.update(Minecraft.getInstance().player);
        }

    }
}
