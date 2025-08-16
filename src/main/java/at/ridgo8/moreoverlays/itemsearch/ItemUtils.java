package at.ridgo8.moreoverlays.itemsearch;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import net.minecraft.nbt.ListTag;

import javax.annotation.Nullable;

import at.ridgo8.moreoverlays.MoreOverlays;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public final class ItemUtils {

    private ItemUtils() {
        //EMPTY
    }

    public static boolean ingredientMatches(Object ingredient, ItemStack stack) {
        if (ingredient instanceof ItemStack) {
            ItemStack stack1 = (ItemStack) ingredient;
            return ItemStack.isSameItem(stack, stack1) && JeiModule.areItemsEqualInterpreter(stack1, stack);
        }

        return false;
    }

    public static Collection<EnchantmentInstance> getEnchantmentData(@Nullable ItemEnchantments nbtList) {
        if (nbtList == null) {
            return Collections.emptySet();
        }
        Collection<EnchantmentInstance> enchantments = new HashSet<>();
        for (Holder<Enchantment> nbt : nbtList.keySet()) {
            int level = nbt.value().getMaxLevel();
           
            if (nbt.value() != null && level > 0) {
                enchantments.add(new EnchantmentInstance(nbt, level));
            }
        }
        return enchantments;
    }

    public static boolean matchNBT(ItemStack a, ItemStack b) {
        MoreOverlays.logger.error(a.getTags());
        return a.getTags() == b.getTags(); //TODO: work on
    }
}
