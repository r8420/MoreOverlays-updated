package at.ridgo8.moreoverlays.api.lightoverlay;

import net.minecraftforge.eventbus.api.Event;

public class LightOverlayReloadHandlerEvent extends Event {

    final boolean ignoreSpawner;
    Class<? extends ILightRenderer> rendererClass;
    Class<? extends ILightScanner> scannerClass;

    public LightOverlayReloadHandlerEvent(boolean ignoreSpawner, Class<? extends ILightRenderer> rendererClass, Class<? extends ILightScanner> scannerClass) {
        this.ignoreSpawner = ignoreSpawner;
        this.rendererClass = rendererClass;
        this.scannerClass = scannerClass;
    }

    public Class<? extends ILightRenderer> getRenderer() {
        return this.rendererClass;
    }

    public void setRenderer(Class<? extends ILightRenderer> rendererClass) {
        this.rendererClass = rendererClass;
    }

    public Class<? extends ILightScanner> getScanner() {
        return this.scannerClass;
    }

    public void setScanner(Class<? extends ILightScanner> scannerClass) {
        this.scannerClass = scannerClass;
    }

    public boolean isIgnoringSpawner() {
        return this.ignoreSpawner;
    }

}
