package nl.pim16aap2.armoredElytra.util;

import org.bukkit.Material;

import javax.annotation.Nullable;

public final class MappedMaterial
{
    public static final @Nullable Material COPPER_CHESTPLATE = Material.getMaterial("COPPER_CHESTPLATE");
    public static final @Nullable Material COPPER_INGOT = Material.getMaterial("COPPER_INGOT");

    private MappedMaterial()
    {
        throw new UnsupportedOperationException();
    }
}
