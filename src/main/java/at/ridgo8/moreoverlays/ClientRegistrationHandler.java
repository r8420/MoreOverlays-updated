package at.ridgo8.moreoverlays;

import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsHandler;
import at.ridgo8.moreoverlays.compatibility.IrisCompatibility;
import at.ridgo8.moreoverlays.config.Config;
import at.ridgo8.moreoverlays.gui.ConfigScreen;
import at.ridgo8.moreoverlays.itemsearch.GuiHandler;
import at.ridgo8.moreoverlays.itemsearch.GuiUtils;
import at.ridgo8.moreoverlays.lightoverlay.LightOverlayHandler;
import at.ridgo8.moreoverlays.lightoverlay.integration.AlternateLightHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;


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
        ctx.registerExtensionPoint(IConfigScreenFactory.class,
                () -> (minecraft, screen) -> new ConfigScreen(screen, Config.config_client, MoreOverlays.MOD_ID));

        enable_jei = ModList.get().isLoaded("jei");

        LightOverlayHandler.init();
        ChunkBoundsHandler.init();
        GuiUtils.initUtil();
        AlternateLightHandler.init();

        GuiHandler.init();
        IrisCompatibility.assignPipelines();

        // Quick fix for light level in 1.18 (need better fix)
        if (!Config.light_FinishedMigration.get()) {
            Config.light_SaveLevel.set(1);
            Config.light_FinishedMigration.set(true);
        }
        NeoForge.EVENT_BUS.register(new ClientRegistrationHandler());
    }

    public static Screen openSettings(Minecraft mc, Screen modlist) {
        return new ConfigScreen(modlist, Config.config_client, MoreOverlays.MOD_ID);
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        checkAndToggleKeyMappings();
    }

    private void checkAndToggleKeyMappings() {
        if (KeyBindings.lightOverlayKeyMapping.get().consumeClick()) {
            LightOverlayHandler.setEnabled(!LightOverlayHandler.isEnabled());
        }
        if (KeyBindings.chunkBoundsKeyMapping.get().consumeClick()) {
            ChunkBoundsHandler.toggleMode();
        }
    }
}