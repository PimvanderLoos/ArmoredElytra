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
import nl.pim16aap2.armoredElytra.nms.NBTEditor_V1_13_R2;
import nl.pim16aap2.armoredElytra.nms.NBTEditor_V1_9_R1;
import nl.pim16aap2.armoredElytra.nms.NBTEditor_V1_9_R2;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.Messages;
import nl.pim16aap2.armoredElytra.util.Metrics;
import nl.pim16aap2.armoredElytra.util.Update;

public class ArmoredElytra extends JavaPlugin implements Listener 
{
    // TODO: Merge EventHandlers and EventHandlers_V1.9.
    private NBTEditor          nbtEditor;
    private Messages            messages;
    private ConfigLoader          config;

    private String leatherName, ironName, goldName, chainName, diamondName;
    private String elytraReceivedMessage;
    private String    usageDeniedMessage;
    private boolean        uninstallMode;
    private String            elytraLore;
    private boolean             upToDate;
    private String                locale;
    private boolean                is1_9;
    
    @Override
    public void onEnable()
    {
        this.readConfigValues();
        this.messages = new Messages(this);
        this.readMessages();
        
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
                        String thisVersion = plugin.getDescription().getVersion();
                        // Check if this is the latest version or not.
                        int updateStatus   = update.versionCompare(latestVersion, thisVersion);
                        
                        if (updateStatus   > 0)
                        {
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

        this.locale = config.getString("languageFile");
        
        
        // Load the files for the correct version of Minecraft.
        if (compatibleMCVer()) 
        {
//            if (this.is1_9)
//                Bukkit.getPluginManager().registerEvents(new EventHandlers_V1_9(this, nbtEditor), this);
//            else
//                Bukkit.getPluginManager().registerEvents(new EventHandlers(this, nbtEditor), this);
            Bukkit.getPluginManager().registerEvents(new EventHandlers(this, nbtEditor, this.is1_9), this);
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
            } 
            else
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
        this.config = new ConfigLoader(this);

        // Check if the plugin should go into uninstall mode.
        this.uninstallMode = config.getBool("uninstallMode");
    }
    
    public Messages getMyMessages()
    {
        return this.messages;
    }
    
    private void readMessages()
    {
        // Replace color codes by the corresponding colors.
        this.usageDeniedMessage    = this.getMyMessages().getString("MESSAGES.UsageDenied"   ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
        this.elytraReceivedMessage = this.getMyMessages().getString("MESSAGES.ElytraReceived").replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
        this.elytraLore            = this.getMyMessages().getString("MESSAGES.Lore"          ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");

        this.leatherName           = this.getMyMessages().getString("TIER.Leather"           ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
        this.goldName              = this.getMyMessages().getString("TIER.Gold"              ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
        this.chainName             = this.getMyMessages().getString("TIER.Chain"             ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
        this.ironName              = this.getMyMessages().getString("TIER.Iron"              ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
        this.diamondName           = this.getMyMessages().getString("TIER.Diamond"           ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
        
        // Change the string to null if it says "NONE".
        this.usageDeniedMessage    = (Objects.equals(usageDeniedMessage,    new String("NONE")) ? null : usageDeniedMessage   );
        this.elytraReceivedMessage = (Objects.equals(elytraReceivedMessage, new String("NONE")) ? null : elytraReceivedMessage);
        this.elytraLore            = (Objects.equals(elytraLore,            new String("NONE")) ? null : elytraLore           );
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
    public void messagePlayer(Player player, ChatColor color, String str)
    {
        player.sendMessage(color + str);
    }
    
    // Send a message to a player.
    public void messagePlayer(Player player, String str)
    {
        messagePlayer(player, ChatColor.WHITE, str);
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
    
    public String getLocale()
    {
        return locale == null ? "en_US" : locale;
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

        if (     version.equals("v1_9_R1"))
        {
            nbtEditor  = new NBTEditor_V1_9_R1(this);
            this.is1_9 = true;
        }
        else if (version.equals("v1_9_R2"))
        {
            nbtEditor  = new NBTEditor_V1_9_R2(this);
            this.is1_9 = true;
        }
        else if (version.equals("v1_10_R1"))
            nbtEditor  = new NBTEditor_V1_10_R1(this);
        else if (version.equals("v1_11_R1"))
            nbtEditor  = new NBTEditor_V1_11_R1(this);
        else if (version.equals("v1_12_R1"))
            nbtEditor  = new NBTEditor_V1_12_R1(this);
        else if (version.equals("v1_13_R1"))
            nbtEditor  = new NBTEditor_V1_13_R1(this);
        else if (version.equals("v1_13_R2"))
            nbtEditor  = new NBTEditor_V1_13_R2(this);
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

    public void setUpToDate(boolean upToDate)
    {
        this.upToDate = upToDate;
    }

    public boolean getUninstallMode()
    {
        return uninstallMode;
    }
}