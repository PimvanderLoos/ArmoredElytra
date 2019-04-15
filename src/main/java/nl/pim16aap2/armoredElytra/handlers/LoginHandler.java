package nl.pim16aap2.armoredElytra.handlers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import nl.pim16aap2.armoredElytra.ArmoredElytra;

public class LoginHandler implements Listener
{
    private final ArmoredElytra plugin;
    private final String       message;

    public LoginHandler(ArmoredElytra plugin, String message)
    {
        this.plugin  =  plugin;
        this.message = message;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event)
    {
        Player player = event.getPlayer();
        if (player.hasPermission("armoredElytra.admin"))
            // Slight delay so the player actually receives the message;
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    plugin.messagePlayer(player, ChatColor.AQUA, message);
                }
            }.runTaskLater(plugin, 10);
    }
}
