package at.ridgo8.moreoverlays.itemsearch;

import at.ridgo8.moreoverlays.MoreOverlays;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

public class GuiUtils {

    private static Field fieldLeft;
    private static Field fieldTop;

    public static void initUtil() {
        try {
            fieldLeft = ObfuscationReflectionHelper.findField(AbstractContainerScreen.class, "f_97735_");
            fieldLeft.setAccessible(true);

            fieldTop = ObfuscationReflectionHelper.findField(AbstractContainerScreen.class, "f_97736_");
            fieldTop.setAccessible(true);
        } catch (ObfuscationReflectionHelper.UnableToFindFieldException e) {
            MoreOverlays.logger.error("Tried to load gui coordinate fields for reflection");
            e.printStackTrace();
            fieldTop = null;
            fieldLeft = null;
        }
    }

    public static int getGuiTop(AbstractContainerScreen<?> container) {
        if (fieldTop == null) {
            return 0;
        }

        try {
            return fieldTop.getInt(container);
        } catch (IllegalAccessException ignore) {
            // EMPTY
        }
        return 0;
    }

    public static int getGuiLeft(AbstractContainerScreen<?> container) {
        if (fieldLeft == null) {
            return 0;
        }

        try {
            return fieldLeft.getInt(container);
        } catch (IllegalAccessException ignore) {
            // EMPTY
        }
        return 0;
    }
}
