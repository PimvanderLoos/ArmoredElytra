package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.Util;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

import javax.annotation.Nullable;

import static nl.pim16aap2.armoredElytra.handlers.SmithingTableListener.*;

/**
 * Represents the input of a smithing table.
 *
 * @param elytra
 *     The elytra to be combined.
 * @param combinedWith
 *     The chestplate to be combined with the elytra.
 * @param template
 *     The template to be used for the combination.
 * @param newArmorTier
 *     The new armor tier of the resulting armored elytra.
 */
public record SmithingTableInput(
    @Nullable ItemStack elytra,
    @Nullable ItemStack combinedWith,
    @Nullable ItemStack template,
    ArmorTier oldArmorTier,
    ArmorTier newArmorTier
)
{
    public SmithingTableInput
    {
        if (elytra != null && elytra.getType() != Material.ELYTRA)
            throw new IllegalArgumentException("Elytra must be an elytra!");
    }

    /**
     * Checks if all the required items are present.
     *
     * @return True if all the required items are present, false otherwise.
     */
    public boolean isFull()
    {
        return elytra != null &&
            combinedWith != null &&
            (!HAS_TEMPLATE_SLOT || template != null);
    }

    /**
     * Checks if all the required items are present, except for the template.
     *
     * @return True if all the required items are present, false otherwise.
     */
    public boolean isFullWithoutTemplate()
    {
        return elytra != null && combinedWith != null;
    }

    /**
     * Creates a new {@link SmithingTableInput} from the given {@link SmithingInventory}.
     *
     * @param inventory
     *     The inventory to create the {@link SmithingTableInput} from.
     *
     * @return The created {@link SmithingTableInput} or null if the input is invalid.
     */
    public static @Nullable SmithingTableInput fromInventory(SmithingInventory inventory)
    {
        final ItemStack[] contents = inventory.getContents();
        @Nullable ItemStack itemStackA = contents[SMITHING_TABLE_INPUT_SLOT_1];
        @Nullable ItemStack itemStackB = contents[SMITHING_TABLE_INPUT_SLOT_2];

        // Ensure that itemStackA is the elytra.
        if (itemStackA != null && itemStackA.getType() != Material.ELYTRA &&
            itemStackB != null && itemStackB.getType() == Material.ELYTRA)
        {
            final ItemStack temp = itemStackA;
            itemStackA = itemStackB;
            itemStackB = temp;
        }

        if (itemStackA != null && itemStackA.getType() != Material.ELYTRA)
            return null;

        final @Nullable ItemStack template = HAS_TEMPLATE_SLOT ? contents[SMITHING_TABLE_TEMPLATE_SLOT] : null;
        final var oldArmorTier = ArmoredElytra.getInstance().getNbtEditor().getArmorTier(itemStackA);
        final var newArmorTier = Util.armorToTier(itemStackB);

        return new SmithingTableInput(
            itemStackA,
            itemStackB,
            template,
            oldArmorTier,
            newArmorTier
        );
    }
}
