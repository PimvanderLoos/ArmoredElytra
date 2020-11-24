package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

/**
 * Base class for the anvil / smithing table handlers.
 *
 * @author Pim
 */
abstract class ArmoredElytraHandler
{
    protected final ArmoredElytra plugin;

    protected final boolean creationEnabled;

    public ArmoredElytraHandler(final ArmoredElytra plugin, final boolean creationEnabled)
    {
        this.plugin = plugin;
        this.creationEnabled = creationEnabled;
    }

    protected void cleanAnvilInventory(AnvilInventory anvilInventory)
    {
        if (anvilInventory.getItem(0) != null)
            anvilInventory.getItem(0).setAmount(0);
        if (anvilInventory.getItem(1) != null)
            anvilInventory.getItem(1).setAmount(anvilInventory.getItem(1).getAmount() - 1);
        if (anvilInventory.getItem(2) != null)
            anvilInventory.getItem(2).setAmount(0);
    }

    // Repair an Armored Elytra
    protected short repairItem(short curDur, ItemStack repairItem)
    {
        // Get the multiplier for the repair items.
        double mult = 0.01;
        if (repairItem.getType().equals(Material.LEATHER))
            mult *= (100.0f / plugin.getConfigLoader().LEATHER_TO_FULL());

        else if (repairItem.getType().equals(Material.GOLD_INGOT))
            mult *= (100.0f / plugin.getConfigLoader().GOLD_TO_FULL());

        else if (repairItem.getType().equals(Material.IRON_INGOT))
            mult *= (100.0f / plugin.getConfigLoader().IRON_TO_FULL());

        else if (repairItem.getType().equals(Material.DIAMOND))
            mult *= (100.0f / plugin.getConfigLoader().DIAMONDS_TO_FULL());

        else if (repairItem.getType().equals(XMaterial.NETHERITE_INGOT.parseMaterial()))
            mult *= (100.0f / plugin.getConfigLoader().NETHERITE_TO_FULL());

        int maxDurability = Material.ELYTRA.getMaxDurability();
        int newDurability = (int) (curDur - (maxDurability * mult));
        return (short) (Math.max(newDurability, 0));
    }

    /**
     * Attempts to move an item to a player's inventory.
     *
     * @param player The player to give the item to.
     * @param item   The item to give.
     * @param direct Whether or not to put it in the player's inventory. When set to false it will be put in their
     *               cursor instead.
     * @return True if the item could be given to the player, otherwise false (e.g. when their inventory is full).
     */
    protected boolean giveItemToPlayer(final Player player, final ItemStack item, final boolean direct)
    {
        if (direct)
        {
            // If the player's inventory is full, don't do anything.
            if (player.getInventory().firstEmpty() == -1)
                return false;
            player.getInventory().addItem(item);
        }
        else
            player.setItemOnCursor(item);
        return true;
    }
}
