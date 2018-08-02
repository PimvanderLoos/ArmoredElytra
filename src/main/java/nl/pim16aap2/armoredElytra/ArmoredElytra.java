package nl.pim16aap2.armoredElytra;

import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import nl.pim16aap2.armoredElytra.handlers.CommandHandler;
import nl.pim16aap2.armoredElytra.handlers.EventHandlers;
import nl.pim16aap2.armoredElytra.handlers.FlyDurabilityHandler;
import nl.pim16aap2.armoredElytra.handlers.LoginHandler;
import nl.pim16aap2.armoredElytra.handlers.Uninstaller;
import nl.pim16aap2.armoredElytra.nms.NBTEditor;
import nl.pim16aap2.armoredElytra.nms.NBTEditor_V1_10_R1;
import nl.pim16aap2.armoredElytra.nms.NBTEditor_V1_11_R1;
import nl.pim16aap2.armoredElytra.nms.NBTEditor_V1_12_R1;
import nl.pim16aap2.armoredElytra.nms.NBTEditor_V1_13_R1;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.Metrics;
import nl.pim16aap2.armoredElytra.util.Update;
 
public class ArmoredElytra extends JavaPlugin implements Listener 
{
	private NBTEditor           nbtEditor;
	private ConfigLoader           config;

	private String     usageDeniedMessage;
	private String  elytraReceivedMessage;
	private String             elytraLore;
	private boolean              upToDate;
	private boolean         uninstallMode;
	private String leatherName,  ironName,  goldName,  chainName,  diamondName;
	
