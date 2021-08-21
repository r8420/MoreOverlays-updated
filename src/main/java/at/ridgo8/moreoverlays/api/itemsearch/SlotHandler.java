package at.ridgo8.moreoverlays.api.itemsearch;

import at.ridgo8.moreoverlays.itemsearch.DefaultSlotView;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;

public final class SlotHandler {

    public static final SlotHandler INSTANCE = new SlotHandler();

    private final ArrayList<IOverrideSlotPos> overrides = new ArrayList<>();

    /*
     *  Register a IOverrideSlotPos for non GuiContainers
     */
    public void addPositionOverride(IOverrideSlotPos slotPos) {
        if (overrides.contains(slotPos) || slotPos instanceof AbstractContainerScreen<?>)
            return;
        overrides.add(slotPos);
    }

    public IViewSlot getViewSlot(AbstractContainerScreen<?> container, Slot slot) {
        if (container instanceof IOverrideSlotPos) {
            IViewSlot slot1 = ((IOverrideSlotPos) container).getSlot(container, slot);
            if (slot1 != null)
                return slot1;
        } else {
            if (!overrides.isEmpty()) {
                for (IOverrideSlotPos override : overrides) {
                    IViewSlot slot1 = override.getSlot(container, slot);
                    if (slot1 != null)
                        return slot1;
                }
            }
        }

        return new DefaultSlotView(slot);
    }
}
