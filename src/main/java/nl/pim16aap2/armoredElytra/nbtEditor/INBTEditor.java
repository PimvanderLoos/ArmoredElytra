package nl.pim16aap2.armoredElytra.nbtEditor;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import org.bukkit.inventory.ItemStack;

public interface INBTEditor
{
    /**
     * Adds a given {@link ArmorTier} to an item. The item will be cloned. Note that setting the armor tier to {@link
     * ArmorTier#NONE} has no effect (besides making a copy of the item). The default name for the given tier is
     * applied. See {@link ArmoredElytra#getArmoredElytraName(ArmorTier)}.
     *
     * @param item        The item.
     * @param armorTier   The {@link ArmorTier} that will be added to it.
     * @param unbreakable Whether the resulting item should be unbreakable.
     * @return The NEW item.
     */
    ItemStack addArmorNBTTags(ItemStack item, ArmorTier armorTier, boolean unbreakable);

    /**
     * Adds a given {@link ArmorTier} to an item. The item will be cloned. Note that setting the armor tier to {@link
     * ArmorTier#NONE} has no effect (besides making a copy of the item).
     *
     * @param item        The item.
     * @param armorTier   The {@link ArmorTier} that will be added to it.
     * @param unbreakable Whether the resulting item should be unbreakable.
     * @param name        The name fo the item.
     * @return The NEW item.
     */
    ItemStack addArmorNBTTags(ItemStack item, ArmorTier armorTier, boolean unbreakable, final String name);

    /**
     * Checks which {@link ArmorTier} is on an item.
     *
     * @param item The item to check.
     * @return The {@link ArmorTier} that is on the item. If none is found, {@link ArmorTier#NONE} is returned.
     */
    ArmorTier getArmorTier(ItemStack item);
}