	@Override
    public void onEnable()
	{
		readConfigValues();
		
		// Check if the user allows checking for updates. 
		if (config.getBool("checkForUpdates"))
		{
			// Check for updates in a new thread, so the server won't hang when it cannot contact the update servers.
			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					ArmoredElytra plugin = getPlugin();
					Update update        = new Update(278437, plugin);
					String latestVersion = update.getLatestVersion();
					
					if (latestVersion == null)
						plugin.myLogger(Level.WARNING, "Encountered problem contacting update servers! Please check manually! The error above does not affect the plugin!");
					else
					{
						String thisVersion   = plugin.getDescription().getVersion();
						// Check if this is the latest version or not.
						int updateStatus     = update.versionCompare(latestVersion, thisVersion);
						
						if (updateStatus > 0)
						{
							// TODO: Insert download link to latest version.
							// TODO: Use Spiget as backup?
							// TODO: Add auto update option?
							
							// Load the loginHandler to show messages to the user when they join.
							Bukkit.getPluginManager().registerEvents(new LoginHandler(plugin, "The Armored Elytra plugin is out of date!"), plugin);
							plugin.myLogger(Level.INFO, "Plugin out of date! You are using version " + thisVersion + " but the latest version is version " + latestVersion + "!");
							plugin.setUpToDate(false);
						}
						else 
						{
							plugin.setUpToDate(true);
							plugin.myLogger(Level.INFO, "You seem to be using the latest version of this plugin!");
						}
					}
				}
		
			});
			thread.start();
		} 
		else 
			myLogger(Level.INFO, "Plugin update checking not enabled! You will not receive any messages about new updates for this plugin. Please consider turning this on in the config.");
		
		
		
		
		// Are stats allowed?
		if (config.getBool("allowStats"))
		{
			myLogger(Level.INFO, "Enabling stats! Thanks, it really helps!");
			@SuppressWarnings("unused")
			Metrics metrics = new Metrics(this);
		} 
		else 
			// Y u do dis? :(
			myLogger(Level.INFO, "Stats disabled, not laoding stats :(... Please consider enabling it! I am a simple man, seeing higher user numbers helps me stay motivated!");

		
		
		
		// Load the files for the correct version of Minecraft.
		if (compatibleMCVer()) 
		{
			Bukkit.getPluginManager().registerEvents(new EventHandlers(this, nbtEditor), this);
			getCommand("ArmoredElytra").setExecutor(new CommandHandler(this, nbtEditor));
		} 
		else
		{
			myLogger(Level.WARNING, "Trying to load the plugin on an incompatible version of Minecraft! This plugin will NOT be enabled!");
			return;
		}
		
		
		
		
		// Load the plugin normally if not in uninstall mode.
		if (!uninstallMode)
		{
			// Check if the user wants to disable durability penalty for flying with an armored elytra.
			if (config.getBool("noFlightDurability"))
			{
				Bukkit.getPluginManager().registerEvents(new FlyDurabilityHandler(nbtEditor), this);
				myLogger(Level.INFO, "Durability penalty for flying disabled!");
			} else
				myLogger(Level.INFO, "Durability penalty for flying enabled!");
			
			
			// Log all allowed enchantments.
			myLogger(Level.INFO, ("Allowed enchantments:"));
			for (String s : config.getStringList("allowedEnchantments"))
				myLogger(Level.INFO, " - " + s);
		} 
		else
		{
			myLogger(Level.WARNING, "Plugin in uninstall mode!");
			Bukkit.getPluginManager().registerEvents(new Uninstaller(this, nbtEditor), this);
		}
	}
	
	public void readConfigValues()
	{
		// Load the settings from the config file.
		this.config                 	= new ConfigLoader(this);

		// Replace color codes by the corresponding colors.
		this.usageDeniedMessage     	= config.getString("usageDeniedMessage"      ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
		this.elytraReceivedMessage  	= config.getString("elytraReceivedMessage"   ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
		this.elytraLore             	= config.getString("elytraLore"              ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");

		this.leatherName 			= config.getString("leatherName"      		).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
		this.goldName 				= config.getString("goldName"      			).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
		this.chainName 				= config.getString("chainName"      			).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
		this.ironName 				= config.getString("ironName"      			).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
		this.diamondName 			= config.getString("diamondName"      		).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
		
		// Change the string to null if it says "NONE".
		this.usageDeniedMessage     	= (Objects.equals(usageDeniedMessage,    new String("NONE")) ? null : usageDeniedMessage   );
		this.elytraReceivedMessage  	= (Objects.equals(elytraReceivedMessage, new String("NONE")) ? null : elytraReceivedMessage);
		this.elytraLore             	= (Objects.equals(elytraLore,            new String("NONE")) ? null : elytraLore           );
		
		// Check if the plugin should go into uninstall mode.
		this.uninstallMode          	= config.getBool("uninstallMode");
	}
	
	// Returns true if this is the latest version of this plugin.
	public boolean isUpToDate()
	{
		return upToDate;
	}
	
	// Get this.
	public ArmoredElytra getPlugin()
	{
		return this;
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
	
	// Send the usageDeniedMessage message to the player.
	public void usageDeniedMessage(Player player, ArmorTier armorTier)
	{
		if (usageDeniedMessage != null)
		{
			String message = fillInArmorTierInStringNoColor(usageDeniedMessage, armorTier);
			messagePlayer(player, ChatColor.RED, message);
		}
	}
	
	// Send the elytraReceivedMessage message to the player.
	public void elytraReceivedMessage(Player player, ArmorTier armorTier)
	{
		if (elytraReceivedMessage != null)
		{
			String message = fillInArmorTierInStringNoColor(elytraReceivedMessage, armorTier);
			messagePlayer(player, ChatColor.GREEN, message);
		}
	}
	
	// Replace %ARMOR_TIER% by the name of that armor tier in a string, but strip %ARMOR_TIER% of its color.
	public String fillInArmorTierInStringNoColor(String string, ArmorTier armorTier)
	{
		return string.replace("%ARMOR_TIER%", ChatColor.stripColor(getArmoredElytrName(armorTier)));
	}
	
	// Print a string to the log.
	public void myLogger(Level level, String str)
	{
		Bukkit.getLogger().log(level, "[" + this.getName() + "] " + str);
	}
	
	// Log message that only gets printed when debugging is enabled in the config file.
	public void debugMsg(Level level, String str)
	{
		if (config.getBool("enableDebug"))
			myLogger(level, str);
	}
	
	// Give the provided player the provided item.
	public void giveArmoredElytraToPlayer(Player player, ItemStack item)
	{
		if (item != null)
			player.getInventory().addItem(item);
	}
	
	// Check + initialize for the correct version of Minecraft.
	public boolean compatibleMCVer()
	{
        String version;

        try 
        {
            version = Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];
        } 
        catch (ArrayIndexOutOfBoundsException useAVersionMentionedInTheDescriptionPleaseException) 
        {
            return false;
        }

        if (version.equals("v1_10_R1"))
        		nbtEditor = new NBTEditor_V1_10_R1(this);
        else if (version.equals("v1_11_R1"))
    			nbtEditor = new NBTEditor_V1_11_R1(this);
        else if (version.equals("v1_12_R1"))
        		nbtEditor = new NBTEditor_V1_12_R1(this);
        else if (version.equals("v1_13_R1"))
    			nbtEditor = new NBTEditor_V1_13_R1(this);
        // Return true if compatible.
        return nbtEditor != null;
	}
	
	public String getElytraLore()
	{
		return this.elytraLore;
	}
	
	public String getArmoredElytrName(ArmorTier tier)
	{
		String ret;
		switch(tier)
		{
		case LEATHER:
			ret = this.leatherName;
			break;
		case GOLD:
			ret = this.goldName;
			break;
		case CHAIN:
			ret = this.chainName;
			break;
		case IRON:
			ret = this.ironName;
			break;
		case DIAMOND:
			ret = this.diamondName;
			break;
		default:
			ret = "NONE";
		}
		return ret;
	}
	
	public boolean playerHasCraftPerm(Player player, ArmorTier tier)
	{
		switch (tier)
		{
		case LEATHER:
			return player.hasPermission("armoredelytra.craft.leather");
		case GOLD:
			return player.hasPermission("armoredelytra.craft.gold");
		case CHAIN:
			return player.hasPermission("armoredelytra.craft.chain");
		case IRON:
			return player.hasPermission("armoredelytra.craft.iron");
		case DIAMOND:
			return player.hasPermission("armoredelytra.craft.diamond");
		default:
			return false;
		}
	}


	public void setUpToDate(boolean upToDate)
	{
		this.upToDate = upToDate;
	}

	public boolean getUninstallMode()
	{
		return uninstallMode;
	}
}