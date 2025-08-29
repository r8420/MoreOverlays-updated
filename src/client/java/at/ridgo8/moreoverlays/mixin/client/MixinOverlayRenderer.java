package at.ridgo8.moreoverlays.mixin.client;


import java.lang.reflect.Field;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import at.ridgo8.moreoverlays.chunkbounds.ChunkBoundsHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;



@Mixin(Gui.class)
public class MixinOverlayRenderer {
    @Inject(at = @At("TAIL"), method = "render")
    private void onRender(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        try {
            if (mc.getDebugOverlay().showDebugScreen()) {
                return;
            }
            // Checks if the debug screen is not shown
            if (!ChunkBoundsHandler.regionInfo.isEmpty()) {
                int y = 0;
                for (String text : ChunkBoundsHandler.regionInfo) {
                    guiGraphics.drawString(mc.font, text, 10, y += 10, 0xFFFFFF);
                }
            }
        } catch (NoSuchMethodError e) {
            try{
                Field renderDebugField = null;
                // Use reflection to check if the renderDebug field exists in mc.options. Note: remove this for future versions
                try{
                    renderDebugField = mc.options.getClass().getDeclaredField("field_1866");
                    renderDebugField.setAccessible(true);
                } catch(Exception o){
                    renderDebugField = mc.options.getClass().getField("renderDebug");
                }
                
                boolean renderDebug = renderDebugField.getBoolean(mc.options);

                if (renderDebug) {
                    return;
                }

                if (!ChunkBoundsHandler.regionInfo.isEmpty()) {
                    int y = 0;
                    for (String text : ChunkBoundsHandler.regionInfo) {
                        guiGraphics.drawString(mc.font, text, 10, y += 10, 0xFFFFFF);
                    }
                }
            } catch(Exception g){
                // Ignore
            }
            
        }
    }
}
