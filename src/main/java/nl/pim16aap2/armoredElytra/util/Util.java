package nl.pim16aap2.armoredElytra.util;

import org.bukkit.entity.Player;

public class Util
{
	// Remove item from player's chestplate slot and puts it in their normal inventory.
	public static void unenquipChestPlayer(Player p) 
	{
		p.getInventory().addItem(p.getInventory().getChestplate());
		p.getInventory().getChestplate().setAmount(0);
	}
}
