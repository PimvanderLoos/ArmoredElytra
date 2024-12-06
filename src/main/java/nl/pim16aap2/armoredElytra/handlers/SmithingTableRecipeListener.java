package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nbtEditor.AutoPersistentDataContainer;
import nl.pim16aap2.armoredElytra.nbtEditor.DurabilityManager;
import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.Util;
import nl.pim16aap2.armoredElytra.util.itemInput.ElytraInput;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;

import static nl.pim16aap2.armoredElytra.util.SmithingTableUtil.SMITHING_TABLE_HAS_TEMPLATE_SLOT;
import static nl.pim16aap2.armoredElytra.util.SmithingTableUtil.SMITHING_TABLE_RESULT_SLOT;

/**
 * Class for handling smithing table events using recipes.
 * <p>
 * This class should only be used on servers running Minecraft 1.21.1 or newer.
 */
class SmithingTableRecipeListener extends AbstractSmithingTableListener implements Listener
{
    /**
     * The template item for upgrading to netherite.
     * <p>
     * This is {@code null} on versions without a template slot.
     */
    // Currently broken on 1.20.6 :(
    private static final @Nullable Material NETHERITE_UPGRADE_TEMPLATE_MATERIAL =
        SMITHING_TABLE_HAS_TEMPLATE_SLOT ?
        Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE :
        null;

    /**
     * The recipe choice for the template item for upgrading to netherite.
     * <p>
     * This is {@code null} on versions without a template slot.
     */
    private static final @Nullable RecipeChoice NETHERITE_UPGRADE_TEMPLATE_CHOICE =
        NETHERITE_UPGRADE_TEMPLATE_MATERIAL == null ?
        null :
        new RecipeChoice.MaterialChoice(NETHERITE_UPGRADE_TEMPLATE_MATERIAL);

    /**
     * The namespaced key for the placeholder result.
     */
    private static final NamespacedKey RECIPE_PLACEHOLDER_KEY =
        new NamespacedKey(ArmoredElytra.getInstance(), "st_placeholder");

    /**
     * Placeholder result. Our event handler will handle the actual result, including:
     * <ul>
     *     <li>Permissions</li>
     *     <li>Durability</li>
     *     <li>Enchantments</li>
     *     <li>Whatever else we might add in the future.</li>
     * </ul>
     */
    private static final ItemStack RECIPE_RESULT_PLACEHOLDER = createRecipeResultPlaceholder();

    /**
     * The recipe choice for the elytra.
     */
    private static final RecipeChoice RECIPE_CHOICE_ELYTRA = new RecipeChoice.MaterialChoice(Material.ELYTRA);

    private static final @Nullable RecipeChoice RECIPE_NULL_TEMPLATE = null;

    SmithingTableRecipeListener(
        ArmoredElytra plugin,
        NBTEditor nbtEditor,
        DurabilityManager durabilityManager,
        ConfigLoader config)
    {
        super(plugin, nbtEditor, durabilityManager, config);

        registerRecipes();
    }

    @EventHandler(ignoreCancelled = true)
    public void onSmithingTableUsage(final PrepareSmithingEvent event)
    {
        final ElytraInput input = onSmithingTableUsage0(event);
        verifyRecipeResultPlaceholder(event.getInventory(), input);
    }

    /**
     * Processes the general {@link InventoryClickEvent} for this plugin.
     * <p>
     * This method will check if the event is fired while a smithing table is open, and if so, will call the appropriate
     * methods to further process the event.
     * <p>
     * See {@link #onSmithingInventoryClick(InventoryClickEvent, Player, SmithingInventory)}.
     *
     * @param event
     *     The {@link InventoryClickEvent} to process.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent event)
    {
        final Player player = Util.humanEntityToPlayer(event.getWhoClicked());

        if (!(player.getOpenInventory().getTopInventory() instanceof SmithingInventory))
            return;

        if (event.getClickedInventory() instanceof SmithingInventory clickedSmithingInventory)
            onSmithingInventoryClick(event, player, clickedSmithingInventory);
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
    }

    /**
     * Creates a placeholder result for the recipe.
     * <p>
     * It is a regular elytra with custom marker data so we can identify it.
     *
     * @return The placeholder result.
     */
    private static ItemStack createRecipeResultPlaceholder()
    {
        final ItemStack result = new ItemStack(Material.ELYTRA);
        try (var pdc = new AutoPersistentDataContainer(result))
        {
            pdc.set(RECIPE_PLACEHOLDER_KEY, PersistentDataType.BYTE, (byte) 1);
        }
        return result;
    }

    /**
     * Checks if the given item is a placeholder result for a recipe.
     *
     * @param item
     *     The item to check.
     *
     * @return {@code true} if the item is a placeholder result, {@code false} otherwise.
     */
    @Override
    protected boolean isRecipeResultPlaceholder(ItemStack item)
    {
        if (item == null || item.getType() != Material.ELYTRA)
            return false;
        return NBTEditor.hasPdcWithWithKey(item, RECIPE_PLACEHOLDER_KEY, PersistentDataType.BYTE);
    }

    /**
     * Checks if the recipe result is a placeholder and logs it if it is.
     * <p>
     * If the result is a placeholder, it will be removed from the inventory.
     * <p>
     * See {@link #isRecipeResultPlaceholder(ItemStack)}.
     * <p>
     * This method should not do anything unless a bug caused a placeholder result to be present.
     *
     * @param inventory
     *     The inventory to check the result in.
     * @param input
     *     The input for the recipe.
     */
    private void verifyRecipeResultPlaceholder(final SmithingInventory inventory, ElytraInput input)
    {
        final @Nullable ItemStack result = inventory.getItem(SMITHING_TABLE_RESULT_SLOT);
        // This should only be true when the input was handled incorrectly.
        if (isRecipeResultPlaceholder(result))
        {
            plugin.myLogger(
                Level.SEVERE,
                "Smithing Table: " +
                    "Attempted to retrieve a placeholder result! Result: " + result +
                    ", input: " + input
            );
            inventory.setItem(SMITHING_TABLE_RESULT_SLOT, null);
        }
    }

