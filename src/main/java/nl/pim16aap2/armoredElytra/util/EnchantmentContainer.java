package nl.pim16aap2.armoredElytra.util;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

public class EnchantmentContainer
{
    private static final int[] COST_MULTIPLIERS = {1, 2, 4, 8};
    private static final @Nullable RarityRetriever RARITY_RETRIEVER;

    static
    {
        RarityRetriever rarityRetrieverTmp = null;
        try
        {
            rarityRetrieverTmp = new RarityRetriever();
        }
        catch (Exception e)
        {
            ArmoredElytra.getInstance().getLogger().warning(
                "Failed to initialize RarityRetriever! Enchantment costs cannot be enabled! More info:");
            e.printStackTrace();
        }
        RARITY_RETRIEVER = rarityRetrieverTmp;
    }

    private Map<Enchantment, Integer> enchantments;

    /**
     * Creates a new {@link EnchantmentContainer} from a map of enchantments.
     *
     * @param enchantments The enchantments.
     * @param plugin       The {@link ArmoredElytra} instance to use for obtaining the list of allowed enchantments. See
     *                     {@link ConfigLoader#allowedEnchantments()}.
     */
    public EnchantmentContainer(final Map<Enchantment, Integer> enchantments, final ArmoredElytra plugin)
    {
        this(enchantments);
        filter(plugin.getConfigLoader().allowedEnchantments());
    }

    private EnchantmentContainer(final Map<Enchantment, Integer> enchantments)
    {
        this.enchantments = new HashMap<>(enchantments);
    }

    /**
     * Gets all the enchantments from an item.
     *
     * @param is     The item.
     * @param plugin The {@link ArmoredElytra} instance to use.
     * @return A new {@link EnchantmentContainer} with the enchantments from the item.
     */
    public static EnchantmentContainer getEnchantments(final ItemStack is, final ArmoredElytra plugin)
    {
        if (is == null)
            return new EnchantmentContainer(Collections.emptyMap(), plugin);

        return is.getType() == Material.ENCHANTED_BOOK ?
               getEnchantmentsFromBook(is, plugin) :
               getEnchantmentsFromItem(is, plugin);
    }

    /**
     * Gets all the enchantments from an item that is not a book.
     *
     * @param is     The item.
     * @param plugin The {@link ArmoredElytra} instance to use.
     * @return A new {@link EnchantmentContainer} with the enchantments from the item.
     */
    private static EnchantmentContainer getEnchantmentsFromItem(final ItemStack is, final ArmoredElytra plugin)
    {
        return new EnchantmentContainer(is.getEnchantments(), plugin);
    }

    /**
     * Gets all the enchantments from a book.
     *
     * @param is     The book.
     * @param plugin The {@link ArmoredElytra} instance to use.
     * @return A new enchantment container with the enchantments from the book.
     */
    private static EnchantmentContainer getEnchantmentsFromBook(final ItemStack is, final ArmoredElytra plugin)
    {
        if (!is.hasItemMeta())
            return new EnchantmentContainer(new HashMap<>(0), plugin);

        final EnchantmentStorageMeta meta = (EnchantmentStorageMeta) is.getItemMeta();
        if (meta == null || !meta.hasStoredEnchants())
            return new EnchantmentContainer(new HashMap<>(0), plugin);

        return new EnchantmentContainer(meta.getStoredEnchants(), plugin);
    }

    /**
     * Gets the total number of enchantments in this container.
     *
     * @return The total number of enchantments in this container.
     */
    public int getEnchantmentCount()
    {
        return enchantments.size();
    }

    /**
     * Removes any entries from the list of enchantments that do not exist in the provided filter.
     *
     * @param allowed The names of enchantments (upper case) that are allowed. Any names not in this list are removed.
     */
    public void filter(final Collection<Enchantment> allowed)
    {
        if (!enchantments.isEmpty())
            enchantments.keySet().retainAll(allowed);
    }

    /**
     * Applies the enchantments to an itemstack.
     *
     * @param is The itemstack to apply the enchantments to.
     */
    public void applyEnchantments(final ItemStack is)
    {
        is.addUnsafeEnchantments(enchantments);
    }

