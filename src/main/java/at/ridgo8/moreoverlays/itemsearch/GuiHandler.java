package at.ridgo8.moreoverlays.itemsearch;

import at.ridgo8.moreoverlays.ClientRegistrationHandler;
import at.ridgo8.moreoverlays.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuiHandler {

    private long firstClick = 0;

    public static void init() {
        if (ClientRegistrationHandler.isJeiInstalled())
            MinecraftForge.EVENT_BUS.register(new GuiHandler());
    }

    @Deprecated
    public static void toggleMode() {
        GuiRenderer.INSTANCE.toggleMode();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onGuiInit(ScreenEvent.Init.Post event) {
        if(!Config.search_enabled.get()){
            toggleMode();
            return;
        }

        JeiModule.updateModule();
        GuiRenderer.INSTANCE.guiInit(event.getScreen());
    }

    @SubscribeEvent
    public void onGuiOpen(ScreenEvent.Opening event) {
        GuiRenderer.INSTANCE.guiOpen(event.getScreen());
    }

    @SubscribeEvent
    public void onGuiClick(ScreenEvent.MouseButtonPressed.Pre event) {
        EditBox textField = JeiModule.getJEITextField();
        if (textField != null && event.getButton() == 0 && GuiRenderer.INSTANCE.canShowIn(event.getScreen())) {
            int mouse_x = (int) event.getMouseX();
            int mouse_y = (int) event.getMouseY();

            float textField_x = textField.getX() - 2;
            float textField_y = textField.getY() - 4;
            float textField_width = textField.getWidth() + 8;
            float textField_height = textField.getHeight() - 4;

            if (mouse_x > textField_x && mouse_x < textField_x + textField_width && mouse_y > textField_y && mouse_y < textField_y + textField_height) {
                long now = System.currentTimeMillis();
                if (now - firstClick < 1000) {
                    GuiRenderer.INSTANCE.toggleMode();
                    firstClick = 0;
                } else {
                    firstClick = now;
                }
            }
        }
    }

    @SubscribeEvent
    public void onDrawScreen(ScreenEvent.Render.Pre event) {
        GuiRenderer.INSTANCE.preDraw(event.getPoseStack());
    }

    @SubscribeEvent
    public void onDrawScreen(ScreenEvent.Render.Post event) {
        GuiRenderer.INSTANCE.postDraw();
    }

    @SubscribeEvent
    public void onRenderTooltip(RenderTooltipEvent.Pre event) {
        GuiRenderer.INSTANCE.renderTooltip(event.getItemStack());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().player == null)
            return;
        GuiRenderer.INSTANCE.tick();
    }
}
