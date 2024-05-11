package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nbtEditor.DurabilityManager;
import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.SmithingInventory;

import javax.annotation.Nullable;

import static nl.pim16aap2.armoredElytra.util.SmithingTableUtil.SMITHING_TABLE_INPUT_SLOT_1;
import static nl.pim16aap2.armoredElytra.util.SmithingTableUtil.SMITHING_TABLE_INPUT_SLOT_2;
import static nl.pim16aap2.armoredElytra.util.SmithingTableUtil.SMITHING_TABLE_RESULT_SLOT;

/**
 * Abstract class for handling smithing table events.
 */
public abstract class SmithingTableListener extends ArmoredElytraHandler implements Listener
{
    protected SmithingTableListener(
        ArmoredElytra plugin,
        boolean creationEnabled,
        NBTEditor nbtEditor,
        DurabilityManager durabilityManager,
        ConfigLoader config)
    {
        super(plugin, creationEnabled, nbtEditor, durabilityManager, config);
    }

    private static String getItemType(@Nullable ItemStack itemStack)
    {
        return itemStack == null ? "null" : itemStack.getType().name();
    }

    /**
     * Attempts to insert the given {@link ItemStack} into the smithing table.
     * <p>
     * The source item will be inserted into the second slot if the following conditions are met:
     * <ul>
     *     <li>The source item is an elytra.</li>
     *     <li>The first slot is empty or contains a chestplate.</li>
     *     <li>The second slot is empty.</li>
     * </ul>
     * <p>
     * Only 1 item will be inserted into the second slot, and the source item will have its amount reduced by 1.
     *
     * @param smithingInventory
     *     The {@link SmithingInventory} to insert the item into.
     * @param event
     *     The {@link InventoryClickEvent} to process.
     */
    protected void insertElytraToSmithingTable(SmithingInventory smithingInventory, InventoryClickEvent event)
    {
        final @Nullable ItemStack cursor = event.getCursor();
        final @Nullable ItemStack current = event.getCurrentItem();

        // Sometimes the elytra to be inserted is in the cursor,
        // sometimes it is the 'current' item (in which case the cursor is null).
        final @Nullable ItemStack source =
            cursor == null || cursor.getType() == Material.AIR ? current : cursor;

        if (source == null || source.getType() != Material.ELYTRA)
            return;

        // Do not allow the player to insert an armored elytra.
        if (nbtEditor.getArmorTierFromElytra(source) != ArmorTier.NONE)
            return;

        final ItemStack itemA = smithingInventory.getItem(SMITHING_TABLE_INPUT_SLOT_1);
        if (!Util.isChestPlate(itemA))
            return;

        final ItemStack itemB = smithingInventory.getItem(SMITHING_TABLE_INPUT_SLOT_2);
        if (itemB != null)
            return;

        final ItemStack newItemB = source.clone();
        newItemB.setAmount(1);

        smithingInventory.setItem(SMITHING_TABLE_INPUT_SLOT_2, newItemB);
        source.setAmount(source.getAmount() - 1);

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

        insertElytraToSmithingTable(smithingInventory, event);
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

        final ItemStack result = smithingInventory.getItem(SMITHING_TABLE_RESULT_SLOT);

        if (nbtEditor.getArmorTierFromElytra(result) == ArmorTier.NONE)
            return;

        if (!giveItemToPlayer(player, result, event.isShiftClick()))
            return;

        smithingInventory.setItem(SMITHING_TABLE_RESULT_SLOT, null);
        smithingInventory.setItem(SMITHING_TABLE_INPUT_SLOT_1, null);
        smithingInventory.setItem(SMITHING_TABLE_INPUT_SLOT_2, null);

        event.setCancelled(true);
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
        else if (event.getSlot() == SMITHING_TABLE_INPUT_SLOT_2)
            insertElytraToSmithingTable(smithingInventory, event);
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
    protected void onInventoryClick(InventoryClickEvent event)
    {
        final Player player = Util.humanEntityToPlayer(event.getWhoClicked());

        if (!(player.getOpenInventory().getTopInventory() instanceof SmithingInventory smithingInventory))
            return;

        if (event.getClickedInventory() instanceof PlayerInventory)
            onPlayerInventoryClick(event, smithingInventory);
        else if (event.getClickedInventory() instanceof SmithingInventory clickedSmithingInventory)
            onSmithingInventoryClick(event, player, clickedSmithingInventory);
    }
}
