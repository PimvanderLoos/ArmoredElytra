package nl.pim16aap2.armoredElytra.nms;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.minecraft.server.v1_12_R1.NBTTagByte;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagInt;
import net.minecraft.server.v1_12_R1.NBTTagList;
import net.minecraft.server.v1_12_R1.NBTTagString;
import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.ArmorTier;

public class NBTEditor_V1_12_R1 implements NBTEditor 
{
	private String elytraName;
	private String elytraLore;
	private ArmoredElytra plugin;
	
	// Get the names and lores for every tier of armor.
	public NBTEditor_V1_12_R1(String elytraName, String elytraLore, ArmoredElytra plugin)
	{
		this.elytraName = elytraName;
		this.elytraLore = elytraLore;
		this.plugin     = plugin;
	}
	
	// Add armor to the supplied item, based on the armorTier.
	@Override
	public ItemStack addArmorNBTTags(ItemStack item, ArmorTier armorTier, boolean unbreakable) 
	{
		ItemMeta itemmeta   = item.getItemMeta();
		int armorProtection = ArmorTier.getArmor    (armorTier);
		int armorToughness  = ArmorTier.getToughness(armorTier);
		ChatColor color     = ArmorTier.getColor    (armorTier);
		
		itemmeta.setDisplayName(color+plugin.fillInArmorTierInString(elytraName, armorTier));
		if (elytraLore != null)
			itemmeta.setLore(Arrays.asList(plugin.fillInArmorTierInString(elytraLore, armorTier)));
		item.setItemMeta(itemmeta);
		
		net.minecraft.server.v1_12_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound compound   =     (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();
		NBTTagList modifiers      =     new NBTTagList();
		NBTTagCompound armor      =     new NBTTagCompound();
		armor.set("AttributeName",      new NBTTagString("generic.armor"));
	    armor.set("Name",               new NBTTagString("generic.armor"));
		armor.set("Amount",             new NBTTagInt(armorProtection));
		armor.set("Operation",          new NBTTagInt(0));
		armor.set("UUIDLeast",          new NBTTagInt(894654));
		armor.set("UUIDMost",           new NBTTagInt(2872));
		armor.set("Slot",               new NBTTagString("chest"));
		modifiers.add(armor);	
		
		NBTTagCompound armorTough =     new NBTTagCompound();
		armorTough.set("AttributeName", new NBTTagString("generic.armorToughness"));
		armorTough.set("Name",          new NBTTagString("generic.armorToughness"));
		armorTough.set("Amount",        new NBTTagInt(armorToughness));
		armorTough.set("Operation",     new NBTTagInt(0));
		armorTough.set("UUIDLeast",     new NBTTagInt(894654));
		armorTough.set("UUIDMost",      new NBTTagInt(2872));
		armorTough.set("Slot",          new NBTTagString("chest"));
		modifiers.add(armorTough);
		
		if (unbreakable)
			compound.set("Unbreakable", new NBTTagByte((byte) 1));
		
		compound.set("AttributeModifiers", modifiers);
		item = CraftItemStack.asBukkitCopy(nmsStack);
		return item;
	}
	
	// Get the armor tier of the supplied item.
	@Override
	public ArmorTier getArmorTier(ItemStack item)
	{		
		// Get the NBT tags from the item.
		NBTTagCompound compound = CraftItemStack.asNMSCopy(item).getTag();
		if (compound == null)
			return ArmorTier.NONE;
		String nbtTags = compound.toString();
		
		// Check if the item has the generic.armor attribute.
		// Format = <level>,Slot:"chest",AttributeName:"generic.armor so get pos of char before 
		// The start of the string, as that's the value of the generic.armor attribute.
		int pos = nbtTags.indexOf(",Slot:\"chest\",AttributeName:\"generic.armor\"");
		int armorValue = 0;
		if (pos > 0)
		{
			// If so, get the value of the generic.armor attribute.
			pos--;
			String stringAtPos = nbtTags.substring(pos, pos+1);
			armorValue = Integer.parseInt(stringAtPos);
		} else
			// Otherwise, the item has no armor, so return 0;
			return ArmorTier.NONE;
		
		switch (armorValue)
		{
		case 3:
			return ArmorTier.LEATHER;
		case 5:
			return ArmorTier.GOLD;
		case 6:
			return ArmorTier.IRON;
		case 8:
			return ArmorTier.DIAMOND;
		default:
			return ArmorTier.NONE;
		}
	}
}