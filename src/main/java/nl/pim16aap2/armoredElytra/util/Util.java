package nl.pim16aap2.armoredElytra.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

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
    public static ArmorTier armorToTier(Material mat)
    {
        ArmorTier ret = ArmorTier.NONE;
        XMaterial xmat = XMaterial.matchXMaterial(mat);
        if (xmat == null)
            return ret;

        switch (xmat)
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
        default:
            break;
        }
        return ret;
    }

    // Check if mat is a chest plate.
    public static boolean isChestPlate(Material mat)
    {
        XMaterial xmat = XMaterial.matchXMaterial(mat);
        if (xmat == null)
            return false;
        if (xmat == XMaterial.LEATHER_CHESTPLATE   || xmat == XMaterial.GOLDEN_CHESTPLATE ||
            xmat == XMaterial.CHAINMAIL_CHESTPLATE || xmat == XMaterial.IRON_CHESTPLATE ||
            xmat == XMaterial.DIAMOND_CHESTPLATE)
            return true;
        return false;
    }

    // Function that returns which/how many protection enchantments there are.
    // TODO: Use bit flags for this.
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
}
