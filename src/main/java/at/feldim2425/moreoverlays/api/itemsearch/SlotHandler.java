package at.feldim2425.moreoverlays.api.itemsearch;

import at.feldim2425.moreoverlays.itemsearch.DefaultSlotView;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Slot;

import java.util.ArrayList;

public final class SlotHandler {

    public static final SlotHandler INSTANCE = new SlotHandler();

    private final ArrayList<IOverrideSlotPos> overrides = new ArrayList<>();

    /*
     *  Register a IOverrideSlotPos for non GuiContainers
     */
    public void addPositionOverride(final IOverrideSlotPos slotPos) {
        if (this.overrides.contains(slotPos) || slotPos instanceof ContainerScreen<?>)
            return;
		this.overrides.add(slotPos);
    }

    public IViewSlot getViewSlot(final ContainerScreen<?> container, final Slot slot) {
        if (container instanceof IOverrideSlotPos) {
            final IViewSlot slot1 = ((IOverrideSlotPos) container).getSlot(container, slot);
            if (slot1 != null)
                return slot1;
        } else {
            if (!this.overrides.isEmpty()) {
                for (final IOverrideSlotPos override : this.overrides) {
                    final IViewSlot slot1 = override.getSlot(container, slot);
                    if (slot1 != null)
                        return slot1;
                }
            }
        }

        return new DefaultSlotView(slot);
    }
}
