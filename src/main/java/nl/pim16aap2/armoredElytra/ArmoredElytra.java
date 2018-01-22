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
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.Metrics;
import nl.pim16aap2.armoredElytra.util.Update;
 
public class ArmoredElytra extends JavaPlugin implements Listener 
{
	private NBTEditor          nbtEditor;
	private ConfigLoader          config;

	private String    usageDeniedMessage;
	private String elytraReceivedMessage;
	private String            elytraName;
	private String            elytraLore;
	private boolean             upToDate;
	private boolean        uninstallMode;
	
	@Override
    public void onEnable()
	{
		// Load the settings from the config file.
		config                   = new ConfigLoader(this);

		// Replace color codes by the corresponding colors.
		usageDeniedMessage       = config.getString("usageDeniedMessage"      ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
		elytraReceivedMessage    = config.getString("elytraReceivedMessage"   ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
		elytraName               = config.getString("elytraName"              ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
		elytraLore               = config.getString("elytraLore"              ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
		
		// Change the string to null if it says "NONE".
		usageDeniedMessage       = (Objects.equals(usageDeniedMessage,    new String("NONE")) ? null : usageDeniedMessage   );
		elytraReceivedMessage    = (Objects.equals(elytraReceivedMessage, new String("NONE")) ? null : elytraReceivedMessage);
		elytraLore               = (Objects.equals(elytraLore,            new String("NONE")) ? null : elytraLore           );
		
		// Check if the plugin should go into uninstall mode.
		uninstallMode = config.getBool("uninstallMode");
		
		// Check if the user allows checking for updates. 
		if (config.getBool("checkForUpdates"))
		{	
			Update update        = new Update(278437, this);
			
			String latestVersion = update.getLatestVersion();
			String thisVersion   = this.getDescription().getVersion();
			// Check if this is the latest version or not.
			int updateStatus     = update.versionCompare(latestVersion, thisVersion);
			
			if (updateStatus > 0)
			{
				// TODO: Insert download link to latest version.
				// TODO: Use Spiget instead of the unreliable BukkitDev's update stuff?
				// TODO: Add auto update option?
				
				// Load the loginHandler to show messages to the user when they join.
				Bukkit.getPluginManager().registerEvents(new LoginHandler(this, "The Armored Elytra plugin is out of date!"), this);
				myLogger(Level.INFO, "Plugin out of date! You are using version " + thisVersion + " but the latest version is version " + latestVersion + "!");
				this.upToDate = false;
			}
			else 
			{
				this.upToDate = true;
				myLogger(Level.INFO, "You seem to be using the latest version of this plugin!");
			}
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
			myLogger(Level.INFO, "Stats disabled, not laoding stats ::(... Please consider enabling it! I am a simple man, seeing higher user numbers helps me stay motivated!");

		
		
		
		// Load the files for the correct version of Minecraft.
		if (compatibleMCVer()) 
		{
			Bukkit.getPluginManager().registerEvents(new EventHandlers(this, nbtEditor), this);
			getCommand("ArmoredElytra").setExecutor(new CommandHandler(this, nbtEditor, uninstallMode));
		} 
		else
			myLogger(Level.WARNING, "Trying to load the plugin on an incompatible version of Minecraft!");
		
		
		
		
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
			// Log whether or not curses are allowed.
			myLogger(Level.INFO, "Curses on armored elytras are " + (config.getBool("allowCurses") ? "" : "not " + "allowed!"));
		} 
		else
		{
			myLogger(Level.WARNING, "Plugin in uninstall mode!");
			Bukkit.getPluginManager().registerEvents(new Uninstaller(this, nbtEditor), this);
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
	
	// Send the usageDeniedMessage message to the player.
	public void usageDeniedMessage(Player player, ArmorTier armorTier)
	{
		if (usageDeniedMessage != null)
		{
			String message = fillInArmorTierInString(usageDeniedMessage, armorTier);
			messagePlayer(player, ChatColor.RED, message);
		}
	}
	
	// Send the elytraReceivedMessage message to the player.
	public void elytraReceivedMessage(Player player, ArmorTier armorTier)
	{
		if (elytraReceivedMessage != null)
		{
			String message = fillInArmorTierInString(elytraReceivedMessage, armorTier);
			messagePlayer(player, ChatColor.GREEN, message);
		}
	}
	
	// Replace %ARMOR_TIER% by the name of that armor tier in a string.
	public String fillInArmorTierInString(String string, ArmorTier armorTier)
	{
		return string.replace("%ARMOR_TIER%", ArmorTier.getArmorName(armorTier));
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
        catch (ArrayIndexOutOfBoundsException whatVersionAreYouUsingException) 
        {
            return false;
        }

        if (version.equals("v1_10_R1"))
        		nbtEditor = new NBTEditor_V1_10_R1(elytraName, elytraLore, this);
        else if (version.equals("v1_11_R1"))
    			nbtEditor = new NBTEditor_V1_11_R1(elytraName, elytraLore, this);
        else if (version.equals("v1_12_R1"))
    			nbtEditor = new NBTEditor_V1_12_R1(elytraName, elytraLore, this);
        // Return true if compatible.
        return nbtEditor != null;
	}
}