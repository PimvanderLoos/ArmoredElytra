package nl.pim16aap2.armoredElytra.enchantment;

import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;

class EnchantmentContainer
{
    private Map<String, Integer> enchantments;
    private final IEnchantmentPlatform manager;

    public EnchantmentContainer(final Map<String, Integer> enchantments, final IEnchantmentPlatform manager)
    {
        this.enchantments = enchantments;
        this.manager = manager;
    }

    /**
     * Removes any entries from the list of enchantments that do not exist in the provided filter.
     *
     * @param allowed The names of enchantments (upper case) that are allowed. Any names not in this list are removed.
     * @return This instance.
     */
    public EnchantmentContainer filter(final Collection<String> allowed)
    {
        if (!enchantments.isEmpty())
            enchantments.keySet().retainAll(allowed);
        return this;
    }

    /**
     * Applies the enchantments to an itemstack.
     *
     * @param is The itemstack to apply the enchantments to.
     */
    public void apply(final ItemStack is)
    {
        manager.applyEnchantments(is, enchantments);
    }

    /**
     * Merges this container with another one.
     */
    public void merge(EnchantmentContainer other)
    {
        if (!(manager.getClass() == other.manager.getClass()))
            throw new RuntimeException("Trying to add enchantment containers of different types!");
        enchantments = manager.merge(enchantments, other.enchantments);
    }

    /**
     * Gets the number of enchantments in this container.
     */
    public int size()
    {
        return enchantments.size();
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
}
