package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nbtEditor.INBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class Uninstaller implements Listener
{
    private final ArmoredElytra plugin;
    private final INBTEditor nbtEditor;

    public Uninstaller(ArmoredElytra plugin, INBTEditor nbtEditor)
    {
        this.plugin = plugin;
        this.nbtEditor = nbtEditor;
    }

    public int removeArmoredElytras(Inventory inv)
    {
        int count = 0;
        for (ItemStack is : inv)
            if (is != null && is.getType() == Material.ELYTRA &&
                nbtEditor.getArmorTier(is) != ArmorTier.NONE)
            {
                inv.remove(is);
                ++count;
            }
        return count;
    }

    @EventHandler
    public void onChestOpen(InventoryOpenEvent event)
    {
        if (event.getInventory().getType().equals(InventoryType.CHEST))
            // Slight delay so the inventory has time to get loaded.
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    Inventory inv = event.getInventory();
                    int removed = removeArmoredElytras(inv);
                    if (removed != 0)
                        plugin.messagePlayer((Player) (event.getPlayer()), ChatColor.RED,
                                             "Removed " + removed + " armored elytras from your chest!");
                }
            }.runTaskLater(plugin, 20);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event)
    {
        // Slight delay so the inventory has time to get loaded.
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Inventory inv = event.getPlayer().getInventory();
                int removed = removeArmoredElytras(inv);
                if (removed != 0)
                    plugin.messagePlayer(event.getPlayer(), ChatColor.RED,
                                         "Removed " + removed + " armored elytras from your inventory!");
            }
        }.runTaskLater(plugin, 20);
    }
}
