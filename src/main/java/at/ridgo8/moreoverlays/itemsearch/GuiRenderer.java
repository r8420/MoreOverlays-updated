package at.ridgo8.moreoverlays.itemsearch;

import at.ridgo8.moreoverlays.api.itemsearch.SlotHandler;
import at.ridgo8.moreoverlays.api.itemsearch.SlotViewWrapper;
import at.ridgo8.moreoverlays.config.Config;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.EditBox;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
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

        guiOffsetX = GuiUtils.getGuiLeft((AbstractContainerScreen<?>) gui);
        guiOffsetY = GuiUtils.getGuiTop((AbstractContainerScreen<?>) gui);

    }

    public void guiOpen(Screen gui) {

    }

    public void preDraw() {
        Screen guiscr = Minecraft.getInstance().screen;

//        TextFieldWidget textField = JeiModule.getJEITextField();

        if (canShowIn(guiscr)) {
            allowRender = true;
//            if (textField != null && enabled) {
//                drawSearchFrame(textField);
//            }
        }
    }

    public void postDraw() {
        Screen guiscr = Minecraft.getInstance().screen;

        if (allowRender && canShowIn(guiscr)) {
            allowRender = false;
            drawSlotOverlay((AbstractContainerScreen<?>) guiscr);
        }
    }

    private void drawSearchFrame(EditBox textField) {
        Lighting.setupForFlatItems();
//        GlStateManager._enableAlphaTest();
        GlStateManager._enableDepthTest();
        GlStateManager._disableTexture();
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPushMatrix();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buffer = tess.getBuilder();
        GL11.glColor4f(1, 1, 0, 1);

        float x = textField.x + 2;
        float y = textField.y + 2;
        float width = textField.getWidth() - 4;
        float height = textField.getHeight() - 4;

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
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
        GL11.glColor4f(1, 1, 1, 1);
        GlStateManager._disableBlend();
        GL11.glPopMatrix();
        GlStateManager._enableTexture();
    }

    public void renderTooltip(ItemStack stack) {
        Screen guiscr = Minecraft.getInstance().screen;
        if (allowRender && canShowIn(guiscr)) {
            AbstractContainerScreen<?> gui = (AbstractContainerScreen<?>) guiscr;
            if (gui.getSlotUnderMouse() != null && gui.getSlotUnderMouse().hasItem()
                    && gui.getSlotUnderMouse().getItem().equals(stack)) {
                allowRender = false;
                drawSlotOverlay((AbstractContainerScreen<?>) guiscr);
            }
        }
    }

    private void drawSlotOverlay(AbstractContainerScreen<?> gui) {
        Lighting.setupForFlatItems();
//        GlStateManager._enableAlphaTest();
        GL11.glColor4f(1, 1, 1, 1);

        if (!enabled || views == null || views.isEmpty())
            return;

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder renderer = tess.getBuilder();

        GL11.glPushMatrix();
        GlStateManager._enableBlend();
        GlStateManager._disableTexture();
        GL11.glColor4f(0, 0, 0, 0.5F);

        renderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        for (Map.Entry<Slot, SlotViewWrapper> slot : views.entrySet()) {
            if (slot.getValue().isEnableOverlay()) {
                Vec2 posvec = slot.getValue().getView().getRenderPos(guiOffsetX, guiOffsetY);
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
        GL11.glPopMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager._disableBlend();
    }

    public boolean canShowIn(Screen gui) {
        return (gui instanceof AbstractContainerScreen<?>) && ((AbstractContainerScreen<?>) gui).getMenu() != null && !((AbstractContainerScreen<?>) gui).getMenu().slots.isEmpty();
    }

    private void checkSlots(AbstractContainerScreen<?> container) {
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
//        for (Object ingredient : JeiModule.filter.getFilteredIngredients()) {
//            if (ItemUtils.ingredientMatches(ingredient, stack)) {
//                return true;
//            }
//        }
//        return Config.search_searchCustom.get() && stack.getDisplayName().getString().toLowerCase().contains(JeiModule.getJEITextField().getValue().toLowerCase());
        return false;
    }

    public void tick() {
        final Screen screen = Minecraft.getInstance().screen;
        if (!canShowIn(screen))
            return;
//        if (enabled && !JeiModule.filter.getFilterText().equals(lastFilterText)) {
//            lastFilterText = JeiModule.filter.getFilterText();
//            emptyFilter = lastFilterText.replace(" ", "").isEmpty();
//        }


        if (enabled && screen instanceof AbstractContainerScreen<?>) {
            checkSlots((AbstractContainerScreen<?>) screen);
            guiOffsetX = GuiUtils.getGuiLeft((AbstractContainerScreen<?>) screen);
            guiOffsetY = GuiUtils.getGuiTop((AbstractContainerScreen<?>) screen);
        } else if (views != null) {
            views.clear();
        }
    }

    public void toggleMode() {
        enabled = !enabled;
        if (enabled) {
//            lastFilterText = JeiModule.filter.getFilterText();
            emptyFilter = lastFilterText.replace(" ", "").isEmpty();
        } else {
            lastFilterText = "";
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
