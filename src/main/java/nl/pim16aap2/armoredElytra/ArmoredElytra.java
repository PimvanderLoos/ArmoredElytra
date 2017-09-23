package nl.pim16aap2.armoredElytra;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import nl.pim16aap2.armoredElytra.nms.NBTEditor;
import nl.pim16aap2.armoredElytra.nms.NBTEditor_V1_11_R1;
import nl.pim16aap2.armoredElytra.nms.NBTEditor_V1_12_R1;
 
public class ArmoredElytra extends JavaPlugin implements Listener 
{
	private NBTEditor nbtEditor;
	private boolean cursesAllowed;
	private int LEATHER_TO_FULL;
	private int GOLD_TO_FULL;
	private int IRON_TO_FULL;
	private int DIAMONDS_TO_FULL;
	private String[] allowedEnchants;
	private String usageDeniedMessage;
	private String elytraReceivedMessage;
	
	@Override
    public void onEnable() 
	{	
		FileConfiguration config = this.getConfig();
		config.addDefault("leatherRepair", 6);
		config.addDefault("goldRepair", 5);
		config.addDefault("ironRepair", 4);
		config.addDefault("diamondsRepair", 3);
		config.addDefault("allowCurses", true);
		config.addDefault("allowedEnchantments", new String[]{"DURABILITY","PROTECTION_FIRE","PROTECTION_EXPLOSIONS",
															 "PROTECTION_PROJECTILE","PROTECTION_ENVIRONMENTAL","THORNS"});
		config.addDefault("usageDeniedMessage", "You do not have the required permission node for this armor tier!");
		config.addDefault("elytraReceivedMessage", "An armored elytra has been bestowed upon you!");
		saveDefaultConfig();
		
		LEATHER_TO_FULL       = config.getInt("leatherRepair", 6);
		GOLD_TO_FULL          = config.getInt("goldRepair", 5);
		IRON_TO_FULL          = config.getInt("ironRepair", 4);
		DIAMONDS_TO_FULL      = config.getInt("diamondsRepair", 3);
		cursesAllowed         = config.getBoolean("allowCurses", true);
		List<String> list     = config.getStringList("allowedEnchantments");
		allowedEnchants       = list.toArray(new String[0]);
		usageDeniedMessage    = config.getString("usageDeniedMessage");
		elytraReceivedMessage = config.getString("elytraReceivedMessage");

		if (Objects.equals(usageDeniedMessage, new String("NONE")))
		{
			usageDeniedMessage = null;
		} 
		
		if (Objects.equals(elytraReceivedMessage, new String("NONE")))
		{
			elytraReceivedMessage = null;
		}
		
		config.options().copyDefaults(true);
		saveConfig();

		Bukkit.getLogger().log(Level.INFO, "["+this.getName()+"] "+"Allowed enchantments:");
		for (String s : allowedEnchants)
		{
			Bukkit.getLogger().log(Level.INFO, "["+this.getName()+"] "+s);
		}
		
		if (compatibleMCVer()) 
		{
			Bukkit.getPluginManager().registerEvents(new EventHandlers(this, nbtEditor, cursesAllowed, LEATHER_TO_FULL, GOLD_TO_FULL, IRON_TO_FULL, DIAMONDS_TO_FULL, allowedEnchants, usageDeniedMessage), this);
		} else {
			Bukkit.getLogger().log(Level.WARNING, "Trying to load the plugin on an incompatible version of Minecraft!");
		}
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
    {
		Player player;
		
		if (sender instanceof Player)
		{
			player = (Player) sender;
			if (cmd.getName().equalsIgnoreCase("ArmoredElytra"))
			{
				if (args.length == 1) 
				{
					ItemStack newElytra = null;
					String tier = args[0];
					// Leather armor.
					if (tier.equalsIgnoreCase("leather"))
					{
						if (player.hasPermission("armoredelytra.give.leather")) 
						{
							if (elytraReceivedMessage != null)
							{
								player.sendMessage(elytraReceivedMessage);
							}
							newElytra = nbtEditor.addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), 1);
						} else 
						{
							player.sendMessage("You do not have the required permission node for this armor tier!");
						}
						
					// Gold armor.
					} else if (tier.equalsIgnoreCase("gold"))
					{
						if (player.hasPermission("armoredelytra.give.gold")) 
						{
							if (elytraReceivedMessage != null)
							{
								player.sendMessage(elytraReceivedMessage);
							}
							newElytra = nbtEditor.addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), 2);
						} else 
						{
							player.sendMessage("You do not have the required permission node for this armor tier!");
						}
						
					// Chain armor.
					} else if (tier.equalsIgnoreCase("chain"))
					{
						if (player.hasPermission("armoredelytra.give.chain")) 
						{
							if (elytraReceivedMessage != null)
							{
								player.sendMessage(elytraReceivedMessage);
							}
							newElytra = nbtEditor.addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), 3);
						} else 
						{
							player.sendMessage("You do not have the required permission node for this armor tier!");
						}
					
