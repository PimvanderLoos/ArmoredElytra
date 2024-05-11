package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nbtEditor.DurabilityManager;
import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.itemInput.ElytraInput;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.SmithingInventory;

import javax.annotation.Nullable;

public class SmithingTableCraftHandler extends SmithingTableListener
{
    public SmithingTableCraftHandler(
        ArmoredElytra plugin,
        NBTEditor nbtEditor,
        DurabilityManager durabilityManager,
        ConfigLoader config)
    {
        super(plugin, true, nbtEditor, durabilityManager, config);

        // Register the anvil handler with creation disabled so AEs can still be repaired and stuff.
        Bukkit.getPluginManager().registerEvents(
            new AnvilHandler(plugin, false, nbtEditor, durabilityManager, config),
            plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSmithingTableUsage(final PrepareSmithingEvent event)
    {
        final SmithingInventory inventory = event.getInventory();

        final @Nullable ElytraInput input = ElytraInput.fromInventory(config, inventory);
        if (ElytraInput.isIgnored(input))
            return;

        event.setResult(armoredElytraBuilder.handleInput(event.getView().getPlayer(), input));
    }

    @Override
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent event)
    {
        super.onInventoryClick(event);
    }
}
