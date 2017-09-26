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

import net.md_5.bungee.api.ChatColor;
import nl.pim16aap2.armoredElytra.nms.NBTEditor;
import nl.pim16aap2.armoredElytra.nms.NBTEditor_V1_11_R1;
import nl.pim16aap2.armoredElytra.nms.NBTEditor_V1_12_R1;
import nl.pim16aap2.armoredElytra.util.Update;
 
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
	private boolean checkForUpdates;
	private boolean upToDate;
	
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
		config.addDefault("usageDeniedMessage", "You do not have the required permissions to wear %ARMOR_TIER% armored elytras!");
		config.addDefault("elytraReceivedMessage", "A(n) %ARMOR_TIER% armored elytra has been bestowed upon you!");
		config.addDefault("checkForUpdates", true);
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
		checkForUpdates       = config.getBoolean("checkForUpdates");

		usageDeniedMessage    = (Objects.equals(usageDeniedMessage,    new String("NONE")) ? null : usageDeniedMessage);
		elytraReceivedMessage = (Objects.equals(elytraReceivedMessage, new String("NONE")) ? null : elytraReceivedMessage);

		
		// Check if the user allows checking for updates. 
		if (checkForUpdates)
		{
			Bukkit.getPluginManager().registerEvents(new LoginHandler(this), this);
			
			Update update        = new Update(278437, this);
			
			String latestVersion = update.getLatestVersion();
			String thisVersion   = this.getDescription().getVersion();
			int updateStatus     = update.versionCompare(latestVersion, thisVersion);
			
			if (updateStatus > 0)
			{
				myLogger(Level.INFO, "Plugin out of date! You are using version "+thisVersion+" but the latest version is version "+latestVersion+"!");
				this.upToDate = false;
			}else 
			{
				this.upToDate = true;
				myLogger(Level.INFO, "You seem to be using the latest version of this plugin!");
			}
		}
		
		config.options().copyDefaults(true);
		saveConfig();

		myLogger(Level.INFO, ("Allowed enchantments:"));
		for (String s : allowedEnchants)
		{
			myLogger(Level.INFO, s);
		}
		
		if (compatibleMCVer()) 
		{
			Bukkit.getPluginManager().registerEvents(new EventHandlers(this, nbtEditor, cursesAllowed, LEATHER_TO_FULL, GOLD_TO_FULL, IRON_TO_FULL, DIAMONDS_TO_FULL, allowedEnchants), this);
		} else {
			myLogger(Level.WARNING, "Trying to load the plugin on an incompatible version of Minecraft!");
		}
	}
	
	
	public boolean isUpToDate()
	{
		return upToDate;
	}
	
	// Send a message to a player in a specific color.
	public void messagePlayer(Player player, ChatColor color, String s)
	{
		player.sendMessage(color + s);
	}
	
	// Send a message to a player.
	public void messagePlayer(Player player, String s)
	{
		messagePlayer(player, ChatColor.WHITE, s);
	}
	
	public String armorTierToString(int armorTier)
	{
		String armorTierName = null;
		switch(armorTier)
		{
		case 0:
			armorTierName = "unarmored";
		case 1:
			armorTierName = "leather";
		case 2:
			armorTierName = "gold";
		case 3:
			armorTierName = "chain";
		case 4:
			armorTierName = "iron";
		case 5:
			armorTierName = "diamond";
		}
		
		return armorTierName;
	}
	
	// Send the usageDeniedMessage message to the player.
	public void usageDeniedMessage(Player player, int armorTier)
	{
		if (usageDeniedMessage != null)
		{
			String armorTierName = armorTierToString(armorTier);
			String message       = usageDeniedMessage.replace("%ARMOR_TIER%", armorTierName);
			messagePlayer(player, ChatColor.RED, message);
		}
	}
	
	// Send the elytraReceivedMessage message to the player.
	public void elytraReceivedMessage(Player player, int armorTier)
	{
		if (elytraReceivedMessage != null)
		{
			String armorTierName  = armorTierToString(armorTier);
			String message        = elytraReceivedMessage.replace("%ARMOR_TIER%", armorTierName);
			messagePlayer(player, ChatColor.GREEN, message);
		}
	}
	
	// Print a string to the log.
	public void myLogger(Level level, String s)
	{
		Bukkit.getLogger().log(level, "["+this.getName()+"] " + s);
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
				if (args.length == 1 || args.length == 2) 
				{
					ItemStack newElytra = null;
					String tier = null;
					Player receiver;
					boolean allowed = false;
					int armorTier = 0;
					if (args.length == 1)
					{
						receiver = player;
						tier = args[0];
					} else 
					{
						receiver = Bukkit.getPlayer(args[0]);
						if (receiver == null)
						{
							messagePlayer(player, ChatColor.RED, "Player \""+args[0]+"\" not found!");
							return true;
						}
						tier = args[1];
					}
					// Leather armor.
					if (tier.equalsIgnoreCase("leather"))
					{
						if (player.hasPermission("armoredelytra.give.leather")) 
						{
							armorTier = 1;
							allowed   = true;
						}
						
					// Gold armor.
					} else if (tier.equalsIgnoreCase("gold"))
					{
						if (player.hasPermission("armoredelytra.give.gold")) 
						{
							armorTier = 2;
							allowed   = true;
						}
						
					// Chain armor.
					} else if (tier.equalsIgnoreCase("chain"))
					{
						if (player.hasPermission("armoredelytra.give.chain")) 
						{
							armorTier = 3;
							allowed   = true;
						}
					
					// Iron armor.
					} else if (tier.equalsIgnoreCase("iron"))
					{
						if (player.hasPermission("armoredelytra.give.iron")) 
						{
							armorTier = 4;
							allowed   = true;
						}
					
					// Diamond armor.
					} else if (tier.equalsIgnoreCase("diamond"))
					{
						if (player.hasPermission("armoredelytra.give.diamond")) 
						{
							armorTier = 5;
							allowed   = true;
						}
					} else 
					{
						messagePlayer(player, "Not a supported armor tier! Try one of these: leather, gold, chain, iron, diamond.");
					}
					if (allowed)
					{
						elytraReceivedMessage(receiver, armorTier);
						newElytra = nbtEditor.addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), armorTier);
						giveArmoredElytraToPlayer(receiver, newElytra);
					} else 
					{
						messagePlayer(player, "You do not have the required permission node to give "+ armorTierToString(armorTier) + " armored elytras.");
					}
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
					int armorTier = 0;
					
					// Leather armor tier.
					if (tier.equalsIgnoreCase("leather"))
					{
						armorTier = 1;
					// Gold armor tier.
					} else if (tier.equalsIgnoreCase("gold"))
					{
						armorTier = 2;
					// Chain armor tier.
					} else if (tier.equalsIgnoreCase("chain"))
					{
						armorTier = 3;
					// Iron armor tier.
					} else if (tier.equalsIgnoreCase("iron"))
					{
						armorTier = 4;
					// Diamond armor tier.
					} else if (tier.equalsIgnoreCase("diamond"))
					{
						armorTier = 5;
					}

					elytraReceivedMessage(player, armorTier);
					newElytra = nbtEditor.addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), armorTier);
					giveArmoredElytraToPlayer(player, newElytra);
					myLogger(Level.INFO, ("Giving an armored elytra of the "+ armorTierToString(armorTier) +" armor tier to player "+player.getName()));
					return true;					
				} else 
				{
					myLogger(Level.INFO, ("Player "+args[1]+" not found!"));
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