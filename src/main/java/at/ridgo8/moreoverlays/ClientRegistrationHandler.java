package at.ridgo8.moreoverlays;

import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsHandler;
import at.ridgo8.moreoverlays.config.Config;
import at.ridgo8.moreoverlays.gui.ConfigScreen;
import at.ridgo8.moreoverlays.itemsearch.GuiHandler;
import at.ridgo8.moreoverlays.itemsearch.GuiUtils;
import at.ridgo8.moreoverlays.lightoverlay.LightOverlayHandler;
import at.ridgo8.moreoverlays.lightoverlay.integration.AlternateLightHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import org.lwjgl.glfw.GLFW;

import static at.ridgo8.moreoverlays.KeyBindings.mappedKey;

public final class ClientRegistrationHandler {

    private static boolean enable_jei = false;

    private ClientRegistrationHandler() {
        // EMPTY
    }

    public static boolean isJeiInstalled() {
        return enable_jei;
    }

    public static KeyMapping lightOverlayKeyMapping = new KeyMapping("key." + MoreOverlays.MOD_ID + ".lightoverlay.desc", KeyConflictContext.IN_GAME, mappedKey(GLFW.GLFW_KEY_F7), "key." + MoreOverlays.MOD_ID + ".category");
    public static KeyMapping chunkBoundsKeyMapping = new KeyMapping("key." + MoreOverlays.MOD_ID + ".chunkbounds.desc", KeyConflictContext.IN_GAME, mappedKey(GLFW.GLFW_KEY_F9), "key." + MoreOverlays.MOD_ID + ".category");

    public static void setupClient() {
        final ModLoadingContext ctx = ModLoadingContext.get();
        ctx.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new ConfigScreen(screen,Config.config_client, MoreOverlays.MOD_ID)));

        enable_jei = ModList.get().isLoaded("jei");
        KeyBindings.init();

        LightOverlayHandler.init();
        ChunkBoundsHandler.init();
        GuiUtils.initUtil();
        AlternateLightHandler.init();

        GuiHandler.init();

        // Quick fix for light level in 1.18 (need better fix)
        if(!Config.light_FinishedMigration.get()){
            Config.light_SaveLevel.set(1);
            Config.light_FinishedMigration.set(true);
        }
    }
    public static Screen openSettings(Minecraft mc, Screen modlist) {
        return new ConfigScreen(modlist, Config.config_client, MoreOverlays.MOD_ID);
    }
}