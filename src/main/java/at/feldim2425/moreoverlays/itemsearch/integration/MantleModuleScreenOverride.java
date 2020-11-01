package at.feldim2425.moreoverlays.itemsearch.integration;

import at.feldim2425.moreoverlays.api.itemsearch.IOverrideSlotPos;
import at.feldim2425.moreoverlays.api.itemsearch.IViewSlot;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.math.vector.Vector2f;
import slimeknights.mantle.client.screen.MultiModuleScreen;

public class MantleModuleScreenOverride implements IOverrideSlotPos {

    @Override
    public IViewSlot getSlot(final ContainerScreen<?> gui, final Slot slot) {
        if (gui instanceof MultiModuleScreen) {
            return new ModuleScreenSlotView(slot, (MultiModuleScreen<?>) gui);
        }
        return null;
    }

    public static class ModuleScreenSlotView implements IViewSlot {

        private final Slot slot;
        private final MultiModuleScreen<?> gui;

        public ModuleScreenSlotView(final Slot slot, final MultiModuleScreen<?> gui) {
            this.slot = slot;
            this.gui = gui;
        }

        @Override
        public Slot getSlot() {
            return this.slot;
        }

        @Override
        public Vector2f getRenderPos(final int guiLeft, final int guiTop) {
            return new Vector2f(-guiLeft + this.gui.cornerX + this.slot.xPos, -guiTop + this.gui.cornerY + this.slot.yPos);
        }

        @Override
        public boolean canSearch() {
            return this.slot.inventory.getSizeInventory() > this.slot.getSlotIndex();
        }
    }
}
