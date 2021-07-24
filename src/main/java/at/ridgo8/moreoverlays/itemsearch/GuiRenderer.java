package at.ridgo8.moreoverlays.itemsearch;

import at.ridgo8.moreoverlays.api.itemsearch.SlotHandler;
import at.ridgo8.moreoverlays.api.itemsearch.SlotViewWrapper;
import at.ridgo8.moreoverlays.config.Config;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector2f;
import org.lwjgl.opengl.GL11;

import java.util.Map;

public class GuiRenderer {

    public static final GuiRenderer INSTANCE = new GuiRenderer();

    private static final float OVERLAY_ZLEVEL = 299F;
    private static final float FRAME_RADIUS = 1.0F;

    private static boolean enabled = false;

    private static String lastFilterText = "";
    private static boolean emptyFilter = true;
    private static BiMap<Slot, SlotViewWrapper> views = HashBiMap.create();

    private boolean allowRender = false;
    private int guiOffsetX = 0;
    private int guiOffsetY = 0;

    public void guiInit(Screen gui) {
        if (!canShowIn(gui)) {
            return;
        }

        guiOffsetX = GuiUtils.getGuiLeft((ContainerScreen<?>) gui);
        guiOffsetY = GuiUtils.getGuiTop((ContainerScreen<?>) gui);

    }

    public void guiOpen(Screen gui) {

    }

    public void preDraw() {
        Screen guiscr = Minecraft.getInstance().screen;

        TextFieldWidget textField = JeiModule.getJEITextField();

        if (canShowIn(guiscr)) {
            allowRender = true;
            if (textField != null && enabled) {
                drawSearchFrame(textField);
            }
        }
    }

    public void postDraw() {
        Screen guiscr = Minecraft.getInstance().screen;

        if (allowRender && canShowIn(guiscr)) {
            allowRender = false;
            drawSlotOverlay((ContainerScreen<?>) guiscr);
        }
    }

    private void drawSearchFrame(TextFieldWidget textField) {
        RenderHelper.setupForFlatItems();
        GlStateManager._enableAlphaTest();
        GlStateManager._enableDepthTest();
        GlStateManager._disableTexture();
        GlStateManager._color4f(1, 1, 1, 1);
        GlStateManager._pushMatrix();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuilder();
        GlStateManager._color4f(1, 1, 0, 1);

        float x = textField.x + 2;
        float y = textField.y + 2;
        float width = textField.getWidth() - 4;
        float height = textField.getHeight() - 4;

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buffer.vertex(x + width + FRAME_RADIUS, y - FRAME_RADIUS, 1000).endVertex();
        buffer.vertex(x - FRAME_RADIUS, y - FRAME_RADIUS, 1000).endVertex();
        buffer.vertex(x - FRAME_RADIUS, y, 1000).endVertex();
        buffer.vertex(x + width + FRAME_RADIUS, y, 1000).endVertex();

        buffer.vertex(x, y, 1000).endVertex();
        buffer.vertex(x - FRAME_RADIUS, y, 1000).endVertex();
        buffer.vertex(x - FRAME_RADIUS, y + height, 1000).endVertex();
        buffer.vertex(x, y + height, 1000).endVertex();

        buffer.vertex(x + width + FRAME_RADIUS, y + height, 1000).endVertex();
        buffer.vertex(x - FRAME_RADIUS, y + height, 1000).endVertex();
        buffer.vertex(x - FRAME_RADIUS, y + height + FRAME_RADIUS, 1000).endVertex();
        buffer.vertex(x + width + FRAME_RADIUS, y + height + FRAME_RADIUS, 1000).endVertex();

        buffer.vertex(x + width + FRAME_RADIUS, y, 1000).endVertex();
        buffer.vertex(x + width, y, 1000).endVertex();
        buffer.vertex(x + width, y + height, 1000).endVertex();
        buffer.vertex(x + width + FRAME_RADIUS, y + height, 1000).endVertex();

        tess.end();
        GlStateManager._color4f(1, 1, 1, 1);
        GlStateManager._disableBlend();
        GlStateManager._popMatrix();
        GlStateManager._enableTexture();
    }

