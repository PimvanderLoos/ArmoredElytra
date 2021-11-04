package nl.pim16aap2.armoredElytra.nbtEditor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.enchantmentcontainer.EnchantmentContainer;
import nl.pim16aap2.armoredElytra.enchantmentcontainer.EnchantmentContainerAdapter;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class NBTEditor
{
    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(EnchantmentContainer.class, new EnchantmentContainerAdapter())
        .create();

    private static final NamespacedKey ARMOR_TIER_KEY =
        new NamespacedKey(ArmoredElytra.getInstance(), "ARMOR_TIER_LEVEL");
    private static final NamespacedKey ARMOR_COLOR_KEY =
        new NamespacedKey(ArmoredElytra.getInstance(), "ARMORED_ELYTRA_COLOR");
    private static final NamespacedKey DURABILITY_KEY =
        new NamespacedKey(ArmoredElytra.getInstance(), "ARMORED_ELYTRA_DURABILITY");
    private static final NamespacedKey ENCHANTMENTS_ELYTRA =
        new NamespacedKey(ArmoredElytra.getInstance(), "ARMORED_ELYTRA_ENCHANTMENTS_ELYTRA");
    private static final NamespacedKey ENCHANTMENTS_CHESTPLATE =
        new NamespacedKey(ArmoredElytra.getInstance(), "ARMORED_ELYTRA_ENCHANTMENTS_CHESTPLATE");

    /**
     * Sets the original enchantments of an armored elytra.
     *
     * @param itemStack              The itemstack to store the original enchantments in.
     * @param enchantmentsElytra     The original enchantments of the elytra.
     * @param enchantmentsChestplate The original enchantments of the chestplate.
     */
    public void setOriginalEnchantments(ItemStack itemStack, EnchantmentContainer enchantmentsElytra,
                                        EnchantmentContainer enchantmentsChestplate)
    {
        final ItemMeta meta = getOrCreateItemMeta(itemStack);
        setOriginalEnchantments(meta, ENCHANTMENTS_ELYTRA, enchantmentsElytra);
        setOriginalEnchantments(meta, ENCHANTMENTS_CHESTPLATE, enchantmentsChestplate);
        itemStack.setItemMeta(meta);
    }

    private void setOriginalEnchantments(ItemStack itemStack, NamespacedKey namespacedKey,
                                         EnchantmentContainer enchantments)
    {
        final ItemMeta meta = getOrCreateItemMeta(itemStack);
        setOriginalEnchantments(meta, namespacedKey, enchantments);
        itemStack.setItemMeta(meta);
    }

    /**
     * Sets the original enchantments of an armored elytra.
     *
     * @param itemStack          The itemstack to store the original enchantments in.
     * @param enchantmentsElytra The original enchantments of the elytra.
     */
    public void setOriginalEnchantmentsForElytra(ItemStack itemStack, EnchantmentContainer enchantmentsElytra)
    {
        setOriginalEnchantments(itemStack, ENCHANTMENTS_ELYTRA, enchantmentsElytra);
    }

    /**
     * Sets the original enchantments of an armored elytra.
     *
     * @param itemStack              The itemstack to store the original enchantments in.
     * @param enchantmentsChestplate The original enchantments of the chestplate.
     */
    public void setOriginalEnchantmentsForChestplate(ItemStack itemStack, EnchantmentContainer enchantmentsChestplate)
    {
        setOriginalEnchantments(itemStack, ENCHANTMENTS_CHESTPLATE, enchantmentsChestplate);
    }

    private void setOriginalEnchantments(ItemMeta meta, NamespacedKey namespacedKey, EnchantmentContainer enchantments)
    {
        if (enchantments.isEmpty())
            meta.getPersistentDataContainer().remove(namespacedKey);
        else
            meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, gson.toJson(enchantments));
    }

    /**
     * Checks if an armored elytra has the original enchantments used to create it.
     *
     * @param armoredElytra An itemstack representing an armored elytra.
     * @return True if the armored elytra has the original enchantments.
     */
    public boolean hasOriginalEnchantments(ItemStack armoredElytra)
    {
        final @Nullable ItemMeta meta = armoredElytra.getItemMeta();
        if (meta == null || getArmorTier(meta) == ArmorTier.NONE)
            return false;
        return meta.getPersistentDataContainer().has(ENCHANTMENTS_ELYTRA, PersistentDataType.STRING);
    }

    /**
     * Retrieves the original enchantments that were stored on the elytra and the chestplate that were combined to
     * create and armored elytra.
     *
     * @param itemStack The itemstack from which to retrieve the original enchantments.
     * @return A pair containing the enchantments that were present on the original elytra and the original chestplate
     * respectively.
     */
    public OriginalEnchantments<EnchantmentContainer, EnchantmentContainer> getOriginalEnchantments(ItemStack itemStack)
    {
        final @Nullable ItemMeta meta = itemStack.getItemMeta();
        if (meta == null)
            return new OriginalEnchantments<>(new EnchantmentContainer(), new EnchantmentContainer());
        return new OriginalEnchantments<>(getOriginalEnchantments(meta, ENCHANTMENTS_ELYTRA),
                                          getOriginalEnchantments(meta, ENCHANTMENTS_CHESTPLATE));
    }

    private EnchantmentContainer getOriginalEnchantments(ItemMeta meta, NamespacedKey namespacedKey)
    {
        final @Nullable String json = meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
        return json == null ? new EnchantmentContainer() : gson.fromJson(json, EnchantmentContainer.class);
    }

    /**
     * Gets the real durability value as stored in the NBT of an armored elytra.
     *
     * @param itemStack    The item for which to retrieve the real durability.
     * @param providedTier The armor tier of the armored elytra. If this is null, it will be retrieved from NBT.
     * @return The real durability of the itemstack if the itemstack has the AE durability attribute, or -1 otherwise.
     */
    public int getRealDurability(ItemStack itemStack, @Nullable ArmorTier providedTier)
    {
        final @Nullable ItemMeta meta = itemStack.getItemMeta();
        final ArmorTier armorTier = providedTier == null ? getArmorTier(meta) : providedTier;

        if (armorTier == ArmorTier.NONE)
            return -1;

        if (!(meta instanceof Damageable))
            throw new IllegalStateException("Item \"" + itemStack + "\" with meta \"" + meta + "\" is not Damageable!");

        final @Nullable Integer realDurability =
            Objects.requireNonNull(meta, "Meta cannot be null for armored elytras!")
                   .getPersistentDataContainer().get(DURABILITY_KEY, PersistentDataType.INTEGER);

        return realDurability == null ? -1 : realDurability;
    }

    /**
     * Updates the durability values of an item.
     *
     * @param itemStack         The itemstack to which the durability values will be applied.
     * @param realDurability    The real durability to store in NBT.
     * @param displayDurability The durability value to display on the item. This is the durability value the client can
     *                          actually see.This only works if the item's meta is an instance of {@link Damageable}.
     */
    public void updateDurability(ItemStack itemStack, int realDurability, int displayDurability)
    {
        final ItemMeta meta = getOrCreateItemMeta(itemStack);
        meta.getPersistentDataContainer().set(DURABILITY_KEY, PersistentDataType.INTEGER, realDurability);

        if (meta instanceof Damageable)
            ((Damageable) meta).setDamage(displayDurability);

        itemStack.setItemMeta(meta);
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
    public ItemStack addArmorNBTTags(ItemStack item, ArmorTier armorTier, boolean unbreakable, String name,
                                     @Nullable List<String> lore, @Nullable Color color)
    {
        if (armorTier == null || armorTier == ArmorTier.NONE)
            return new ItemStack(item);

        final ItemStack ret = new ItemStack(item);
        final ItemMeta meta = getOrCreateItemMeta(ret);
        meta.getPersistentDataContainer().set(ARMOR_TIER_KEY, PersistentDataType.INTEGER,
                                              ArmorTier.getTierID(armorTier));

        if (color != null && armorTier == ArmorTier.LEATHER)
            meta.getPersistentDataContainer().set(ARMOR_COLOR_KEY, PersistentDataType.INTEGER, color.asRGB());

        overwriteNBTValue(meta, Attribute.GENERIC_ARMOR, ArmorTier.getArmor(armorTier), "generic.armor");
        if (ArmorTier.getToughness(armorTier) > 0)
            overwriteNBTValue(meta, Attribute.GENERIC_ARMOR_TOUGHNESS, ArmorTier.getToughness(armorTier),
                              "generic.armor_toughness");

        if (ArmorTier.getKnockbackResistance(armorTier) > 0)
            overwriteNBTValue(meta, Attribute.GENERIC_KNOCKBACK_RESISTANCE, ArmorTier.getKnockbackResistance(armorTier),
                              "generic.knockback_resistance");

        meta.setUnbreakable(unbreakable);
        meta.setDisplayName(name);
        if (lore != null)
            meta.setLore(lore);

        ret.setItemMeta(meta);
        return ret;
    }

    void overwriteNBTValue(ItemMeta meta, Attribute attribute, double value, String modifierName)
    {
        if (meta.hasAttributeModifiers())
            meta.removeAttributeModifier(attribute);

        final AttributeModifier attributeModifier = new AttributeModifier(UUID.randomUUID(), modifierName, value,
                                                                          AttributeModifier.Operation.ADD_NUMBER,
                                                                          EquipmentSlot.CHEST);
        meta.addAttributeModifier(attribute, attributeModifier);
    }

    ArmorTier getArmorTier(@Nullable ItemMeta meta)
    {
        if (meta == null || !meta.hasAttributeModifiers())
            return ArmorTier.NONE;

        final @Nullable Integer tierID = meta.getPersistentDataContainer()
                                             .get(ARMOR_TIER_KEY, PersistentDataType.INTEGER);
        if (tierID != null)
            return ArmorTier.getArmorTierFromID(tierID);

        final Collection<AttributeModifier> attributeModifiers = meta.getAttributeModifiers(Attribute.GENERIC_ARMOR);
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

    /**
     * Checks which {@link ArmorTier} is on an item.
     *
     * @param item The item to check.
     * @return The {@link ArmorTier} that is on the item. If none is found, {@link ArmorTier#NONE} is returned.
     */
    public ArmorTier getArmorTier(@Nullable ItemStack item)
    {
        if (item == null)
            return ArmorTier.NONE;
        return getArmorTier(item.getItemMeta());
    }

    /**
     * Gets the Color of an armored elytra.
     * <p>
     * If the provided {@link ItemStack} is not an AE, null is returned.
     *
     * @param item The armored elytra to check.
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

    public static record OriginalEnchantments<T, U>(T enchantmentsElytra, U enchantmentsChestplate)
    {
    }
}
