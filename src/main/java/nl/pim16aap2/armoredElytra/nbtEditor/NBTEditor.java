package nl.pim16aap2.armoredElytra.nbtEditor;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.UUID;

public class NBTEditor implements INBTEditor
{
    public NBTEditor()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemStack addArmorNBTTags(ItemStack item, ArmorTier armorTier, boolean unbreakable)
    {
        ItemStack ret = new ItemStack(item);
        ItemMeta meta = ret.hasItemMeta() ? ret.getItemMeta() : Bukkit.getItemFactory().getItemMeta(ret.getType());
        if (meta == null)
            throw new IllegalArgumentException("Tried to add armor to invalid item: " + item);

        overwriteNBTValue(meta, Attribute.GENERIC_ARMOR, ArmorTier.getArmor(armorTier), "generic.armor");

        if (ArmorTier.getToughness(armorTier) > 0)
            overwriteNBTValue(meta, Attribute.GENERIC_ARMOR_TOUGHNESS, ArmorTier.getToughness(armorTier),
                              "generic.armorToughness");

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
                                                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST);
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
        if (meta == null)
            return ArmorTier.NONE;

        Collection<AttributeModifier> attributeModifiers = meta.getAttributeModifiers(Attribute.GENERIC_ARMOR);
        if (attributeModifiers == null)
            return ArmorTier.NONE;

        for (final AttributeModifier attributeModifier : attributeModifiers)
        {
            ArmorTier armorTier = ArmorTier.getArmorTier((int) attributeModifier.getAmount());
            if (armorTier != ArmorTier.NONE)
                return armorTier;
        }

        return ArmorTier.NONE;
    }
}
