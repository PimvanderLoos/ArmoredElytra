package nl.pim16aap2.armoredElytra.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import nl.pim16aap2.armoredElytra.ArmoredElytra;

public class ConfigLoader
{
	private boolean      unbreakable;
	private boolean      noFlightDurability;
	private int          LEATHER_TO_FULL;
	private int          GOLD_TO_FULL;
	private int          IRON_TO_FULL;
	private int          DIAMONDS_TO_FULL;
	private boolean      cursesAllowed;
	private List<String> allowedEnchantments;
	private String       usageDeniedMessage;
	private String       elytraReceivedMessage;
	private boolean      checkForUpdates;
	private boolean      allowStats;
	private boolean      enableDebug;
	private boolean      uninstallMode;
	private String       elytraName;
	private String       elytraLore;
	
	// All the comments for the various config options.
	private String[] unbreakableComment    =
		{"Setting this to true will cause armored elytras to be unbreakable.",
		 "Changing this to false will NOT make unbreakable elytras breakable again!"};
	private String[] flyDurabilityComment  =
		{"Setting this to true will cause armored elytras to not lose any durability while flying.",
		 "This is not a permanent option and will affect ALL elytras."};
    private String[] repairComment         = 
		{"Amount of items it takes to fully repair an armored elytra",
		 "Repair cost for every tier of armored elytra in number of items to repair 100%."};
    private String[] cursesComment         = 
		{"Will curses (vanishing, binding) be transferred when creating armored elytras?"};
    private String[] enchantmentsComment   = 
		{"List of enchantments that are allowed to be put on an armored elytra.",
		 "If you do not want to allow any enchantments, remove them all and add \"NONE\"",
		 "You can find supported enchantments here:",
		 "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/enchantments/Enchantment.html"};
    private String[] usageDeniedComment    = 
		{"Message players receive when they lack the required permissions to wear a certain armor tier. \"NONE\" = no message. ",
		 "%ARMOR_TIER% is replaced by the name of the armor tier."};
    private String[] elytraReceivedComment = 
		{"Message players receive when they are given an armored elytra using commands. \"NONE\" = no message. ",
		 "%ARMOR_TIER% is replaced by the name of the armor tier."};
    private String[] elytraNameComment     = 
		{"The name of armored elytras. %ARMOR_TIER% is replaced by the name of the armor tier."};
    private String[] elytraLoreComment     = 
		{"The lore of armored elytras. \"NONE\" = no lore. %ARMOR_TIER% is replaced by the name of the armor tier."};
    private String[] updateComment         = 
		{"Allow this plugin to check for updates on startup. It will not download new versions!"};
    private String[] bStatsComment         = 
		{"Allow this plugin to send (anonymised) stats using bStats."};
    private String[] debugComment          =
    		{"Print debug messages to console. You will most likely never need this."};
    private String[] uninstallComment      =
	    	{"Setting this to true will disable this plugin and remove any armored elytras it can find.",
		 "It will check player's inventories and their end chest upon login and any regular chest when it is opened.",
		 "This means it will take a while for all armored elytras to be removed from your server, but it doesn't take up ",
		 "a lot of resources, so you can just leave it installed and ignore it.",
		 "Please do not forget to MAKE A BACKUP before enabling this option!"};
	
	private ArrayList<ConfigOption> configOptionsList;
    private ArmoredElytra plugin;
	
	public ConfigLoader(ArmoredElytra plugin) 
	{
		this.plugin = plugin;
		configOptionsList = new ArrayList<ConfigOption>();
		makeConfig();
	}
	
