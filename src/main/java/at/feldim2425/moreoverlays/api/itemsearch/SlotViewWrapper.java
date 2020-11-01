package at.feldim2425.moreoverlays.api.itemsearch;

public class SlotViewWrapper {
    private final IViewSlot view;
    private boolean enableOverlay;

    public SlotViewWrapper(final IViewSlot view) {
        this.view = view;
    }

    public boolean isEnableOverlay() {
        return this.enableOverlay;
    }

    public void setEnableOverlay(final boolean enableOverlay) {
        this.enableOverlay = enableOverlay;
    }

    public IViewSlot getView() {
        return this.view;
    }
}
