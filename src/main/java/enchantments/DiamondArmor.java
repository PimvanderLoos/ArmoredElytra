package enchantments;

import org.bukkit.Material;

import com.rit.sucy.CustomEnchantment;

public class DiamondArmor extends CustomEnchantment {

	// List of items to enchant onto through an enchanting table
    static final Material[] DIAMOND_ARMOR_ITEMS = new Material[] {Material.STICK};
    static final String name = "Diamond Armor Tier";
	
	public DiamondArmor() {
		super(name, DIAMOND_ARMOR_ITEMS, 0);
		this.max = 1;
	}
	
}
