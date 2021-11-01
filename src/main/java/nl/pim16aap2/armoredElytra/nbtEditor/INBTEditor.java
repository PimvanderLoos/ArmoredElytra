package nl.pim16aap2.armoredElytra.nbtEditor;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import javax.annotation.Nullable;
import java.util.List;

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
    default ItemStack addArmorNBTTags(ItemStack item, ArmorTier armorTier, boolean unbreakable)
    {
        return addArmorNBTTags(item, armorTier, unbreakable, (Color) null);
    }

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
    default ItemStack addArmorNBTTags(ItemStack item, ArmorTier armorTier, boolean unbreakable, final String name)
    {
        return addArmorNBTTags(item, armorTier, unbreakable, name, null, null);
    }

    /**
     * Adds a given {@link ArmorTier} to an item. The item will be cloned. Note that setting the armor tier to {@link
     * ArmorTier#NONE} has no effect (besides making a copy of the item). The default name for the given tier is
     * applied. See {@link ArmoredElytra#getArmoredElytraName(ArmorTier)}.
     *
     * @param item        The item.
     * @param armorTier   The {@link ArmorTier} that will be added to it.
     * @param unbreakable Whether the resulting item should be unbreakable.
     * @param color       The color of the armor to store. May be null.
     * @return The NEW item.
     */
    default ItemStack addArmorNBTTags(ItemStack item, ArmorTier armorTier, boolean unbreakable, final Color color)
    {
        return addArmorNBTTags(item, armorTier, unbreakable,
                               ArmoredElytra.getInstance().getArmoredElytraName(armorTier),
                               ArmoredElytra.getInstance().getElytraLore(armorTier), color);
    }

    /**
     * Adds a given {@link ArmorTier} to an item. The item will be cloned. Note that setting the armor tier to {@link
     * ArmorTier#NONE} has no effect (besides making a copy of the item). The default name for the given tier is
     * applied. See {@link ArmoredElytra#getArmoredElytraName(ArmorTier)}.
     *
     * @param item        The item.
     * @param armorTier   The {@link ArmorTier} that will be added to it.
     * @param unbreakable Whether the resulting item should be unbreakable.
     * @param name        The name of the item.
     * @param color       The color of the armor to store. May be null.
     * @return The NEW item.
     */
    default ItemStack addArmorNBTTags(ItemStack item, ArmorTier armorTier, boolean unbreakable, String name,
                                      Color color)
    {
        return addArmorNBTTags(item, armorTier, unbreakable, name,
                               ArmoredElytra.getInstance().getElytraLore(armorTier), color);
    }


    /**
     * Adds a given {@link ArmorTier} to an item. The item will be cloned. Note that setting the armor tier to {@link
     * ArmorTier#NONE} has no effect (besides making a copy of the item).
     *
     * @param item        The item.
     * @param armorTier   The {@link ArmorTier} that will be added to it.
     * @param unbreakable Whether the resulting item should be unbreakable.
     * @param name        The name of the item.
     * @param lore        The lore of the item.
     * @param color       The color of the armor to store. May be null.
     * @return The NEW item.
     */
    ItemStack addArmorNBTTags(ItemStack item, ArmorTier armorTier, boolean unbreakable, final String name,
                              @Nullable List<String> lore, @Nullable Color color);

    /**
     * Checks which {@link ArmorTier} is on an item.
     *
     * @param item The item to check.
     * @return The {@link ArmorTier} that is on the item. If none is found, {@link ArmorTier#NONE} is returned.
     */
    ArmorTier getArmorTier(@Nullable ItemStack item);

    /**
     * Gets the Color of an armored elytra.
     * <p>
     * If the provided {@link ItemStack} is not an AE, null is returned.
     *
     * @param item The armored elytra to check.
     * @return The color of the armored elytra, if the input is a colored armored elytra, otherwise null.
     */
    Color getColorOfArmoredElytra(@Nullable ItemStack item);

    /**
     * Updates the durability values of an item.
     *
     * @param itemStack         The itemstack to which the durability values will be applied.
     * @param realDurability    The real durability to store in NBT.
     * @param displayDurability The durability value to display on the item. This is the durability value the client can
     *                          actually see.This only works if the item's meta is an instance of {@link Damageable}.
     */
    void updateDurability(ItemStack itemStack, int realDurability, int displayDurability);

    /**
     * Gets the real durability value as stored in the NBT of an armored elytra.
     *
     * @param itemStack The item for which to retrieve the real durability.
     * @param armorTier The armor tier of the armored elytra. If this is null, it will be retrieved from NBT.
     * @return The real durability of the itemstack if the itemstack has the AE durability attribute, or -1 otherwise.
     */
    int getRealDurability(ItemStack itemStack, @Nullable ArmorTier armorTier);
}
