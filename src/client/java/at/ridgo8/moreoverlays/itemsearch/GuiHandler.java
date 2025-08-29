package at.ridgo8.moreoverlays.itemsearch;

import at.ridgo8.moreoverlays.ClientRegistrationHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.Minecraft;

public class GuiHandler {

    public static void init() {
        if (ClientRegistrationHandler.isJeiInstalled()) {
            registerEvents();
        }
    }

    private static void registerEvents() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            JeiModule.updateModule();
            JeiModule.logMissingSearchTextFieldOnce();
            GuiRenderer.INSTANCE.guiInit(screen);
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            GuiRenderer.INSTANCE.guiOpen(client.screen);
        });

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenEvents.beforeRender(screen).register((screen1, guiGraphics, mouseX, mouseY, tickDelta) -> {
                GuiRenderer.INSTANCE.preDraw(guiGraphics);
            });
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenEvents.afterRender(screen).register((screen1, guiGraphics, mouseX, mouseY, tickDelta) -> {
                GuiRenderer.INSTANCE.renderTooltip(guiGraphics);
                GuiRenderer.INSTANCE.postDraw(guiGraphics);
            });
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (Minecraft.getInstance().player == null) return;
            GuiRenderer.INSTANCE.tick();
        });
        
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if(GuiRenderer.INSTANCE.isEnabled()){
                GuiRenderer.INSTANCE.toggleMode();
            }
        });
    }

    @Deprecated
    public static void toggleMode() {
        GuiRenderer.INSTANCE.toggleMode();
    }
}