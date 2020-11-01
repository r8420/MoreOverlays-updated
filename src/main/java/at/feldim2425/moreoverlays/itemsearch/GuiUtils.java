package at.feldim2425.moreoverlays.itemsearch;

import at.feldim2425.moreoverlays.MoreOverlays;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

public class GuiUtils {

    private static Field fieldLeft;
    private static Field fieldTop;

    public static void initUtil() {
        try {
            GuiUtils.fieldLeft = ObfuscationReflectionHelper.findField(ContainerScreen.class, "field_147003_i");
            GuiUtils.fieldLeft.setAccessible(true);

            GuiUtils.fieldTop = ObfuscationReflectionHelper.findField(ContainerScreen.class, "field_147009_r");
            GuiUtils.fieldTop.setAccessible(true);
        } catch (final ObfuscationReflectionHelper.UnableToFindFieldException e) {
            MoreOverlays.logger.error("Tried to load gui coordinate fields for reflection");
            e.printStackTrace();
            GuiUtils.fieldTop = null;
            GuiUtils.fieldLeft = null;
        }
    }

    public static int getGuiTop(final ContainerScreen<?> container) {
        if (GuiUtils.fieldTop == null) {
            return 0;
        }

        try {
            return GuiUtils.fieldTop.getInt(container);
        } catch (final IllegalAccessException ignore) {
            // EMPTY
        }
        return 0;
    }

    public static int getGuiLeft(final ContainerScreen<?> container) {
        if (GuiUtils.fieldLeft == null) {
            return 0;
        }

        try {
            return GuiUtils.fieldLeft.getInt(container);
        } catch (final IllegalAccessException ignore) {
            // EMPTY
        }
        return 0;
    }
}
