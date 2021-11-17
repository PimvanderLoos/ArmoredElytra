package nl.pim16aap2.armoredElytra.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface to the inventory of a Grindstone.
 */
public class PrepareGrindstoneEvent extends InventoryEvent implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();

    private final GrindstoneInventory grindstoneInventory;
    private final ItemStack[] inputItems;
    @Nullable
    private final ItemStack result;
    private boolean isCancelled = false;

    public PrepareGrindstoneEvent(InventoryView transaction, GrindstoneInventory grindstoneInventory,
                                  ItemStack[] inputItems, @Nullable ItemStack result)
    {
        super(transaction);
        this.grindstoneInventory = grindstoneInventory;
        this.inputItems = inputItems;
        this.result = result;
    }

    public @Nullable ItemStack getResult()
    {
        return result;
    }

    public @Nonnull ItemStack[] getInputItems()
    {
        return inputItems;
    }

    @Override
    public boolean isCancelled()
    {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel)
    {
        this.isCancelled = cancel;
    }

    @Override
    public @Nonnull HandlerList getHandlers()
    {
        return handlers;
    }

    public static @Nonnull HandlerList getHandlerList()
    {
        return handlers;
    }

    @Override
    public @Nonnull GrindstoneInventory getInventory()
    {
        return grindstoneInventory;
    }
}
