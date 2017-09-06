package nl.pim16aap2.armoredElytra;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import com.rit.sucy.EnchantPlugin;
 
public class ArmoredElytra extends EnchantPlugin implements Listener {
	
	@Override
    public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new EventHandlers(this), this);
	}
}