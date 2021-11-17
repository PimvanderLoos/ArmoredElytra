package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.events.PrepareGrindstoneEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * Represents a class that listens for interactions with inventories and creates {@link PrepareGrindstoneEvent}s when
 * appropriate.
 *
 * @author Pim
 */
public class PrepareGrindstoneCreator implements Listener
{
    /**
     * Creates and calls a new {@link PrepareGrindstoneEvent}.
     * <p>
     * If the new grindstone event is cancelled, the input inventory interact event will be cancelled as well.
     *
     * @param inventoryEvent The inventory event that caused the grindstone event. This event will be cancelled if the
     *                       grindstone event is cancelled.
     * @param inventory      The grindstone inventory.
     */
    private void callPrepareGrindstoneEvent(InventoryInteractEvent inventoryEvent, GrindstoneInventory inventory)
    {
        final ItemStack[] inputItems = new ItemStack[]{inventory.getItem(0), inventory.getItem(1)};
        final PrepareGrindstoneEvent prepareGrindstoneEvent =
            new PrepareGrindstoneEvent(inventoryEvent.getView(), inventory, inputItems, inventory.getItem(2));

        Bukkit.getServer().getPluginManager().callEvent(prepareGrindstoneEvent);

        if (prepareGrindstoneEvent.isCancelled())
            inventoryEvent.setResult(Event.Result.DENY);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onGrindStoneDrag(InventoryDragEvent inventoryDragEvent)
    {
        final Inventory mainInventory = inventoryDragEvent.getInventory();
        if (mainInventory.getType() != InventoryType.GRINDSTONE ||
            !(mainInventory instanceof GrindstoneInventory inventory))
            return;

        final var slotIds = inventoryDragEvent.getRawSlots();
        // Players can only interact with raw slots 0 and 1. 2 = output.
        if (!slotIds.contains(0) && !slotIds.contains(1))
            return;

        callPrepareGrindstoneEvent(inventoryDragEvent, inventory);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onGrindStoneClick(InventoryClickEvent inventoryClickEvent)
    {
        if (inventoryClickEvent.getAction() == InventoryAction.NOTHING)
            return;

        final Inventory mainInventory = inventoryClickEvent.getInventory();
        if (mainInventory.getType() != InventoryType.GRINDSTONE ||
            !(mainInventory instanceof GrindstoneInventory inventory))
            return;

        final @Nullable Inventory clickedInventory = inventoryClickEvent.getClickedInventory();
        if (clickedInventory == null)
            return;

        final ClickType clickType = inventoryClickEvent.getClick();
        final boolean isShift = clickType.equals(ClickType.SHIFT_LEFT) || clickType.equals(ClickType.SHIFT_RIGHT);
        // Only way to put something into the grindstone via a single click is by clicking on the slot itself
        // with and item or shift-clicking one into it.
        if (clickedInventory.getType() != InventoryType.GRINDSTONE && !isShift)
            return;

        callPrepareGrindstoneEvent(inventoryClickEvent, inventory);
    }
}
