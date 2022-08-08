package at.ridgo8.moreoverlays;

import at.ridgo8.moreoverlays.config.Config;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MoreOverlays.MOD_ID)
public class MoreOverlays {

    public static final String MOD_ID = "moreoverlays";
    public static final String NAME = "MoreOverlays";

    public static Logger logger = LogManager.getLogger(NAME);

    public MoreOverlays() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        final ModLoadingContext ctx = ModLoadingContext.get();

        modBus.addListener(this::onClientInit);

        Config.initialize();

        ctx.registerConfig(ModConfig.Type.CLIENT, Config.config_client, MOD_ID + ".toml");
        ctx.registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }

    public void onClientInit(FMLClientSetupEvent event) {
        ClientRegistrationHandler.setupClient();
    }


}
