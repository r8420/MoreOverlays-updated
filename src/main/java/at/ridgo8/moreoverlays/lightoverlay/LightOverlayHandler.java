package at.ridgo8.moreoverlays.lightoverlay;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightRenderer;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightScanner;
import at.ridgo8.moreoverlays.api.lightoverlay.LightOverlayReloadHandlerEvent;
import at.ridgo8.moreoverlays.config.Config;
import at.ridgo8.moreoverlays.lightoverlay.render.CrossOverlayRenderer;
import at.ridgo8.moreoverlays.lightoverlay.render.NumberOverlayRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

import net.minecraft.network.chat.Component;

import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.message.FormattedMessage;

public class LightOverlayHandler {

    private static boolean enabled = false;
    public static ILightRenderer renderer = null;
    public static ILightScanner scanner = null;

    // Throttling + mode tracking
    private static long clientTickCounter = 0L;
    private static int lastPlayerBlockX = Integer.MIN_VALUE;
    private static int lastPlayerBlockY = Integer.MIN_VALUE;
    private static int lastPlayerBlockZ = Integer.MIN_VALUE;
    private static float lastPlayerYaw = Float.NaN;
    private static boolean lastRenderNumbers = false;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        if (LightOverlayHandler.enabled == enabled) {
            return;
        }

        if (enabled) {
            reloadHandlerInternal();
            Minecraft.getInstance().player.sendOverlayMessage(Component.nullToEmpty(ChatFormatting.YELLOW + "Light Overlay Enabled"));
            if (Minecraft.getInstance().player != null) {
                scanner.update(Minecraft.getInstance().player);
                net.minecraft.core.BlockPos bp = Minecraft.getInstance().player.blockPosition();
                lastPlayerBlockX = bp.getX();
                lastPlayerBlockY = bp.getY();
                lastPlayerBlockZ = bp.getZ();
                lastPlayerYaw = Minecraft.getInstance().player.getYRot();
                clientTickCounter = 0L;
                lastRenderNumbers = Config.render_spawnNumbers.get();
            }
        } else {
            scanner.clear();
            Minecraft.getInstance().player.sendOverlayMessage(Component.nullToEmpty(ChatFormatting.YELLOW + "Light Overlay Disabled"));
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
        Class<? extends ILightRenderer> rendererCls = Config.render_spawnNumbers.get()
                ? NumberOverlayRenderer.class
                : CrossOverlayRenderer.class;
        LightOverlayReloadHandlerEvent event = new LightOverlayReloadHandlerEvent(Config.light_IgnoreSpawnList.get(), rendererCls, LightScannerVanilla.class);

        if (renderer == null || renderer.getClass() != event.getRenderer()) {
            try {
                renderer = event.getRenderer().getDeclaredConstructor().newInstance();
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
                MoreOverlays.logger.warn(new FormattedMessage("Could not create ILightRenderer from type \"%s\"!", event.getRenderer().getName()), e);
                renderer = new CrossOverlayRenderer();
            }
        }

        if (scanner == null || scanner.getClass() != event.getScanner()) {
            if (scanner != null && enabled) {
                scanner.clear();
            }

            try {
                scanner = event.getScanner().getDeclaredConstructor().newInstance();
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
                MoreOverlays.logger.warn(new FormattedMessage("Could not create ILightScanner from type \"%s\"!", event.getScanner().getName()), e);
                scanner = new LightScannerVanilla();
            }
        }
    }

    public static void onClientTick() {
        if (Minecraft.getInstance().level != null && Minecraft.getInstance().player != null && enabled &&
                (Minecraft.getInstance().gui.screen() == null || !Minecraft.getInstance().gui.screen().isPauseScreen())) {
            clientTickCounter++;

            boolean currentRenderNumbers = Config.render_spawnNumbers.get();
            if (currentRenderNumbers != lastRenderNumbers) {
                reloadHandlerInternal();
                lastRenderNumbers = currentRenderNumbers;
            }

            net.minecraft.world.entity.player.Player player = Minecraft.getInstance().player;
            net.minecraft.core.BlockPos bp = player.blockPosition();

            boolean movedBlock = (bp.getX() != lastPlayerBlockX) || (bp.getY() != lastPlayerBlockY) || (bp.getZ() != lastPlayerBlockZ);
            float yaw = player.getYRot();
            boolean rotated = Float.isNaN(lastPlayerYaw) || Math.abs(yaw - lastPlayerYaw) > 15.0f;
            int updateInterval = Math.max(1, Config.light_UpdateIntervalFrames.get());
            boolean periodicRefresh = (clientTickCounter % updateInterval) == 0L;

            if (movedBlock || rotated || periodicRefresh) {
                scanner.update(player);
                lastPlayerBlockX = bp.getX();
                lastPlayerBlockY = bp.getY();
                lastPlayerBlockZ = bp.getZ();
                lastPlayerYaw = yaw;
            }
        }
    }
}
