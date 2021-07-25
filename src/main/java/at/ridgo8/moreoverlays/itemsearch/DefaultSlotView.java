package at.ridgo8.moreoverlays.itemsearch;

import at.ridgo8.moreoverlays.api.itemsearch.IViewSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.Vec2;

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
    public Vec2 getRenderPos(int guiLeft, int guiTop) {
        return new Vec2(slot.x, slot.y);
    }

    @Override
    public boolean canSearch() {
        return true;
    }
}
