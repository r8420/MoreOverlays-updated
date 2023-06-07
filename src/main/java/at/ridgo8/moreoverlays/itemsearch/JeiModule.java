package at.ridgo8.moreoverlays.itemsearch;

import at.ridgo8.moreoverlays.MoreOverlays;
import at.ridgo8.moreoverlays.util.ReflectionUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

@JeiPlugin
public class JeiModule implements IModPlugin {

    public static IIngredientListOverlay overlay;
    public static IIngredientFilter filter;
    private static IJeiHelpers jeiHelpers;
    private static EditBox textField;

    public static void updateModule() {
        if (overlay != null) {
            textField = ReflectionUtil.findFieldsWithClass(overlay, EditBox.class)
                .findFirst()
                .orElseGet(() -> {
                    MoreOverlays.logger.error("Something went wrong. Could not find JEI Search Text Field object");
                    return null;
                });
        } else {
            textField = null;
        }
    }

    public static EditBox getJEITextField() {
        return textField;
    }

    public static boolean areItemsEqualInterpreter(ItemStack stack1, ItemStack stack2) {
        if (jeiHelpers == null) {
            return ItemUtils.matchNBT(stack1, stack2);
        }
        return jeiHelpers.getStackHelper().isEquivalent(stack1, stack2, UidContext.Ingredient);
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
        overlay = jeiRuntime.getIngredientListOverlay();
        filter = jeiRuntime.getIngredientFilter();
        updateModule();
    }

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registration) {
        jeiHelpers = registration.getJeiHelpers();
    }

    @Override
    @Nonnull
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(MoreOverlays.MOD_ID, "jei_module");
    }
}
