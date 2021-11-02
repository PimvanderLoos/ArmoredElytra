package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nbtEditor.INBTEditor;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.DurabilityManager;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import javax.annotation.CheckReturnValue;

/**
 * Base class for the anvil / smithing table handlers.
 *
 * @author Pim
 */
abstract class ArmoredElytraHandler
{
    private static final Color DEFAULT_LEATHER_COLOR = Bukkit.getServer().getItemFactory().getDefaultLeatherColor();

    protected final ArmoredElytra plugin;
    protected final boolean creationEnabled;
    protected final ConfigLoader config;
    protected final INBTEditor nbtEditor;
    protected final DurabilityManager durabilityManager;

    protected ArmoredElytraHandler(ArmoredElytra plugin, boolean creationEnabled, INBTEditor nbtEditor,
                                   DurabilityManager durabilityManager, ConfigLoader config)
    {
        this.plugin = plugin;
        this.creationEnabled = creationEnabled;
        this.nbtEditor = nbtEditor;
        this.durabilityManager = durabilityManager;
        this.config = config;
    }

    /**
     * Gets the color of the item if the item has a color.
     * <p>
     * See {@link LeatherArmorMeta#getColor()}.
     *
     * @param itemA The first {@link ItemStack} to check.
     * @param itemB The second {@link ItemStack} to check.
     * @return The color of the item, if it has a color, otherwise null.
     */
    protected Color getItemColor(final ItemStack itemA, final ItemStack itemB)
    {
        final Color colorA = getItemColor(itemA);
        if (colorA != null && !colorA.equals(DEFAULT_LEATHER_COLOR))
            return colorA;

        final Color colorB = getItemColor(itemB);
        return colorB != null ? colorB : colorA;
    }

    private Color getItemColor(final ItemStack itemStack)
    {
        if (itemStack == null)
            return null;

        if (itemStack.getType() == Material.ELYTRA)
            return nbtEditor.getColorOfArmoredElytra(itemStack);

        if (!itemStack.hasItemMeta() || !(itemStack.getItemMeta() instanceof LeatherArmorMeta))
            return null;

        return ((LeatherArmorMeta) itemStack.getItemMeta()).getColor();
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
