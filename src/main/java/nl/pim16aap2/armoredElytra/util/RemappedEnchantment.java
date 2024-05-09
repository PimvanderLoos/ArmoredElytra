package nl.pim16aap2.armoredElytra.util;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

import javax.annotation.Nullable;

/**
 * A class containing mappings for enchantments that have different names in different versions of Minecraft.
 */
public final class RemappedEnchantment
{
    public static final Enchantment UNBREAKING =
        findEnchantment("UNBREAKING", "DURABILITY");

    public static final Enchantment PROTECTION_ENVIRONMENTAL =
        findEnchantment("PROTECTION", "PROTECTION_ENVIRONMENTAL");

    public static final Enchantment PROTECTION_FIRE =
        findEnchantment("FIRE_PROTECTION", "PROTECTION_FIRE");

    public static final Enchantment PROTECTION_FALL =
        findEnchantment("FEATHER_FALLING", "PROTECTION_FALL");

    public static final Enchantment PROTECTION_EXPLOSIONS =
        findEnchantment("BLAST_PROTECTION", "PROTECTION_EXPLOSIONS");

    public static final Enchantment PROTECTION_PROJECTILE =
        findEnchantment("PROJECTILE_PROTECTION", "PROTECTION_PROJECTILE");


    private static Enchantment findEnchantment(String... names)
    {
        for (final String name : names)
        {
            final @Nullable Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(name));
            if (enchantment != null)
                return enchantment;
        }
        throw new IllegalArgumentException("No enchantment found with the given names: " + String.join(", ", names));
    }
}
