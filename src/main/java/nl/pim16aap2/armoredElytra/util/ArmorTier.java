package nl.pim16aap2.armoredElytra.util;

import org.bukkit.Material;

public enum ArmorTier
{
	// Tier: tier armor-value, tier armor-toughness, repair 
	NONE     (0              , 0                   , null               ),
	LEATHER  (3              , 0                   , Material.LEATHER   ),
	GOLD     (5              , 0                   , Material.GOLD_INGOT),
	CHAIN    (5              , 0                   , Material.IRON_INGOT),
	IRON     (6              , 0                   , Material.IRON_INGOT),
	DIAMOND  (8              , 2                   , Material.DIAMOND   );
	
    private int         armor;
	private int     toughness;
	private Material   repair;

    private ArmorTier (int armor, int toughness, Material repair) 
    {
        this.armor     = armor;
        this.toughness = toughness;
        this.repair    = repair;
    }
    
    // return the armor value of a tier.
    public static int getArmor           (ArmorTier tier) { return tier.armor;     }
    
    // return the armor toughness of a tier.
    public static int getToughness       (ArmorTier tier) { return tier.toughness; }
    
    // return the repair item of a tier
    public static Material getRepairItem (ArmorTier tier) { return tier.repair;    }
}
