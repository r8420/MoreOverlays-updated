package at.ridgo8.moreoverlays.itemsearch.integration;

import at.ridgo8.moreoverlays.api.itemsearch.IOverrideSlotPos;
import at.ridgo8.moreoverlays.api.itemsearch.IViewSlot;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.math.vector.Vector2f;
//import slimeknights.mantle.client.screen.MultiModuleScreen;

public class MantleModuleScreenOverride implements IOverrideSlotPos {

    @Override
    public IViewSlot getSlot(ContainerScreen<?> gui, Slot slot) {
//        if (gui instanceof MultiModuleScreen) {
//            return new ModuleScreenSlotView(slot, (MultiModuleScreen<?>) gui);
//        }
        return null;
    }
}
