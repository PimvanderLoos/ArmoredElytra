package nl.pim16aap2.armoredElytra.nbtEditor;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.handlers.SmithingTableInput;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.EnchantmentContainer;
import nl.pim16aap2.armoredElytra.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import javax.annotation.Nullable;
import java.util.List;

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
    public IStep0 newBuilder()
    {
        return new Builder(nbtEditor, durabilityManager, config, plugin);
    }

    /**
     * Shortcut for repairing an armored elytra.
     *
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
    public @Nullable ItemStack repair(ItemStack armoredElytra, ItemStack repairItems, @Nullable String name)
    {
        return newBuilder().ofElytra(armoredElytra).repair(repairItems.getAmount()).withName(name).build();
    }

    /**
     * Shortcut for enchanting an armored elytra.
     *
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
    public @Nullable ItemStack enchant(ItemStack armoredElytra, ItemStack sourceItem, @Nullable String name)
    {
        final EnchantmentContainer enchantments = EnchantmentContainer.getEnchantmentsOf(sourceItem, plugin);
        if (enchantments.isEmpty())
            return null;
        return newBuilder().ofElytra(armoredElytra).addEnchantments(enchantments).withName(name).build();
    }

    /**
     * Shortcut for combining two items in a smithing table.
     *
     * @param input
     *     The smithing table input.
     * @param name
     *     The new name of the output armored elytra. When this is null, the default name for the new tier will be
     *     used.
     *
     * @return The new armored elytra.
     */
    public ItemStack combine(SmithingTableInput input, @Nullable String name)
    {
        return newBuilder()
            .ofElytra(input.elytra())
            .combineWith(input.combinedWith(), input.newArmorTier())
            .withTemplate(input.template())
            .withName(name)
            .build();
    }

    /**
     * Shortcut for creating a new armored elytra from two items.
     *
     * @param elytra
     *     The input item. This should be an (armored) elytra.
     * @param combiner
     *     The item to combine with the elytra. This should either be an armored elytra of the same non-NONE tier as the
     *     input elytra or a chestplate.
     * @param armorTier
     *     The armor tier of the input item. If this is not known, use {@link #combine(ItemStack, ItemStack, String)}
     *     instead.
     * @param name
     *     The new name of the output armored elytra. When this is null,
     *     {@link ArmoredElytra#getArmoredElytraName(ArmorTier)} is used to set the name.
     *
     * @return The new armored elytra.
     */
    public ItemStack combine(ItemStack elytra, ItemStack combiner, ArmorTier armorTier, @Nullable String name)
    {
        return newBuilder().ofElytra(elytra).combineWith(combiner, armorTier).withName(name).build();
    }

    /**
     * See {@link #combine(ItemStack, ItemStack, ArmorTier, String)} for unknown armor tiers.
     */
    public ItemStack combine(ItemStack elytra, ItemStack combiner, @Nullable String name)
    {
        return newBuilder().ofElytra(elytra).combineWith(combiner).withName(name).build();
    }

    /**
     * Creates a new armored elytra of a specific tier.
     *
     * @param armorTier
     *     The tier of the new armored elytra.
     *
     * @return The new armored elytra.
     */
    public ItemStack newArmoredElytra(ArmorTier armorTier)
    {
        return newBuilder().newItem(armorTier).build();
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
         * Specifies the template of the armored elytra.
         *
         * @param template
         *     The template to use. When this is null (default), the template is inferred from the creation process.
         *
         * @return The current builder step.
         */
        IStep2 withTemplate(ItemStack template);

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
         * The template of the output armored elytra. This defaults to null.
         */
        private @Nullable ItemStack template;

        /**
         * The other item to combine with the input elytra.
         */
        private @Nullable ItemStack otherItem;

        /**
         * Whether the output armored elytra should be unbreakable. This defaults to {@link ConfigLoader#unbreakable()}
         * when not overridden.
         */
        private @Nullable Boolean isUnbreakable = null;

        private Builder(
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

        @Override
        public ItemStack build()
        {
            // Get default values if unset.
            newArmorTier = newArmorTier == null ? currentArmorTier : newArmorTier;
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
                color);
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
        public IStep2 withTemplate(ItemStack template)
        {
            this.template = template;
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
        public IStep2 combineWith(ItemStack item, ArmorTier armorTier)
        {
            if (armorTier == ArmorTier.NONE && !Util.isChestPlate(item))
                throw new IllegalArgumentException("Non-armored elytras can only be combined with chest plates!");

            otherItem = item;

            newArmorTier = armorTier;
            if (currentArmorTier == ArmorTier.NONE &&
                item.getType().equals(Material.ELYTRA) && newArmorTier != ArmorTier.NONE)
                throw new IllegalArgumentException("A regular elytra cannot be combined with an armored one!");

            withColor(getItemColor(newArmoredElytra, item));

            addEnchantments(item);

            durability = durabilityManager.getCombinedDurability(
                newArmoredElytra, item, currentArmorTier, newArmorTier);
            return this;
        }

        @Override
        public IStep2 combineWith(ItemStack item)
        {
            final ArmorTier armorTier = item.getType().equals(Material.ELYTRA) ?
                                        nbtEditor.getArmorTier(item) : Util.armorToTier(item.getType());
            return combineWith(item, armorTier);
        }

        @Override
        public IStep2 upgradeToTier(ArmorTier armorTier)
        {
            newArmorTier = armorTier;
            return this;
        }

        @Override
        public IStep1 ofElytra(ItemStack elytra)
        {
            if (!elytra.getType().equals(Material.ELYTRA))
                throw new IllegalArgumentException("Expected elytra as input, but got: " + elytra);

            newArmoredElytra = new ItemStack(elytra);

            if (currentArmorTier == null)
                currentArmorTier = nbtEditor.getArmorTier(elytra);

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
