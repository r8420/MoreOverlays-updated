package at.ridgo8.moreoverlays.itemsearch;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;

public class ItemUtils {

    public static boolean ingredientMatches(Object ingredient, ItemStack stack) {
        if (ingredient instanceof ItemStack item) {
            return ItemStack.isSameItemSameComponents(item, stack);
        }
        if (ingredient instanceof EnchantmentInstance enchInst) {
            ItemEnchantments tags = stack.getEnchantments();
            return getEnchantmentData(tags).stream().anyMatch((ench) -> ench.enchantment.equals(enchInst.enchantment()) && ench.level == enchInst.level());
        }
        return false;
    }

    public static boolean matchNBT(ItemStack a, ItemStack b) {
        // Fallback: compare items and components including enchantments and custom data
        return ItemStack.isSameItemSameComponents(a, b);
    }

    public record EnchantmentData(net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment, int level) {}

    public static List<EnchantmentData> getEnchantmentData(ItemEnchantments tags) {
        java.util.ArrayList<EnchantmentData> list = new java.util.ArrayList<>();
        for (var entry : tags.entrySet()) {
            list.add(new EnchantmentData(entry.getKey(), entry.getIntValue()));
        }
        return list;
    }
}