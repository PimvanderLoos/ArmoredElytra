package nl.pim16aap2.armoredElytra.util;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A class containing mappings for enchantments that have different names in different versions of Minecraft.
 */
public final class RemappedEnchantment
{
    private static final RemappedEnchantment REMAPPED_UNBREAKING =
        new RemappedEnchantment("unbreaking", "durability");

    private static final RemappedEnchantment REMAPPED_PROTECTION_ENVIRONMENTAL =
        new RemappedEnchantment("protection", "protection_environmental");

    private static final RemappedEnchantment REMAPPED_PROTECTION_FIRE =
        new RemappedEnchantment("fire_protection", "protection_fire");

    private static final RemappedEnchantment REMAPPED_PROTECTION_FALL =
        new RemappedEnchantment("feather_falling", "protection_fall");

    private static final RemappedEnchantment REMAPPED_PROTECTION_EXPLOSIONS =
        new RemappedEnchantment("blast_protection", "protection_explosions");

    private static final RemappedEnchantment REMAPPED_PROTECTION_PROJECTILE =
        new RemappedEnchantment("projectile_protection", "protection_projectile");

    private static final List<RemappedEnchantment> REMAPPED_ENCHANTMENTS = List.of(
        REMAPPED_UNBREAKING,
        REMAPPED_PROTECTION_ENVIRONMENTAL,
        REMAPPED_PROTECTION_FIRE,
        REMAPPED_PROTECTION_FALL,
        REMAPPED_PROTECTION_EXPLOSIONS,
        REMAPPED_PROTECTION_PROJECTILE
    );

    /*
     * The resulting enchantments.
     */
    public static final Enchantment UNBREAKING = REMAPPED_UNBREAKING.getEnchantment();

    public static final Enchantment PROTECTION_ENVIRONMENTAL = REMAPPED_PROTECTION_ENVIRONMENTAL.getEnchantment();

    public static final Enchantment PROTECTION_FIRE = REMAPPED_PROTECTION_FIRE.getEnchantment();

    public static final Enchantment PROTECTION_FALL = REMAPPED_PROTECTION_FALL.getEnchantment();

    public static final Enchantment PROTECTION_EXPLOSIONS = REMAPPED_PROTECTION_EXPLOSIONS.getEnchantment();

    public static final Enchantment PROTECTION_PROJECTILE = REMAPPED_PROTECTION_PROJECTILE.getEnchantment();


    /**
     * The names of the enchantment in different versions of Minecraft.
     */
    private final List<String> names;

    private final Enchantment enchantment;

    private RemappedEnchantment(String... names)
    {
        this.names = List.of(names);
        this.enchantment = findEnchantment();
    }

    /**
     * Gets all remapped enchantments.
     *
     * @return The remapped enchantments.
     */
    public static List<RemappedEnchantment> getRemappedEnchantments()
    {
        return REMAPPED_ENCHANTMENTS;
    }

    /**
     * Gets the names of the enchantment in different versions of Minecraft.
     *
     * @return The names of the enchantment in different versions of Minecraft.
     */
    public List<String> getNames()
    {
        return names;
    }

    /**
     * Gets the enchantment that corresponds to one of the {@link #names}.
     *
     * @return The enchantment one of the {@link #names} corresponds to.
     */
    public Enchantment getEnchantment()
    {
        return enchantment;
    }

    /**
     * Finds the enchantment that corresponds to one of the {@link #names}.
     *
     * @return The enchantment one of the {@link #names} corresponds to.
     *
     * @throws IllegalArgumentException
     *     If none of the {@link #names} correspond to an enchantment.
     */
    private Enchantment findEnchantment()
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
