package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nbtEditor.DurabilityManager;
import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

import java.util.logging.Level;

abstract class SmithingTableListener extends ArmoredElytraHandler implements Listener
{
    protected SmithingTableListener(ArmoredElytra plugin, boolean creationEnabled,
                                    NBTEditor nbtEditor, DurabilityManager durabilityManager, ConfigLoader config)
    {
        super(plugin, creationEnabled, nbtEditor, durabilityManager, config);
    }

    protected void onInventoryClick(InventoryClickEvent event)
    {
        if (!isAESmithingTableEvent(event))
            return;
        final SmithingInventory smithingInventory = (SmithingInventory) event.getInventory();
        final ItemStack result = smithingInventory.getItem(2);

        // This cast may look unchecked, but it was checked by isSmithingTableEvent already.
        if (!giveItemToPlayer((Player) event.getWhoClicked(), result, event.isShiftClick()))
            return;
        smithingInventory.clear();
    }

    /**
     * Checks if an {@link InventoryClickEvent} is useful for this plugin. I.e., it is about a smithing inventory and
     * there is an (armored) elytra involved somehow.
     *
     * @param event The {@link InventoryClickEvent} which may be of use to us.
     * @return True if this plugin can process this event further.
     */
    protected boolean isAESmithingTableEvent(final InventoryClickEvent event)
    {
        if (event.getRawSlot() != 2)
            return false;

        // Check if the event was a player who interacted with a smithing table.
        final Player player = (Player) event.getWhoClicked();
        if (event.getView().getType() != InventoryType.SMITHING)
            return false;

        SmithingInventory smithingInventory;
        // Try to cast inventory being used in the event to a smithing inventory.
        // This will throw a ClassCastException when a CraftInventoryCustom is used.
        try
        {
            smithingInventory = (SmithingInventory) event.getInventory();
        }
        catch (ClassCastException exception)
        {
            // Print warning to console and exit onInventoryClick event (no support for
            // custom inventories as they are usually used for GUI's).
            plugin.debugMsg(Level.WARNING, "Could not cast inventory to SmithingInventory for player " +
                player.getName() + "! Armored Elytras cannot be crafted!");
            exception.printStackTrace();
            return false;
        }

        if (smithingInventory.getItem(0) == null ||
            smithingInventory.getItem(1) == null ||
            smithingInventory.getItem(2) == null)
            return false;

        final ItemStack result = smithingInventory.getItem(2);
        if (result == null || result.getType() != Material.ELYTRA ||
            nbtEditor.getArmorTier(result) == ArmorTier.NONE)
            return false;
        return true;
    }
}
