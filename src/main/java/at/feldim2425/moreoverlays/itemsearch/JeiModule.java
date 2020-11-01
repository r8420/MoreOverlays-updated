package at.feldim2425.moreoverlays.itemsearch;

import at.feldim2425.moreoverlays.MoreOverlays;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.gui.overlay.IngredientListOverlay;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

@JeiPlugin
public class JeiModule implements IModPlugin {

    public static IIngredientListOverlay overlay;
    public static IIngredientFilter filter;
    private static IJeiHelpers jeiHelpers;
    private static IngredientListOverlay overlayInternal;
    private static TextFieldWidget textField;

    public static void updateModule() {
        if (JeiModule.overlay instanceof IngredientListOverlay) {
			JeiModule.overlayInternal = ((IngredientListOverlay) JeiModule.overlay);
            try {
                final Field searchField = IngredientListOverlay.class.getDeclaredField("searchField");
                searchField.setAccessible(true);
				JeiModule.textField = (TextFieldWidget) searchField.get(JeiModule.overlayInternal);
            } catch (final NoSuchFieldException | IllegalAccessException e) {
                MoreOverlays.logger.error("Something went wrong. Tried to load JEI Search Text Field object");
                e.printStackTrace();
            }
        } else {
			JeiModule.overlayInternal = null;
			JeiModule.textField = null;
        }
    }

    public static TextFieldWidget getJEITextField() {
        return JeiModule.textField;
    }

    public static boolean areItemsEqualInterpreter(final ItemStack stack1, final ItemStack stack2) {
        if (JeiModule.jeiHelpers == null) {
            return ItemUtils.matchNBT(stack1, stack2);
        }
        return JeiModule.jeiHelpers.getStackHelper().isEquivalent(stack1, stack2);

		/*
		String info1 = subtypes.getSubtypeInfo(stack1);
		String info2 = subtypes.getSubtypeInfo(stack2);
		if (info1 == null || info2 == null) {
			return ItemUtils.matchNBT(stack1, stack2);
		} else {
			return info1.equals(info2);
		}*/
    }

    @Override
    public void onRuntimeAvailable(@Nonnull final IJeiRuntime jeiRuntime) {
		JeiModule.overlay = jeiRuntime.getIngredientListOverlay();
		JeiModule.filter = jeiRuntime.getIngredientFilter();
		JeiModule.updateModule();
    }

    @Override
    public void registerAdvanced(final IAdvancedRegistration registration) {
		JeiModule.jeiHelpers = registration.getJeiHelpers();
    }

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(MoreOverlays.MOD_ID, "jei_module");
    }
}