    /**
     * Registers the recipes for the armored elytra.
     * <p>
     * The exact recipes depend on the configuration.
     */
    private void registerRecipes()
    {
        final IRegisterSmithingRecipeFunction fun =
            SMITHING_TABLE_HAS_TEMPLATE_SLOT ?
            this::registerCraftingRecipeWithTemplate :
            this::registerCraftingRecipeWithoutTemplate;

        if (config.allowCraftingInSmithingTable())
            registerCraftingRecipes(fun);

        if (config.allowUpgradeToNetherite())
            registerUpgradeToNetheriteRecipe(fun);
    }

    /**
     * Registers the recipe for upgrading diamond elytras to netherite elytras.
     *
     * @param fun
     *     The function to call to register the created recipe.
     */
    private void registerUpgradeToNetheriteRecipe(IRegisterSmithingRecipeFunction fun)
    {
        final NamespacedKey key = new NamespacedKey(plugin, "st_upgrade_to_netherite");

        final RecipeChoice netheriteIngot = new RecipeChoice.MaterialChoice(Material.NETHERITE_INGOT);

        fun.register(
            key, RECIPE_RESULT_PLACEHOLDER, NETHERITE_UPGRADE_TEMPLATE_CHOICE, RECIPE_CHOICE_ELYTRA, netheriteIngot
        );
    }

    /**
     * Registers the crafting recipes for the armored elytra.
     */
    private void registerCraftingRecipes(IRegisterSmithingRecipeFunction fun)
    {
        ArmorTier.ARMOR_TIERS.forEach(tier -> registerCraftingRecipe(tier, fun));
    }

    /**
     * Registers a crafting recipe for the given tier.
     * <p>
     * This method will handle the differences between versions with and without a template slot.
     *
     * @param tier
     *     The tier to register the recipe for.
     * @param fun
     *     The function to call to register the created recipe.
     */
    private void registerCraftingRecipe(ArmorTier tier, IRegisterSmithingRecipeFunction fun)
    {
        final NamespacedKey key = new NamespacedKey(plugin, "st_recipe_" + tier.name().toLowerCase(Locale.ROOT));

        final RecipeChoice chestPlate = new RecipeChoice.MaterialChoice(
            Objects.requireNonNull(Util.tierToChestPlate(tier)));

        fun.register(key, RECIPE_RESULT_PLACEHOLDER, RECIPE_NULL_TEMPLATE, RECIPE_CHOICE_ELYTRA, chestPlate);
    }

    /**
     * Registers a smithing recipe with a template item.
     * <p>
     * This method cannot be used on versions without a template slot.
     *
     * @param key
     *     The key for the recipe.
     * @param result
     *     The result of the recipe.
     * @param template
     *     The recipe choice for the template item. May be null to not require a template item.
     * @param elytra
     *     The recipe choice for the elytra.
     * @param chestPlate
     *     The recipe choice for the chest plate.
     */
    private void registerCraftingRecipeWithTemplate(
        NamespacedKey key,
        ItemStack result,
        @Nullable RecipeChoice template,
        RecipeChoice elytra,
        RecipeChoice chestPlate)
    {
        Bukkit.addRecipe(new SmithingTransformRecipe(key, result, template, elytra, chestPlate));
    }

    /**
     * Registers a smithing recipe without a template item.
     * <p>
     * This method does nothing on versions with a template slot.
     *
     * @param key
     *     The key for the recipe.
     * @param result
     *     The result of the recipe.
     * @param template
     *     Unused. Only present for compatibility with {@link IRegisterSmithingRecipeFunction}.
     * @param elytra
     *     The recipe choice for the elytra.
     * @param chestPlate
     *     The recipe choice for the chest plate.
     */
    private void registerCraftingRecipeWithoutTemplate(
        NamespacedKey key,
        ItemStack result,
        @Nullable RecipeChoice template,
        RecipeChoice elytra,
        RecipeChoice chestPlate)
    {
        // The SmithingRecipe constructor is not deprecated on MC <1.20.
        //noinspection deprecation
        Bukkit.addRecipe(new SmithingRecipe(key, result, elytra, chestPlate));
    }

    /**
     * Function to register a smithing recipe.
     * <p>
     * On versions without a template slot (i.e. <1.20), this function will register a SmithingRecipe.
     * <p>
     * On versions with a template slot (i.e. >=1.20), this function will register a SmithingTransformRecipe.
     * <p>
     * This is done because versions that use SmithingTransformRecipe do not support SmithingRecipe and vice versa.
     */
    private interface IRegisterSmithingRecipeFunction
    {
        /**
         * Registers a smithing recipe.
         *
         * @param key
         *     The key for the recipe.
         * @param result
         *     The result of the recipe.
         * @param template
         *     The recipe choice for the template item. May be null to not require a template item.
         *     <p>
         *     On versions without a template slot, this parameter is ignored.
         * @param elytra
         *     The recipe choice for the elytra.
         * @param chestPlate
         *     The recipe choice for the chest plate.
         */
        void register(
            NamespacedKey key,
            ItemStack result,
            @Nullable RecipeChoice template,
            RecipeChoice elytra,
            RecipeChoice chestPlate
        );
    }
}