					// Iron armor.
					} else if (tier.equalsIgnoreCase("iron"))
					{
						if (player.hasPermission("armoredelytra.give.iron")) 
						{
							if (elytraReceivedMessage != null)
							{
								player.sendMessage(elytraReceivedMessage);
							}
							newElytra = nbtEditor.addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), 4);
						} else 
						{
							player.sendMessage("You do not have the required permission node for this armor tier!");
						}
					
					// Diamond armor.
					} else if (tier.equalsIgnoreCase("diamond"))
					{
						if (player.hasPermission("armoredelytra.give.diamond")) 
						{
							if (elytraReceivedMessage != null)
							{
								player.sendMessage(elytraReceivedMessage);
							}
							newElytra = nbtEditor.addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), 5);
						} else 
						{
							player.sendMessage("You do not have the required permission node for this armor tier!");
						}
					
					} else 
					{
						player.sendMessage("Not a supported armor tier! Try one of these: leather, gold, chain, iron, diamond.");
					}
					giveArmoredElytraToPlayer(player, newElytra);
					return true;
				}
			}
		} else 
		{
			if (args.length == 2) 
			{
				ItemStack newElytra = null;
				String tier = args[1];
				if (Bukkit.getPlayer(args[0]) != null)
				{
					player = Bukkit.getPlayer(args[0]);
					
					// Leather armor tier.
					if (tier.equalsIgnoreCase("leather"))
					{
						if (elytraReceivedMessage != null)
						{
							player.sendMessage(elytraReceivedMessage);
						}
						Bukkit.getLogger().log(Level.INFO, "Giving an armored elytra of the leather armor tier to player "+player.getName());
						newElytra = nbtEditor.addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), 1);
					// Gold armor tier.
					} else if (tier.equalsIgnoreCase("gold"))
					{
						if (elytraReceivedMessage != null)
						{
							player.sendMessage(elytraReceivedMessage);
						}
						Bukkit.getLogger().log(Level.INFO, "Giving an armored elytra of the gold armor tier to player "+player.getName());
						newElytra = nbtEditor.addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), 2);
					// Chain armor tier.
					} else if (tier.equalsIgnoreCase("chain"))
					{
						if (elytraReceivedMessage != null)
						{
							player.sendMessage(elytraReceivedMessage);
						}
						Bukkit.getLogger().log(Level.INFO, "Giving an armored elytra of the chain armor tier to player "+player.getName());
						newElytra = nbtEditor.addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), 3);
					// Iron armor tier.
					} else if (tier.equalsIgnoreCase("iron"))
					{
						if (elytraReceivedMessage != null)
						{
							player.sendMessage(elytraReceivedMessage);
						}
						Bukkit.getLogger().log(Level.INFO, "Giving an armored elytra of the iron armor tier to player "+player.getName());
						newElytra = nbtEditor.addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), 4);
					// Diamond armor tier.
					} else if (tier.equalsIgnoreCase("diamond"))
					{
						if (elytraReceivedMessage != null)
						{
							player.sendMessage(elytraReceivedMessage);
						}
						Bukkit.getLogger().log(Level.INFO, "Giving an armored elytra of the armor armor tier to player "+player.getName());
						newElytra = nbtEditor.addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), 5);
					}
					giveArmoredElytraToPlayer(player, newElytra);
					return true;					
				} else 
				{
					Bukkit.getLogger().log(Level.INFO, "Player "+args[1]+" not found!");
					return true;
				}
			}
		}
		return false;
    }
	
	
	// Give the provided player the provided item.
	public void giveArmoredElytraToPlayer(Player player, ItemStack item)
	{
		if (item != null)
		{
			player.getInventory().addItem(item);
		}
	}
	
	
	// Check + initialize for the correct version of Minecraft.
	public boolean compatibleMCVer()
	{
        String version;

        try 
        {
            version = Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];
        } catch (ArrayIndexOutOfBoundsException whatVersionAreYouUsingException) 
        {
            return false;
        }

        if (version.equals("v1_11_R1")) 
        {
        		nbtEditor = new NBTEditor_V1_11_R1();

        } else if (version.equals("v1_12_R1")) 
        {
        		nbtEditor = new NBTEditor_V1_12_R1();
        }
        // Return true if compatible.
        return nbtEditor != null;
	}
}