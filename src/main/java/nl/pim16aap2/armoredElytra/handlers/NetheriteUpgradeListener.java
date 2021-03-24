package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

public class NetheriteUpgradeListener extends SmithingTableListener
{
    public NetheriteUpgradeListener(final ArmoredElytra plugin)
    {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSmithingTableUsage(final PrepareSmithingEvent event)
    {
        super.onSmithingTableUsage(event);
    }

    @Override
    protected ArmorTier getArmorTier(ItemStack itemStackA, ItemStack itemStackB)
    {
        if (itemStackA == null || itemStackB == null ||
            itemStackA.getType() != Material.ELYTRA ||
            plugin.getNbtEditor().getArmorTier(itemStackA) != ArmorTier.DIAMOND ||
            itemStackB.getType() != Material.NETHERITE_INGOT)
            return ArmorTier.NONE;

        // For some reason, adding multiple netherite ingots causes the view to not update properly.
        // The resulting armored elytra is hidden and the red cross indicates the combination is impossible.
        // But if you click on where the output was supposed to be, it DOES work for some reason.
        // It kinda works if you add a slight delay, but I don't really like that. Might revisit this later. CBA now.
        if (itemStackA.getAmount() != 1 || itemStackB.getAmount() != 1)
            return ArmorTier.NONE;

        return ArmorTier.NETHERITE;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (!isAESmithingTableEvent(event))
            return;

        // These casts may look unchecked, but it was checked by isSmithingTableEvent already.
        SmithingInventory smithingInventory = (SmithingInventory) event.getInventory();
        Player player = (Player) event.getWhoClicked();

        giveItemToPlayer(player, smithingInventory.getItem(2), event.isShiftClick());
        smithingInventory.clear();
    }
}
