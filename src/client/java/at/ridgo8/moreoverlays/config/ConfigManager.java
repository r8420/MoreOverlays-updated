package at.ridgo8.moreoverlays.config;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.gui.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ConfigManager {

    public static final MoreOverlaysConfig CONFIG;

    static {
        Config.initialize();
        OwoConfigMigration.migrateIfNeeded(Config.config_client);
        Config.config_client.load();
        CONFIG = new MoreOverlaysConfig();
    }

    private ConfigManager() {
    }

    public static void openConfigScreen(Screen parent) {
        Minecraft.getInstance().gui.setScreen(createConfigScreen(parent));
    }

    public static Screen createConfigScreen(Screen parent) {
        return new ConfigScreen(parent, Config.config_client, MoreOverlays.MOD_ID);
    }
}