    /**
     * Merges this container with another one.
     */
    public void merge(EnchantmentContainer other)
    {
        enchantments = merge(enchantments, other.enchantments);
    }

    /**
     * Merges two enchantment containers.
     *
     * @param first  The first enchantment container.
     * @param second The second enchantment container. In case of conflicts, this will take precedence.
     * @return The new map of enchantments.
     */
    public static EnchantmentContainer merge(final EnchantmentContainer first, final EnchantmentContainer second)
    {
        return new EnchantmentContainer(merge(first.enchantments, second.enchantments));
    }

    /**
     * Merges two maps of enchantments.
     *
     * @param first  The first maps of enchantments.
     * @param second The second maps of enchantments. In case of conflicts, this will take precedence.
     * @return The new map of enchantments.
     */
    private static Map<Enchantment, Integer> merge(final Map<Enchantment, Integer> first,
                                                   final Map<Enchantment, Integer> second)
    {
        if (second == null || second.isEmpty())
            return first;

        if (first == null || first.isEmpty())
            return second;

        final Map<Enchantment, Integer> combined = new HashMap<>(first);
        for (Map.Entry<Enchantment, Integer> entry : second.entrySet())
        {
            Integer enchantLevel = first.get(entry.getKey());
            if (enchantLevel != null)
            {
                if (entry.getValue().equals(enchantLevel) && entry.getValue() < entry.getKey().getMaxLevel())
                    enchantLevel = entry.getValue() + 1;
                else if (entry.getValue() > enchantLevel)
                    enchantLevel = entry.getValue();

                // If the enchantment level has changed,
                if (!enchantLevel.equals(first.get(entry.getKey())))
                {
                    combined.remove(entry.getKey());
                    combined.put(entry.getKey(), enchantLevel);
                }
            }
            else
                combined.put(entry.getKey(), entry.getValue());
        }

        if (!ArmoredElytra.getInstance().getConfigLoader().allowMultipleProtectionEnchantments())
        {
            // Get the protection enchantment rating for both enchantment sets.
            int protVal0 = Util.getProtectionEnchantmentsVal(first);
            int protVal1 = Util.getProtectionEnchantmentsVal(second);

            // If they have different protection enchantments, keep enchantment1's enchantments
            // And remove the protection enchantment from enchantments0.
            if (protVal0 != 0 && protVal1 != 0 && protVal0 != protVal1)
                switch (protVal0)
                {
                    case 1:
                        combined.remove(Enchantment.PROTECTION_ENVIRONMENTAL);
                        break;
                    case 2:
                        combined.remove(Enchantment.PROTECTION_EXPLOSIONS);
                        break;
                    case 4:
                        combined.remove(Enchantment.PROTECTION_FALL);
                        break;
                    case 8:
                        combined.remove(Enchantment.PROTECTION_FIRE);
                        break;
                    case 16:
                        combined.remove(Enchantment.PROTECTION_PROJECTILE);
                        break;
                }
        }

        return combined;
    }

    /**
     * Gets the rarity of an enchantment.
     *
     * @param enchantment The enchantment for which to look up the rarity.
     * @return The rarity of the enchantment if it could be found, otherwise and empty optional int.
     */
    public static OptionalInt getRarity(Enchantment enchantment)
    {
        return RARITY_RETRIEVER == null ? OptionalInt.empty() : RARITY_RETRIEVER.getOrdinal(enchantment);
    }

    /**
     * Retrieves the cost multiplier of an enchantment based on its rarity. See {@link #getRarity(Enchantment)}.
     * <p>
     * If the rarity could not be retrieved, this will always return 1.
     *
     * @param enchantment The enchantment for which the multiplier is retrieved.
     * @return The cost multiplier of the enchantment.
     */
    public static int getCostMultiplier(Enchantment enchantment)
    {
        int rarityOrdinal = getRarity(enchantment).orElse(-1);
        return rarityOrdinal == -1 ? 1 : COST_MULTIPLIERS[rarityOrdinal];
    }

