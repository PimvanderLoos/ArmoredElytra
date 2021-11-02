package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.nbtEditor.INBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.DurabilityManager;
import nl.pim16aap2.armoredElytra.util.Util;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class FlyDurabilityHandler implements Listener
{
    private final boolean disableDurability;
    private final INBTEditor nbtEditor;
    private final DurabilityManager durabilityManager;

    public FlyDurabilityHandler(boolean disableDurability, INBTEditor nbtEditor, DurabilityManager durabilityManager)
    {
        this.disableDurability = disableDurability;
        this.nbtEditor = nbtEditor;
        this.durabilityManager = durabilityManager;
    }

    // Do not decrease elytra durability while flying.
    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemDamage(PlayerItemDamageEvent e)
    {
        if (e.getItem().getType() != Material.ELYTRA)
            return;

        if (!e.getPlayer().isGliding())
            return;

        final ArmorTier armorTier = nbtEditor.getArmorTier(e.getItem());
        if (armorTier == ArmorTier.NONE)
            return;

        // This also cancels durability decrease when it should (i.e. getting hit) while flying,
        // but that is likely to be rare enough for it to not matter.
        e.setCancelled(true);
        if (disableDurability)
            return;

        final int newDurability = durabilityManager.removeDurability(e.getItem(), e.getDamage(), armorTier);
        if (newDurability >= durabilityManager.getMaxDurability(armorTier))
            Util.moveChestplateToInventory(e.getPlayer());
    }
}
