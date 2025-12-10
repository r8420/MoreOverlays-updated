package at.ridgo8.moreoverlays.itemsearch;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.util.ReflectionUtil;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.item.ItemStack;

public class JeiModule {

    public static IIngredientListOverlay overlay;
    public static IIngredientFilter filter;
    private static IJeiHelpers jeiHelpers;
    private static EditBox textField;
    private static boolean missingTextFieldLogged = false;

    public static void updateModule() {
        if (overlay != null) {
            EditBox found = ReflectionUtil.findFieldsWithClass(overlay, EditBox.class)
                .findFirst()
                .orElse(null);
            if (found == null) {
                textField = null;
                if (!missingTextFieldLogged) {
                    // With some UI mods (e.g., EMI) JEI's search box may not be directly discoverable via reflection. Suppress noisy errors.
                    MoreOverlays.logger.warn("JEI search text field not found via reflection (possibly due to other UI mods). Using fallback: no text field.");
                    missingTextFieldLogged = true;
                }
            } else {
                textField = found;
                missingTextFieldLogged = false;
            }
        } else {
            textField = null;
            missingTextFieldLogged = false;
        }
    }

    public static EditBox getJEITextField() {
        return textField;
    }

    public static boolean logMissingSearchTextFieldOnce() {
        if (overlay != null && textField == null && !missingTextFieldLogged) {
            MoreOverlays.logger.warn("JEI search text field not found via reflection (possibly due to other UI mods). Using fallback: no text field.");
            missingTextFieldLogged = true;
            return true;
        }
        return false;
    }

    public static boolean areItemsEqualInterpreter(ItemStack stack1, ItemStack stack2) {
        if (jeiHelpers == null) {
            return ItemUtils.matchNBT(stack1, stack2);
        }
        return jeiHelpers.getStackHelper().isEquivalent(stack1, stack2, UidContext.Ingredient);
    }
}
