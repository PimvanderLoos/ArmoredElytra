package nl.pim16aap2.armoredElytra.nbtEditor;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

/**
 * Represents the data of an {@link ArmorTrim} on an {@link ItemStack}.
 *
 * @param pattern
 *     The pattern of the {@link ArmorTrim}.
 * @param material
 *     The material of the {@link ArmorTrim}.
 */
public record ArmorTrimData(Material pattern, Material material)
{
    public ArmorTrimData
    {
        if (pattern == null)
            throw new IllegalArgumentException("Pattern cannot be null.");
        if (material == null)
            throw new IllegalArgumentException("Material cannot be null.");
    }
}

