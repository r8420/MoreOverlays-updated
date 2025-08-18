package at.ridgo8.moreoverlays.itemsearch;

import at.ridgo8.moreoverlays.api.itemsearch.SlotHandler;
import at.ridgo8.moreoverlays.api.itemsearch.SlotViewWrapper;
import at.ridgo8.moreoverlays.config.Config;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.world.item.Item;
import org.joml.Matrix4f;
import mezz.jei.api.constants.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.EditBox;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec2;

import java.util.List;
import java.util.Locale;
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

    public void preDraw(GuiGraphics guiGraphics) {
        Screen guiscr = Minecraft.getInstance().screen;

        EditBox textField = JeiModule.getJEITextField();

        if (canShowIn(guiscr)) {
            allowRender = true;
            // draw in postDraw to ensure on top
        }
    }

    public void postDraw(GuiGraphics guiGraphics) {
        Screen guiscr = Minecraft.getInstance().screen;

        if(canShowIn(guiscr) ){
            if( enabled ) {
                EditBox textField = JeiModule.getJEITextField();
                if (textField != null) {
                    drawSearchFrame(textField, guiGraphics);
                }
            }
            if (allowRender) {
                allowRender = false;
                drawSlotOverlay((AbstractContainerScreen<?>) guiscr);
            }
        }
    }

    private void drawSearchFrame(EditBox textField, GuiGraphics guiGraphics) {
        int x = textField.getX() - 2;
        int y = textField.getY() - 4;
        int width = textField.getWidth() + 8;
        int height = textField.getHeight() - 4;

        int rgb = Config.search_searchBoxColor.get() & 0xFFFFFF;
        int color = 0xFF000000 | rgb;

        int left = x - (int) FRAME_RADIUS;
        int top = y - (int) FRAME_RADIUS;
        int right = x + width + (int) FRAME_RADIUS;
        int bottom = y + height + (int) FRAME_RADIUS;

        guiGraphics.fill(left, top, right, y, color);
        guiGraphics.fill(left, y + height, right, bottom, color);
        guiGraphics.fill(left, y, x, y + height, color);
        guiGraphics.fill(x + width, y, right, y + height, color);
    }

    public void renderTooltip(ItemStack stack) {
        Screen guiscr = Minecraft.getInstance().screen;
        if (allowRender && canShowIn(guiscr)) {
            allowRender = false;
            drawSlotOverlay((AbstractContainerScreen<?>) guiscr);
        }
    }

    private void drawSlotOverlay(AbstractContainerScreen<?> gui) {
        Lighting.setupForFlatItems();

        if (!enabled || views == null || views.isEmpty())
            return;

        Tesselator tess = Tesselator.getInstance();

        RenderSystem.enableBlend();

        RenderSystem.setShader(CoreShaders.POSITION_COLOR);

        BufferBuilder renderer = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float r = ((float) ((Config.search_filteredSlotColor.get() >> 16) & 0xFF)) / 255F;
        float g = ((float) ((Config.search_filteredSlotColor.get() >> 8) & 0xFF)) / 255F;
        float b = ((float) (Config.search_filteredSlotColor.get() & 0xFF)) / 255F;
        float a = Config.search_filteredSlotTransparancy.get().floatValue();

        for (Map.Entry<Slot, SlotViewWrapper> slot : views.entrySet()) {
            if (slot.getValue().isEnableOverlay()) {
                Vec2 posvec = slot.getValue().getView().getRenderPos(guiOffsetX, guiOffsetY);
                float px = posvec.x;
                float py = posvec.y;
                renderer.addVertex(px + 16 + guiOffsetX, py + guiOffsetY, OVERLAY_ZLEVEL).setColor(r, g, b, a);
                renderer.addVertex(px + guiOffsetX, py + guiOffsetY, OVERLAY_ZLEVEL).setColor(r, g, b, a);
                renderer.addVertex(px + guiOffsetX, py + 16 + guiOffsetY, OVERLAY_ZLEVEL).setColor(r, g, b, a);
                renderer.addVertex(px + 16 + guiOffsetX, py + 16 + guiOffsetY, OVERLAY_ZLEVEL).setColor(r, g, b, a);
            }
        }

        MeshData meshData = renderer.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }

        RenderSystem.disableBlend();
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

        List<ItemStack> filteredIngredients = JeiModule.filter.getFilteredIngredients(VanillaTypes.ITEM_STACK);
        if(filteredIngredients.size() > Config.search_maxResults.get()) return;

        for (Slot slot : container.getMenu().slots) {
            SlotViewWrapper wrapper;
            if (!views.containsKey(slot)) {
                wrapper = new SlotViewWrapper(SlotHandler.INSTANCE.getViewSlot(container, slot));
                views.put(slot, wrapper);
            } else {
                wrapper = views.get(slot);
            }

            wrapper.setEnableOverlay(wrapper.getView().canSearch() && !isSearchedItem(slot.getItem(), filteredIngredients));
        }
    }

    private boolean isSearchedItem(ItemStack stack,List<ItemStack> filteredIngredients) {
        if (emptyFilter) return true;
        else if (stack.isEmpty()) return false;
        for (Object ingredient : filteredIngredients) {
            if (ItemUtils.ingredientMatches(ingredient, stack)) {
                return true;
            }
        }

        return matchesTooltipAndDisplayName(stack);
    }

    private boolean matchesTooltipAndDisplayName(ItemStack stack) {
        String searchString = JeiModule.getJEITextField().getValue().toLowerCase(Locale.ROOT);
        String[] searchWords = searchString.split(" "); // Split the search string into words

        // Check tooltips for all words presence
        return stack.getTooltipLines(Item.TooltipContext.of(Minecraft.getInstance().level), Minecraft.getInstance().player, TooltipFlag.Default.NORMAL)
                .stream()
                .anyMatch(tip -> {
                    String tipLower = tip.getString().toLowerCase(Locale.ROOT);

                    String displayNameLower = stack.getDisplayName().getString().toLowerCase(Locale.ROOT);
                    // Check if all search words are in the tooltip text or display name text
                    return java.util.Arrays.stream(searchWords).allMatch(tipLower::contains) || java.util.Arrays.stream(searchWords).allMatch(displayNameLower::contains);
                });
    }

    public void tick() {
        final Screen screen = Minecraft.getInstance().screen;
        if (!canShowIn(screen))
            return;
        if (enabled && !JeiModule.filter.getFilterText().equals(lastFilterText)) {
            lastFilterText = JeiModule.filter.getFilterText();
            emptyFilter = lastFilterText.replace(" ", "").isEmpty();
        }


        if (enabled && screen instanceof AbstractContainerScreen<?>) {
            checkSlots((AbstractContainerScreen<?>) screen);
            guiOffsetX = GuiUtils.getGuiLeft((AbstractContainerScreen<?>) screen);
            guiOffsetY = GuiUtils.getGuiTop((AbstractContainerScreen<?>) screen);
        } else if (views != null) {
            views.clear();
        }
    }

    public void toggleMode() {
        if(!Config.search_enabled.get()){
            enabled = false;
            return;
        }
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
