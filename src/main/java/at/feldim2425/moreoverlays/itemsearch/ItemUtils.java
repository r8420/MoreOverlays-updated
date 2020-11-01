package at.feldim2425.moreoverlays.itemsearch;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public final class ItemUtils {

    private ItemUtils() {
        //EMPTY
    }

    public static boolean ingredientMatches(final Object ingredient, final ItemStack stack) {
        if (ingredient instanceof ItemStack) {
            final ItemStack stack1 = (ItemStack) ingredient;
            return stack1.isItemEqualIgnoreDurability(stack) && JeiModule.areItemsEqualInterpreter(stack1, stack);
        } else if (ingredient instanceof EnchantmentData) {
            final ListNBT tags;
            if (stack.getItem() instanceof EnchantedBookItem) {
                tags = EnchantedBookItem.getEnchantments(stack);
            } else {
                tags = stack.getEnchantmentTagList();
            }
            return ItemUtils.getEnchantmentData(tags).stream().anyMatch((ench) -> ench.enchantment.equals(((EnchantmentData) ingredient).enchantment) &&
                    ench.enchantmentLevel == ((EnchantmentData) ingredient).enchantmentLevel);
        }

        return false;
    }

    public static Collection<EnchantmentData> getEnchantmentData(@Nullable final ListNBT nbtList) {
        if (nbtList == null) {
            return Collections.emptySet();
        }

        final Collection<EnchantmentData> enchantments = new HashSet<>();
        for (final INBT nbt : nbtList) {
            if (nbt instanceof CompoundNBT) {
                final CompoundNBT nbttagcompound = (CompoundNBT) nbt;
                final int id = nbttagcompound.getShort("id");
                final int level = nbttagcompound.getShort("lvl");
                final Enchantment enchantment = Enchantment.getEnchantmentByID(id);
                if (enchantment != null && level > 0) {
                    enchantments.add(new EnchantmentData(enchantment, level));
                }
            }
        }
        return enchantments;
    }

    public static boolean matchNBT(final ItemStack a, final ItemStack b) {
        return a.hasTag() == b.hasTag() && (!a.hasTag() || a.getTag().equals(b.getTag()));
    }
}
