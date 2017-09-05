package nl.pim16aap2.armoredElytra;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import com.rit.sucy.EnchantPlugin;
import com.rit.sucy.EnchantmentAPI;

import enchantments.DiamondArmor;
 
public class ArmoredElytra extends EnchantPlugin implements Listener {
	
	@Override
    public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new EventHandlers(this), this);
	}
	
	@Override
    public void registerEnchantments() {
        EnchantmentAPI.registerCustomEnchantment(new DiamondArmor());
    }
}