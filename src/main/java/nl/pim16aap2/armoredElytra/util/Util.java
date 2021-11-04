package nl.pim16aap2.armoredElytra.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

public class Util
{
    public static String errorToString(Error e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static String exceptionToString(Exception e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    // Check if an item is broken or not.
    public static boolean isBroken(ItemStack item)
    {
        return item.getDurability() >= item.getType().getMaxDurability();
    }

    // Get the armor tier from a chest plate.
    public static ArmorTier armorToTier(ItemStack itemStack)
    {
        return armorToTier(itemStack.getType());
    }

    // Get the armor tier from a chest plate.
    public static ArmorTier armorToTier(Material mat)
    {
        return switch (mat)
            {
                case LEATHER_CHESTPLATE -> ArmorTier.LEATHER;
                case GOLDEN_CHESTPLATE -> ArmorTier.GOLD;
                case CHAINMAIL_CHESTPLATE -> ArmorTier.CHAIN;
                case IRON_CHESTPLATE -> ArmorTier.IRON;
                case DIAMOND_CHESTPLATE -> ArmorTier.DIAMOND;
                case NETHERITE_CHESTPLATE -> ArmorTier.NETHERITE;
                default -> ArmorTier.NONE;
            };
    }

    public static Material tierToChestplate(ArmorTier armorTier)
    {
        return switch (armorTier)
            {
                case LEATHER -> Material.LEATHER_CHESTPLATE;
                case GOLD -> Material.GOLDEN_CHESTPLATE;
                case CHAIN -> Material.CHAINMAIL_CHESTPLATE;
                case IRON -> Material.IRON_CHESTPLATE;
                case DIAMOND -> Material.DIAMOND_CHESTPLATE;
                case NETHERITE -> Material.NETHERITE_CHESTPLATE;
                default -> throw new IllegalStateException("Could not create chestplate for tier: " + armorTier);
            };
    }

    public static boolean isChestPlate(ItemStack itemStack)
    {
        return isChestPlate(itemStack.getType());
    }

    // Check if mat is a chest plate.
    public static boolean isChestPlate(Material mat)
    {
        return armorToTier(mat) != ArmorTier.NONE;
    }

    public static String snakeToCamelCase(String input)
    {
        final char[] arr = input.toLowerCase(Locale.US).toCharArray();

        int skipped = 0;
        boolean capitalize = false;

        for (int idx = 0; idx < arr.length; ++idx)
        {
            char current = arr[idx];

            if (current == '_')
            {
                ++skipped;
                capitalize = true;
                continue;
            }

            final int targetIdx = idx - skipped;

            if (capitalize)
            {
                if (targetIdx > 0)
                    current = Character.toUpperCase(current);
                capitalize = false;
            }

            // targetIdx is always <= idx, so we can reuse the current array
            // without overwriting any values we will need in the future.
            arr[targetIdx] = current;
        }

        return new String(arr, 0, arr.length - skipped);
    }

    // Function that returns which/how many protection enchantments there are.
    public static int getProtectionEnchantmentsVal(Map<Enchantment, Integer> enchantments)
    {
        int ret = 0;
        if (enchantments.containsKey(Enchantment.PROTECTION_ENVIRONMENTAL))
            ret += 1;
        if (enchantments.containsKey(Enchantment.PROTECTION_EXPLOSIONS))
            ret += 2;
        if (enchantments.containsKey(Enchantment.PROTECTION_FALL))
            ret += 4;
        if (enchantments.containsKey(Enchantment.PROTECTION_FIRE))
            ret += 8;
        if (enchantments.containsKey(Enchantment.PROTECTION_PROJECTILE))
            ret += 16;
        return ret;
    }

    public static void moveChestplateToInventory(Player player)
    {
        final PlayerInventory inventory = player.getInventory();
        inventory.addItem(inventory.getChestplate());

        final @Nullable ItemStack chestplate = inventory.getChestplate();
        if (chestplate != null)
            chestplate.setAmount(0);

        player.updateInventory();
    }

    /**
     * Ensures that a given value does not exceed the provided upper and lower bounds.
     *
     * @param val The value to check.
     * @param min The lower bound limit.
     * @param max The upper bound limit.
     * @return The value if it is bigger than min and larger than max, otherwise either min or max.
     */
    public static int between(int val, int min, int max)
    {
        return Math.max(min, Math.min(max, val));
    }
}
