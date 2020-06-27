package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class FlyDurabilityHandler implements Listener
{
    public FlyDurabilityHandler()
    {
    }

    // Do not decrease elytra durability while flying. This also cancels durability decrease when
    // it should (i.e. getting hit) while flying, but I don't really care.
    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent e)
    {
        if (e.getItem().getType() != Material.ELYTRA)
            return;

        if (!e.getPlayer().isGliding())
            return;

        if (ArmoredElytra.getInstance().getNbtEditor().getArmorTier(e.getItem()) != ArmorTier.NONE)
            e.setCancelled(true);
    }
}
