package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nbtEditor.DurabilityManager;
import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.Util;
import nl.pim16aap2.armoredElytra.util.itemInput.ElytraInput;
import nl.pim16aap2.armoredElytra.util.itemInput.InputAction;
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
import org.semver4j.Semver;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Objects;
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
    /**
     * Whether the inventory needs to be updated manually after (some) smithing table events.
     */
    private static final boolean NEEDS_INV_UPDATE = ArmoredElytra.SERVER_VERSION.isLowerThan(Semver.of(1, 17, 0));

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
     * Placeholder result. Our event handler will handle the actual result, including:
     * <ul>
     *     <li>Permissions</li>
     *     <li>Durability</li>
     *     <li>Enchantments</li>
     *     <li>Whatever else we might add in the future.</li>
     * </ul>
     */
    private static final ItemStack RECIPE_RESULT_PLACEHOLDER = new ItemStack(Material.ELYTRA);

    /**
     * The recipe choice for the elytra.
     */
    private static final RecipeChoice RECIPE_CHOICE_ELYTRA = new RecipeChoice.MaterialChoice(Material.ELYTRA);

    public SmithingTableListener(
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
        final SmithingInventory inventory = event.getInventory();

        final var input = ElytraInput.fromInventory(config, inventory);
        if (input.isIgnored())
            return;

        event.setResult(armoredElytraBuilder.handleInput(event.getView().getPlayer(), input));

        if (NEEDS_INV_UPDATE)
            // Player::updateInventory should not be used directly (anymore?).
            // However, if we don't use it on <1.17, the invalid result will still
            // be shown to the player.
            //noinspection UnstableApiUsage
            event.getViewers().stream()
                 .filter(Player.class::isInstance).map(Player.class::cast)
                 .forEach(Player::updateInventory);
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

        fun.register(key, RECIPE_RESULT_PLACEHOLDER, null, RECIPE_CHOICE_ELYTRA, chestPlate);
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
