package nl.pim16aap2.armoredElytra.enchantment;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

interface IEnchantmentPlatform
{
    EnchantmentContainer getEnchantmentsFromItem(final ItemStack is);

    EnchantmentContainer getEnchantmentsFromBook(final ItemStack is);

    default EnchantmentContainer getEnchantments(final ItemStack is)
    {
        return is.getType() == Material.ENCHANTED_BOOK ? getEnchantmentsFromBook(is) : getEnchantmentsFromItem(is);
    }

    void applyEnchantments(final ItemStack is, final Map<String, Integer> enchantments);

    Map<String, Integer> merge(final Map<String, Integer> first, final Map<String, Integer> second);
}
