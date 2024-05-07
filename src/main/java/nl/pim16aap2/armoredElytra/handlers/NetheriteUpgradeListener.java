package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nbtEditor.DurabilityManager;
import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

import javax.annotation.Nullable;

public class NetheriteUpgradeListener extends SmithingTableListener
{
    public NetheriteUpgradeListener(
        ArmoredElytra plugin,
        NBTEditor nbtEditor,
        DurabilityManager durabilityManager,
        ConfigLoader config)
    {
        super(plugin, false, nbtEditor, durabilityManager, config);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSmithingTableUsage(final PrepareSmithingEvent event)
    {
        final SmithingInventory inventory = event.getInventory();
        final ItemStack[] contents = inventory.getContents();

        final ItemStack itemStackA = contents[SMITHING_TABLE_INPUT_SLOT_1];
        final ItemStack itemStackB = contents[SMITHING_TABLE_INPUT_SLOT_2];

        if (!validInput(itemStackA, itemStackB))
            return;

        if (!plugin.playerHasCraftPerm(event.getView().getPlayer(), ArmorTier.NETHERITE))
            return;

        event.setResult(armoredElytraBuilder
                            .newBuilder()
                            .ofElytra(itemStackA)
                            .upgradeToTier(ArmorTier.NETHERITE)
                            .build());
    }

    private boolean validInput(@Nullable ItemStack itemStackA, @Nullable ItemStack itemStackB)
    {
        if (itemStackA == null || itemStackB == null ||
            itemStackA.getType() != Material.ELYTRA ||
            plugin.getNbtEditor().getArmorTier(itemStackA) != ArmorTier.DIAMOND ||
            itemStackB.getType() != Material.NETHERITE_INGOT)
            return false;

        // For some reason, adding multiple netherite ingots causes the view to not update properly.
        // The resulting armored elytra is hidden and the red cross indicates the combination is impossible.
        // But if you click on where the output was supposed to be, it DOES work for some reason.
        // It kinda works if you add a slight delay, but I don't really like that. Might revisit this later. CBA now.
        return itemStackA.getAmount() == 1 && itemStackB.getAmount() == 1;
    }

    @Override
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event)
    {
        super.onInventoryClick(event);
    }
}
