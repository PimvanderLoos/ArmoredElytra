package nl.pim16aap2.armoredElytra.nbtEditor;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.AttributeUtil;
import nl.pim16aap2.armoredElytra.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.semver4j.Semver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class NBTEditor
{
    public static final boolean HAS_FIRE_RESISTANT_METHOD =
        ArmoredElytra.SERVER_VERSION.isGreaterThanOrEqualTo(Semver.of(1, 20, 5));

    private static final NamespacedKey ARMOR_TIER_KEY =
        new NamespacedKey(ArmoredElytra.getInstance(), "armor_tier_level");

    private static final NamespacedKey ARMOR_COLOR_KEY =
        new NamespacedKey(ArmoredElytra.getInstance(), "armored_elytra_color");

    private static final NamespacedKey DURABILITY_KEY =
        new NamespacedKey(ArmoredElytra.getInstance(), "armored_elytra_durability");

    /**
     * Magic value to indicate that an item has no custom durability.
     */
    public static final int HAS_NO_CUSTOM_DURABILITY = -1;

    private final AttributeModifierManager attributeModifierManager;
    private final @Nullable TrimEditor trimEditor;

    public NBTEditor()
    {
        attributeModifierManager = AttributeModifierManager.create(ArmoredElytra.SERVER_VERSION);
        trimEditor = newTrimEditor();
    }

    /**
     * Checks if an item has a specific {@link PersistentDataType} with a specific key.
     *
     * @param item
     *     The item to check.
     * @param key
     *     The key to check for.
     * @param type
     *     The type to check for.
     * @param <P>
     *     The type of the persistent data.
     * @param <C>
     *     The type of the container.
     *
     * @return True if the item has the specified key with the specified type, false otherwise.
     */
    public static <P, C> boolean hasPdcWithWithKey(
        @Nullable ItemStack item, @Nonnull NamespacedKey key, @Nonnull PersistentDataType<P, C> type)
    {
        if (item == null || !item.hasItemMeta())
            return false;

        final @Nullable ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return false;

        final PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(key, type);
    }

    /**
     * Gets the real durability value as stored in the NBT of an armored elytra.
     *
     * @param itemStack
     *     The item for which to retrieve the real durability.
     * @param providedTier
     *     The armor tier of the armored elytra. If this is null, it will be retrieved from NBT.
     *
     * @return The real durability of the itemstack if the itemstack has the AE durability attribute, or
     * {@link #HAS_NO_CUSTOM_DURABILITY} otherwise.
     */
    public int getRealDurability(ItemStack itemStack, @Nullable ArmorTier providedTier)
    {
        final @Nullable ItemMeta meta = itemStack.getItemMeta();
        final ArmorTier armorTier = providedTier == null ? getArmorTier(meta) : providedTier;

        if (armorTier == ArmorTier.NONE)
            return HAS_NO_CUSTOM_DURABILITY;

        final @Nullable Integer realDurability =
            Objects.requireNonNull(meta, "Meta cannot be null for armored elytras!")
                   .getPersistentDataContainer().get(DURABILITY_KEY, PersistentDataType.INTEGER);

        return realDurability == null ? HAS_NO_CUSTOM_DURABILITY : realDurability;
    }

    /**
     * Updates the durability values of an item.
     *
     * @param itemStack
     *     The itemstack to which the durability values will be applied.
     * @param realDurability
     *     The real durability to store in NBT.
     * @param displayDurability
     *     The durability value to display on the item. This is the durability value the client can actually see.This
     *     only works if the item's meta is an instance of {@link Damageable}.
     */
    public void updateDurability(ItemStack itemStack, int realDurability, int displayDurability)
    {
        final ItemMeta meta = getOrCreateItemMeta(itemStack);
        meta.getPersistentDataContainer().set(DURABILITY_KEY, PersistentDataType.INTEGER, realDurability);

        // If the real durability is not 0 (i.e. fully repaired), the display durability should be at least 1.
        // This is to prevent issues with the assumption that a vanilla durability of 0 means that the item
        // was externally repaired (e.g. with /repair).
        final int fixedDisplayDurability = realDurability > 0 ? Math.max(1, displayDurability) : displayDurability;

        if (meta instanceof Damageable damageable)
            damageable.setDamage(fixedDisplayDurability);

        itemStack.setItemMeta(meta);
    }

    /**
     * Copies the armor trim from a chestplate to an elytra.
     *
     * @param elytraMeta
     *     The elytra meta.
     * @param chestplate
     *     The chestplate to copy the trim from.
     *
     * @return The resulting item meta. If any changes were made, this will be a new instance of {@link ItemMeta}.
     * Otherwise, the input meta is returned.
     */
    private void copyArmorTrim(ItemMeta elytraMeta, ItemStack chestplate)
    {
        if (trimEditor == null)
            return;

        trimEditor.copyArmorTrim(elytraMeta, chestplate);
    }

    /**
     * Adds a given {@link ArmorTier} to an item. The item will be cloned. Note that setting the armor tier to
     * {@link ArmorTier#NONE} has no effect (besides making a copy of the item).
     *
     * @param item
     *     The item.
     * @param armorTier
     *     The {@link ArmorTier} that will be added to it.
     * @param otherItem
     *     The item being applied to the elytra. May be null.
     * @param unbreakable
     *     Whether the resulting item should be unbreakable.
     * @param name
     *     The name of the item.
     * @param lore
     *     The lore of the item.
     * @param color
     *     The color of the armor to store. May be null.
     *
     * @return The NEW item.
     */
    public ItemStack addArmorNBTTags(
        ItemStack item,
        ArmorTier armorTier,
        @Nullable ItemStack otherItem,
        boolean unbreakable,
        String name,
        @Nullable List<String> lore,
        @Nullable Color color)
    {
        if (armorTier == null || armorTier == ArmorTier.NONE)
            return new ItemStack(item);

        final ItemStack ret = new ItemStack(item);
        ItemMeta meta = getOrCreateItemMeta(ret);

        if (Util.isChestPlate(otherItem))
            copyArmorTrim(meta, otherItem);

        meta.getPersistentDataContainer().set(
            ARMOR_TIER_KEY,
            PersistentDataType.INTEGER,
            ArmorTier.getTierID(armorTier));

        if (color != null && armorTier == ArmorTier.LEATHER)
            meta.getPersistentDataContainer().set(
                ARMOR_COLOR_KEY,
                PersistentDataType.INTEGER,
                color.asRGB());

        attributeModifierManager.overwriteAttributeModifiers(meta, armorTier);

        meta.setUnbreakable(unbreakable);
        meta.setDisplayName(name);
        if (lore != null)
            meta.setLore(lore);

        if (armorTier == ArmorTier.NETHERITE && HAS_FIRE_RESISTANT_METHOD)
            meta.setFireResistant(true);

        if (!ret.setItemMeta(meta))
            throw new IllegalStateException("Failed to set item meta '" + meta + "' for item: " + ret);
        return ret;
    }

    ArmorTier getArmorTier(@Nullable ItemMeta meta)
    {
        if (meta == null || !meta.hasAttributeModifiers())
            return ArmorTier.NONE;

        final @Nullable Integer tierID = meta.getPersistentDataContainer()
                                             .get(ARMOR_TIER_KEY, PersistentDataType.INTEGER);
        if (tierID != null)
            return ArmorTier.getArmorTierFromID(tierID);

        final Collection<AttributeModifier> attributeModifiers =
            meta.getAttributeModifiers(AttributeUtil.ATTRIBUTE_ARMOR);
        if (attributeModifiers == null)
            return ArmorTier.NONE;

        for (final AttributeModifier attributeModifier : attributeModifiers)
        {
            final ArmorTier armorTier = ArmorTier.getArmorTierFromArmor((int) attributeModifier.getAmount());
            if (armorTier != ArmorTier.NONE)
                return armorTier;
        }

        return ArmorTier.NONE;
    }

    private boolean isElytra(@Nullable ItemStack item)
    {
        return item != null && item.getType() == Material.ELYTRA;
    }

    /**
     * Gets the {@link ArmorTier} of an item.
     *
     * @param itemStack
     *     The item to check. If the item is not an armored elytra or chestplate, {@link ArmorTier#NONE} is returned.
     *
     * @return The {@link ArmorTier} of the item if it is an armored elytra or chestplate. Otherwise,
     * {@link ArmorTier#NONE} is returned.
     */
    public ArmorTier getArmorTier(@Nullable ItemStack itemStack)
    {
        if (itemStack == null)
            return ArmorTier.NONE;

        if (Util.isChestPlate(itemStack))
            return Util.armorToTier(itemStack);

        return getArmorTier(itemStack.getItemMeta());
    }

    /**
     * Checks which {@link ArmorTier} is on an elytra.
     *
     * @param elytra
     *     The elytra to check.
     *
     * @return The {@link ArmorTier} if the elytra is an armored elytra, otherwise {@link ArmorTier#NONE}.
     */
    public ArmorTier getArmorTierFromElytra(@Nullable ItemStack elytra)
    {
        if (!isElytra(elytra))
            return ArmorTier.NONE;
        return getArmorTier(elytra.getItemMeta());
    }

    /**
     * Checks if an item is unbreakable.
     *
     * @param item
     *     The item to check. This may or may not be an armored elytra.
     *
     * @return True if the item exists and is unbreakable. Otherwise, false.
     */
    public boolean isUnbreakable(@Nullable ItemStack item)
    {
        final @Nullable ItemMeta meta = item == null ? null : item.getItemMeta();
        return meta != null && meta.isUnbreakable();
    }

    /**
     * Gets the Color of an armored elytra.
     * <p>
     * If the provided {@link ItemStack} is not an AE, null is returned.
     *
     * @param item
     *     The armored elytra to check.
     *
     * @return The color of the armored elytra, if the input is a colored armored elytra, otherwise null.
     */
    public Color getColorOfArmoredElytra(@Nullable ItemStack item)
    {
        if (item == null || item.getType() != Material.ELYTRA || !item.hasItemMeta())
            return null;

        final ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return null;

        final PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(ARMOR_COLOR_KEY, PersistentDataType.INTEGER))
            return null;

        final Integer rgb = container.get(ARMOR_COLOR_KEY, PersistentDataType.INTEGER);
        return rgb == null ? null : Color.fromRGB(rgb);
    }

    static ItemMeta getOrCreateItemMeta(ItemStack item)
    {
        final ItemMeta meta = item.hasItemMeta() ?
                              item.getItemMeta() :
                              Bukkit.getItemFactory().getItemMeta(item.getType());
        if (meta == null)
            throw new IllegalArgumentException("Tried to add armor to invalid item: " + item);
        return meta;
    }

    private static @Nullable TrimEditor newTrimEditor()
    {
        if (!ArmoredElytra.SERVER_VERSION.isGreaterThanOrEqualTo(Semver.of(1, 20, 0)))
            return null;
        try
        {
            return new TrimEditor();
        }
        catch (Throwable t)
        {
            ArmoredElytra.getInstance().myLogger(
                Level.INFO, "Failed to initialize TrimEditor! Item trimming will be disabled!");
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Updates the fire resistance of an item.
     * <p>
     * This method is intended to be used to ensure that netherite items are fire-resistant, even if they were created
     * before the fire resistance was added.
     * <p>
     * If the item is not a netherite armored elytra, this method does nothing.
     *
     * @param itemStack
     *     The item to update the fire resistance of.
     *
     * @return True if the item was updated, false otherwise.
     *
     * @throws IllegalStateException
     *     If the server version does not support fire resistance.
     */
    public boolean updateFireResistance(@Nullable ItemStack itemStack)
    {
        if (!HAS_FIRE_RESISTANT_METHOD)
            throw new IllegalStateException("Trying to set fire resistance on a version that does not support it!");

        if (!isElytra(itemStack))
            return false;

        final @Nullable ItemMeta meta = itemStack.getItemMeta();
        if (meta == null)
            return false;

        final ArmorTier armorTier = getArmorTier(meta);
        if (armorTier != ArmorTier.NETHERITE)
            return false;

        if (meta.isFireResistant())
            return false;

        meta.setFireResistant(true);
        itemStack.setItemMeta(meta);
        return true;
    }
}
