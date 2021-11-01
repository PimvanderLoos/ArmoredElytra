package nl.pim16aap2.armoredElytra.nbtEditor;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
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

public class NBTEditor implements INBTEditor
{
    private static final NamespacedKey ARMOR_TIER_KEY = new NamespacedKey(ArmoredElytra.getInstance(),
                                                                          "ARMOR_TIER_LEVEL");
    private static final NamespacedKey ARMOR_COLOR_KEY = new NamespacedKey(ArmoredElytra.getInstance(),
                                                                           "ARMORED_ELYTRA_COLOR");
    private static final NamespacedKey DURABILITY_KEY = new NamespacedKey(ArmoredElytra.getInstance(),
                                                                          "ARMORED_ELYTRA_DURABILITY");

    @Override
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

    @Override
    public void updateDurability(ItemStack itemStack, int realDurability, int displayDurability)
    {
        final ItemMeta meta = getOrCreateItemMeta(itemStack);
        meta.getPersistentDataContainer().set(DURABILITY_KEY, PersistentDataType.INTEGER, realDurability);

        if (meta instanceof Damageable)
            ((Damageable) meta).setDamage(displayDurability);

        itemStack.setItemMeta(meta);
    }

    @Override
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

    private void overwriteNBTValue(ItemMeta meta, Attribute attribute, double value, String modifierName)
    {
        if (meta.hasAttributeModifiers())
            meta.removeAttributeModifier(attribute);

        final AttributeModifier attributeModifier = new AttributeModifier(UUID.randomUUID(), modifierName, value,
                                                                          AttributeModifier.Operation.ADD_NUMBER,
                                                                          EquipmentSlot.CHEST);
        meta.addAttributeModifier(attribute, attributeModifier);
    }

    private ArmorTier getArmorTier(@Nullable ItemMeta meta)
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

    @Override
    public ArmorTier getArmorTier(@Nullable ItemStack item)
    {
        if (item == null)
            return ArmorTier.NONE;
        return getArmorTier(item.getItemMeta());
    }

    @Override
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

    private static ItemMeta getOrCreateItemMeta(ItemStack item)
    {
        final ItemMeta meta = item.hasItemMeta() ?
                              item.getItemMeta() :
                              Bukkit.getItemFactory().getItemMeta(item.getType());
        if (meta == null)
            throw new IllegalArgumentException("Tried to add armor to invalid item: " + item);
        return meta;
    }
}
