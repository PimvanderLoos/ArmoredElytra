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
import nl.pim16aap2.bigDoors.util.ConfigOption;

public class ConfigLoader
{
    private boolean         allowStats;
    private boolean        unbreakable;
    private boolean        enableDebug;
    private String        languageFile;
    private int           GOLD_TO_FULL;
    private int           IRON_TO_FULL;
    private boolean      uninstallMode;
    private boolean    checkForUpdates;
    private int        LEATHER_TO_FULL;
    private int       DIAMONDS_TO_FULL;
    private boolean noFlightDurability;
    private List<String> allowedEnchantments;
    
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
        // All the comments for the various config options.
        String[] unbreakableComment    =
            {
                 "Setting this to true will cause armored elytras to be unbreakable.",
                 "Changing this to false will NOT make unbreakable elytras breakable again!"
            };
        String[] flyDurabilityComment  =
            {
             "Setting this to true will cause armored elytras to not lose any durability while flying.",
             "This is not a permanent option and will affect ALL elytras."
            };
        String[] repairComment         = 
            {
                "Amount of items it takes to fully repair an armored elytra",
                "Repair cost for every tier of armored elytra in number of items to repair 100%."
            };
        String[] enchantmentsComment   = 
            {
                "List of enchantments that are allowed to be put on an armored elytra.",
                "If you do not want to allow any enchantments at all, remove them all and add \"NONE\"",
                "You can find supported enchantments here:",
                "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/enchantments/Enchantment.html",
                "Note that only 1 protection enchantment (PROTECTION_FIRE, PROTECTION_ENVIRONMENTAL etc) can be active on an elytra."
            };
        String[] updateComment         = 
            {
                "Allow this plugin to check for updates on startup. It will not download new versions!"
            };
        String[] bStatsComment         = 
            {
                "Allow this plugin to send (anonymised) stats using bStats. Please consider keeping it enabled.",
                "It has a negligible impact on performance and more users on stats keeps me more motivated to support this plugin!"
            };
        String[] debugComment          =
            {
                "Print debug messages to console. You will most likely never need this."
            };
        String[] uninstallComment      =
            {
                "Setting this to true will disable this plugin and remove any armored elytras it can find.",
                "It will check player's inventories and their end chest upon login and any regular chest when it is opened.",
                "This means it will take a while for all armored elytras to be removed from your server, but it doesn't take up ",
                "a lot of resources, so you can just leave it installed and ignore it.",
                "Please do not forget to MAKE A BACKUP before enabling this option!"
            };
        String[] languageFileComment   =
            {
                "Specify a language file to be used. Note that en_US.txt will get regenerated!"
            };
        
        
        
        FileConfiguration config = plugin.getConfig();
        
        // Read all the options from the config, then put them in a configOption with their name, value and comment.
        // THen put all configOptions into an ArrayList.
        unbreakable           = config.getBoolean   ("unbreakable"       , false);
        configOptionsList.add(new ConfigOption      ("unbreakable"       , unbreakable       , unbreakableComment  ));
        noFlightDurability    = config.getBoolean   ("noFlightDurability", false);
        configOptionsList.add(new ConfigOption      ("noFlightDurability", noFlightDurability, flyDurabilityComment));
        
        LEATHER_TO_FULL       = config.getInt       ("leatherRepair" , 6);
        configOptionsList.add(new ConfigOption      ("leatherRepair" , LEATHER_TO_FULL, repairComment));
        GOLD_TO_FULL          = config.getInt       ("goldRepair"    , 5);
        configOptionsList.add(new ConfigOption      ("goldRepair"    , GOLD_TO_FULL));
        IRON_TO_FULL          = config.getInt       ("ironRepair"    , 4);
        configOptionsList.add(new ConfigOption      ("ironRepair"    , IRON_TO_FULL));
        DIAMONDS_TO_FULL      = config.getInt       ("diamondsRepair", 3);
        configOptionsList.add(new ConfigOption      ("diamondsRepair", DIAMONDS_TO_FULL));
        
        allowedEnchantments   = config.getStringList("allowedEnchantments");
        configOptionsList.add(new ConfigOption      ("allowedEnchantments", allowedEnchantments, enchantmentsComment));
        
        checkForUpdates       = config.getBoolean   ("checkForUpdates", true );
        configOptionsList.add(new ConfigOption      ("checkForUpdates", checkForUpdates, updateComment));
        allowStats            = config.getBoolean   ("allowStats"     , true );
        configOptionsList.add(new ConfigOption      ("allowStats"     , allowStats, bStatsComment));
        enableDebug           = config.getBoolean   ("enableDebug"    , false);
        configOptionsList.add(new ConfigOption      ("enableDebug"    , enableDebug, debugComment));
        uninstallMode         = config.getBoolean   ("uninstallMode"  , false);
        configOptionsList.add(new ConfigOption      ("uninstallMode"  , uninstallMode, uninstallComment));
        languageFile          = config.getString    ("languageFile"   , "en_US");
        configOptionsList.add(new ConfigOption      ("languageFile"   , languageFile, languageFileComment));
        
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
        }
        catch (IOException e)
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
