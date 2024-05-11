package nl.pim16aap2.armoredElytra.nbtEditor;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.EnchantmentContainer;
import nl.pim16aap2.armoredElytra.util.itemInput.ElytraInput;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ArmoredElytraBuilder
{
    private final NBTEditor nbtEditor;
    private final DurabilityManager durabilityManager;
    private final ConfigLoader config;
    private final ArmoredElytra plugin;

    public ArmoredElytraBuilder(
        NBTEditor nbtEditor,
        DurabilityManager durabilityManager,
        ConfigLoader config,
        ArmoredElytra plugin)
    {
        this.nbtEditor = nbtEditor;
        this.durabilityManager = durabilityManager;
        this.config = config;
        this.plugin = plugin;
    }

    /**
     * Creates a new builder for an armored elytra.
     *
     * @return The first step of the new builder.
     */
    public IStep0 newBuilder(HumanEntity player)
    {
        return new Builder(player, nbtEditor, durabilityManager, config, plugin);
    }

    /**
     * Shortcut for repairing an armored elytra.
     *
     * @param player
     *     The player that is repairing the armored elytra.
     * @param armoredElytra
     *     The armored elytra to repair.
     * @param repairItems
     *     The repair item(s) to use for repairing the armored elytra. It is assumed that they are of the correct type.
     * @param name
     *     The new name of the output armored elytra. When this is null,
     *     {@link ArmoredElytra#getArmoredElytraName(ArmorTier)} is used to set the name.
     *
     * @return The new armored elytra.
     */
    public @Nullable ItemStack repair(
        HumanEntity player,
        ItemStack armoredElytra,
        ItemStack repairItems,
        @Nullable String name)
    {
        return newBuilder(player)
            .ofElytra(armoredElytra)
            .repair(repairItems.getAmount())
            .withName(name)
            .build();
    }

    /**
     * Shortcut for enchanting an armored elytra.
     *
     * @param player
     *     The player that is enchanting the armored elytra.
     * @param armoredElytra
     *     The armored elytra to repair.
     * @param sourceItem
     *     The source item from which to copy the enchantments from.
     * @param name
     *     The new name of the output armored elytra. When this is null,
     *     {@link ArmoredElytra#getArmoredElytraName(ArmorTier)} is used to set the name.
     *
     * @return The new armored elytra.
     */
    public @Nullable ItemStack enchant(
        HumanEntity player,
        ItemStack armoredElytra,
        ItemStack sourceItem,
        @Nullable String name)
    {
        final EnchantmentContainer enchantments = EnchantmentContainer.getEnchantmentsOf(sourceItem, plugin);
        if (enchantments.isEmpty())
            return null;

        return newBuilder(player)
            .ofElytra(armoredElytra)
            .addEnchantments(enchantments)
            .withName(name)
            .build();
    }

    /**
     * Shortcut for handling elytra input.
     *
     * @param player
     *     The player that provided the input.
     * @param input
     *     The input to process.
     *
     * @return The new armored elytra.
     */
    public @Nullable ItemStack handleInput(HumanEntity player, ElytraInput input)
    {
        if (input.isBlocked())
            return null;

        final var builder = newBuilder(player)
            .ofElytra(input.elytra());

        final @Nullable var withAction = switch (input.inputAction())
        {
            case APPLY_TEMPLATE -> builder.applyTrim(input.template(), input.combinedWith());
            case CREATE -> builder.combineWith(input.combinedWith(), input.newArmorTier());
            case ENCHANT ->
            {
                final var container = EnchantmentContainer.getEnchantmentsOf(input.combinedWith(), plugin);
                if (container.isEmpty())
                    yield null;
                yield builder.addEnchantments(container);
            }
            case RENAME -> builder.skipStep();
            case REPAIR -> builder.repair(input.combinedWith());
            case UPGRADE -> builder.upgradeToTier(input.newArmorTier());

            // 'BLOCK' should have been handled by the caller. Nothing we can do about that here.
            default -> throw new IllegalStateException("Unexpected input action: '" + input.inputAction() + "'");
        };

        return withAction == null ? null : withAction
            .withName(input.name())
            .build();
    }

    /**
     * Shortcut for creating a new armored elytra from two items.
     *
     * @param player
     *     The player that is combining the items.
     * @param elytra
     *     The input item. This should be an (armored) elytra.
     * @param combiner
     *     The item to combine with the elytra. This should either be an armored elytra of the same non-NONE tier as the
     *     input elytra or a chestplate.
     * @param armorTier
     *     The armor tier of the input item. If this is not known, use
     *     {@link #combine(HumanEntity, ItemStack, ItemStack, String)} instead.
     * @param name
     *     The new name of the output armored elytra. When this is null,
     *     {@link ArmoredElytra#getArmoredElytraName(ArmorTier)} is used to set the name.
     *
     * @return The new armored elytra.
     */
    public ItemStack combine(
        HumanEntity player,
        ItemStack elytra,
        ItemStack combiner,
        ArmorTier armorTier,
        @Nullable String name)
    {
        return newBuilder(player).ofElytra(elytra).combineWith(combiner, armorTier).withName(name).build();
    }

    /**
     * See {@link #combine(HumanEntity, ItemStack, ItemStack, ArmorTier, String)} for unknown armor tiers.
     */
    public ItemStack combine(HumanEntity player, ItemStack elytra, ItemStack combiner, @Nullable String name)
    {
        return newBuilder(player)
            .ofElytra(elytra)
            .combineWith(combiner)
            .withName(name)
            .build();
    }

    /**
     * Creates a new armored elytra of a specific tier.
     *
     * @param player
     *     The player that is creating the new armored elytra.
     * @param armorTier
     *     The tier of the new armored elytra.
     *
     * @return The new armored elytra.
     */
    public ItemStack newArmoredElytra(HumanEntity player, ArmorTier armorTier)
    {
        return newBuilder(player).newItem(armorTier).build();
    }

    /**
     * Represents the third and last step of the armored elytra build process.
     */
    public interface IStep2
    {
        /**
         * Specifies the new name of the armored elytra.
         *
         * @param name
         *     The new name of the armored elytra. When this is null (default), the default name for the new tier will
         *     be used.
         *
         * @return The current builder step.
         */
        IStep2 withName(@Nullable String name);

        /**
         * Specifies the new color of the armored elytra.
         *
         * @param color
         *     The new color of the armored elytra. When this is null (default), the color to use is inferred from the
         *     creation process.
         *
         * @return The current builder step.
         */
        IStep2 withColor(@Nullable Color color);

        /**
         * Specifies the new lore of the armored elytra.
         *
         * @param lore
         *     The new lore of the armored elytra. When this is null (default), the default lore for the new tier will
         *     be used.
         *
         * @return The current builder step.
         */
        IStep2 withLore(@Nullable List<String> lore);

        /**
         * Specifies whether the armored elytra should be unbreakable.
         * <p>
         * By default, this value is read from {@link ConfigLoader#unbreakable()}.
         *
         * @param isUnbreakable
         *     True if the armored elytra should be unbreakable.
         *
         * @return The current builder step.
         */
        IStep2 unbreakable(boolean isUnbreakable);

        /**
         * Constructs the armored elytra from the provided configuration.
         *
         * @return The new armored elytra.
         */
        ItemStack build();
    }

    /**
     * Represents the second and last step of the armored elytra build process.
     */
    public interface IStep1
    {
        /**
         * Repairs the armored elytra provided as input.
         *
         * @param count
         *     The amount of repair items to process.
         *
         * @return The next step of the builder process.
         */
        IStep2 repair(int count);

        /**
         * Repairs the armored elytra provided as input.
         *
         * @param repairItems
         *     The repair items to use for repairing the armored elytra.
         *
         * @return The next step of the builder process.
         *
         * @throws NullPointerException
         *     If the repair items are null.
         */
        default IStep2 repair(@Nullable ItemStack repairItems)
        {
            return repair(Objects.requireNonNull(repairItems, "Repair ItemStack cannot be null!").getAmount());
        }

        /**
         * Adds a set of enchantments to the armored elytra.
         *
         * @param enchantmentContainer
         *     The enchantments to add.
         *
         * @return The next step of the builder process.
         */
        IStep2 addEnchantments(EnchantmentContainer enchantmentContainer);

        /**
         * Adds a set of enchantments to the armored elytra.
         *
         * @param sourceItem
         *     The source item from which to take the enchantments.
         *
         * @return The next step of the builder process.
         */
        IStep2 addEnchantments(ItemStack sourceItem);

        /**
         * Applies a pattern to the armored elytra.
         *
         * @param pattern
         *     The pattern of the trim to apply.
         * @param material
         *     The material of the trim to apply.
         *
         * @return The next step of the builder process.
         */
        IStep2 applyTrim(Material pattern, Material material);

        /**
         * Applies a pattern to the armored elytra.
         * <p>
         * This is a convenience method that calls {@link #applyTrim(Material, Material)}.
         *
         * @param pattern
         *     The item representing the pattern of the trim to apply.
         * @param material
         *     The item representing the material of the trim to apply.
         *
         * @return The next step of the builder process.
         *
         * @throws NullPointerException
         *     If either of the items is null.
         */
        default IStep2 applyTrim(ItemStack pattern, ItemStack material)
        {
            return applyTrim(Objects.requireNonNull(pattern).getType(), Objects.requireNonNull(material).getType());
        }

        /**
         * Combines the input elytra with another item.
         *
         * @param item
         *     The item to combine with the input elytra. This can either be a chestplate or, if the input elytra is an
         *     armored one, another armored elytra.
         * @param armorTier
         *     The armor tier of the input item. If this is not known, use {@link #combineWith(ItemStack)} instead.
         *
         * @return The next step of the builder process.
         */
        IStep2 combineWith(ItemStack item, ArmorTier armorTier);

        /**
         * See {@link #combineWith(ItemStack, ArmorTier)}. Used when the armor tier of the input item is not known.
         *
         * @return The next step of the builder process.
         */
        IStep2 combineWith(ItemStack item);

        /**
         * Upgrades the elytra to a specific armor tier.
         *
         * @param armorTier
         *     The new armor tier.
         *
         * @return The next step of the builder process.
         */
        IStep2 upgradeToTier(ArmorTier armorTier);

        /**
         * Skips the current step and returns the next step.
         *
         * @return The next step of the builder process.
         */
        IStep2 skipStep();
    }

    /**
     * Represents the first and last step of the armored elytra build process.
     */
    public interface IStep0
    {
        /**
         * Use an elytra as base item to create the new armored elytra from.
         *
         * @param elytra
         *     An itemstack that represents an elytra. It does not matter whether the elytra is armored or not.
         *
         * @return The next step of the builder process.
         */
        IStep1 ofElytra(ItemStack elytra);

        /**
         * Creates a fresh new armored elytra of a specific tier.
         *
         * @param armorTier
         *     The tier of the new armored elytra.
         *
         * @return The next step of the builder process.
         */
        IStep2 newItem(ArmorTier armorTier);
    }

    private static final class Builder implements IStep0, IStep1, IStep2
    {
        private static final Color DEFAULT_LEATHER_COLOR = Bukkit.getServer().getItemFactory().getDefaultLeatherColor();

        private final NBTEditor nbtEditor;
        private final DurabilityManager durabilityManager;
        private final ConfigLoader config;
        private final ArmoredElytra plugin;

        /**
         * The player that is responsible for the build process.
         */
        private final HumanEntity player;

        // These aren't nullable, as they are set by the only entry points.
        /**
         * The new armored elytra that will be returned at the end of the build process.
         */
        private ItemStack newArmoredElytra;

        /**
         * The combined enchantments of the input items.
         */
        private EnchantmentContainer combinedEnchantments;

        /**
         * The current armor tier of the input elytra.
         */
        private ArmorTier currentArmorTier;

        /**
         * The durability of the output armored elytra.
         */
        private int durability;


        /**
         * The armor tier of the output armored elytra. This defaults to {@link #currentArmorTier} if this isn't set.
         */
        private @Nullable ArmorTier newArmorTier;

        /**
         * The name of the output armored elytra. This defaults to {@link ArmoredElytra#getArmoredElytraName(ArmorTier)}
         * when not overridden.
         */
        private @Nullable String name;

        /**
         * The lore of the output armored elytra. This defaults to {@link ArmoredElytra#getElytraLore(ArmorTier)} when
         * not overridden.
         */
        private @Nullable List<String> lore;

        /**
         * The color of the output armored elytra. By default, the existing color (if any is used). When combined with
         * another item, the color is inferred using {@link #getItemColor(ItemStack, ItemStack)}.
         */
        private @Nullable Color color;

        /**
         * The other item to combine with the input elytra.
         */
        private @Nullable ItemStack otherItem;

        /**
         * Whether the output armored elytra should be unbreakable. This defaults to {@link ConfigLoader#unbreakable()}
         * when not overridden.
         */
        private @Nullable Boolean isUnbreakable = null;

        /**
         * The trim data of the output armored elytra.
         */
        private @Nullable ArmorTrimData trimData = null;

        private Builder(
            HumanEntity player,
            NBTEditor nbtEditor,
            DurabilityManager durabilityManager,
            ConfigLoader config,
            ArmoredElytra plugin)
        {
            this.player = player;
            this.nbtEditor = nbtEditor;
            this.durabilityManager = durabilityManager;
            this.config = config;
            this.plugin = plugin;
        }

        @Override
        public ItemStack build()
        {
            // Get default values if unset.
            newArmorTier = newArmorTier == null ? currentArmorTier : newArmorTier;

            if (!plugin.playerHasCraftPerm(player, newArmorTier))
                return null;

            name = name == null ? plugin.getArmoredElytraName(newArmorTier) : name;
            lore = lore == null ? plugin.getElytraLore(newArmorTier) : lore;

            isUnbreakable = isUnbreakable == null ? config.unbreakable() : isUnbreakable;

            final ItemStack output = nbtEditor.addArmorNBTTags(
                newArmoredElytra,
                newArmorTier,
                otherItem,
                isUnbreakable,
                name,
                lore,
                color,
                trimData);
            durabilityManager.setDurability(output, durability, newArmorTier);
            combinedEnchantments.applyEnchantments(output);

            return output;
        }

        @Override
        public IStep2 withName(@Nullable String name)
        {
            this.name = name;
            return this;
        }

        @Override
        public IStep2 withColor(@Nullable Color color)
        {
            this.color = color;
            return this;
        }

        @Override
        public IStep2 withLore(@Nullable List<String> lore)
        {
            this.lore = lore;
            return this;
        }

        @Override
        public IStep2 unbreakable(boolean isUnbreakable)
        {
            this.isUnbreakable = isUnbreakable;
            return this;
        }

        @Override
        public IStep2 repair(int count)
        {
            if (currentArmorTier == ArmorTier.NONE)
                throw new IllegalArgumentException("Non-armored elytras cannot be repaired!");
            durability = durabilityManager.getRepairedDurability(newArmoredElytra, count, currentArmorTier);
            return this;
        }

        @Override
        public IStep2 addEnchantments(EnchantmentContainer enchantmentContainer)
        {
            combinedEnchantments.merge(enchantmentContainer);
            return this;
        }

        @Override
        public IStep2 addEnchantments(ItemStack sourceItem)
        {
            return addEnchantments(EnchantmentContainer.getEnchantmentsOf(sourceItem, plugin));
        }

        @Override
        public IStep2 applyTrim(Material pattern, Material material)
        {
            trimData = new ArmorTrimData(pattern, material);
            return this;
        }

        @Override
        public IStep2 combineWith(ItemStack item, ArmorTier armorTier)
        {
            if (armorTier == ArmorTier.NONE)
                throw new IllegalArgumentException("Cannot combine an elytra with a non-armor item!");

            if (currentArmorTier != ArmorTier.NONE)
                throw new IllegalArgumentException("An armored elytra cannot be combined with another chestplate!");

            otherItem = item;
            newArmorTier = armorTier;

            withColor(getItemColor(newArmoredElytra, item));
            addEnchantments(item);

            durability = durabilityManager.getCombinedDurability(
                newArmoredElytra, item, currentArmorTier, newArmorTier);

            return this;
        }

        @Override
        public IStep2 combineWith(ItemStack item)
        {
            return combineWith(item, nbtEditor.getArmorTier(item));
        }

        @Override
        public IStep2 upgradeToTier(ArmorTier armorTier)
        {
            newArmorTier = armorTier;
            return this;
        }

        @Override
        public IStep2 skipStep()
        {
            return this;
        }

        @Override
        public IStep1 ofElytra(ItemStack elytra)
        {
            if (!elytra.getType().equals(Material.ELYTRA))
                throw new IllegalArgumentException("Expected elytra as input, but got: " + elytra);

            newArmoredElytra = new ItemStack(elytra);

            if (currentArmorTier == null)
                currentArmorTier = nbtEditor.getArmorTierFromElytra(elytra);

            combinedEnchantments = EnchantmentContainer.getEnchantmentsOf(newArmoredElytra, plugin);

            durability = durabilityManager.getRealDurability(newArmoredElytra, currentArmorTier);
            return this;
        }

        @Override
        public IStep2 newItem(ArmorTier armorTier)
        {
            currentArmorTier = newArmorTier = armorTier;
            ofElytra(new ItemStack(Material.ELYTRA));
            return this;
        }

        /**
         * Gets the color of the item if the item has a color.
         * <p>
         * See {@link LeatherArmorMeta#getColor()}.
         *
         * @param itemA
         *     The first {@link ItemStack} to check.
         * @param itemB
         *     The second {@link ItemStack} to check.
         *
         * @return The color of the item, if it has a color, otherwise null.
         */
        private @Nullable Color getItemColor(final ItemStack itemA, final ItemStack itemB)
        {
            final @Nullable Color colorA = getItemColor(itemA);
            if (colorA != null && !colorA.equals(DEFAULT_LEATHER_COLOR))
                return colorA;

            final @Nullable Color colorB = getItemColor(itemB);
            return colorB != null ? colorB : colorA;
        }

        /**
         * Gets the colors of an item if available.
         * <p>
         * This currently only applies to leather armor(ed elytras).
         *
         * @param itemStack
         *     The item to analyze.
         *
         * @return The color of the item, if available, otherwise null.
         */
        private @Nullable Color getItemColor(final ItemStack itemStack)
        {
            if (itemStack.getType() == Material.ELYTRA)
                return nbtEditor.getColorOfArmoredElytra(itemStack);

            if (!itemStack.hasItemMeta() || !(itemStack.getItemMeta() instanceof LeatherArmorMeta))
                return null;

            return ((LeatherArmorMeta) itemStack.getItemMeta()).getColor();
        }
    }
}