    public void renderTooltip(ItemStack stack) {
        Screen guiscr = Minecraft.getInstance().screen;
        if (allowRender && canShowIn(guiscr)) {
            ContainerScreen<?> gui = (ContainerScreen<?>) guiscr;
            if (gui.getSlotUnderMouse() != null && gui.getSlotUnderMouse().hasItem()
                    && gui.getSlotUnderMouse().getItem().equals(stack)) {
                allowRender = false;
                drawSlotOverlay((ContainerScreen<?>) guiscr);
            }
        }
    }

    private void drawSlotOverlay(ContainerScreen<?> gui) {
        RenderHelper.setupForFlatItems();
        GlStateManager._enableAlphaTest();
        GlStateManager._color4f(1, 1, 1, 1);

        if (!enabled || views == null || views.isEmpty())
            return;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder renderer = tess.getBuilder();

        GlStateManager._pushMatrix();
        GlStateManager._enableBlend();
        GlStateManager._disableTexture();
        GlStateManager._color4f(0, 0, 0, 0.5F);

        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        for (Map.Entry<Slot, SlotViewWrapper> slot : views.entrySet()) {
            if (slot.getValue().isEnableOverlay()) {
                Vector2f posvec = slot.getValue().getView().getRenderPos(guiOffsetX, guiOffsetY);
                float px = posvec.x;
                float py = posvec.y;
                renderer.vertex(px + 16 + guiOffsetX, py + guiOffsetY, OVERLAY_ZLEVEL).endVertex();
                renderer.vertex(px + guiOffsetX, py + guiOffsetY, OVERLAY_ZLEVEL).endVertex();
                renderer.vertex(px + guiOffsetX, py + 16 + guiOffsetY, OVERLAY_ZLEVEL).endVertex();
                renderer.vertex(px + 16 + guiOffsetX, py + 16 + guiOffsetY, OVERLAY_ZLEVEL).endVertex();
            }
        }

        tess.end();

        GlStateManager._enableTexture();
        GlStateManager._popMatrix();
        GlStateManager._color4f(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager._disableBlend();
    }

    public boolean canShowIn(Screen gui) {
        return (gui instanceof ContainerScreen<?>) && ((ContainerScreen<?>) gui).getMenu() != null && !((ContainerScreen<?>) gui).getMenu().slots.isEmpty();
    }

    private void checkSlots(ContainerScreen<?> container) {
        if (views == null) {
            views = HashBiMap.create();
        } else {
            views.clear();
        }
        for (Slot slot : container.getMenu().slots) {
            //System.out.println(slot);
            SlotViewWrapper wrapper;
            if (!views.containsKey(slot)) {
                wrapper = new SlotViewWrapper(SlotHandler.INSTANCE.getViewSlot(container, slot));
                views.put(slot, wrapper);
            } else {
                wrapper = views.get(slot);
            }

            wrapper.setEnableOverlay(wrapper.getView().canSearch() && !isSearchedItem(slot.getItem()));
        }
    }

    private boolean isSearchedItem(ItemStack stack) {
        if (emptyFilter) return true;
        else if (stack.isEmpty()) return false;
        for (Object ingredient : JeiModule.filter.getFilteredIngredients()) {
            if (ItemUtils.ingredientMatches(ingredient, stack)) {
                return true;
            }
        }
        return Config.search_searchCustom.get() && stack.getDisplayName().getString().toLowerCase().contains(JeiModule.getJEITextField().getValue().toLowerCase());
    }

    public void tick() {
        final Screen screen = Minecraft.getInstance().screen;
        if (!canShowIn(screen))
            return;
        if (enabled && !JeiModule.filter.getFilterText().equals(lastFilterText)) {
            lastFilterText = JeiModule.filter.getFilterText();
            emptyFilter = lastFilterText.replace(" ", "").isEmpty();
        }


        if (enabled && screen instanceof ContainerScreen<?>) {
            checkSlots((ContainerScreen<?>) screen);
            guiOffsetX = GuiUtils.getGuiLeft((ContainerScreen<?>) screen);
            guiOffsetY = GuiUtils.getGuiTop((ContainerScreen<?>) screen);
        } else if (views != null) {
            views.clear();
        }
    }

    public void toggleMode() {
        enabled = !enabled;
        if (enabled) {
            lastFilterText = JeiModule.filter.getFilterText();
            emptyFilter = lastFilterText.replace(" ", "").isEmpty();
        } else {
            lastFilterText = "";
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
