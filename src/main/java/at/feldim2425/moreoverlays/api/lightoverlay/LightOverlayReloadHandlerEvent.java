package at.feldim2425.moreoverlays.api.lightoverlay;

import net.minecraftforge.eventbus.api.Event;

public class LightOverlayReloadHandlerEvent extends Event {

    final boolean ignoreSpawner;
    Class<? extends ILightRenderer> rendererClass;
    Class<? extends ILightScanner> scannerClass;

    public LightOverlayReloadHandlerEvent(final boolean ignoreSpawner, final Class<? extends ILightRenderer> rendererClass, final Class<? extends ILightScanner> scannerClass) {
        this.ignoreSpawner = ignoreSpawner;
        this.rendererClass = rendererClass;
        this.scannerClass = scannerClass;
    }

    public Class<? extends ILightRenderer> getRenderer() {
        return rendererClass;
    }

    public void setRenderer(final Class<? extends ILightRenderer> rendererClass) {
        this.rendererClass = rendererClass;
    }

    public Class<? extends ILightScanner> getScanner() {
        return scannerClass;
    }

    public void setScanner(final Class<? extends ILightScanner> scannerClass) {
        this.scannerClass = scannerClass;
    }

    public boolean isIgnoringSpawner() {
        return ignoreSpawner;
    }

}
