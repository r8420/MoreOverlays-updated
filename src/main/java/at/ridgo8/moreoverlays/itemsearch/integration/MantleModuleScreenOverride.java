package at.ridgo8.moreoverlays.itemsearch.integration;

import at.ridgo8.moreoverlays.api.itemsearch.IOverrideSlotPos;
import at.ridgo8.moreoverlays.api.itemsearch.IViewSlot;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.Vec2;
import slimeknights.mantle.client.screen.MultiModuleScreen;

public class MantleModuleScreenOverride implements IOverrideSlotPos {

    @Override
    public IViewSlot getSlot(AbstractContainerScreen<?> gui, Slot slot) {
        if (gui instanceof MultiModuleScreen) {
            return new ModuleScreenSlotView(slot, (MultiModuleScreen<?>) gui);
        }
        return null;
    }

    public static class ModuleScreenSlotView implements IViewSlot {

        private final Slot slot;
        private final MultiModuleScreen<?> gui;

        public ModuleScreenSlotView(Slot slot, MultiModuleScreen<?> gui) {
            this.slot = slot;
            this.gui = gui;
        }

        @Override
        public Slot slot() {
            return slot;
        }

        @Override
        public Vec2 getRenderPos(int guiLeft, int guiTop) {
            return new Vec2(-guiLeft + gui.cornerX + slot.x, -guiTop + gui.cornerY + slot.y);
        }

        @Override
        public boolean canSearch() {
            return slot.container.getContainerSize() > slot.getSlotIndex();
        }
    }
}