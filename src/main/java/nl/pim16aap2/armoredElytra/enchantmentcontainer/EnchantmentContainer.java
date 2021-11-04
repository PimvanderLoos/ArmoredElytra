package nl.pim16aap2.armoredElytra.enchantmentcontainer;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.Util;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class EnchantmentContainer implements Iterable<Map.Entry<Enchantment, Integer>>
{
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

    /**
     * Copy constructor.
     */
    public EnchantmentContainer(EnchantmentContainer other)
    {
        this(other.enchantments);
    }

    public EnchantmentContainer()
    {
        enchantments = new LinkedHashMap<>();
    }

    EnchantmentContainer(final Map<Enchantment, Integer> enchantments)
    {
        this.enchantments = new LinkedHashMap<>(enchantments);
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
            return new EnchantmentContainer(new LinkedHashMap<>(0), plugin);

        final EnchantmentStorageMeta meta = (EnchantmentStorageMeta) is.getItemMeta();
        if (meta == null || !meta.hasStoredEnchants())
            return new EnchantmentContainer(new LinkedHashMap<>(0), plugin);

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
        if (this == other)
            throw new IllegalArgumentException("EnchantmentContainers cannot be combined with themselves!");
        enchantments = merge(enchantments, other.enchantments);
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

        final Map<Enchantment, Integer> combined = new LinkedHashMap<>(first);
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
     * Removes all enchantments from this container that also exist in the provided one.
     * <p>
     * If an enchantment exists in both containers, but with a higher level in the except set, the enchantment is kept,
     * but at 1 level below the level found in the 'except' set. This reflects the fact that in vanilla, adding an
     * enchantment of the same level twice results in the same enchantment with level + 1.
     *
     * @param base   The base enchantment container.
     * @param except The set of enchantments to remove from the current set.
     * @return The result of the except action.
     */
    public static EnchantmentContainer except(EnchantmentContainer base, EnchantmentContainer except)
    {
        return new EnchantmentContainer(except(base.enchantments, except.enchantments));
    }

    private static Map<Enchantment, Integer> except(Map<Enchantment, Integer> base, Map<Enchantment, Integer> except)
    {
        if (except.isEmpty())
            return Collections.unmodifiableMap(base);
        if (base.isEmpty() || base == except)
            return Collections.emptyMap();

        final Map<Enchantment, Integer> result = new LinkedHashMap<>(base.size());
        for (var entry : base.entrySet())
        {
            @Nullable Integer exceptLevel = except.get(entry.getKey());
            if (exceptLevel == null)
                result.put(entry.getKey(), entry.getValue());
            else if (exceptLevel > entry.getValue())
                result.put(entry.getKey(), exceptLevel - 1);
        }
        return result;
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

    @Override
    public Iterator<Map.Entry<Enchantment, Integer>> iterator()
    {
        return enchantments.entrySet().iterator();
    }

    @Override
    public int hashCode()
    {
        return enchantments.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj)
    {
        return obj instanceof EnchantmentContainer other && enchantments.equals(other.enchantments);
    }
}
