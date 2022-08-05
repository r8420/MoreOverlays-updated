package at.ridgo8.moreoverlays.lightoverlay;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightRenderer;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightScanner;
import at.ridgo8.moreoverlays.api.lightoverlay.LightOverlayReloadHandlerEvent;
import at.ridgo8.moreoverlays.config.Config;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.message.FormattedMessage;

public class LightOverlayHandler {

    private static boolean enabled = false;
    public static ILightRenderer renderer = null;
    public static ILightScanner scanner = null;

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new LightOverlayHandler());
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        if (LightOverlayHandler.enabled == enabled) {
            return;
        }

        if (enabled) {
            reloadHandlerInternal();
        } else {
            scanner.clear();
        }
        LightOverlayHandler.enabled = enabled;
    }

    public static void reloadHandler() {
        if (enabled) {
            MoreOverlays.logger.info("Light overlay handlers reloaded");
            reloadHandlerInternal();
        }
    }

    private static void reloadHandlerInternal() {
        LightOverlayReloadHandlerEvent event = new LightOverlayReloadHandlerEvent(Config.light_IgnoreSpawnList.get(), LightOverlayRenderer.class, LightScannerVanilla.class);
        MinecraftForge.EVENT_BUS.post(event);

        if (renderer == null || renderer.getClass() != event.getRenderer()) {
            try {
                renderer = event.getRenderer().newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                MoreOverlays.logger.warn(new FormattedMessage("Could not create ILightRenderer from type \"%s\"!", event.getRenderer().getName()), e);
                renderer = new LightOverlayRenderer();
            }
        }

        if (scanner == null || scanner.getClass() != event.getScanner()) {
            if (scanner != null && enabled) {
                scanner.clear();
            }

            try {
                scanner = event.getScanner().newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                MoreOverlays.logger.warn(new FormattedMessage("Could not create ILightScanner from type \"%s\"!", event.getScanner().getName()), e);
                scanner = new LightScannerVanilla();
            }
        }
    }
    @SubscribeEvent
    public void onWorldUnload(final LevelEvent.Unload event) {
        setEnabled(false);
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderLevelStageEvent event) {
        if(!event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_PARTICLES)) return;

        if (enabled &&  Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FABULOUS) {
            renderer.renderOverlays(scanner, event.getPoseStack());
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().player != null && enabled && event.phase == TickEvent.Phase.END &&
                (Minecraft.getInstance().screen == null || !Minecraft.getInstance().screen.isPauseScreen())) {
            scanner.update(Minecraft.getInstance().player);
        }
    }
}
