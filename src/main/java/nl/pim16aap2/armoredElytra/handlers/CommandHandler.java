package nl.pim16aap2.armoredElytra.handlers;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nms.NBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;

public class CommandHandler implements CommandExecutor
{
	ArmoredElytra  plugin;
	NBTEditor   nbtEditor;
	
	public CommandHandler(ArmoredElytra plugin, NBTEditor nbtEditor)
	{
		this.plugin        = plugin;
		this.nbtEditor     = nbtEditor;
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
    {
		Player player;
		
		if (sender instanceof Player)
		{	
			player = (Player) sender;

			if (plugin.getUninstallMode())
			{
				plugin.messagePlayer(player, "Plugin in uninstall mode! New Armored Elytras are not allowed!");
				return true;
			}
				
			
			if (cmd.getName().equalsIgnoreCase("ArmoredElytra"))
			{
				if (args.length == 1 || args.length == 2) 
				{
					ItemStack newElytra = null;
					String tier = null;
					Player receiver;
					boolean allowed = false;
					ArmorTier armorTier = ArmorTier.NONE;
					if (args.length == 1)
					{
						receiver = player;
						tier     = args[0];
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
					
					// TODO: Use armorTier name from ArmorTier struct.
					// Also, use AT-name for permission node verification.
					
					
					// Leather armor.
					if (tier.equalsIgnoreCase("leather"))
					{
						armorTier = ArmorTier.LEATHER;
						if (player.hasPermission("armoredelytra.give.leather"))
							allowed = true;
						
					// Gold armor.
					} 
					else if (tier.equalsIgnoreCase("gold"))
					{
						armorTier = ArmorTier.GOLD;
						if (player.hasPermission("armoredelytra.give.gold"))
							allowed = true;
						
					// Chain armor.
					} 
					else if (tier.equalsIgnoreCase("chain"))
					{
						armorTier = ArmorTier.CHAIN;
						if (player.hasPermission("armoredelytra.give.chain")) 
							allowed = true;
					
					// Iron armor.
					} 
					else if (tier.equalsIgnoreCase("iron"))
					{
						armorTier = ArmorTier.IRON;
						if (player.hasPermission("armoredelytra.give.iron"))
							allowed = true;
					
					// Diamond armor.
					} 
					else if (tier.equalsIgnoreCase("diamond"))
					{
						armorTier = ArmorTier.DIAMOND;
						if (player.hasPermission("armoredelytra.give.diamond"))
							allowed = true;
					} 
					else 
						plugin.messagePlayer(player, "Not a supported armor tier! Try one of these: leather, gold, chain, iron, diamond.");
					
					if (allowed)
					{
						plugin.elytraReceivedMessage(receiver, armorTier);
						newElytra = nbtEditor.addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), armorTier, plugin.getConfigLoader().getBool("unbreakable"));
						plugin.giveArmoredElytraToPlayer(receiver, newElytra);
					} 
					else
						plugin.messagePlayer(player, "You do not have the required permission node to give " + plugin.getArmoredElytrName(armorTier) + " armored elytras.");
					return true;
				}
			}
		} 
		else 
		{
			if (plugin.getUninstallMode())
			{
				plugin.myLogger(Level.INFO, "Plugin in uninstall mode! New Armored Elytras are not allowed!");
				return true;
			}
			
			if (args.length == 2) 
			{
				ItemStack newElytra = null;
				String tier = args[1];
				if (Bukkit.getPlayer(args[0]) != null)
				{
					player = Bukkit.getPlayer(args[0]);
					ArmorTier armorTier = ArmorTier.NONE;
					
					// TODO: Again, use the ArmorTier struct for tier retrieval.
					
					if (tier.equalsIgnoreCase("leather"))
						armorTier = ArmorTier.LEATHER;
					else if (tier.equalsIgnoreCase("gold"))
						armorTier = ArmorTier.GOLD;
					else if (tier.equalsIgnoreCase("chain"))
						armorTier = ArmorTier.CHAIN;
					else if (tier.equalsIgnoreCase("iron"))
						armorTier = ArmorTier.IRON;
					else if (tier.equalsIgnoreCase("diamond"))
						armorTier = ArmorTier.DIAMOND;
					// TODO: Catch user requesting non-existent tier.
					
					plugin.elytraReceivedMessage(player, armorTier);
					newElytra = nbtEditor.addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), armorTier, plugin.getConfigLoader().getBool("unbreakable"));
					plugin.giveArmoredElytraToPlayer(player, newElytra);
					plugin.myLogger(Level.INFO, ("Giving an armored elytra of the " + ArmorTier.getArmor(armorTier) + " armor tier to player " + player.getName()));
					return true;					
				} 
				else 
				{
					plugin.myLogger(Level.INFO, ("Player " + args[1] + " not found!"));
					return true;
				}
			}
		}
		return false;
    }
}
