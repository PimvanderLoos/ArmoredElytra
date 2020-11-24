package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.enchantment.EnchantmentManager;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

import java.util.logging.Level;

public class SmithingTableHandler extends ArmoredElytraHandler implements Listener
{
    public SmithingTableHandler(final ArmoredElytra plugin)
    {
        super(plugin, true);
        // Register the anvil handler with creation disabled so AEs can still be repaired and stuff.
        Bukkit.getPluginManager().registerEvents(new AnvilHandler(plugin, false), plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSmithingTableUsage(final PrepareSmithingEvent event)
    {
        final SmithingInventory inventory = event.getInventory();
        final ItemStack[] contents = inventory.getContents();

        final ItemStack itemStackA = contents[0];
        final ItemStack itemStackB = contents[1];

        if (itemStackA == null || itemStackB == null ||
            itemStackA.getType() != Material.ELYTRA || !Util.isChestPlate(itemStackB))
            return;

        final ArmorTier newTier = Util.armorToTier(itemStackB.getType());
        final EnchantmentManager enchantments = new EnchantmentManager(itemStackA);
        final Player player = (Player) event.getView().getPlayer();

        final ItemStack result;
        if (plugin.playerHasCraftPerm(player, newTier))
        {
            result = ArmoredElytra.getInstance().getNbtEditor()
                                  .addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), newTier,
                                                   plugin.getConfigLoader().unbreakable());
            enchantments.apply(result);
            event.setResult(result);
        }
    }

    // Let the player take items out of the smithing table.
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e)
    {
        if (e.getRawSlot() != 2 || !(e.getWhoClicked() instanceof Player))
            return;

        // Check if the event was a player who interacted with a smithing table.
        Player player = (Player) e.getWhoClicked();
        if (e.getView().getType() != InventoryType.SMITHING)
            return;

        SmithingInventory smithingInventory;
        // Try to cast inventory being used in the event to a smithing inventory.
        // This will throw a ClassCastException when a CraftInventoryCustom is used.
        try
        {
            smithingInventory = (SmithingInventory) e.getInventory();
        }
        catch (ClassCastException exception)
        {
            // Print warning to console and exit onInventoryClick event (no support for
            // custom inventories as they are usually used for GUI's).
            plugin.debugMsg(Level.WARNING, "Could not cast inventory to SmithingInventory for player " +
                player.getName() + "! Armored Elytras cannot be crafted!");
            exception.printStackTrace();
            return;
        }

        final ItemStack result = smithingInventory.getItem(2);
        if (result == null || result.getType() != Material.ELYTRA ||
            ArmoredElytra.getInstance().getNbtEditor().getArmorTier(result) == ArmorTier.NONE)
            return;

        giveItemToPlayer(player, result, e.isShiftClick());
        smithingInventory.clear();
    }
}
