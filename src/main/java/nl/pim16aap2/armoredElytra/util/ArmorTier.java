package nl.pim16aap2.armoredElytra.util;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public enum ArmorTier
{
    // Tier:  TierID, armor-value, armor-toughness, knockbackResistance, repair
    NONE(0, 0, 0, 0, null, ""),
    LEATHER(1, 3, 0, 0, Material.LEATHER, "leather"),
    GOLD(2, 5, 0, 0, Material.GOLD_INGOT, "gold"),
    CHAIN(3, 5, 0, 0, Material.IRON_INGOT, "chain"),
    IRON(4, 6, 0, 0, Material.IRON_INGOT, "iron"),
    DIAMOND(5, 8, 2, 0, Material.DIAMOND, "diamond"),
    NETHERITE(6, 8, 3, 0.1, XMaterial.NETHERITE_INGOT.parseMaterial(), "netherite"),
    ;

    private final int tierID;
    private final int armor;
    private final int toughness;
    private final double knockbackResistance;
    private final Material repair;
    private final String name;
    private static final Map<String, ArmorTier> map = new HashMap<>();
    private static final Map<Integer, ArmorTier> armorValueMap = new HashMap<>();
    private static final Map<Integer, ArmorTier> armorIDMap = new HashMap<>();

    ArmorTier(int tierID, int armor, int toughness, double knockbackResistance, Material repair, String name)
    {
        this.tierID = tierID;
        this.armor = armor;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repair = repair;
        this.name = name;
    }

    // return the armor value of a tier.
    public static int getArmor(ArmorTier tier)
    {
        return tier.armor;
    }

    // return the armor value of a tier.
    public static int getTierID(ArmorTier tier)
    {
        return tier.tierID;
    }

    // return the armor toughness of a tier.
    public static int getToughness(ArmorTier tier)
    {
        return tier.toughness;
    }

    // return the armor toughness of a tier.
    public static double getKnockbackResistance(ArmorTier tier)
    {
        return tier.knockbackResistance;
    }

    // return the repair item of a tier
    public static Material getRepairItem(ArmorTier tier)
    {
        return tier.repair;
    }

    public static String getName(ArmorTier tier)
    {
        return tier.name;
    }

    public static ArmorTier valueOfName(String name)
    {
        return map.get(name);
    }

    public static ArmorTier getArmorTierFromArmor(int armor)
    {
        ArmorTier tier = armorValueMap.get(armor);
        return tier == null ? ArmorTier.NONE : tier;
    }

    public static ArmorTier getArmorTierFromID(int tierID)
    {
        ArmorTier tier = armorIDMap.get(tierID);
        return tier == null ? ArmorTier.NONE : tier;
    }

    static
    {
        for (ArmorTier tier : ArmorTier.values())
        {
            map.put(tier.name, tier);
            armorValueMap.put(tier.armor, tier);
            armorIDMap.put(tier.tierID, tier);
        }
        // Overwrite the index for diamond-tier armor value.
        // This value is the same as netherite's tier. However, with the introduction of the NETHERITE armor tier,
        // a new system was introduced that doesn't rely on the armor value for determining the armortier.
        // Therefore, when using the old backup system, it is always going to be the diamond tier instead.
        // While no new elytras cna be created using the old system, some may still be around from when it was still used.
        armorValueMap.put(ArmorTier.DIAMOND.armor, ArmorTier.DIAMOND);
    }
}
