package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nbtEditor.ArmoredElytraBuilder;
import nl.pim16aap2.armoredElytra.nbtEditor.DurabilityManager;
import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;

public class CommandHandler implements CommandExecutor
{
    private final ArmoredElytra plugin;
    private static Field BY_KEY_FIELD;
    private final ArmoredElytraBuilder armoredElytraBuilder;

    public CommandHandler(ArmoredElytra plugin, NBTEditor nbtEditor, DurabilityManager durabilityManager)
    {
        this.plugin = plugin;
        armoredElytraBuilder = new ArmoredElytraBuilder(nbtEditor, durabilityManager, plugin.getConfigLoader(), plugin);
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
                        plugin.giveArmoredElytraToPlayer(receiver, armoredElytraBuilder.newArmoredElytra(armorTier));
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

            if (args.length == 1 &&
                (args[0].equalsIgnoreCase("listAvailableEnchantments") ||
                    args[0].equalsIgnoreCase("listEnchantments") ||
                    args[0].equalsIgnoreCase("enchantments")))
            {
                listAvailableEnchantments();
                return true;
            }

            if (args.length == 2)
            {
                final String tier = args[1];
                player = Bukkit.getPlayer(args[0]);
                if (player != null)
                {
                    ArmorTier armorTier = ArmorTier.valueOfName(tier.toLowerCase());
                    if (armorTier == null)
                        return false;

                    plugin.elytraReceivedMessage(player, armorTier);
                    plugin.giveArmoredElytraToPlayer(player, armoredElytraBuilder.newArmoredElytra(armorTier));
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

    private void listAvailableEnchantments()
    {
        try
        {
            if (BY_KEY_FIELD == null)
            {
                BY_KEY_FIELD = Enchantment.class.getDeclaredField("byKey");
                BY_KEY_FIELD.setAccessible(true);
            }

            @SuppressWarnings("unchecked") final Map<NamespacedKey, Enchantment> byKey = (Map<NamespacedKey, Enchantment>) BY_KEY_FIELD.get(
                null);

            final StringBuilder sb = new StringBuilder("\nAvailable enchantments: \n");
            byKey.keySet().stream()
                 .map(NamespacedKey::toString).sorted()
                 .forEach(name -> sb.append("  - ").append(name).append("\n"));

            plugin.getLogger().info(sb.toString());
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            throw new RuntimeException("Failed to get registered enchantments!", e);
        }
    }
}
