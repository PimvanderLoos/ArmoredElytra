package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nbtEditor.DurabilityManager;
import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

public class SmithingTableCraftHandler extends SmithingTableListener
{
    public SmithingTableCraftHandler(final ArmoredElytra plugin, NBTEditor nbtEditor,
                                     DurabilityManager durabilityManager, ConfigLoader config)
    {
        super(plugin, true, nbtEditor, durabilityManager, config);
        // Register the anvil handler with creation disabled so AEs can still be repaired and stuff.
        Bukkit.getPluginManager()
              .registerEvents(new AnvilHandler(plugin, false, nbtEditor, durabilityManager, config), plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSmithingTableUsage(final PrepareSmithingEvent event)
    {
        final SmithingInventory inventory = event.getInventory();
        final ItemStack[] contents = inventory.getContents();

        final ItemStack itemStackA = contents[0];
        final ItemStack itemStackB = contents[1];

        final ArmorTier newArmorTier = getNewArmorTier(itemStackA, itemStackB);
        if (newArmorTier == ArmorTier.NONE)
            return;

        if (!plugin.playerHasCraftPerm(event.getView().getPlayer(), newArmorTier))
            return;

        event.setResult(armoredElytraBuilder.combine(itemStackA, itemStackB, newArmorTier, null));
    }

    protected ArmorTier getNewArmorTier(ItemStack itemStackA, ItemStack itemStackB)
    {
        if (itemStackA == null || itemStackB == null ||
            itemStackA.getType() != Material.ELYTRA || !Util.isChestPlate(itemStackB))
            return ArmorTier.NONE;

        return Util.armorToTier(itemStackB.getType());
    }

    @Override
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event)
    {
        super.onInventoryClick(event);
    }
}
