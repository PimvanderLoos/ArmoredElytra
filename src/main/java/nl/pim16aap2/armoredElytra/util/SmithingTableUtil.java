package nl.pim16aap2.armoredElytra.util;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

/**
 * Utility class for the smithing table.
 */
public final class SmithingTableUtil
{
    /**
     * Whether the smithing table inventory has a slot for a template item.
     * <p>
     * Versions prior to 1.20 do not have this slot.
     */
    public static final boolean SMITHING_TABLE_HAS_TEMPLATE_SLOT;

    /**
     * The slot in the smithing table inventory where the template item is placed.
     * <p>
     * This slot is -1 on versions that do not have this slot in the smithing table inventory.
     */
    public static final int SMITHING_TABLE_TEMPLATE_SLOT;

    /**
     * The slot in the smithing table inventory where the first input item is placed.
     */
    public static final int SMITHING_TABLE_INPUT_SLOT_1;

    /**
     * The slot in the smithing table inventory where the second input item is placed.
     */
    public static final int SMITHING_TABLE_INPUT_SLOT_2;

    /**
     * The slot in the smithing table inventory where the result is placed.
     */
    public static final int SMITHING_TABLE_RESULT_SLOT;

    static
    {
        final Inventory smithingInventory = Bukkit.createInventory(null, InventoryType.SMITHING);
        SMITHING_TABLE_HAS_TEMPLATE_SLOT = smithingInventory.getSize() == 4;

        SMITHING_TABLE_RESULT_SLOT = smithingInventory.getSize() - 1;

        SMITHING_TABLE_INPUT_SLOT_2 = SMITHING_TABLE_RESULT_SLOT - 1;
        SMITHING_TABLE_INPUT_SLOT_1 = SMITHING_TABLE_INPUT_SLOT_2 - 1;
        SMITHING_TABLE_TEMPLATE_SLOT = SMITHING_TABLE_HAS_TEMPLATE_SLOT ? 0 : -1;
    }

    private SmithingTableUtil()
    {
    }
}
