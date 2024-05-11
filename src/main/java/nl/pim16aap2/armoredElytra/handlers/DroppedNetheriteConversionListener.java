package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listens for dropped armored elytras of the netherite tier and converts them to netherite chest plates.
 * <p>
 * Also listens for picked-up netherite chest plates and converts them to armored elytras of the netherite tier.
 */
public class DroppedNetheriteConversionListener implements Listener
{
    private final NBTEditor nbtEditor;

    public DroppedNetheriteConversionListener(NBTEditor nbtEditor)
    {
        this.nbtEditor = nbtEditor;
    }

    /**
     * Gets a new item to drop by analyzing an item that will be dropped.
     * <p>
     * So when dropping an armored elytra of the netherite tier, it will return a netherite chestplate (with the meta
     * copied over).
     *
     * @param itemStack
     *     The dropped item to analyze.
     *
     * @return The new item to drop if it should be changed. If no change is required, null is returned.
     */
    private ItemStack getNewDrop(final ItemStack itemStack)
    {
        if (nbtEditor.getArmorTierFromElytra(itemStack) != ArmorTier.NETHERITE)
            return null;

        final ItemStack newDrop = new ItemStack(Material.NETHERITE_CHESTPLATE, 1);
        newDrop.setItemMeta(itemStack.getItemMeta());

        return newDrop;
    }

    /**
     * Gets a new item to pick up by analyzing an item that would be picked up.
     * <p>
     * So when picking up a placeholder netherite chest plate (as replaced by {@link #getNewDrop(ItemStack)}) it would
     * return an armored elytra of the netherite tier.
     *
     * @param itemStack
     *     The picked-up item to analyze.
     *
     * @return The new item to pick up if it should be changed. If no change is required, null is returned.
     */
    private ItemStack getNewPickup(final ItemStack itemStack)
    {
        if (itemStack == null || itemStack.getType() != Material.NETHERITE_CHESTPLATE ||
            nbtEditor.getArmorTierFromElytra(itemStack) != ArmorTier.NETHERITE)
            return null;

        final ItemStack newDrop = new ItemStack(Material.ELYTRA, 1);
        newDrop.setItemMeta(itemStack.getItemMeta());

        return newDrop;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDrop(final ItemSpawnEvent event)
    {
        final ItemStack newDrop = getNewDrop(event.getEntity().getItemStack());
        if (newDrop != null)
            event.getEntity().setItemStack(newDrop);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityPickup(EntityPickupItemEvent event)
    {
        final ItemStack newPickup = getNewPickup(event.getItem().getItemStack());
        if (newPickup != null)
            event.getItem().setItemStack(newPickup);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryPickup(InventoryPickupItemEvent event)
    {
        final ItemStack newPickup = getNewPickup(event.getItem().getItemStack());
        if (newPickup != null)
            event.getItem().setItemStack(newPickup);
    }
}
