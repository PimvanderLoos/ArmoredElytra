package nl.pim16aap2.armoredElytra.nms;

import java.util.Arrays;

import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagInt;
import net.minecraft.server.v1_12_R1.NBTTagList;
import net.minecraft.server.v1_12_R1.NBTTagString;
import nl.pim16aap2.armoredElytra.ArmoredElytra;

public class NBTEditor_V1_12_R1 implements NBTEditor 
{
	String elytraName;
	String elytraLore;
	ArmoredElytra plugin;
	
	// Get the names and lores for every tier of armor.
	public NBTEditor_V1_12_R1(String elytraName, String elytraLore, ArmoredElytra plugin)
	{
		this.elytraName = elytraName;
		this.elytraLore = elytraLore;
		this.plugin     = plugin;
	}
	
	// Add armor to the supplied item, based on the armorTier.
	@Override
	public ItemStack addArmorNBTTags(ItemStack item, int armorTier) 
	{
		ItemMeta itemmeta = item.getItemMeta();
		ChatColor color = ChatColor.WHITE;
		int armorProtection = 0;
		int armorToughness = 0;
		/* 0 = No Armor.
		 * 1 = Leather Armor.
		 * 2 = Gold Armor.
		 * 3 = Chain Armor.
		 * 4 = Iron Armor.
		 * 5 = Diamond Armor.
		 */
		// Give the name the correct color.
		switch (armorTier)
		{
		case 1:
			color = ChatColor.DARK_GREEN;
			armorProtection = 3;
			break;
		case 2:
			color = ChatColor.YELLOW;
			armorProtection = 5;
			break;
		case 3:
			color = ChatColor.DARK_GRAY;
			armorProtection = 5;
			break;
		case 4:
			color = ChatColor.GRAY;
			armorProtection = 6;
			break;
		case 5:
			color = ChatColor.AQUA;
			armorProtection = 8;
			armorToughness  = 2;
			break;
		default:
			color = ChatColor.WHITE;
		}

		itemmeta.setDisplayName(color+plugin.fillInArmorTierInString(elytraName, armorTier));
		if (elytraLore != null)
			itemmeta.setLore(Arrays.asList(plugin.fillInArmorTierInString(elytraLore, armorTier)));
		item.setItemMeta(itemmeta);
		
		net.minecraft.server.v1_12_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound compound = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();
		NBTTagList modifiers = new NBTTagList();
		NBTTagCompound armor = new NBTTagCompound();
		armor.set("AttributeName", new NBTTagString("generic.armor"));
	    armor.set("Name", new NBTTagString("generic.armor"));
		armor.set("Amount", new NBTTagInt(armorProtection));
		armor.set("Operation", new NBTTagInt(0));
		armor.set("UUIDLeast", new NBTTagInt(894654));
		armor.set("UUIDMost", new NBTTagInt(2872));
		armor.set("Slot", new NBTTagString("chest"));
		modifiers.add(armor);	
		
		NBTTagCompound armorTough = new NBTTagCompound();
		armorTough.set("AttributeName", new NBTTagString("generic.armorToughness"));
		armorTough.set("Name", new NBTTagString("generic.armorToughness"));
		armorTough.set("Amount", new NBTTagInt(armorToughness));
		armorTough.set("Operation", new NBTTagInt(0));
		armorTough.set("UUIDLeast", new NBTTagInt(894654));
		armorTough.set("UUIDMost", new NBTTagInt(2872));
		armorTough.set("Slot", new NBTTagString("chest"));
		modifiers.add(armorTough);
		
		compound.set("AttributeModifiers", modifiers);
		item = CraftItemStack.asBukkitCopy(nmsStack);
		return item;
	}
	
	// Get the armor tier of the supplied item.
	@Override
	public int getArmorTier(ItemStack item)
	{
		int armorTier = 0;
		int armorValue = 0;
		
		// Get the NBT tags from the item.
		NBTTagCompound compound = CraftItemStack.asNMSCopy(item).getTag();
		if (compound == null)
			return 0;
		String nbtTags = compound.toString();
		
		// Check if the item has the generic.armor attribute.
		int pos = nbtTags.indexOf(",Slot:\"chest\",AttributeName:\"generic.armor\"");
		if (pos > 0)
		{
			// If so, get the value of the generic.armor attribute.
			pos--;
			String stringAtPos = nbtTags.substring(pos, pos+1);
			armorValue = Integer.parseInt(stringAtPos);
		} else
			// Otherwise, the item has no armor, so return 0;
			return 0;
		
		switch (armorValue)
		{
		case 3:
			armorTier = 1;
			break;
		case 5:
			armorTier = 2;
			break;
		case 6:
			armorTier = 4;
			break;
		case 8:
			armorTier = 5;
			break;
		}
		return armorTier;
	}
}