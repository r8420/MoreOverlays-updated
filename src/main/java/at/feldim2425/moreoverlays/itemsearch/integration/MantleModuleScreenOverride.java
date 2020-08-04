package at.feldim2425.moreoverlays.itemsearch.integration;

import at.feldim2425.moreoverlays.api.itemsearch.IOverrideSlotPos;
import at.feldim2425.moreoverlays.api.itemsearch.IViewSlot;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.math.vector.Vector2f;
import slimeknights.mantle.client.screen.MultiModuleScreen;

public class MantleModuleScreenOverride implements IOverrideSlotPos {

	@Override
	public IViewSlot getSlot(ContainerScreen<?> gui, Slot slot) {
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
		public Slot getSlot() {
			return slot;
		}

		@Override
		public Vector2f getRenderPos(int guiLeft, int guiTop) {
			return new Vector2f(-guiLeft + gui.cornerX + slot.xPos, -guiTop + gui.cornerY + slot.yPos);
		}

		@Override
		public boolean canSearch() {
			return slot.inventory.getSizeInventory() > slot.getSlotIndex();
		}
	}
}
