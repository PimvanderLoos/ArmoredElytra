package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nbtEditor.ArmoredElytraBuilder;
import nl.pim16aap2.armoredElytra.nbtEditor.DurabilityManager;
import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.CheckReturnValue;

/**
 * Base class for the anvil / smithing table handlers.
 *
 * @author Pim
 */
abstract class ArmoredElytraHandler
{
    protected final ArmoredElytra plugin;
    protected final boolean creationEnabled;
    protected final ConfigLoader config;
    protected final NBTEditor nbtEditor;
    protected final DurabilityManager durabilityManager;
    protected final ArmoredElytraBuilder armoredElytraBuilder;

    protected ArmoredElytraHandler(
        ArmoredElytra plugin,
        boolean creationEnabled,
        NBTEditor nbtEditor,
        DurabilityManager durabilityManager,
        ConfigLoader config,
        ArmoredElytraBuilder armoredElytraBuilder)
    {
        this.plugin = plugin;
        this.creationEnabled = creationEnabled;
        this.nbtEditor = nbtEditor;
        this.durabilityManager = durabilityManager;
        this.config = config;
        this.armoredElytraBuilder = armoredElytraBuilder;
    }

    protected ArmoredElytraHandler(
        ArmoredElytra plugin,
        boolean creationEnabled,
        NBTEditor nbtEditor,
        DurabilityManager durabilityManager,
        ConfigLoader config)
    {
        this(plugin, creationEnabled, nbtEditor, durabilityManager, config,
             new ArmoredElytraBuilder(nbtEditor, durabilityManager, config, plugin));
    }

    /**
     * Attempts to move an item to a player's inventory.
     *
     * @param player
     *     The player to give the item to.
     * @param item
     *     The item to give.
     * @param direct
     *     Whether to put it in the player's inventory. When set to false it will be put in their cursor instead.
     *
     * @return True if the item could be given to the player, otherwise false (e.g. when their inventory is full).
     */
    @CheckReturnValue
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
