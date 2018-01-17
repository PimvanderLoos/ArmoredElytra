package nl.pim16aap2.armoredElytra.nms;

import org.bukkit.inventory.ItemStack;

import nl.pim16aap2.armoredElytra.util.ArmorTier;

public interface NBTEditor 
{
	public ItemStack addArmorNBTTags(ItemStack item, ArmorTier armorTier, boolean unbreakable);

	public ArmorTier getArmorTier(ItemStack item);
}
