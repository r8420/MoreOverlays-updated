package at.ridgo8.moreoverlays.itemsearch;

import at.ridgo8.moreoverlays.ClientRegistrationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraftforge.client.event.ScreenOpenEvent;
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
    public void onGuiInit(ScreenEvent.InitScreenEvent.Post event) {
        JeiModule.updateModule();
        GuiRenderer.INSTANCE.guiInit(event.getScreen());
    }

    @SubscribeEvent
    public void onGuiOpen(ScreenOpenEvent event) {
        GuiRenderer.INSTANCE.guiOpen(event.getScreen());
    }

    @SubscribeEvent
    public void onGuiClick(ScreenEvent.MouseClickedEvent.Pre event) {
        EditBox searchField = JeiModule.getJEITextField();
        //Minecraft mc = Minecraft.getInstance();
        if (searchField != null && event.getButton() == 0 && GuiRenderer.INSTANCE.canShowIn(event.getScreen())) {
            //Screen guiScreen = event.getGui();
            //int x = event.getMouseX() * guiScreen.width / mc.displayWidth;
            //int y = guiScreen.height - event.getMouseY() * guiScreen.height / mc.displayHeight - 1;
            int x = (int) event.getMouseX();
            int y = (int) event.getMouseY();

            if (x > searchField.x && x < searchField.x + searchField.getWidth() && y > searchField.y && y < searchField.y + searchField.getHeight()) {
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
    public void onDrawScreen(ScreenEvent.DrawScreenEvent.Pre event) {
        GuiRenderer.INSTANCE.preDraw(event.getPoseStack());
    }

    @SubscribeEvent
    public void onDrawScreen(ScreenEvent.DrawScreenEvent.Post event) {
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
