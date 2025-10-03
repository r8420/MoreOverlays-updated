package at.ridgo8.moreoverlays;

import at.ridgo8.moreoverlays.config.Config;
import at.ridgo8.moreoverlays.KeyBindings;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = MoreOverlays.MOD_ID, dist = Dist.CLIENT) 
public class MoreOverlays {

    public static final String MOD_ID = "moreoverlays";
    public static final String NAME = "MoreOverlays";

    public static Logger logger = LogManager.getLogger(NAME);

    public MoreOverlays(final IEventBus modBus, final ModContainer modContainer) {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            modBus.addListener(this::onClientInit);
            // Register key mappings on the mod event bus in 1.21.9
            modBus.addListener(KeyBindings::registerKeyMappings);
            Config.initialize();
            modContainer.registerConfig(ModConfig.Type.CLIENT, Config.config_client, MOD_ID + ".toml");
        }
    }

    public void onClientInit(FMLClientSetupEvent event) {
        ClientRegistrationHandler.setupClient();
    }


}