    /**
     * Calculates the cost of merging two {@link EnchantmentContainer}s.
     *
     * @param first    The first {@link EnchantmentContainer}
     * @param second   The second {@link EnchantmentContainer}
     * @param fromBook Whether or not the enchantment(s) are added from a book.
     * @return The experience cost of merging the two {@link EnchantmentContainer}s.
     */
    public static int getMergeCost(EnchantmentContainer first, EnchantmentContainer second, boolean fromBook)
    {
        int cost = first.size();
        for (Map.Entry<Enchantment, Integer> enchantmentSpec : second.enchantments.entrySet())
        {
            final int right = enchantmentSpec.getValue();
            final int left = first.enchantments.getOrDefault(enchantmentSpec.getKey(), 0);
            int currentCost = right == left ? right + 1 : Math.max(right, left);

            int costMultiplier = getCostMultiplier(enchantmentSpec.getKey());
            if (fromBook)
                costMultiplier = Math.max(1, costMultiplier / 2);

            currentCost *= costMultiplier;
            cost += currentCost;
        }
        return cost;
    }

    /**
     * Calculates the cost of merging this {@link EnchantmentContainer}.
     * <p>
     * See {@link #getMergeCost(EnchantmentContainer, EnchantmentContainer, boolean)}.
     */
    public int getMergeCost(EnchantmentContainer other, boolean fromBook)
    {
        return getMergeCost(this, other, fromBook);
    }

    /**
     * Gets the number of enchantments in this container.
     */
    public int size()
    {
        return enchantments.size();
    }

    /**
     * Checks if this container is empty.
     *
     * @return True if there are exactly 0 enchantments in this container.
     */
    public boolean isEmpty()
    {
        return enchantments.isEmpty();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("[");
        enchantments.forEach((k, v) -> sb.append("\"").append(k).append("\" (").append(v).append("), "));
        String ret = sb.toString();
        ret = ret.length() > 1 ? ret.substring(0, ret.length() - 2) : ret;
        return ret + "]";
    }

    /**
     * Represents a class that can be used to obtain the rarity value of enchantments.
     */
    private static final class RarityRetriever
    {
        private final @Nonnull Method getRaw;
        private final @Nonnull Field rarityField;
        private final @Nonnull Class<Enum<?>> rarityClass;

        private RarityRetriever()
            throws Exception
        {
            Class<?> craftEnchantment =
                Objects.requireNonNull(ReflectionUtil.getCraftBukkitClass("enchantments.CraftEnchantment"));

            getRaw =
                Objects.requireNonNull(ReflectionUtil.getMethod(craftEnchantment, "getRaw", true, Enchantment.class));

            Class<?> nmsEchant = Objects.requireNonNull(getRaw.getReturnType());

            Class<Enum<?>> rarityClassTmp = null;
            for (Class<?> clz : nmsEchant.getDeclaredClasses())
                if (clz.isEnum() && clz.getSimpleName().equals("Rarity"))
                {
                    //noinspection unchecked
                    rarityClassTmp = (Class<Enum<?>>) clz;
                    break;
                }
            rarityClass = Objects.requireNonNull(rarityClassTmp);
            rarityField = Objects.requireNonNull(ReflectionUtil.getTypedField(nmsEchant, rarityClass, true));
        }

        /**
         * Retrieves the ordinal value of the enchantment rarity.
         * <p>
         * 0 = COMMON, 1 = UNCOMMON, 2 = RARE, 3 = VERY_RARE
         *
         * @param enchantment The enchantment for which to retrieve the rarity.
         * @return The ordinal rarity value of the enchantment's rarity.
         */
        public OptionalInt getOrdinal(Enchantment enchantment)
        {
            if (enchantment == null)
                return OptionalInt.empty();
            try
            {
                Object nmsEnchantment = getRaw.invoke(null, enchantment);
                Enum<?> rarity = rarityClass.cast(rarityField.get(nmsEnchantment));
                return OptionalInt.of(rarity.ordinal());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return OptionalInt.empty();
        }
    }
}
