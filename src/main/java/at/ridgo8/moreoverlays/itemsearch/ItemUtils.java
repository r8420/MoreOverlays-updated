package at.ridgo8.moreoverlays.itemsearch;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;

import javax.annotation.Nullable;
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
            return stack1.sameItem(stack) && JeiModule.areItemsEqualInterpreter(stack1, stack);
        } else if (ingredient instanceof EnchantmentInstance) {
            ListTag tags;
            if (stack.getItem() instanceof EnchantedBookItem) {
                tags = EnchantedBookItem.getEnchantments(stack);
            } else {
                tags = stack.getEnchantmentTags();
            }
            return getEnchantmentData(tags).stream().anyMatch((ench) -> ench.enchantment.equals(((EnchantmentInstance) ingredient).enchantment) &&
                    ench.level == ((EnchantmentInstance) ingredient).level);
        }

        return false;
    }

    public static Collection<EnchantmentInstance> getEnchantmentData(@Nullable ListTag nbtList) {
        if (nbtList == null) {
            return Collections.emptySet();
        }

        Collection<EnchantmentInstance> enchantments = new HashSet<>();
        for (Tag nbt : nbtList) {
            if (nbt instanceof CompoundTag) {
                CompoundTag nbttagcompound = (CompoundTag) nbt;
                int id = nbttagcompound.getShort("id");
                int level = nbttagcompound.getShort("lvl");
                Enchantment enchantment = Enchantment.byId(id);
                if (enchantment != null && level > 0) {
                    enchantments.add(new EnchantmentInstance(enchantment, level));
                }
            }
        }
        return enchantments;
    }

    public static boolean matchNBT(ItemStack a, ItemStack b) {
        return a.hasTag() == b.hasTag() && (!a.hasTag() || a.getTag().equals(b.getTag()));
    }
}
