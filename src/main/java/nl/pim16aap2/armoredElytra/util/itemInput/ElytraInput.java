package nl.pim16aap2.armoredElytra.util.itemInput;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nbtEditor.DurabilityManager;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

import static nl.pim16aap2.armoredElytra.util.SmithingTableUtil.SMITHING_TABLE_HAS_TEMPLATE_SLOT;
import static nl.pim16aap2.armoredElytra.util.SmithingTableUtil.SMITHING_TABLE_INPUT_SLOT_1;
import static nl.pim16aap2.armoredElytra.util.SmithingTableUtil.SMITHING_TABLE_INPUT_SLOT_2;
import static nl.pim16aap2.armoredElytra.util.SmithingTableUtil.SMITHING_TABLE_TEMPLATE_SLOT;

/**
 * Represents some input that involves an (armored) elytra.
 * <p>
 * The recommended way to create an instance of this class is by using the static factory methods
 * {@link #fromInventory(ConfigLoader, DurabilityManager, AnvilInventory)} and
 * {@link #fromInventory(ConfigLoader, SmithingInventory)}. These methods will ensure that the input is valid.
 *
 * @param elytra
 *     The elytra item that is being processed.
 * @param combinedWith
 *     The item that the elytra is being combined with.
 * @param template
 *     The template item that is being used.
 * @param name
 *     The name that the elytra should be renamed to. If null, the name will not be changed.
 * @param inputAction
 *     The action that is being performed.
 * @param oldArmorTier
 *     The old armor tier of the elytra.
 * @param newArmorTier
 *     The new armor tier of the elytra.
 */
