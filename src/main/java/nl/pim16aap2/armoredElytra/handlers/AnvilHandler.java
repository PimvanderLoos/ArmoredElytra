package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nbtEditor.DurabilityManager;
import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.itemInput.ElytraInput;
import nl.pim16aap2.armoredElytra.util.itemInput.InputAction;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.logging.Level;

public class AnvilHandler extends ArmoredElytraHandler implements Listener
{
    public AnvilHandler(
        ArmoredElytra plugin,
        NBTEditor nbtEditor,
        DurabilityManager durabilityManager,
        ConfigLoader config)
    {
        super(plugin, nbtEditor, durabilityManager, config);
    }

    // Handle all anvil related stuff for this plugin.
    @EventHandler(priority = EventPriority.LOWEST)
    private void onAnvilInventoryOpen(PrepareAnvilEvent event)
    {
        if (!(event.getView().getPlayer() instanceof Player player))
            return;

        final var input = ElytraInput.fromInventory(config, durabilityManager, event.getInventory());
        if (input.isIgnored())
            return;

        event.setResult(armoredElytraBuilder.handleInput(player, input));
    }

    // Let the player take items out of the anvil.
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getRawSlot() != 2 || !(event.getWhoClicked() instanceof Player player))
            return;

        // Check if the event was a player who interacted with an anvil.
        if (event.getView().getType() != InventoryType.ANVIL ||
            !(event.getInventory() instanceof AnvilInventory anvilInventory))
            return;

        final @Nullable ItemStack result = anvilInventory.getItem(2);
        if (result == null)
            return;

        final var input = ElytraInput.fromInventory(config, durabilityManager, anvilInventory);
        if (input.isIgnored())
            return;

        if (nbtEditor.getArmorTierFromElytra(result) == ArmorTier.NONE)
        {
            plugin.myLogger(
                Level.SEVERE,
                "Anvil: Attempted to retrieve an item that is not an armored elytra! Result: " + result +
                    ", input: " + input);
            return;
        }

        event.setCancelled(true);

        if (input.isBlocked())
        {
            plugin.getLogger().severe("Anvil: Attempted to retrieve an item from a blocked recipe! Input: " + input);
            return;
        }

        if (!giveItemToPlayer(player, result, event.isShiftClick()))
            return;

        try
        {
            consumeInput(anvilInventory, input.inputAction());
        }
        catch (Exception e)
        {
            plugin.getLogger().severe("An error occurred while consuming the input of the anvil.");
            e.printStackTrace();
            anvilInventory.clear();
        }
    }

    /**
     * Consumes the input of the anvil.
     *
     * @param anvilInventory
     *     The anvil inventory to consume the input from.
     * @param inputAction
     *     The action the player performed.
     */
    private void consumeInput(AnvilInventory anvilInventory, InputAction inputAction)
    {
        if (inputAction != InputAction.REPAIR)
        {
            anvilInventory.clear();
            return;
        }

        final @Nullable ItemStack item0 = anvilInventory.getItem(0);
        final @Nullable ItemStack item1 = anvilInventory.getItem(1);

        final ItemStack elytra;
        final ItemStack repairItem;

        final int elytraSlot;
        final int repairItemSlot;

        ArmorTier armorTier = nbtEditor.getArmorTierFromElytra(item0);
        if (armorTier != ArmorTier.NONE)
        {
            elytraSlot = 0;
            repairItemSlot = 1;

            elytra = item0;
            repairItem = item1;
        }
        else
        {
            elytraSlot = 1;
            repairItemSlot = 0;

            armorTier = nbtEditor.getArmorTierFromElytra(item1);
            elytra = item1;
            repairItem = item0;
        }

        if (armorTier == ArmorTier.NONE)
            throw new IllegalStateException(
                "No elytra found in anvil inventory with contents: " + Arrays.toString(anvilInventory.getContents()));

        final Material expectedRepairItem = ArmorTier.getRepairItem(armorTier);
        if (repairItem == null || expectedRepairItem != repairItem.getType())
            throw new IllegalStateException(
                "Expected repair item to be '" + expectedRepairItem + "' but received '" + repairItem + "' in anvil " +
                    "inventory with contents: " + Arrays.toString(anvilInventory.getContents()));

        anvilInventory.setItem(2, null);

        final int fullRepairAmount = durabilityManager.getFullRepairItemCount(elytra, armorTier);
        final int newRepairItemAmount = Math.max(0, repairItem.getAmount() - fullRepairAmount);

        final @Nullable ItemStack newRepairItem;
        if (newRepairItemAmount == 0)
            newRepairItem = null;
        else
        {
            newRepairItem = repairItem.clone();
            newRepairItem.setAmount(newRepairItemAmount);
        }

        anvilInventory.setItem(repairItemSlot, newRepairItem);
        anvilInventory.setItem(elytraSlot, null);
    }
}
