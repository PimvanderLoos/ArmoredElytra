package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nbtEditor.INBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.DurabilityManager;
import nl.pim16aap2.armoredElytra.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

public class SmithingTableCraftHandler extends SmithingTableListener
{
    public SmithingTableCraftHandler(final ArmoredElytra plugin, INBTEditor nbtEditor,
                                     DurabilityManager durabilityManager, ConfigLoader config)
    {
        super(plugin, true, nbtEditor, durabilityManager, config);
        // Register the anvil handler with creation disabled so AEs can still be repaired and stuff.
        Bukkit.getPluginManager()
              .registerEvents(new AnvilHandler(plugin, false, nbtEditor, durabilityManager, config), plugin);
    }

    @Override
    @EventHandler(ignoreCancelled = true)
    public void onSmithingTableUsage(final PrepareSmithingEvent event)
    {
        super.onSmithingTableUsage(event);
    }

    @Override
    protected ArmorTier getArmorTier(ItemStack itemStackA, ItemStack itemStackB)
    {
        if (itemStackA == null || itemStackB == null ||
            itemStackA.getType() != Material.ELYTRA || !Util.isChestPlate(itemStackB))
            return ArmorTier.NONE;

        return Util.armorToTier(itemStackB.getType());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e)
    {
        if (!isAESmithingTableEvent(e))
            return;
        final SmithingInventory smithingInventory = (SmithingInventory) e.getInventory();
        final ItemStack result = smithingInventory.getItem(2);

        // This cast may look unchecked, but it was checked by isSmithingTableEvent already.
        if (!giveItemToPlayer((Player) e.getWhoClicked(), result, e.isShiftClick()))
            return;
        smithingInventory.clear();
    }
}
