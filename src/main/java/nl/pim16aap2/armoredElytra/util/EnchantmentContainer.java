package nl.pim16aap2.armoredElytra.util;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
        filterMutuallyExclusive();
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
        enchantments = new HashMap<>();
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
    public static EnchantmentContainer getEnchantmentsOf(final ItemStack is, final ArmoredElytra plugin)
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
     * Returns a list of all enchantments that are mutually exclusive with the provided enchantment.
     * <br>This does <b>not</b> include the provided enchantment
     *
     * @param enchantment The enchantment to get the mutually exclusives of
     * @return A linked list containing all the mutually exclusive enchantments
     */
    private static List<Enchantment> getMutuallyExclusiveEnchantments(Enchantment enchantment)
    {
        final List<Enchantment> enchantments = new LinkedList<>();
        for (final List<Enchantment> mutuallyExclusiveEnchantments :
            ArmoredElytra.getInstance().getConfigLoader().getMutuallyExclusiveEnchantments())
        {
            for (Enchantment mutuallyExclusiveEnchantment : mutuallyExclusiveEnchantments)
            {
                if (mutuallyExclusiveEnchantment.equals(enchantment))
                {
                    enchantments.addAll(
                        mutuallyExclusiveEnchantments.stream().filter(i -> !i.equals(enchantment)).toList());
                    break;
                }
            }
        }
        return enchantments;
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
     * Remove any entries from the list of enchantments that are mutually exclusive to each other.
     * <br>First instance of a mutually exclusive enchantment gets priority
     */
    private void filterMutuallyExclusive()
    {
        final List<Enchantment> disallowedEnchantments = new LinkedList<>();
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet())
        {
            if (disallowedEnchantments.contains(entry.getKey())) continue;
            disallowedEnchantments.addAll(getMutuallyExclusiveEnchantments(entry.getKey()));
        }
        disallowedEnchantments.forEach(enchantments.keySet()::remove);
    }

    /**
     * Applies the enchantments to an itemstack.
     *
     * @param is The itemstack to apply the enchantments to.
     */
    public void applyEnchantments(final ItemStack is)
    {
        // Clear enchantments before applying new ones
        for (Enchantment enchantment : is.getEnchantments().keySet())
            is.removeEnchantment(enchantment);

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

        final List<Enchantment> blackList =
            second.keySet().stream()
                  .flatMap(ench -> getMutuallyExclusiveEnchantments(ench).stream())
                  .toList();
        blackList.forEach(first.keySet()::remove);

        final Map<Enchantment, Integer> combined = new HashMap<>(first);
        for (Map.Entry<Enchantment, Integer> entry : second.entrySet())
        {
            // Check for enchants with higher level
            Integer enchantLevel = combined.get(entry.getKey());
            if (enchantLevel != null)
            {
                final int oldLevel = enchantLevel;
                if (entry.getValue().equals(enchantLevel) && entry.getValue() < entry.getKey().getMaxLevel())
                    enchantLevel = entry.getValue() + 1;
                else if (entry.getValue() > enchantLevel)
                    enchantLevel = entry.getValue();

                if (enchantLevel != oldLevel)
                    combined.put(entry.getKey(), enchantLevel);
            }
            else
                combined.put(entry.getKey(), entry.getValue());
        }

        return combined;
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
}
