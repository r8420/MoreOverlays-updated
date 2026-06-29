package at.ridgo8.moreoverlays.config;

import io.wispforest.owo.config.ui.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ConfigManager {

    public static final MoreOverlaysConfig CONFIG = MoreOverlaysConfig.createAndLoad();

    private ConfigManager() {
    }

    public static void openConfigScreen(Screen parent) {
        Minecraft.getInstance().gui.setScreen(ConfigScreen.create(CONFIG, parent));
    }
}


