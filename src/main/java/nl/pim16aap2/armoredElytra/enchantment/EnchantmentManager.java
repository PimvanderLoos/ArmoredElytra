package nl.pim16aap2.armoredElytra.enchantment;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EnchantmentManager
{
    private final ArmoredElytra armoredElytra;
    private final List<EnchantmentContainer> containers = new ArrayList<>();
    private Integer count = null;

    public EnchantmentManager(final ItemStack is)
    {
        armoredElytra = ArmoredElytra.getInstance();
        EnchantmentPlatformManager.get().getPlatforms().forEach(platform ->
                                                                    containers.add(platform.getEnchantments(is)));

        Collection<String> filter = armoredElytra.getConfigLoader().allowedEnchantments();
        containers.forEach(container -> container.filter(filter));
    }

    /**
     * Merges this EnchantmentManager with another one.
     *
     * @param other The EnchantmentManager to merge into the current one.
     * @return The instance of the current EnchantmentManager.
     */
    public EnchantmentManager merge(final EnchantmentManager other)
    {
        for (int idx = 0; idx < containers.size(); ++idx)
            containers.get(idx).merge(other.containers.get(idx));
        count = null; // Reset count, as it's no longer up-to-date.
        return this;
    }

    /**
     * Applies all the enchantments to the provided itemstack.
     *
     * @param is The itemstack to apply all enchantments to.
     */
    public void apply(final ItemStack is)
    {
        containers.forEach(container -> container.apply(is));
    }

    /**
     * Gets the total number of enchantments.
     *
     * @return The total number of enchantments.
     */
    public int getEnchantmentCount()
    {
        if (count != null)
            return count;

        count = 0;
        for (EnchantmentContainer container : containers)
            count += container.size();
        return count;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("Enchantments: \n");
        containers.forEach(container -> sb.append(container.toString()).append("\n"));
        return sb.toString();
    }
}
