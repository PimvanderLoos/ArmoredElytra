package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.EnchantmentContainer;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

import java.util.logging.Level;

abstract class SmithingTableListener extends ArmoredElytraHandler implements Listener
{
    protected SmithingTableListener(ArmoredElytra plugin, boolean creationEnabled)
    {
        super(plugin, creationEnabled);

    }

    protected SmithingTableListener(ArmoredElytra plugin)
    {
        this(plugin, false);
    }

    public void onSmithingTableUsage(final PrepareSmithingEvent event)
    {
        final SmithingInventory inventory = event.getInventory();
        final ItemStack[] contents = inventory.getContents();

        final ItemStack itemStackA = contents[0];
        final ItemStack itemStackB = contents[1];

        final ArmorTier newTier = getArmorTier(itemStackA, itemStackB);
        if (newTier == ArmorTier.NONE)
            return;

        final Player player = (Player) event.getView().getPlayer();

        final ItemStack result;
        if (plugin.playerHasCraftPerm(player, newTier))
        {

            EnchantmentContainer enchantments = EnchantmentContainer.getEnchantments(itemStackA, plugin);
            enchantments.merge(EnchantmentContainer.getEnchantments(itemStackB, plugin));
            final Color color = getItemColor(itemStackA, itemStackB);

            result = ArmoredElytra.getInstance().getNbtEditor()
                                  .addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), newTier,
                                                   plugin.getConfigLoader().unbreakable(), color);
            enchantments.applyEnchantments(result);
            event.setResult(result);
        }
    }

    /**
     * Checks if the provided input {@link ItemStack}s form a valid input pattern for a smithing table, and, if so,
     * which tier it combines into.
     *
     * @param itemStackA The first {@link ItemStack}.
     * @param itemStackB The second {@link ItemStack}.
     * @return The {@link ArmorTier} as figured out from the input pattern. If the pattern is invalid, {@link
     * ArmorTier#NONE} is returned.
     */
    protected abstract ArmorTier getArmorTier(ItemStack itemStackA, ItemStack itemStackB);

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
        Player player = (Player) event.getWhoClicked();
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
            ArmoredElytra.getInstance().getNbtEditor().getArmorTier(result) == ArmorTier.NONE)
            return false;

        return true;
    }
}