public record ElytraInput(
    ItemStack elytra,
    @Nullable ItemStack combinedWith,
    @Nullable ItemStack template,
    @Nullable String name,
    InputAction inputAction,
    ArmorTier oldArmorTier,
    ArmorTier newArmorTier
)
{
    /**
     * The default instance of {@link ElytraInput} used when the {@link InputAction} is {@link InputAction#IGNORE}.
     */
    private static final ElytraInput IGNORED =
        new ElytraInput(
            new ItemStack(Material.ELYTRA),
            null,
            null,
            null,
            InputAction.IGNORE,
            ArmorTier.NONE,
            ArmorTier.NONE);

    public ElytraInput
    {
        if (Objects.requireNonNull(elytra).getType() != Material.ELYTRA)
            throw new IllegalArgumentException("Elytra must be an elytra!");
    }

    private ElytraInput(
        InputItems inputItems,
        @Nullable ItemStack template,
        @Nullable String name,
        InputAction inputAction,
        ArmorTier oldArmorTier,
        ArmorTier newArmorTier)
    {
        this(inputItems.elytra(), inputItems.combinedWith(), template, name, inputAction, oldArmorTier, newArmorTier);
    }

    /**
     * Checks whether the input is ignored.
     *
     * @param input
     *     The input to check. If null, this method will return false.
     *
     * @return Whether the input is ignored.
     */
    public static boolean isIgnored(@Nullable ElytraInput input)
    {
        return input != null && input.inputAction() == InputAction.IGNORE;
    }

    /**
     * Creates a new {@link ElytraInput} from the given {@link AnvilInventory}.
     *
     * @param config
     *     The {@link ConfigLoader} to use for determining how to handle the input (e.g.
     *     {@link ConfigLoader#allowCraftingInAnvil()}).
     * @param durabilityManager
     *     The {@link DurabilityManager} to use for determining the durability of the elytra.
     * @param inventory
     *     The inventory to create the {@link ElytraInput} from.
     *
     * @return The created {@link ElytraInput} or null if the input is invalid.
     */
    public static @Nullable ElytraInput fromInventory(
        ConfigLoader config,
        DurabilityManager durabilityManager,
        AnvilInventory inventory)
    {
        final var nbtEditor = ArmoredElytra.getInstance().getNbtEditor();

        final InputItems inputItems = InputItems.fromContents(0, 1, inventory.getContents());
        if (!inputItems.isValid())
            return ElytraInput.IGNORED;

        final ArmorTier oldArmorTier = nbtEditor.getArmorTierFromElytra(inputItems.elytra());
        final ArmorTier newArmorTier = Util.armorToTier(inputItems.combinedWith());

        final ArmorTier nameUpdateArmorTier = newArmorTier != ArmorTier.NONE ? newArmorTier : oldArmorTier;

        final NameUpdate nameUpdate =
            NameUpdate.fromInput(config, inputItems.elytra(), nameUpdateArmorTier, inventory.getRenameText());

        @Nullable InputAction inputAction = null;

        final boolean elytraIsArmored = oldArmorTier != ArmorTier.NONE;

        // If we only have an armored elytra in the first slot and the name is updated,
        // we know that the player is renaming the elytra.
        if (elytraIsArmored &&
            inputItems.combinedWith() == null &&
            inputItems.elytraIsFirst() &&
            nameUpdate.overrideName() != null)
        {
            inputAction = config.allowRenaming() ? InputAction.RENAME : InputAction.BLOCK;
        }

        else if (!elytraIsArmored)
        {
            // If the item we are combining with is not a chest plate, we ignore the input.
            if (newArmorTier == ArmorTier.NONE)
                inputAction = InputAction.IGNORE;
            else if (!config.allowCraftingInAnvil())
                inputAction = InputAction.BLOCK;
            else
                inputAction = InputAction.CREATE;
        }

        // For all other cases, we need a second item to combine the elytra with.
        else if (!inputItems.isFilled())
        {
            inputAction = InputAction.IGNORE;
        }

        else if (inputItems.combinedWithType() == Material.ENCHANTED_BOOK)
        {
            inputAction = config.allowAddingEnchantments() ? InputAction.ENCHANT : InputAction.BLOCK;
        }

        else if (inputItems.combinedWithType() == ArmorTier.getRepairItem(oldArmorTier))
        {
            inputAction =
                durabilityManager.getRealDurability(inputItems.elytra(), oldArmorTier) == 0 ?
                InputAction.BLOCK :
                InputAction.REPAIR;
        }

        else if (oldArmorTier != ArmorTier.LEATHER &&
            inputItems.combinedWithType() == Material.ELYTRA ||
            inputItems.combinedWithType() == Material.LEATHER ||
            inputItems.combinedWithType() == Material.PHANTOM_MEMBRANE)
        {
            inputAction = InputAction.BLOCK;
        }

        if (inputAction == null)
            return null;

        if (inputAction == InputAction.IGNORE)
            return ElytraInput.IGNORED;

        return new ElytraInput(
            inputItems,
            null,
            nameUpdate.finalName(),
            inputAction,
            oldArmorTier,
            newArmorTier
        );
    }

    /**
     * Creates a new {@link ElytraInput} from the given {@link SmithingInventory}.
     *
     * @param config
     *     The {@link ConfigLoader} to use for determining how to handle the input (e.g.
     *     {@link ConfigLoader#allowCraftingInSmithingTable()}).
     * @param inventory
     *     The inventory to create the {@link ElytraInput} from.
     *
     * @return The created {@link ElytraInput} or null if the input is invalid.
     */
    public static @Nullable ElytraInput fromInventory(ConfigLoader config, SmithingInventory inventory)
    {
        final ItemStack[] contents = inventory.getContents();

        final InputItems inputItems = InputItems.fromContents(
            SMITHING_TABLE_INPUT_SLOT_1, SMITHING_TABLE_INPUT_SLOT_2, contents);

        if (!inputItems.isValid())
            return ElytraInput.IGNORED;

        // Only a regular elytra can be upgraded into an armored elytra.
        final var oldArmorTier = ArmoredElytra.getInstance().getNbtEditor().getArmorTierFromElytra(inputItems.elytra());
        if (oldArmorTier != ArmorTier.NONE)
            return ElytraInput.IGNORED;

        // We can only apply chest plates to elytras.
        final var newArmorTier = Util.armorToTier(inputItems.combinedWith());
        if (newArmorTier == ArmorTier.NONE)
            return ElytraInput.IGNORED;

        final @Nullable ItemStack template =
            SMITHING_TABLE_HAS_TEMPLATE_SLOT ? contents[SMITHING_TABLE_TEMPLATE_SLOT] : null;

        if (!config.allowCraftingInSmithingTable())
            return ElytraInput.IGNORED;

        final var inputAction = InputAction.CREATE;

        return new ElytraInput(
            inputItems.elytra(),
            inputItems.combinedWith(),
            template,
            null,
            inputAction,
            oldArmorTier,
            newArmorTier
        );
    }

    /**
     * Represents the elytra and the item it is combined with.
     *
     * @param elytra
     *     The 'elytra' item. Whether this is actually an elytra should be checked by the caller or by using the
     *     {@link #isValid()} method.
     * @param combinedWith
     *     The item the elytra is combined with.
     * @param combinedWithType
     *     The type of the item the elytra is combined with.
     * @param elytraIsFirst
     *     Whether the elytra is the first item in the input.
     */
    private record InputItems(
        @Nullable ItemStack elytra,
        @Nullable ItemStack combinedWith,
        @Nullable Material combinedWithType,
        boolean elytraIsFirst
    )
    {
        /**
         * Checks whether the input is filled. This means that both the elytra and the item it is combined with are not
         * null.
         *
         * @return Whether the input is filled.
         */
        boolean isFilled()
        {
            return elytra != null && combinedWith != null;
        }

        /**
         * Checks whether the input is valid. This means that the elytra is not null and is an elytra.
         * <p>
         * No constraints are placed on the combinedWith item.
         *
         * @return Whether the input is valid.
         */
        boolean isValid()
        {
            return elytra != null && elytra.getType() == Material.ELYTRA;
        }

        /**
         * Creates a new {@link InputItems} from the given contents.
         *
         * @param idxElytra
         *     The index of the elytra item.
         * @param idxCombinedWIth
         *     The index of the item the elytra is combined with.
         * @param contents
         *     The contents to create the {@link InputItems} from.
         *
         * @return The created {@link InputItems}.
         *
         * @throws IllegalArgumentException
         *     If the contents do not have a length of 2.
         *     <p>
         *     If the elytra is not an elytra.
         */
        private static InputItems fromContents(int idxElytra, int idxCombinedWIth, ItemStack... contents)
        {
            if (contents.length < 2)
                throw new IllegalArgumentException(
                    "Contents must have a length of 2! Got " + contents.length + " instead. " +
                        Arrays.toString(contents));

            @Nullable ItemStack elytra = contents[idxElytra];
            @Nullable ItemStack combinedWith = contents[idxCombinedWIth];

            final boolean elytraIsFirst = elytra != null && elytra.getType() == Material.ELYTRA;

            if (elytra != null && elytra.getType() != Material.ELYTRA &&
                combinedWith != null && combinedWith.getType() == Material.ELYTRA)
            {
                final ItemStack temp = elytra;
                elytra = combinedWith;
                combinedWith = temp;
            }

            final @Nullable Material combinedWithType = combinedWith != null ? combinedWith.getType() : null;

            return new InputItems(elytra, combinedWith, combinedWithType, elytraIsFirst);
        }
    }

    /**
     * Represents a name update for an elytra.
     *
     * @param defaultName
     *     The default name of the elytra as specified in the config for a given armor tier.
     * @param overrideName
     *     The name provided as input by the player to override the default name.
     * @param finalName
     *     The final name that the elytra should have. This can be the default name or the override name depending on
     *     the input and the config (e.g. whether renaming is allowed).
     */
    private record NameUpdate(
        String defaultName,
        @Nullable String overrideName,
        String finalName
    )
    {
        public NameUpdate
        {
            if (defaultName.isEmpty())
                throw new IllegalArgumentException("Default name cannot be empty!");
            if (finalName.isEmpty())
                throw new IllegalArgumentException("Final name cannot be empty!");
        }

        private NameUpdate(String defaultName, @Nullable String overrideName)
        {
            this(defaultName, overrideName,
                 (overrideName != null && !overrideName.isBlank()) ? overrideName : defaultName);
        }

        private static NameUpdate fromInput(
            ConfigLoader config,
            ItemStack elytra,
            ArmorTier newArmorTier,
            @Nullable String renameText)
        {
            final String name = ArmoredElytra.getInstance().getArmoredElytraName(newArmorTier);

            if (renameText == null)
                return new NameUpdate(name, null);

            final @Nullable ItemMeta meta = elytra.getItemMeta();
            if (meta == null)
                throw new IllegalArgumentException("Item meta is null for input elytra: '" + elytra + "'");

            final @Nullable String strippedCurrentName = ChatColor.stripColor(meta.getDisplayName());
            if (strippedCurrentName.equals(ChatColor.stripColor(renameText)))
                return new NameUpdate(name, null);

            // If the player is now allowed to rename, the final name should be the default name.
            if (!config.allowRenaming())
                return new NameUpdate(name, renameText, name);

            return new NameUpdate(name, renameText);
        }
    }
}
