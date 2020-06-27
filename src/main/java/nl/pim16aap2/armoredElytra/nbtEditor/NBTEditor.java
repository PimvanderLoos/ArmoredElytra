package nl.pim16aap2.armoredElytra.nbtEditor;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.UUID;

// TODO: Consider using static UUIDs, to ensure attributes aren't stacked.
public class NBTEditor implements INBTEditor
{
//    private static final Map<ArmorTier, NamespacedKey> namespaceKeys;
//
//    static
//    {
//        final Map<ArmorTier, NamespacedKey> namespaceKeysTmp = new EnumMap<ArmorTier, NamespacedKey>(ArmorTier.class);
//        for (final ArmorTier tier : ArmorTier.values())
//            namespaceKeysTmp.put(tier, new NamespacedKey(ArmoredElytra.getInstance(), "ARMORTIER_" + tier.name()));
//        namespaceKeys = Collections.unmodifiableMap(namespaceKeysTmp);
//    }

    private static final NamespacedKey armorTierKey = new NamespacedKey(ArmoredElytra.getInstance(),
                                                                        "ARMOR_TIER_LEVEL");

    public NBTEditor()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemStack addArmorNBTTags(ItemStack item, ArmorTier armorTier, boolean unbreakable)
    {
        if (armorTier == null || armorTier == ArmorTier.NONE)
            return new ItemStack(item);

        ItemStack ret = new ItemStack(item);
        ItemMeta meta = ret.hasItemMeta() ? ret.getItemMeta() : Bukkit.getItemFactory().getItemMeta(ret.getType());
        if (meta == null)
            throw new IllegalArgumentException("Tried to add armor to invalid item: " + item);
        meta.getPersistentDataContainer().set(armorTierKey, PersistentDataType.INTEGER, ArmorTier.getTierID(armorTier));

        overwriteNBTValue(meta, Attribute.GENERIC_ARMOR, ArmorTier.getArmor(armorTier), "generic.armor");
        if (ArmorTier.getToughness(armorTier) > 0)
            overwriteNBTValue(meta, Attribute.GENERIC_ARMOR_TOUGHNESS, ArmorTier.getToughness(armorTier),
                              "generic.armor_toughness");

        if (ArmorTier.getKnockbackResistance(armorTier) > 0)
            overwriteNBTValue(meta, Attribute.GENERIC_KNOCKBACK_RESISTANCE, ArmorTier.getKnockbackResistance(armorTier),
                              "generic.knockback_resistance");

        meta.setUnbreakable(unbreakable);
        meta.setDisplayName(ArmoredElytra.getInstance().getArmoredElytraName(armorTier));

        ret.setItemMeta(meta);
        return ret;
    }

    private void overwriteNBTValue(ItemMeta meta, Attribute attribute, double value, String modifierName)
    {
        if (meta.hasAttributeModifiers())
            meta.removeAttributeModifier(attribute);

        AttributeModifier attributeModifier = new AttributeModifier(UUID.randomUUID(), modifierName, value,
                                                                    AttributeModifier.Operation.ADD_NUMBER,
                                                                    EquipmentSlot.CHEST);
        meta.addAttributeModifier(attribute, attributeModifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArmorTier getArmorTier(ItemStack item)
    {
        if (item == null)
            return ArmorTier.NONE;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasAttributeModifiers())
            return ArmorTier.NONE;

        Integer tierID = meta.getPersistentDataContainer().get(armorTierKey, PersistentDataType.INTEGER);
        if (tierID != null)
            return ArmorTier.getArmorTierFromID(tierID);

        Collection<AttributeModifier> attributeModifiers = meta.getAttributeModifiers(Attribute.GENERIC_ARMOR);
        if (attributeModifiers == null)
            return ArmorTier.NONE;

        for (final AttributeModifier attributeModifier : attributeModifiers)
        {
            ArmorTier armorTier = ArmorTier.getArmorTierFromArmor((int) attributeModifier.getAmount());
            if (armorTier != ArmorTier.NONE)
                return armorTier;
        }

        return ArmorTier.NONE;
    }
}
