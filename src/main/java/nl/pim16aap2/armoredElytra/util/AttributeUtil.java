package nl.pim16aap2.armoredElytra.util;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Utility class for attributes.
 */
public class AttributeUtil
{
    /**
     * The cross-version mapped attribute for armor.
     */
    public static final Attribute ATTRIBUTE_ARMOR = getAttribute("armor");

    /**
     * The cross-version mapped attribute for armor toughness.
     */
    public static final Attribute ATTRIBUTE_ARMOR_TOUGHNESS = getAttribute("armor_toughness");

    /**
     * The cross-version mapped attribute for knockback resistance.
     */
    public static final Attribute ATTRIBUTE_KNOCKBACK_RESISTANCE = getAttribute("knockback_resistance");

    private static Attribute getAttribute(String key)
    {
        // First try to use the provided name for retrieval.
        // If that doesn't work, we try again, but now prepend "generic." to the key.
        // This is the format used before ~1.21.4 (or 2/3, not sure).
        @Nullable Attribute ret = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(key));
        if (ret == null)
            ret = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic." + key));
        return Objects.requireNonNull(ret, "Could not find attribute with key: '" + key + "'!");
    }
}
