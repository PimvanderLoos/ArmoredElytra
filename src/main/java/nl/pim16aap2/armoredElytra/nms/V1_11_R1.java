package nl.pim16aap2.armoredElytra.nms;

import java.util.Arrays;

import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import net.minecraft.server.v1_11_R1.NBTTagInt;
import net.minecraft.server.v1_11_R1.NBTTagList;
import net.minecraft.server.v1_11_R1.NBTTagString;

public class V1_11_R1 implements NBTEditor 
{

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
//			color = ChatColor.valueOf("733D31");
			color = ChatColor.DARK_GREEN;
			armorProtection = 3;
			break;
		case 2:
//			color = ChatColor.valueOf("FFD700");
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
		itemmeta.setDisplayName(color+"Armored Elytra");
		itemmeta.setLore(Arrays.asList("This is an armored Elytra."));
		item.setItemMeta(itemmeta);
		net.minecraft.server.v1_11_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
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
	
	// Get the armor tier of the item.
	public int getArmorTier(ItemStack item)
	{
		ItemStack itemTest = item.clone();
		itemTest = addArmorNBTTags(itemTest, 1);
		if (itemTest.equals(item))
		{
			return 1;
		}
		
		itemTest = addArmorNBTTags(itemTest, 2);
		if (itemTest.equals(item))
		{
			return 2;
		}
		
		itemTest = addArmorNBTTags(itemTest, 3);
		if (itemTest.equals(item))
		{
			return 3;
		}
		
		itemTest = addArmorNBTTags(itemTest, 4);
		if (itemTest.equals(item))
		{
			return 4;
		}

		itemTest = addArmorNBTTags(itemTest, 5);
		if (itemTest.equals(item))
		{
			return 5;
		}
		return 0;
	}
}
