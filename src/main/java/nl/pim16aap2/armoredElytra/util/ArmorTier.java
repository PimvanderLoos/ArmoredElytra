package nl.pim16aap2.armoredElytra.util;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public enum ArmorTier
{
    // Tier:  TierID, armor-value, armor-toughness, knockbackResistance, repair, defaultRepairCount, name, durability
    NONE(0, 0, 0, 0, null, 0, "", 0),
    LEATHER(1, 3, 0, 0, Material.LEATHER, 6, "leather", 80),
    GOLD(2, 5, 0, 0, Material.GOLD_INGOT, 6, "gold", 112),
    CHAIN(3, 5, 0, 0, Material.IRON_INGOT, 4, "chain", 240),
    IRON(4, 6, 0, 0, Material.IRON_INGOT, 4, "iron", 240),
    DIAMOND(5, 8, 2, 0, Material.DIAMOND, 3, "diamond", 528),
    NETHERITE(6, 8, 3, 0.1, XMaterial.NETHERITE_INGOT.parseMaterial(), 3, "netherite", 592),
    ;

    private final int tierID;
    private final int armor;
    private final int toughness;
    private final double knockbackResistance;
    private final Material repair;
    private final int defaultRepairCount;
    private final String name;
    private final int durability;
    private static final Map<String, ArmorTier> map = new HashMap<>();
    private static final Map<Integer, ArmorTier> armorValueMap = new HashMap<>();
    private static final Map<Integer, ArmorTier> armorIDMap = new HashMap<>();

    ArmorTier(int tierID, int armor, int toughness, double knockbackResistance, Material repair,
              int defaultRepairCount, String name, int durability)
    {
        this.tierID = tierID;
        this.armor = armor;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repair = repair;
        this.defaultRepairCount = defaultRepairCount;
        this.name = name;
        this.durability = durability;
    }

    public static int getArmor(ArmorTier tier)
    {
        return tier.armor;
    }

    public static int getMaxDurability(ArmorTier tier)
    {
        return tier.durability;
    }

    public static int getTierID(ArmorTier tier)
    {
        return tier.tierID;
    }

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
        // While no new elytras can be created using the old system, some may still
        // be around from when it was still used.
        armorValueMap.put(ArmorTier.DIAMOND.armor, ArmorTier.DIAMOND);
    }

    public static int getDefaultRepairCount(ArmorTier armorTier)
    {
        return armorTier.defaultRepairCount;
    }
}
