package nl.pim16aap2.armoredElytra;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class LoginHandler implements Listener {
	
	ArmoredElytra plugin;
	
	public LoginHandler(ArmoredElytra plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onLogin(PlayerLoginEvent event)
	{
		Player player = event.getPlayer();
		if (player.hasPermission("armoredElytra.admin") && !plugin.isUpToDate())
		{
			new BukkitRunnable() 
			{
	            @Override
                public void run() 
	            {
	            		plugin.messagePlayer(player, ChatColor.AQUA, "The Armored Elytra plugin is out of date!");
	            }
			}.runTaskLater(this.plugin, 10);
		}
	}
}
