package at.ridgo8.moreoverlays.itemsearch;

import at.ridgo8.moreoverlays.api.itemsearch.SlotHandler;
import at.ridgo8.moreoverlays.api.itemsearch.SlotViewWrapper;
import at.ridgo8.moreoverlays.config.ConfigManager;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.world.item.Item;
import mezz.jei.api.constants.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec2;

import java.util.List;
import java.util.Map;

public class GuiRenderer {

    public static final GuiRenderer INSTANCE = new GuiRenderer();

    private static final float FRAME_RADIUS = 1.0F;

    private static boolean enabled = false;

    private static String lastFilterText = "";
    private static boolean emptyFilter = true;
    private static BiMap<Slot, SlotViewWrapper> views = HashBiMap.create();

    private boolean allowRender = false;
    private int guiOffsetX = 0;
    private int guiOffsetY = 0;
    private boolean drewOverlayInTooltipPhase = false;
    private boolean drewFrameInTooltipPhase = false;

    public void guiInit(Screen gui) {
        if (!canShowIn(gui)) {
            return;
        }

        guiOffsetX = GuiUtils.getGuiLeft((AbstractContainerScreen<?>) gui);
        guiOffsetY = GuiUtils.getGuiTop((AbstractContainerScreen<?>) gui);

        if (enabled && gui instanceof AbstractContainerScreen<?>) {
            checkSlots((AbstractContainerScreen<?>) gui);
        }
    }

    public void guiOpen(Screen gui) {

    }

    public void preDraw(GuiGraphics guiGraphics) {
        if (!ConfigManager.CONFIG.search_enabled()) return;
        Screen guiscr = Minecraft.getInstance().screen;
        if (canShowIn(guiscr)) {
            allowRender = true;
        }
    }

    public void postDraw(GuiGraphics guiGraphics) {
        if (!ConfigManager.CONFIG.search_enabled()) return;
        Screen guiscr = Minecraft.getInstance().screen;

        if (allowRender && canShowIn(guiscr)) {
            allowRender = false;
            if (enabled) {
                EditBox textField = JeiModule.getJEITextField();
                if (textField != null) {
                    if (!drewFrameInTooltipPhase) {
                        drawSearchFrame(textField, guiGraphics);
                    }
                }
            }
            if (!drewOverlayInTooltipPhase) {
                drawSlotOverlay(guiGraphics, (AbstractContainerScreen<?>) guiscr);
            }
            drewOverlayInTooltipPhase = false;
            drewFrameInTooltipPhase = false;
        }
    }

    private void drawSearchFrame(EditBox textField, GuiGraphics guiGraphics) {
        int x = textField.getX() - 2;
        int y = textField.getY() - 4;
        int width = textField.getWidth() + 8;
        int height = textField.getHeight() - 4;

        int boxColor = ConfigManager.CONFIG.search_searchBoxColor().argb();
        int rgb = boxColor & 0xFFFFFF;
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

    public void renderTooltip(GuiGraphics guiGraphics) {
        if (!ConfigManager.CONFIG.search_enabled()) return;
        Screen guiscr = Minecraft.getInstance().screen;
        if (enabled && canShowIn(guiscr)) {
            EditBox textField = JeiModule.getJEITextField();
            if (textField != null) {
                drawSearchFrame(textField, guiGraphics);
                drewFrameInTooltipPhase = true;
            }
            drawSlotOverlay(guiGraphics, (AbstractContainerScreen<?>) guiscr);
            drewOverlayInTooltipPhase = true;
        }
    }

    private void drawSlotOverlay(GuiGraphics guiGraphics, AbstractContainerScreen<?> gui) {
        if (!enabled || views == null || views.isEmpty())
            return;

        float r = ((float) ((ConfigManager.CONFIG.search_filteredSlotColor().argb() >> 16) & 0xFF)) / 255F;
        float g = ((float) ((ConfigManager.CONFIG.search_filteredSlotColor().argb() >> 8) & 0xFF)) / 255F;
        float b = ((float) (ConfigManager.CONFIG.search_filteredSlotColor().argb() & 0xFF)) / 255F;
        float a = (float) ConfigManager.CONFIG.search_filteredSlotTransparancy();

        for (Map.Entry<Slot, SlotViewWrapper> entry : views.entrySet()) {
            if (entry.getValue().isEnableOverlay()) {
                Vec2 pos = entry.getValue().getView().getRenderPos(guiOffsetX, guiOffsetY);
                int left = Math.round(pos.x) + guiOffsetX;
                int top = Math.round(pos.y) + guiOffsetY;
                int right = left + 16;
                int bottom = top + 16;
                int argb = ((int)(a * 255.0f) << 24) | ((int)(r * 255.0f) << 16) | ((int)(g * 255.0f) << 8) | (int)(b * 255.0f);
                guiGraphics.fill(left, top, right, bottom, argb);
            }
        }
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
        int checked = 0;
        int max = Math.max(1, ConfigManager.CONFIG.search_maxResults());
        for (Object ingredient : JeiModule.filter.getFilteredIngredients(VanillaTypes.ITEM_STACK)) {
            if (ItemUtils.ingredientMatches(ingredient, stack)) {
                return true;
            }
            checked++;
            if (checked >= max) break;
        }
        EditBox tf = JeiModule.getJEITextField();
        String q = tf != null ? tf.getValue().toLowerCase() : lastFilterText.toLowerCase();
        if (ConfigManager.CONFIG.search_searchCustom()) {
            if (stack.getDisplayName().getString().toLowerCase().contains(q)) {
                return true;
            }
        }
        if (ConfigManager.CONFIG.search_searchTooltip()) {
            try {
                TooltipFlag flag = TooltipFlag.NORMAL;
                Item.TooltipContext ctx = Item.TooltipContext.of(Minecraft.getInstance().level);
                List<net.minecraft.network.chat.Component> lines = stack.getTooltipLines(ctx, Minecraft.getInstance().player, flag);
                for (net.minecraft.network.chat.Component comp : lines) {
                    if (comp.getString().toLowerCase().contains(q)) {
                        return true;
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    public void tick() {
        if (!ConfigManager.CONFIG.search_enabled()) return;
        final Screen screen = Minecraft.getInstance().screen;
        if (!canShowIn(screen))
            return;
        if (enabled && JeiModule.filter != null && !JeiModule.filter.getFilterText().equals(lastFilterText)) {
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
        if (!ConfigManager.CONFIG.search_enabled()) {
            enabled = false;
            return;
        }
        enabled = !enabled;
        if (enabled && JeiModule.filter != null) {
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

