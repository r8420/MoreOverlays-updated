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

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        if (LightOverlayHandler.enabled == enabled) {
            return;
        }

        if (enabled) {
            reloadHandlerInternal();
            Minecraft.getInstance().player.displayClientMessage(Component.nullToEmpty(ChatFormatting.YELLOW + "Light Overlay Enabled"), true);
        } else {
            scanner.clear();
            Minecraft.getInstance().player.displayClientMessage(Component.nullToEmpty(ChatFormatting.YELLOW + "Light Overlay Disabled"), true);
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
        LightOverlayReloadHandlerEvent event = new LightOverlayReloadHandlerEvent(ConfigManager.CONFIG.light_IgnoreSpawnList(), LightOverlayRenderer.class, LightScannerVanilla.class);

        if (renderer == null || renderer.getClass() != event.getRenderer()) {
            try {
                renderer = event.getRenderer().getDeclaredConstructor().newInstance();
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
                MoreOverlays.logger.warn(new FormattedMessage("Could not create ILightRenderer from type \"%s\"!", event.getRenderer().getName()), e);
                renderer = new LightOverlayRenderer();
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
}
