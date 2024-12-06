package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nbtEditor.DurabilityManager;
import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.itemInput.ElytraInput;
import nl.pim16aap2.armoredElytra.util.itemInput.InputAction;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.semver4j.Semver;

import javax.annotation.Nullable;
import java.util.logging.Level;

import static nl.pim16aap2.armoredElytra.util.SmithingTableUtil.SMITHING_TABLE_HAS_TEMPLATE_SLOT;
import static nl.pim16aap2.armoredElytra.util.SmithingTableUtil.SMITHING_TABLE_INPUT_SLOT_1;
import static nl.pim16aap2.armoredElytra.util.SmithingTableUtil.SMITHING_TABLE_INPUT_SLOT_2;
import static nl.pim16aap2.armoredElytra.util.SmithingTableUtil.SMITHING_TABLE_RESULT_SLOT;
import static nl.pim16aap2.armoredElytra.util.SmithingTableUtil.SMITHING_TABLE_TEMPLATE_SLOT;

/**
 * Abstract class for handling SmithingTable events.
 * <p>
 * New instances of this class should be created using the
 * {@link #create(Semver, ArmoredElytra, NBTEditor, DurabilityManager, ConfigLoader)} method.
 */
public abstract class AbstractSmithingTableListener extends ArmoredElytraHandler implements Listener
{
    protected AbstractSmithingTableListener(
        ArmoredElytra plugin,
        NBTEditor nbtEditor,
        DurabilityManager durabilityManager,
        ConfigLoader config)
    {
        super(plugin, nbtEditor, durabilityManager, config);
    }

    public static AbstractSmithingTableListener create(
        Semver serverVersion,
        ArmoredElytra plugin,
        NBTEditor nbtEditor,
        DurabilityManager durabilityManager,
        ConfigLoader config)
    {
        if (serverVersion.isGreaterThanOrEqualTo(Semver.of(1, 21, 1)))
            return new SmithingTableRecipeListener(plugin, nbtEditor, durabilityManager, config);

        return new SmithingTableManualListener(plugin, nbtEditor, durabilityManager, config);
    }

    /**
     * Handles the usage of the SmithingTable.
     *
     * @param event
     *     The {@link PrepareSmithingEvent} to handle.
     *
     * @return The {@link ElytraInput} that was used to handle the event.
     */
    protected ElytraInput onSmithingTableUsage0(final PrepareSmithingEvent event)
    {
        final SmithingInventory inventory = event.getInventory();

        final var input = ElytraInput.fromInventory(config, inventory);
        if (input.isIgnored())
            return input;

        event.setResult(armoredElytraBuilder.handleInput(event.getView().getPlayer(), input));
        return input;
    }

    /**
     * Processes the {@link InventoryClickEvent} when the player clicks on the result slot in a smithing table.
     * <p>
     * This method will check if the result is an armored elytra, and if so, give it to the player.
     *
     * @param event
     *     The {@link InventoryClickEvent} to process.
     * @param player
     *     The {@link Player} who clicked on the result slot.
     * @param smithingInventory
     *     The {@link SmithingInventory} which was clicked.
     *
     * @throws IllegalArgumentException
     *     if the clicked slot is not the result slot.
     */
    protected final void onSmithingInventoryResultClick(
        InventoryClickEvent event,
        Player player,
        SmithingInventory smithingInventory)
    {
        if (event.getSlot() != SMITHING_TABLE_RESULT_SLOT)
            throw new IllegalArgumentException(
                "Clicked slot must be '" + SMITHING_TABLE_RESULT_SLOT + "' but received '" + event.getSlot() + "'");

        if (smithingInventory.getItem(SMITHING_TABLE_INPUT_SLOT_1) == null ||
            smithingInventory.getItem(SMITHING_TABLE_INPUT_SLOT_2) == null ||
            smithingInventory.getItem(SMITHING_TABLE_RESULT_SLOT) == null)
            return;

        final @Nullable ItemStack result = smithingInventory.getItem(SMITHING_TABLE_RESULT_SLOT);
        if (result == null)
            return;

        final var input = ElytraInput.fromInventory(config, smithingInventory);
        if (input.isIgnored())
            return;

        if (nbtEditor.getArmorTierFromElytra(result) == ArmorTier.NONE)
        {
            plugin.myLogger(
                Level.SEVERE,
                "Smithing Table: Attempted to retrieve an item that is not an armored elytra! Result: " + result +
                    ", input: " + input);
            return;
        }

        event.setCancelled(true);

        if (isRecipeResultPlaceholder(result))
        {
            plugin.myLogger(
                Level.SEVERE,
                "Smithing Table: Attempted to retrieve a placeholder result! Result: " + result + ", input: " + input);
            return;
        }

        if (input.isBlocked())
        {
            plugin.myLogger(
                Level.SEVERE,
                "Smithing Table: Attempted to retrieve an item from a blocked recipe! Input: " + input);
            return;
        }

        if (!giveItemToPlayer(player, result, event.isShiftClick()))
            return;

        useItem(smithingInventory, SMITHING_TABLE_RESULT_SLOT);
        useItem(smithingInventory, SMITHING_TABLE_INPUT_SLOT_1);
        useItem(smithingInventory, SMITHING_TABLE_INPUT_SLOT_2);

        if (SMITHING_TABLE_HAS_TEMPLATE_SLOT && input.inputAction() == InputAction.UPGRADE)
            useItem(smithingInventory, SMITHING_TABLE_TEMPLATE_SLOT);
    }

    /**
     * Checks if the given {@link ItemStack} is a placeholder for a recipe result.
     * <p>
     * Implementations that do not use a placeholder result do not need to override this method.
     *
     * @param result
     *     The {@link ItemStack} to check.
     *
     * @return {@code true} if the given {@link ItemStack} is a placeholder for a recipe result, {@code false}
     * otherwise.
     */
    protected boolean isRecipeResultPlaceholder(@Nullable ItemStack result)
    {
        return false;
    }

    /**
     * Consumes a single item from the given slot in the given {@link SmithingInventory}.
     *
     * @param smithingInventory
     *     The {@link SmithingInventory} to consume the item from.
     * @param slot
     *     The slot to consume the item from.
     */
    protected void useItem(SmithingInventory smithingInventory, int slot)
    {
        final ItemStack item = smithingInventory.getItem(slot);
        if (item == null)
            return;

        item.setAmount(item.getAmount() - 1);
        if (item.getAmount() == 0)
            smithingInventory.setItem(slot, null);
        smithingInventory.setItem(slot, item);
    }
}
