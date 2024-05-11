package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nbtEditor.DurabilityManager;
import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.Util;
import nl.pim16aap2.armoredElytra.util.itemInput.ElytraInput;
import nl.pim16aap2.armoredElytra.util.itemInput.InputAction;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.SmithingInventory;

import javax.annotation.Nullable;
import java.util.logging.Level;

import static nl.pim16aap2.armoredElytra.util.SmithingTableUtil.SMITHING_TABLE_HAS_TEMPLATE_SLOT;
import static nl.pim16aap2.armoredElytra.util.SmithingTableUtil.SMITHING_TABLE_INPUT_SLOT_1;
import static nl.pim16aap2.armoredElytra.util.SmithingTableUtil.SMITHING_TABLE_INPUT_SLOT_2;
import static nl.pim16aap2.armoredElytra.util.SmithingTableUtil.SMITHING_TABLE_RESULT_SLOT;
import static nl.pim16aap2.armoredElytra.util.SmithingTableUtil.SMITHING_TABLE_TEMPLATE_SLOT;

/**
 * Class for handling smithing table events.
 */
public class SmithingTableListener extends ArmoredElytraHandler implements Listener
{
    public SmithingTableListener(
        ArmoredElytra plugin,
        NBTEditor nbtEditor,
        DurabilityManager durabilityManager,
        ConfigLoader config)
    {
        super(plugin, nbtEditor, durabilityManager, config);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSmithingTableUsage(final PrepareSmithingEvent event)
    {
        final SmithingInventory inventory = event.getInventory();

        final var input = ElytraInput.fromInventory(config, inventory);
        if (input.isIgnored())
            return;

        event.setResult(armoredElytraBuilder.handleInput(event.getView().getPlayer(), input));
    }

    /**
     * Processes the general {@link InventoryClickEvent} for this plugin.
     * <p>
     * This method will check if the event is fired while a smithing table is open, and if so, will call the appropriate
     * methods to further process the event.
     * <p>
     * See {@link #onPlayerInventoryClick(InventoryClickEvent, SmithingInventory)} and
     * {@link #onSmithingInventoryClick(InventoryClickEvent, Player, SmithingInventory)}.
     *
     * @param event
     *     The {@link InventoryClickEvent} to process.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent event)
    {
        final Player player = Util.humanEntityToPlayer(event.getWhoClicked());

        if (!(player.getOpenInventory().getTopInventory() instanceof SmithingInventory smithingInventory))
            return;

        if (event.getClickedInventory() instanceof PlayerInventory)
            onPlayerInventoryClick(event, smithingInventory);
        else if (event.getClickedInventory() instanceof SmithingInventory clickedSmithingInventory)
            onSmithingInventoryClick(event, player, clickedSmithingInventory);
    }

    /**
     * Attempts to insert the given {@link ItemStack} into the smithing table.
     * <p>
     * The source item will be inserted into the first slot if it is an armored elytra, and into the second slot if it
     * is a regular elytra.
     * <p>
     * Only 1 item will be inserted into the second slot, and the source item will have its amount reduced by 1.
     *
     * @param smithingInventory
     *     The {@link SmithingInventory} to insert the item into.
     * @param event
     *     The {@link InventoryClickEvent} to process.
     * @param clickedSlot
     *     The slot that was clicked in the smithing table or {@code null} if the slot was not in the smithing table.
     */
    protected void insertElytraToSmithingTable(
        SmithingInventory smithingInventory,
        InventoryClickEvent event,
        @Nullable Integer clickedSlot)
    {
        final @Nullable ItemStack cursor = event.getCursor();
        final @Nullable ItemStack current = event.getCurrentItem();
        final @Nullable ItemStack source = event.isShiftClick() ? current : cursor;

        if (source == null || source.getType() != Material.ELYTRA)
            return;

        final @Nullable ItemStack itemA = smithingInventory.getItem(SMITHING_TABLE_INPUT_SLOT_1);
        final @Nullable ItemStack itemB = smithingInventory.getItem(SMITHING_TABLE_INPUT_SLOT_2);

        if (itemA != null && itemB != null)
            return;

        final ArmorTier armorTier = nbtEditor.getArmorTierFromElytra(source);
        final int targetSlot;
        if (armorTier == ArmorTier.NONE)
        {
            if (!config.allowCraftingInSmithingTable())
                return;
            targetSlot = SMITHING_TABLE_INPUT_SLOT_2;
        }
        else
            targetSlot = SMITHING_TABLE_INPUT_SLOT_1;

        if (clickedSlot != null && clickedSlot != targetSlot)
            return;

        if (event.isShiftClick() && smithingInventory.getItem(targetSlot) != null)
            return;

        final ItemStack insertedItem = source.clone();
        insertedItem.setAmount(1);

        final @Nullable ItemStack swapItem = current;

        smithingInventory.setItem(targetSlot, insertedItem);
        source.setAmount(source.getAmount() - 1);

        event.getWhoClicked().setItemOnCursor(swapItem);

        event.setCancelled(true);
    }

    /**
     * Processes the {@link InventoryClickEvent} when the player clicks on a slot in their inventory while a smithing
     * table is open.
     * <p>
     * This method will check if the player is shift-clicking, and if so, try to move the item to the smithing table if
     * possible.
     *
     * @param event
     *     The {@link InventoryClickEvent} to process.
     * @param smithingInventory
     *     The {@link SmithingInventory} that the player has open (but was not clicked).
     */
    protected void onPlayerInventoryClick(
        InventoryClickEvent event,
        SmithingInventory smithingInventory)
    {
        if (!event.isShiftClick())
            return;

        insertElytraToSmithingTable(smithingInventory, event, null);
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
    protected void onSmithingInventoryResultClick(
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
        final var input = ElytraInput.fromInventory(config, smithingInventory);

        if (nbtEditor.getArmorTierFromElytra(result) == ArmorTier.NONE)
        {
            plugin.myLogger(
                Level.SEVERE,
                "Smithing Table: Attempted to retrieve an item that is not an armored elytra! Result: " + result +
                    ", input: " + input);
            return;
        }

        if (input.isIgnored())
            return;

        event.setCancelled(true);

        if (input.isBlocked())
        {
            plugin.myLogger(
                Level.SEVERE,
                "Smithing Table: Attempted to retrieve an item from a blocked recipe! Input: " + input);
            return;
        }

        if (!giveItemToPlayer(player, result, event.isShiftClick()))
            return;

        smithingInventory.setItem(SMITHING_TABLE_RESULT_SLOT, null);
        smithingInventory.setItem(SMITHING_TABLE_INPUT_SLOT_1, null);
        useItem(smithingInventory, SMITHING_TABLE_INPUT_SLOT_2);

        if (SMITHING_TABLE_HAS_TEMPLATE_SLOT &&
            (input.inputAction() == InputAction.APPLY_TEMPLATE || input.inputAction() == InputAction.UPGRADE))
        {
            useItem(smithingInventory, SMITHING_TABLE_TEMPLATE_SLOT);
        }
    }

    /**
     * Consumes a single item from the given slot in the given {@link SmithingInventory}.
     *
     * @param smithingInventory
     *     The {@link SmithingInventory} to consume the item from.
     * @param slot
     *     The slot to consume the item from.
     */
    private void useItem(SmithingInventory smithingInventory, int slot)
    {
        final ItemStack item = smithingInventory.getItem(slot);
        if (item == null)
            return;

        item.setAmount(item.getAmount() - 1);
        if (item.getAmount() == 0)
            smithingInventory.setItem(slot, null);
        smithingInventory.setItem(slot, item);
    }

    /**
     * Processes the {@link InventoryClickEvent} when the player clicks on a slot in a smithing table.
     *
     * @param event
     *     The {@link InventoryClickEvent} to process.
     * @param player
     *     The {@link Player} who clicked on a slot in the smithing table.
     * @param smithingInventory
     *     The {@link SmithingInventory} which was clicked.
     */
    protected void onSmithingInventoryClick(
        InventoryClickEvent event,
        Player player,
        SmithingInventory smithingInventory)
    {
        if (event.getSlot() == SMITHING_TABLE_RESULT_SLOT)
            onSmithingInventoryResultClick(event, player, smithingInventory);
        else if (event.getSlot() == SMITHING_TABLE_INPUT_SLOT_2 || event.getSlot() == SMITHING_TABLE_INPUT_SLOT_1)
            insertElytraToSmithingTable(smithingInventory, event, event.getSlot());
    }
}
