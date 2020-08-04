package at.feldim2425.moreoverlays.itemsearch;

import at.feldim2425.moreoverlays.api.itemsearch.IViewSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.math.vector.Vector2f;

public class DefaultSlotView implements IViewSlot {

	private final Slot slot;

	public DefaultSlotView(Slot slot) {
		this.slot = slot;
	}

	@Override
	public Slot getSlot() {
		return slot;
	}

	@Override
	public Vector2f getRenderPos(int guiLeft, int guiTop) {
		return new Vector2f(slot.xPos,slot.yPos);
	}

	@Override
	public boolean canSearch() {
		return true;
	}
}
