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
	public ItemStack addNBTTags(ItemStack item) 
	{
		ItemMeta itemmeta = item.getItemMeta();
		itemmeta.setDisplayName(ChatColor.AQUA+"Armored Elytra");
		itemmeta.setLore(Arrays.asList("This is an armored Elytra."));
		item.setItemMeta(itemmeta);
		net.minecraft.server.v1_11_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound compound = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();
		NBTTagList modifiers = new NBTTagList();
		NBTTagCompound armor = new NBTTagCompound();
		armor.set("AttributeName", new NBTTagString("generic.armor"));
	    armor.set("Name", new NBTTagString("generic.armor"));
		armor.set("Amount", new NBTTagInt(8));
		armor.set("Operation", new NBTTagInt(0));
		armor.set("UUIDLeast", new NBTTagInt(894654));
		armor.set("UUIDMost", new NBTTagInt(2872));
		armor.set("Slot", new NBTTagString("chest"));
		modifiers.add(armor);			
		NBTTagCompound armorTough = new NBTTagCompound();
		armorTough.set("AttributeName", new NBTTagString("generic.armorToughness"));
		armorTough.set("Name", new NBTTagString("generic.armorToughness"));
		armorTough.set("Amount", new NBTTagInt(2));
		armorTough.set("Operation", new NBTTagInt(0));
		armorTough.set("UUIDLeast", new NBTTagInt(894654));
		armorTough.set("UUIDMost", new NBTTagInt(2872));
		armorTough.set("Slot", new NBTTagString("chest"));
		modifiers.add(armorTough);
		compound.set("AttributeModifiers", modifiers);
		item = CraftItemStack.asBukkitCopy(nmsStack);
		return item;
	}
}
