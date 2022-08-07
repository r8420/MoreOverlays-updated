package at.ridgo8.moreoverlays;

import at.ridgo8.moreoverlays.api.itemsearch.SlotHandler;
import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsHandler;
import at.ridgo8.moreoverlays.config.Config;
import at.ridgo8.moreoverlays.gui.ConfigScreen;
import at.ridgo8.moreoverlays.itemsearch.GuiHandler;
import at.ridgo8.moreoverlays.itemsearch.GuiUtils;
import at.ridgo8.moreoverlays.itemsearch.integration.MantleModuleScreenOverride;
import at.ridgo8.moreoverlays.lightoverlay.LightOverlayHandler;
import at.ridgo8.moreoverlays.lightoverlay.integration.AlternateLightHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;

public final class ClientRegistrationHandler {

    private static boolean enable_jei = false;

    private ClientRegistrationHandler() {
        // EMPTY
    }

    public static boolean isJeiInstalled() {
        return enable_jei;
    }

    public static void setupClient() {
        final ModLoadingContext ctx = ModLoadingContext.get();
        ctx.registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory((minecraft, screen) -> new ConfigScreen(screen,Config.config_client, MoreOverlays.MOD_ID)));

        enable_jei = ModList.get().isLoaded("jei");
        KeyBindings.init();

        LightOverlayHandler.init();
        ChunkBoundsHandler.init();
        GuiUtils.initUtil();
        AlternateLightHandler.init();

        GuiHandler.init();

        if (enable_jei && ModList.get().isLoaded("mantle")) {
            SlotHandler.INSTANCE.addPositionOverride(new MantleModuleScreenOverride());
        }
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
