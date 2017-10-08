package nl.pim16aap2.armoredElytra;

import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import nl.pim16aap2.armoredElytra.handlers.EventHandlers;
import nl.pim16aap2.armoredElytra.handlers.LoginHandler;
import nl.pim16aap2.armoredElytra.nms.NBTEditor;
import nl.pim16aap2.armoredElytra.nms.NBTEditor_V1_11_R1;
import nl.pim16aap2.armoredElytra.nms.NBTEditor_V1_12_R1;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.Metrics;
import nl.pim16aap2.armoredElytra.util.Update;
 
public class ArmoredElytra extends JavaPlugin implements Listener 
{
	private NBTEditor nbtEditor;

	private String usageDeniedMessage;
	private String elytraReceivedMessage;
	private String elytraName;
	private String elytraLore;
	private boolean upToDate;
	
	private ConfigLoader config;
	
	@Override
    public void onEnable() 
	{	
		config = new ConfigLoader(this);

		// Replace color codes by the corresponding colors.
		usageDeniedMessage    = config.getString("usageDeniedMessage").replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
		elytraReceivedMessage = config.getString("elytraReceivedMessage").replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
		elytraName            = config.getString("elytraName").replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
		elytraLore            = config.getString("elytraLore").replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
		
		// Change the string to null if it says "NONE".
		usageDeniedMessage    = (Objects.equals(usageDeniedMessage,    new String("NONE")) ? null : usageDeniedMessage);
		elytraReceivedMessage = (Objects.equals(elytraReceivedMessage, new String("NONE")) ? null : elytraReceivedMessage);
		elytraLore            = (Objects.equals(elytraLore,            new String("NONE")) ? null : elytraLore);
		
		
		
		// Check if the user allows checking for updates. 
		if (config.getBool("checkForUpdates"))
		{
			// Load the loginHandler to show messages to the user when they join.
			Bukkit.getPluginManager().registerEvents(new LoginHandler(this), this);
			
			Update update        = new Update(278437, this);
			
			String latestVersion = update.getLatestVersion();
			String thisVersion   = this.getDescription().getVersion();
			// Check if this is the latest version or not.
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
		
		// Are stats allowed?
		if (config.getBool("allowStats"))
		{
			myLogger(Level.INFO, "Enabling stats!");
			@SuppressWarnings("unused")
			Metrics metrics = new Metrics(this);
		} else 
			// Y u do dis? :(
			myLogger(Level.INFO, "Stats disabled, not laoding stats :(");

		// Log all allowed enchantments.
		myLogger(Level.INFO, ("Allowed enchantments:"));
		for (String s : config.getStringList("allowedEnchantments"))
		{
			myLogger(Level.INFO, " - " + s);
		}
		// Log whether or not curses are allowed.
		myLogger(Level.INFO, "Curses on armored elytras are " + (config.getBool("allowCurses") ? "" : "not " + "allowed!"));
		
		// Load the files for the correct version of Minecraft.
		if (compatibleMCVer()) 
		{
			Bukkit.getPluginManager().registerEvents(new EventHandlers(this, nbtEditor), this);
		} else {
			myLogger(Level.WARNING, "Trying to load the plugin on an incompatible version of Minecraft!");
		}
	}
	
	// Returns true if this is the latest version of this plugin.
	public boolean isUpToDate()
	{
		return upToDate;
	}
	
	// Returns the config handler.
	public ConfigLoader getConfigLoader()
	{
		return config;
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
	
	// Convert int of armorTier to its string. 
	public String armorTierToString(int armorTier)
	{
		String armorTierName = null;
		switch(armorTier)
		{
		case 0:
			armorTierName = "Unarmored";
			break;
		case 1:
			armorTierName = "Leather";
			break;
		case 2:
			armorTierName = "Gold";
			break;
		case 3:
			armorTierName = "Chain";
			break;
		case 4:
			armorTierName = "Iron";
			break;
		case 5:
			armorTierName = "Diamond";
			break;
		}
		return armorTierName;
	}
	
	// Send the usageDeniedMessage message to the player.
	public void usageDeniedMessage(Player player, int armorTier)
	{
		if (usageDeniedMessage != null)
		{
			String message = fillInArmorTierInString(usageDeniedMessage, armorTier);
			messagePlayer(player, ChatColor.RED, message);
		}
	}
	
	// Send the elytraReceivedMessage message to the player.
	public void elytraReceivedMessage(Player player, int armorTier)
	{
		if (elytraReceivedMessage != null)
		{
			String message = fillInArmorTierInString(elytraReceivedMessage, armorTier);
			messagePlayer(player, ChatColor.GREEN, message);
		}
	}
	
	// Replace %ARMOR_TIER% by the name of that armor tier in a string.
	public String fillInArmorTierInString(String string, int armorTier)
	{
		String armorTierName  = armorTierToString(armorTier);
		String replaced       = string.replace("%ARMOR_TIER%", armorTierName);
		return replaced;
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
						armorTier = 1;
						if (player.hasPermission("armoredelytra.give.leather"))
							allowed   = true;
						
					// Gold armor.
					} else if (tier.equalsIgnoreCase("gold"))
					{
						armorTier = 2;
						if (player.hasPermission("armoredelytra.give.gold"))
							allowed   = true;
						
					// Chain armor.
					} else if (tier.equalsIgnoreCase("chain"))
					{
						armorTier = 3;
						if (player.hasPermission("armoredelytra.give.chain")) 
							allowed   = true;
					
					// Iron armor.
					} else if (tier.equalsIgnoreCase("iron"))
					{
						armorTier = 4;
						if (player.hasPermission("armoredelytra.give.iron"))
							allowed   = true;
					
					// Diamond armor.
					} else if (tier.equalsIgnoreCase("diamond"))
					{
						armorTier = 5;
						if (player.hasPermission("armoredelytra.give.diamond"))
							allowed   = true;
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
        		nbtEditor = new NBTEditor_V1_11_R1(elytraName, elytraLore, this);

        } else if (version.equals("v1_12_R1")) 
        {
        		nbtEditor = new NBTEditor_V1_12_R1(elytraName, elytraLore, this);
        }
        // Return true if compatible.
        return nbtEditor != null;
	}
}