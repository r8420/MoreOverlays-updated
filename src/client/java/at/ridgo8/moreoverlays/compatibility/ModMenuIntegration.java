package at.ridgo8.moreoverlays.compatibility;

import at.ridgo8.moreoverlays.config.ConfigManager;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigManager::createConfigScreen;
    }
}
