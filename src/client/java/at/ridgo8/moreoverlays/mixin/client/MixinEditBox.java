package at.ridgo8.moreoverlays.mixin.client;

import at.ridgo8.moreoverlays.ClientRegistrationHandler;
import at.ridgo8.moreoverlays.itemsearch.GuiRenderer;
import at.ridgo8.moreoverlays.itemsearch.JeiModule;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditBox.class)
public abstract class MixinEditBox {
    private long firstClick = 0;

    @Inject(method = "onClick", at = @At("HEAD"), cancellable = true)
    private void onClick(MouseButtonEvent mouseButtonEvent, boolean bl, CallbackInfo cir) {
        EditBox textField = (EditBox) (Object) this;
        
        if(ClientRegistrationHandler.isJeiInstalled() && JeiModule.getJEITextField() != null && textField.getClass() == JeiModule.getJEITextField().getClass()){
            long now = System.currentTimeMillis();
            if (now - firstClick < 1000) {
                GuiRenderer.INSTANCE.toggleMode();
                firstClick = 0;
            } else {
                firstClick = now;
            }
        }
    }
}