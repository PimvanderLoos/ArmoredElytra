package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class CommandHandler implements CommandExecutor
{
    private final ArmoredElytra plugin;

    public CommandHandler(ArmoredElytra plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player;

        if (sender instanceof Player)
        {
            player = (Player) sender;

            if (plugin.getConfigLoader().uninstallMode())
            {
                plugin.messagePlayer(player, plugin.getMyMessages().getString(Message.MESSAGES_UNINSTALLMODE));
                return true;
            }

            if (cmd.getName().equalsIgnoreCase("ArmoredElytra"))
                if (args.length == 1 || args.length == 2)
                {
                    ItemStack newElytra = null;
                    String tier = null;
                    Player receiver;
                    boolean allowed = false;

                    if (args.length == 1)
                    {
                        receiver = player;
                        tier = args[0];
                    }
                    else
                    {
                        receiver = Bukkit.getPlayer(args[0]);
                        if (receiver == null)
                        {
                            plugin.messagePlayer(player, ChatColor.RED, "Player \"" + args[0] + "\" not found!");
                            return true;
                        }
                        tier = args[1];
                    }

                    ArmorTier armorTier = ArmorTier.valueOfName(tier.toLowerCase());
                    if (armorTier != null)
                        allowed = player.hasPermission("armoredelytra.give." + ArmorTier.getName(armorTier));
                    else
                    {
                        plugin.messagePlayer(player, plugin.getMyMessages()
                                                           .getString(Message.MESSAGES_UNSUPPORTEDTIER));
                        return false;
                    }

                    if (allowed)
                    {
                        plugin.elytraReceivedMessage(receiver, armorTier);
                        newElytra = ArmoredElytra.getInstance().getNbtEditor()
                                                 .addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), armorTier,
                                                                  plugin.getConfigLoader().unbreakable());
                        plugin.giveArmoredElytraToPlayer(receiver, newElytra);
                    }
                    else
                        plugin.sendNoGivePermissionMessage(player, armorTier);
                    return true;
                }
        }
        else
        {
            if (plugin.getConfigLoader().uninstallMode())
            {
                plugin.myLogger(Level.INFO, "Plugin in uninstall mode! New Armored Elytras are not allowed!");
                return true;
            }

            if (args.length == 2)
            {
                ItemStack newElytra;
                final String tier = args[1];
                player = Bukkit.getPlayer(args[0]);
                if (player != null)
                {
                    ArmorTier armorTier = ArmorTier.valueOfName(tier.toLowerCase());
                    if (armorTier == null)
                        return false;

                    plugin.elytraReceivedMessage(player, armorTier);
                    newElytra = ArmoredElytra.getInstance().getNbtEditor()
                                             .addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), armorTier,
                                                              plugin.getConfigLoader().unbreakable());
                    plugin.giveArmoredElytraToPlayer(player, newElytra);
                    plugin.myLogger(Level.INFO, ("Giving an armored elytra of the " + ArmorTier.getArmor(armorTier) +
                        " armor tier to player " + player.getName()));
                    return true;
                }
                plugin.myLogger(Level.INFO, ("Player " + args[1] + " not found!"));
                return true;
            }
        }
        return false;
    }
}
