package nl.pim16aap2.armoredElytra.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum ArmorTier
{
	// Tier: tier armor-value, tier armor-toughness, tier name color     , tier name. 
	NONE     (0              , 0                   , ChatColor.WHITE     , "Unarmored"),
	LEATHER  (3              , 0                   , ChatColor.DARK_GREEN, "Leather"  ),
	GOLD     (5              , 0                   , ChatColor.YELLOW    , "Gold"     ),
	CHAIN    (5              , 0                   , ChatColor.DARK_GRAY , "Chain"    ),
	IRON     (6              , 0                   , ChatColor.GRAY      , "Iron"     ),
	DIAMOND  (8              , 2                   , ChatColor.AQUA      , "Diamond"  );
	
    private int armor;
	private int toughness;
	private ChatColor color;
	private String name;

	// Create a new chip with the given face and suit
    private ArmorTier (int armor, int toughness, ChatColor color, String name) 
    {
        this.armor     = armor;
        this.color     = color;
        this.toughness = toughness;
        this.name      = name;
    }
    
    // return the armor value of a tier.
    public static int getArmor        (ArmorTier item) { return item.armor;     }
    
    // return the armor toughness of a tier.
    public static int getToughness    (ArmorTier item) { return item.toughness; }
    
    // return the color of a tier.
    public static ChatColor getColor  (ArmorTier item) { return item.color;     }
    
    // return the name of a tier.
    public static String getArmorName (ArmorTier item) { return item.name;      }

	public static ArmorTier tierFromMat(Material type)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
