package at.ridgo8.moreoverlays.lightoverlay;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightRenderer;
import at.ridgo8.moreoverlays.api.lightoverlay.ILightScanner;
import at.ridgo8.moreoverlays.api.lightoverlay.LightOverlayReloadHandlerEvent;
import at.ridgo8.moreoverlays.config.ConfigManager;
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
                lastRenderNumbers = ConfigManager.CONFIG.render_spawnNumbers();
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
        Class<? extends ILightRenderer> rendererCls = ConfigManager.CONFIG.render_spawnNumbers()
                ? at.ridgo8.moreoverlays.lightoverlay.render.NumberOverlayRenderer.class
                : at.ridgo8.moreoverlays.lightoverlay.render.CrossOverlayRenderer.class;
        LightOverlayReloadHandlerEvent event = new LightOverlayReloadHandlerEvent(ConfigManager.CONFIG.light_IgnoreSpawnList(), rendererCls, LightScannerVanilla.class);

        if (renderer == null || renderer.getClass() != event.getRenderer()) {
            try {
                renderer = event.getRenderer().getDeclaredConstructor().newInstance();
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
                MoreOverlays.logger.warn(new FormattedMessage("Could not create ILightRenderer from type \"%s\"!", event.getRenderer().getName()), e);
                renderer = new at.ridgo8.moreoverlays.lightoverlay.render.CrossOverlayRenderer();
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

            boolean currentRenderNumbers = ConfigManager.CONFIG.render_spawnNumbers();
            if (currentRenderNumbers != lastRenderNumbers) {
                reloadHandlerInternal();
                lastRenderNumbers = currentRenderNumbers;
            }

            net.minecraft.world.entity.player.Player player = Minecraft.getInstance().player;
            net.minecraft.core.BlockPos bp = player.blockPosition();

            boolean movedBlock = (bp.getX() != lastPlayerBlockX) || (bp.getY() != lastPlayerBlockY) || (bp.getZ() != lastPlayerBlockZ);
            float yaw = player.getYRot();
            boolean rotated = Float.isNaN(lastPlayerYaw) || Math.abs(yaw - lastPlayerYaw) > 15.0f;
            int updateInterval = Math.max(1, ConfigManager.CONFIG.light_UpdateIntervalFrames());
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