	// Read the current config, the make a new one based on the old one or default values, whichever is applicable.
	public void makeConfig()
	{
		FileConfiguration config = plugin.getConfig();
		
		// Read all the options from the config, then put them in a configOption with their name, value and comment.
		// THen put all configOptions into an ArrayList.
		unbreakable           = config.getBoolean("unbreakable", false);
		configOptionsList.add(new ConfigOption   ("unbreakable", unbreakable, unbreakableComment));
		noFlightDurability    = config.getBoolean("noFlightDurability", false);
		configOptionsList.add(new ConfigOption   ("noFlightDurability", noFlightDurability, flyDurabilityComment));
		
		LEATHER_TO_FULL       = config.getInt ("leatherRepair", 6);
		configOptionsList.add(new ConfigOption("leatherRepair", LEATHER_TO_FULL, repairComment));
		GOLD_TO_FULL          = config.getInt ("goldRepair", 5);
		configOptionsList.add(new ConfigOption("goldRepair", GOLD_TO_FULL));
		IRON_TO_FULL          = config.getInt ("ironRepair", 4);
		configOptionsList.add(new ConfigOption("ironRepair", IRON_TO_FULL));
		DIAMONDS_TO_FULL      = config.getInt ("diamondsRepair", 3);
		configOptionsList.add(new ConfigOption("diamondsRepair", DIAMONDS_TO_FULL));
		
		cursesAllowed         = config.getBoolean   ("allowCurses", true);
		configOptionsList.add(new ConfigOption      ("allowCurses", cursesAllowed, cursesComment));
		allowedEnchantments   = config.getStringList("allowedEnchantments");
		configOptionsList.add(new ConfigOption      ("allowedEnchantments", allowedEnchantments, enchantmentsComment));
		
		usageDeniedMessage    = config.getString("usageDeniedMessage");
		configOptionsList.add(new ConfigOption  ("usageDeniedMessage", usageDeniedMessage, usageDeniedComment));
		elytraReceivedMessage = config.getString("elytraReceivedMessage");
		configOptionsList.add(new ConfigOption  ("elytraReceivedMessage", elytraReceivedMessage, elytraReceivedComment));
		elytraName            = config.getString("elytraName");
		configOptionsList.add(new ConfigOption  ("elytraName", elytraName, elytraNameComment));
		elytraLore            = config.getString("elytraLore");
		configOptionsList.add(new ConfigOption  ("elytraLore", elytraLore, elytraLoreComment));
		
		checkForUpdates       = config.getBoolean("checkForUpdates", true);
		configOptionsList.add(new ConfigOption   ("checkForUpdates", checkForUpdates, updateComment));
		allowStats            = config.getBoolean("allowStats", true);
		configOptionsList.add(new ConfigOption   ("allowStats", allowStats, bStatsComment));
		enableDebug           = config.getBoolean("enableDebug", false);
		configOptionsList.add(new ConfigOption   ("enableDebug", enableDebug, debugComment));
		uninstallMode         = config.getBoolean("uninstallMode", false);
		configOptionsList.add(new ConfigOption   ("uninstallMode", uninstallMode, uninstallComment));
		
		writeConfig();
	}
	
	// Write new config file.
	public void writeConfig()
	{
		// Write all the config options to the config.yml.
		try
		{
			File dataFolder = plugin.getDataFolder();
			if (!dataFolder.exists())
				dataFolder.mkdir();

			File saveTo = new File(plugin.getDataFolder(), "config.yml");
			if (!saveTo.exists())
				saveTo.createNewFile();
			else
			{
				saveTo.delete();
				saveTo.createNewFile();
			}
			FileWriter  fw = new FileWriter(saveTo, true);
			PrintWriter pw = new PrintWriter(fw);
			
			for (ConfigOption configOption : configOptionsList)
				pw.println(configOption.toString());
			 
			pw.flush();
			pw.close();
		} catch (IOException e)
		{
			Bukkit.getLogger().log(Level.SEVERE, "Could not save config.yml! Please contact pim16aap2 and show him the following code:");
			e.printStackTrace();
		}
	}
	
	public Integer getInt(String path)
	{
		for (ConfigOption configOption : configOptionsList)
			if (configOption.getName().equals(path))
				return configOption.getInt();
		return null;
	}
	
	public Boolean getBool(String path)
	{
		for (ConfigOption configOption : configOptionsList)
			if (configOption.getName().equals(path))
				return configOption.getBool();
		return null;
	}
	
	public String getString(String path)
	{
		for (ConfigOption configOption : configOptionsList)
			if (configOption.getName().equals(path))
				return configOption.getString();
		return null;
	}
	
	public List<String> getStringList(String path)
	{
		for (ConfigOption configOption : configOptionsList)
			if (configOption.getName().equals(path))
				return configOption.getStringList();
		return null;
	}
}
