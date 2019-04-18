package nl.pim16aap2.armoredElytra.handlers;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;

public class FlyDurabilityHandler implements Listener
{
    private final NBTEditor nbtEditor;

    public FlyDurabilityHandler(NBTEditor nbtEditor)
    {
        this.nbtEditor = nbtEditor;
    }

    // Do not decrease elytra durability while flying. This also cancels durability decrease when
    // it should (i.e. getting hit) while flying, but I don't really care.
    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent e)
    {
        if (e.getItem().getType() == Material.ELYTRA)
            if (nbtEditor.getArmorTier(e.getItem()) != ArmorTier.NONE)
                if (e.getPlayer().isFlying())
                    e.setCancelled(true);
    }
}
