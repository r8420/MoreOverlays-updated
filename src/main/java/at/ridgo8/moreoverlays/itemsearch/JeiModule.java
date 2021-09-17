package at.ridgo8.moreoverlays.itemsearch;

import at.ridgo8.moreoverlays.MoreOverlays;
//import mezz.jei.api.IModPlugin;
//import mezz.jei.api.JeiPlugin;
//import mezz.jei.api.helpers.IJeiHelpers;
//import mezz.jei.api.registration.IAdvancedRegistration;
//import mezz.jei.api.runtime.IIngredientFilter;
//import mezz.jei.api.runtime.IIngredientListOverlay;
//import mezz.jei.api.runtime.IJeiRuntime;
//import mezz.jei.gui.overlay.IngredientListOverlay;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

//@JeiPlugin
public class JeiModule {//implements IModPlugin {

//    public static IIngredientListOverlay overlay;
//    public static IIngredientFilter filter;
//    private static IJeiHelpers jeiHelpers;
//    private static IngredientListOverlay overlayInternal;
    private static EditBox textField;

    public static void updateModule() {
//        if (overlay instanceof IngredientListOverlay) {
//            overlayInternal = ((IngredientListOverlay) overlay);
            try {
                Field searchField = null;//IngredientListOverlay.class.getDeclaredField("searchField");
                searchField.setAccessible(true);
                textField = (EditBox) searchField.get(null);
            } catch (IllegalAccessException e) {
                MoreOverlays.logger.error("Something went wrong. Tried to load JEI Search Text Field object");
                e.printStackTrace();
            }
    }

    public static EditBox getJEITextField() {
        return textField;
    }

    public static boolean areItemsEqualInterpreter(ItemStack stack1, ItemStack stack2) {
        if (null == null) {
            return ItemUtils.matchNBT(stack1, stack2);
        }
        return false;//jeiHelpers.getStackHelper().isEquivalent(stack1, stack2);

		/*
		String info1 = subtypes.getSubtypeInfo(stack1);
		String info2 = subtypes.getSubtypeInfo(stack2);
		if (info1 == null || info2 == null) {
			return ItemUtils.matchNBT(stack1, stack2);
		} else {
			return info1.equals(info2);
		}*/
    }

//    @Override
//    public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
//        overlay = jeiRuntime.getIngredientListOverlay();
//        filter = jeiRuntime.getIngredientFilter();
//        updateModule();
//    }
//
//    @Override
//    public void registerAdvanced(IAdvancedRegistration registration) {
//        jeiHelpers = registration.getJeiHelpers();
//    }

//    @Override
//    public ResourceLocation getPluginUid() {
//        return new ResourceLocation(MoreOverlays.MOD_ID, "jei_module");
//    }
}
