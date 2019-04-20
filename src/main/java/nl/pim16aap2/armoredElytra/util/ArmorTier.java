package nl.pim16aap2.armoredElytra.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

public enum ArmorTier
{
    // Tier:  armor-value, armor-toughness, repair
    NONE     (0          , 0              , null               , ""),
    LEATHER  (3          , 0              , Material.LEATHER   , "leather"),
    GOLD     (5          , 0              , Material.GOLD_INGOT, "gold"),
    CHAIN    (5          , 0              , Material.IRON_INGOT, "chain"),
    IRON     (6          , 0              , Material.IRON_INGOT, "iron"),
    DIAMOND  (8          , 2              , Material.DIAMOND   , "diamond");

    private final int         armor;
    private final int     toughness;
    private final Material   repair;
    private final String       name;
    private static Map<String, ArmorTier> map = new HashMap<>();

    private ArmorTier (int armor, int toughness, Material repair, String name)
    {
        this.armor      = armor;
        this.toughness  = toughness;
        this.repair     = repair;
        this.name       = name;
    }

    // return the armor value of a tier.
    public static int getArmor           (ArmorTier tier) { return tier.armor;     }

    // return the armor toughness of a tier.
    public static int getToughness       (ArmorTier tier) { return tier.toughness; }

    // return the repair item of a tier
    public static Material getRepairItem (ArmorTier tier) { return tier.repair;    }

    public static String getName         (ArmorTier tier) { return tier.name;      }

    public static ArmorTier valueOfName  (String name)    { return map.get(name);  }

    static
    {
        for (ArmorTier tier : ArmorTier.values())
            map.put(tier.name, tier);
    }
}
