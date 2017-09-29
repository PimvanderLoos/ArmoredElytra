package nl.pim16aap2.armoredElytra.nms;

import org.bukkit.inventory.ItemStack;

public interface NBTEditor 
{
	public ItemStack addArmorNBTTags(ItemStack item, int armorTier);

	public int getArmorTier(ItemStack item);
}
