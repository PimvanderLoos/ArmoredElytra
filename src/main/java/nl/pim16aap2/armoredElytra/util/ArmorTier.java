package nl.pim16aap2.armoredElytra.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum ArmorTier
{
	// Tier: tier armor-value, tier armor-toughness, tier name color     , tier name  , repair 
	NONE     (0              , 0                   , ChatColor.WHITE     , "Unarmored", null               ),
	LEATHER  (3              , 0                   , ChatColor.DARK_GREEN, "Leather"  , Material.LEATHER   ),
	GOLD     (5              , 0                   , ChatColor.YELLOW    , "Gold"     , Material.GOLD_INGOT),
	CHAIN    (5              , 0                   , ChatColor.DARK_GRAY , "Chain"    , Material.IRON_INGOT),
	IRON     (6              , 0                   , ChatColor.GRAY      , "Iron"     , Material.IRON_INGOT),
	DIAMOND  (8              , 2                   , ChatColor.AQUA      , "Diamond"  , Material.DIAMOND   );
	
    private int       armor;
	private int   toughness;
	private ChatColor color;
	private String     name;
	private Material repair;

    private ArmorTier (int armor, int toughness, ChatColor color, String name, Material repair) 
    {
        this.armor     = armor;
        this.color     = color;
        this.toughness = toughness;
        this.name      = name;
        this.repair    = repair;
    }
    
    // return the armor value of a tier.
    public static int getArmor           (ArmorTier tier) { return tier.armor;     }
    
    // return the armor toughness of a tier.
    public static int getToughness       (ArmorTier tier) { return tier.toughness; }
    
    // return the color of a tier.
    public static ChatColor getColor     (ArmorTier tier) { return tier.color;     }
    
    // return the name of a tier.
    public static String getArmorName    (ArmorTier tier) { return tier.name;      }
    
    // return the repair item of a tier
    public static Material getRepairItem (ArmorTier tier) { return tier.repair;    }
}
