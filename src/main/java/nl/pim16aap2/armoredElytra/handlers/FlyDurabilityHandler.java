package nl.pim16aap2.armoredElytra.handlers;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

import nl.pim16aap2.armoredElytra.nms.NBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;

public class FlyDurabilityHandler implements Listener
{
	private NBTEditor nbtEditor;
	
	public FlyDurabilityHandler(NBTEditor nbtEditor) 
	{
		this.nbtEditor = nbtEditor;
	}
	
	// Do not decrease elytra durability while flying. 
	// This also cancels durability decrease when it should while flying, but that shouldn't really matter.
	@EventHandler
	public void onItemDamage(PlayerItemDamageEvent e) 
	{
		if (e.getItem().getType() == Material.ELYTRA)
			if (nbtEditor.getArmorTier(e.getItem()) != ArmorTier.NONE)
				if (e.getPlayer().isFlying())
					e.setCancelled(true);
	}
}
