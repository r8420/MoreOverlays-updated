package at.ridgo8.moreoverlays;

import at.ridgo8.moreoverlays.compatibility.IrisCompatibility;
import at.ridgo8.moreoverlays.itemsearch.GuiHandler;
import at.ridgo8.moreoverlays.itemsearch.GuiUtils;
import net.fabricmc.loader.api.FabricLoader;


public final class ClientRegistrationHandler {

    private static boolean enable_jei = false;

    private ClientRegistrationHandler() {
        // EMPTY
    }

    public static boolean isJeiInstalled() {
        return enable_jei;
    }

    public static void setupClient() {
        enable_jei = FabricLoader.getInstance().isModLoaded("jei");
        KeyBindings.init();
        GuiUtils.initUtil();
        GuiHandler.init();
        IrisCompatibility.assignPipelines();
    }
}