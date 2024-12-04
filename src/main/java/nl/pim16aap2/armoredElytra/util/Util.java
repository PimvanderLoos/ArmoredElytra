package nl.pim16aap2.armoredElytra.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Util
{
    /**
     * The cross-version mapped attribute for armor.
     */
    public static final Attribute ATTRIBUTE_ARMOR = getAttribute("armor");

    /**
     * The cross-version mapped attribute for armor toughness.
     */
    public static final Attribute ATTRIBUTE_ARMOR_TOUGHNESS = getAttribute("armor_toughness");

    /**
     * The cross-version mapped attribute for knockback resistance.
     */
    public static final Attribute ATTRIBUTE_KNOCKBACK_RESISTANCE = getAttribute("knockback_resistance");

    private static Attribute getAttribute(String key)
    {
        // First try to use the provided name for retrieval.
        // If that doesn't work, we try again, but now prepend "generic." to the key.
        // This is the format used before ~1.21.4 (or 2/3, not sure).
        @Nullable Attribute ret = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(key));
        if (ret == null)
            ret = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic." + key));
        return Objects.requireNonNull(ret, "Could not find attribute with key: '" + key + "'!");
    }

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

    /**
     * Get the armor tier from an item stack.
     * <p>
     * If the item stack is null or not a chestplate, it will return {@link ArmorTier#NONE}.
     *
     * @param itemStack
     *     The item stack to get the armor tier from.
     *
     * @return The armor tier of the item stack.
     */
    public static ArmorTier armorToTier(@Nullable ItemStack itemStack)
    {
        if (itemStack == null)
            return ArmorTier.NONE;
        return armorToTier(itemStack.getType());
    }

    // Get the armor tier from a chest plate.
    public static ArmorTier armorToTier(Material mat)
    {
        ArmorTier ret = ArmorTier.NONE;

        switch (mat)
        {
            case LEATHER_CHESTPLATE:
                ret = ArmorTier.LEATHER;
                break;
            case GOLDEN_CHESTPLATE:
                ret = ArmorTier.GOLD;
                break;
            case CHAINMAIL_CHESTPLATE:
                ret = ArmorTier.CHAIN;
                break;
            case IRON_CHESTPLATE:
                ret = ArmorTier.IRON;
                break;
            case DIAMOND_CHESTPLATE:
                ret = ArmorTier.DIAMOND;
                break;
            case NETHERITE_CHESTPLATE:
                ret = ArmorTier.NETHERITE;
                break;
            default:
                break;
        }
        return ret;
    }

    public static boolean isChestPlate(@Nullable ItemStack itemStack)
    {
        return itemStack != null && isChestPlate(itemStack.getType());
    }

    // Check if mat is a chest plate.
    public static boolean isChestPlate(Material mat)
    {
        try
        {
            return mat == Material.LEATHER_CHESTPLATE || mat == Material.GOLDEN_CHESTPLATE ||
                mat == Material.CHAINMAIL_CHESTPLATE || mat == Material.IRON_CHESTPLATE ||
                mat == Material.DIAMOND_CHESTPLATE || mat == Material.NETHERITE_CHESTPLATE;
        }
        catch (IllegalArgumentException e)
        {
            // No need to handle this, this is just XMaterial complaining the material doesn't exist.
            return false;
        }
    }

    /**
     * Converts a human entity to a player. If the human entity is not a player, it will try to get the player from the
     * UUID of the human entity.
     * <p>
     * If the player is not found, an {@link NullPointerException} will be thrown.
     *
     * @param humanEntity
     *     The human entity to convert.
     *
     * @return The player.
     *
     * @throws NullPointerException
     *     If the player could not be found.
     */
    public static @Nonnull Player humanEntityToPlayer(HumanEntity humanEntity)
    {
        if (humanEntity instanceof Player player)
            return player;

        return Objects.requireNonNull(
            Bukkit.getPlayer(humanEntity.getUniqueId()),
            "Could not get player from human entity: '" + humanEntity + "'!");
    }

    public static @Nullable Player humanEntityToOptionalPlayer(HumanEntity humanEntity)
    {
        if (humanEntity instanceof Player player)
            return player;
        return null;
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
        if (enchantments.containsKey(RemappedEnchantment.PROTECTION_ENVIRONMENTAL))
            ret += 1;
        if (enchantments.containsKey(RemappedEnchantment.PROTECTION_EXPLOSIONS))
            ret += 2;
        if (enchantments.containsKey(RemappedEnchantment.PROTECTION_FALL))
            ret += 4;
        if (enchantments.containsKey(RemappedEnchantment.PROTECTION_FIRE))
            ret += 8;
        if (enchantments.containsKey(RemappedEnchantment.PROTECTION_PROJECTILE))
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

    public static int toInt(String str, int defaultValue)
    {
        if (str == null)
        {
            return defaultValue;
        }
        try
        {
            return Integer.parseInt(str);
        }
        catch (NumberFormatException nfe)
        {
            return defaultValue;
        }
    }

    /**
     * Ensures that a given value does not exceed the provided upper and lower bounds.
     *
     * @param val
     *     The value to check.
     * @param min
     *     The lower bound limit.
     * @param max
     *     The upper bound limit.
     *
     * @return The value if it is bigger than min and larger than max, otherwise either min or max.
     */
    public static int between(int val, int min, int max)
    {
        return Math.max(min, Math.min(max, val));
    }
}
