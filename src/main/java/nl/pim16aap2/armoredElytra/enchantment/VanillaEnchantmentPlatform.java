package nl.pim16aap2.armoredElytra.enchantment;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

class VanillaEnchantmentPlatform implements IEnchantmentPlatform
{
    private Map<String, Integer> getNamedEnchantments(final Map<Enchantment, Integer> enchantments)
    {
        final Map<String, Integer> enchantmentsStr = new HashMap<>(enchantments.size());
        enchantments.forEach((k, v) -> enchantmentsStr.put(k.getName(), v));
        return enchantmentsStr;
    }

    private Map<Enchantment, Integer> getEnchantments(final Map<String, Integer> enchantments)
    {
        final Map<Enchantment, Integer> enchantmentsStr = new HashMap<>(enchantments.size());
        enchantments.forEach((k, v) -> enchantmentsStr.put(Enchantment.getByName(k), v));
        return enchantmentsStr;
    }

    @Override
    public EnchantmentContainer getEnchantmentsFromItem(final ItemStack is)
    {
        return new EnchantmentContainer(getNamedEnchantments(is.getEnchantments()), this);
    }

    @Override
    public EnchantmentContainer getEnchantmentsFromBook(final ItemStack is)
    {
        if (!is.hasItemMeta())
            return new EnchantmentContainer(new HashMap<>(0), this);

        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) is.getItemMeta();
        if (meta == null || !meta.hasStoredEnchants())
            return new EnchantmentContainer(new HashMap<>(0), this);

        return new EnchantmentContainer(getNamedEnchantments(meta.getStoredEnchants()), this);
    }

    @Override
    public void applyEnchantments(final ItemStack is, final Map<String, Integer> enchantments)
    {
        enchantments.forEach(
            (enchantmentName, level) ->
            {
                Enchantment enchantment = Enchantment.getByName(enchantmentName);
                if (enchantment == null)
                {
                    Bukkit.getLogger().log(Level.INFO, "Failed to find enchantment: \"" + enchantmentName + "\"");
                    return;
                }
                is.addUnsafeEnchantment(enchantment, level);
            }
        );
    }

    @Override
    public Map<String, Integer> merge(final Map<String, Integer> first, final Map<String, Integer> second)
    {
        if (second == null || second.isEmpty())
            return first;

        if (first == null || first.isEmpty())
            return second;

        Map<Enchantment, Integer> enchantments0 = getEnchantments(first);
        Map<Enchantment, Integer> enchantments1 = getEnchantments(second);
        Map<Enchantment, Integer> combined = new HashMap<>(enchantments0);

        for (Map.Entry<Enchantment, Integer> entry : enchantments1.entrySet())
        {
            Integer enchantLevel = enchantments0.get(entry.getKey());
            if (enchantLevel != null)
            {
                if (entry.getValue().equals(enchantLevel) && entry.getValue() < entry.getKey().getMaxLevel())
                    enchantLevel = entry.getValue() + 1;
                else if (entry.getValue() > enchantLevel)
                    enchantLevel = entry.getValue();

                // If the enchantment level has changed,
                if (!enchantLevel.equals(enchantments0.get(entry.getKey())))
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
            int protVal0 = Util.getProtectionEnchantmentsVal(enchantments0);
            int protVal1 = Util.getProtectionEnchantmentsVal(enchantments1);

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

        return getNamedEnchantments(combined);
    }
}
