package nl.pim16aap2.armoredElytra.util;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Util
{
    // Check if an item is broken or not.
    public static boolean isBroken(ItemStack item)
    {
        return item.getDurability() >= item.getType().getMaxDurability();
    }

    // Get the armor tier from a chest plate.
    public static ArmorTier armorToTier(Material item)
    {
        ArmorTier ret = ArmorTier.NONE;

        switch (item)
        {
        case LEATHER_CHESTPLATE:
            ret = ArmorTier.LEATHER;
            break;
        case GOLD_CHESTPLATE:
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
        default:
            break;
        }
        return ret;
    }

    // Check if mat is a chest plate.
    public static boolean isChestPlate(Material mat)
    {
        if (mat == Material.LEATHER_CHESTPLATE   || mat == Material.GOLD_CHESTPLATE ||
            mat == Material.CHAINMAIL_CHESTPLATE || mat == Material.IRON_CHESTPLATE ||
            mat == Material.DIAMOND_CHESTPLATE)
            return true;
        return false;
    }

    // Function that returns which/how many protection enchantments there are.
    public static int getProtectionEnchantmentsVal(Map<Enchantment, Integer> enchantments)
    {
        int ret  =  0;
        if (enchantments.containsKey(Enchantment.PROTECTION_ENVIRONMENTAL))
            ret +=  1;
        if (enchantments.containsKey(Enchantment.PROTECTION_EXPLOSIONS))
            ret +=  2;
        if (enchantments.containsKey(Enchantment.PROTECTION_FALL))
            ret +=  4;
        if (enchantments.containsKey(Enchantment.PROTECTION_FIRE))
            ret +=  8;
        if (enchantments.containsKey(Enchantment.PROTECTION_PROJECTILE))
            ret += 16;
        return ret;
    }

    public static boolean playerHasCraftPerm(Player player, ArmorTier armorTier)
    {
        return ((armorTier == ArmorTier.LEATHER && player.hasPermission("armoredelytra.craft.leather")) ||
                (armorTier == ArmorTier.GOLD    && player.hasPermission("armoredelytra.craft.gold"   )) ||
                (armorTier == ArmorTier.CHAIN   && player.hasPermission("armoredelytra.craft.chain"  )) ||
                (armorTier == ArmorTier.IRON    && player.hasPermission("armoredelytra.craft.iron"   )) ||
                (armorTier == ArmorTier.DIAMOND && player.hasPermission("armoredelytra.craft.diamond")));
    }

    public static boolean playerHasWearPerm(Player player, ArmorTier armorTier)
    {
        return ((armorTier == ArmorTier.LEATHER && player.hasPermission("armoredelytra.wear.leather" )) ||
                (armorTier == ArmorTier.GOLD    && player.hasPermission("armoredelytra.wear.gold"    )) ||
                (armorTier == ArmorTier.CHAIN   && player.hasPermission("armoredelytra.wear.chain"   )) ||
                (armorTier == ArmorTier.IRON    && player.hasPermission("armoredelytra.wear.iron"    )) ||
                (armorTier == ArmorTier.DIAMOND && player.hasPermission("armoredelytra.wear.diamond" )));
    }
}
