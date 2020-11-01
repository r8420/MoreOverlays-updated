package at.feldim2425.moreoverlays.itemsearch;

import at.feldim2425.moreoverlays.api.itemsearch.SlotHandler;
import at.feldim2425.moreoverlays.api.itemsearch.SlotViewWrapper;
import at.feldim2425.moreoverlays.config.Config;
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

    private static boolean enabled;

    private static String lastFilterText = "";
    private static boolean emptyFilter = true;
    private static BiMap<Slot, SlotViewWrapper> views = HashBiMap.create();

    private boolean allowRender;
    private int guiOffsetX;
    private int guiOffsetY;

    public void guiInit(final Screen gui) {
        if (!this.canShowIn(gui)) {
            return;
        }

		this.guiOffsetX = GuiUtils.getGuiLeft((ContainerScreen<?>) gui);
		this.guiOffsetY = GuiUtils.getGuiTop((ContainerScreen<?>) gui);

    }

    public void guiOpen(final Screen gui) {

    }

    public void preDraw() {
        final Screen guiscr = Minecraft.getInstance().currentScreen;

        final TextFieldWidget textField = JeiModule.getJEITextField();

        if (this.canShowIn(guiscr)) {
			this.allowRender = true;
            if (textField != null && GuiRenderer.enabled) {
				this.drawSearchFrame(textField);
            }
        }
    }

    public void postDraw() {
        final Screen guiscr = Minecraft.getInstance().currentScreen;

        if (this.allowRender && this.canShowIn(guiscr)) {
			this.allowRender = false;
			this.drawSlotOverlay((ContainerScreen<?>) guiscr);
        }
    }

    private void drawSearchFrame(final TextFieldWidget textField) {
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableDepthTest();
        GlStateManager.disableTexture();
        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.pushMatrix();
        final Tessellator tess = Tessellator.getInstance();
        final BufferBuilder buffer = tess.getBuffer();
        GlStateManager.color4f(1, 1, 0, 1);

        final float x = textField.x + 2;
        final float y = textField.y + 2;
        final float width = textField.getWidth() - 4;
        final float height = textField.getHeightRealms() - 4;

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buffer.pos(x + width + GuiRenderer.FRAME_RADIUS, y - GuiRenderer.FRAME_RADIUS, 1000).endVertex();
        buffer.pos(x - GuiRenderer.FRAME_RADIUS, y - GuiRenderer.FRAME_RADIUS, 1000).endVertex();
        buffer.pos(x - GuiRenderer.FRAME_RADIUS, y, 1000).endVertex();
        buffer.pos(x + width + GuiRenderer.FRAME_RADIUS, y, 1000).endVertex();

        buffer.pos(x, y, 1000).endVertex();
        buffer.pos(x - GuiRenderer.FRAME_RADIUS, y, 1000).endVertex();
        buffer.pos(x - GuiRenderer.FRAME_RADIUS, y + height, 1000).endVertex();
        buffer.pos(x, y + height, 1000).endVertex();

        buffer.pos(x + width + GuiRenderer.FRAME_RADIUS, y + height, 1000).endVertex();
        buffer.pos(x - GuiRenderer.FRAME_RADIUS, y + height, 1000).endVertex();
        buffer.pos(x - GuiRenderer.FRAME_RADIUS, y + height + GuiRenderer.FRAME_RADIUS, 1000).endVertex();
        buffer.pos(x + width + GuiRenderer.FRAME_RADIUS, y + height + GuiRenderer.FRAME_RADIUS, 1000).endVertex();

        buffer.pos(x + width + GuiRenderer.FRAME_RADIUS, y, 1000).endVertex();
        buffer.pos(x + width, y, 1000).endVertex();
        buffer.pos(x + width, y + height, 1000).endVertex();
        buffer.pos(x + width + GuiRenderer.FRAME_RADIUS, y + height, 1000).endVertex();

        tess.draw();
        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        GlStateManager.enableTexture();
    }

    public void renderTooltip(final ItemStack stack) {
        final Screen guiscr = Minecraft.getInstance().currentScreen;
        if (this.allowRender && this.canShowIn(guiscr)) {
            final ContainerScreen<?> gui = (ContainerScreen<?>) guiscr;
            if (gui.getSlotUnderMouse() != null && gui.getSlotUnderMouse().getHasStack()
                    && gui.getSlotUnderMouse().getStack().equals(stack)) {
				this.allowRender = false;
				this.drawSlotOverlay((ContainerScreen<?>) guiscr);
            }
        }
    }

    private void drawSlotOverlay(final ContainerScreen<?> gui) {
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableAlphaTest();
        GlStateManager.color4f(1, 1, 1, 1);

        if (!GuiRenderer.enabled || GuiRenderer.views == null || GuiRenderer.views.isEmpty())
            return;

        final Tessellator tess = Tessellator.getInstance();
        final BufferBuilder renderer = tess.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.color4f(0, 0, 0, 0.5F);

        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        for (final Map.Entry<Slot, SlotViewWrapper> slot : GuiRenderer.views.entrySet()) {
            if (slot.getValue().isEnableOverlay()) {
                final Vector2f posvec = slot.getValue().getView().getRenderPos(this.guiOffsetX, this.guiOffsetY);
                final float px = posvec.x;
                final float py = posvec.y;
                renderer.pos(px + 16 + this.guiOffsetX, py + this.guiOffsetY, GuiRenderer.OVERLAY_ZLEVEL).endVertex();
                renderer.pos(px + this.guiOffsetX, py + this.guiOffsetY, GuiRenderer.OVERLAY_ZLEVEL).endVertex();
                renderer.pos(px + this.guiOffsetX, py + 16 + this.guiOffsetY, GuiRenderer.OVERLAY_ZLEVEL).endVertex();
                renderer.pos(px + 16 + this.guiOffsetX, py + 16 + this.guiOffsetY, GuiRenderer.OVERLAY_ZLEVEL).endVertex();
            }
        }

        tess.draw();

        GlStateManager.enableTexture();
        GlStateManager.popMatrix();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.disableBlend();
    }

    public boolean canShowIn(final Screen gui) {
        return (gui instanceof ContainerScreen<?>) && ((ContainerScreen<?>) gui).getContainer() != null && !((ContainerScreen<?>) gui).getContainer().inventorySlots.isEmpty();
    }

    private void checkSlots(final ContainerScreen<?> container) {
        if (GuiRenderer.views == null) {
			GuiRenderer.views = HashBiMap.create();
        } else {
			GuiRenderer.views.clear();
        }
        for (final Slot slot : container.getContainer().inventorySlots) {
            //System.out.println(slot);
            final SlotViewWrapper wrapper;
            if (!GuiRenderer.views.containsKey(slot)) {
                wrapper = new SlotViewWrapper(SlotHandler.INSTANCE.getViewSlot(container, slot));
				GuiRenderer.views.put(slot, wrapper);
            } else {
                wrapper = GuiRenderer.views.get(slot);
            }

            wrapper.setEnableOverlay(wrapper.getView().canSearch() && !this.isSearchedItem(slot.getStack()));
        }
    }

    private boolean isSearchedItem(final ItemStack stack) {
        if (GuiRenderer.emptyFilter) return true;
        else if (stack.isEmpty()) return false;
        for (final Object ingredient : JeiModule.filter.getFilteredIngredients()) {
            if (ItemUtils.ingredientMatches(ingredient, stack)) {
                return true;
            }
        }
        return Config.search_searchCustom.get() && stack.getDisplayName().getString().toLowerCase().contains(JeiModule.getJEITextField().getText().toLowerCase());
    }

    public void tick() {
        Screen screen = Minecraft.getInstance().currentScreen;
        if (!this.canShowIn(screen))
            return;
        if (GuiRenderer.enabled && !JeiModule.filter.getFilterText().equals(GuiRenderer.lastFilterText)) {
			GuiRenderer.lastFilterText = JeiModule.filter.getFilterText();
			GuiRenderer.emptyFilter = GuiRenderer.lastFilterText.replace(" ", "").isEmpty();
        }


        if (GuiRenderer.enabled && screen instanceof ContainerScreen<?>) {
			this.checkSlots((ContainerScreen<?>) screen);
			this.guiOffsetX = GuiUtils.getGuiLeft((ContainerScreen<?>) screen);
			this.guiOffsetY = GuiUtils.getGuiTop((ContainerScreen<?>) screen);
        } else if (GuiRenderer.views != null) {
			GuiRenderer.views.clear();
        }
    }

    public void toggleMode() {
		GuiRenderer.enabled = !GuiRenderer.enabled;
        if (GuiRenderer.enabled) {
			GuiRenderer.lastFilterText = JeiModule.filter.getFilterText();
			GuiRenderer.emptyFilter = GuiRenderer.lastFilterText.replace(" ", "").isEmpty();
        } else {
			GuiRenderer.lastFilterText = "";
        }
    }

    public boolean isEnabled() {
        return GuiRenderer.enabled;
    }
}
