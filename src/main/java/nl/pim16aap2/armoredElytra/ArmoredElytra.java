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
import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.Messages;
import nl.pim16aap2.armoredElytra.util.Metrics;
import nl.pim16aap2.armoredElytra.util.Update;

public class ArmoredElytra extends JavaPlugin implements Listener
{
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
        readConfigValues();
        messages = new Messages(this);
        readMessages();

        // Check if the user allows checking for updates.
        if (config.getBool("checkForUpdates"))
        {
            // Check for updates in a new thread, so the server won't hang when it cannot contact the update servers.
            final Thread thread = new Thread(() ->
            {
                final ArmoredElytra plugin = getPlugin();
                final Update update        = new Update(278437, plugin);
                final String latestVersion = update.getLatestVersion();

                if (latestVersion == null)
                    plugin.myLogger(Level.WARNING, "Encountered problem contacting update servers! Please check manually! The error above does not affect the plugin!");
                else
                {
                    final String thisVersion = plugin.getDescription().getVersion();
                    // Check if this is the latest version or not.
                    final int updateStatus   = update.versionCompare(latestVersion, thisVersion);

                    if (updateStatus > 0)
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
            });
            thread.start();
        }
        else
            myLogger(Level.INFO, "Plugin update checking not enabled! You will not receive any messages about new updates for this plugin. Please consider turning this on in the config.");

        if (config.getBool("allowStats"))
        {
            myLogger(Level.INFO, "Enabling stats! Thanks, it really helps!");
            @SuppressWarnings("unused")
            final
            Metrics metrics = new Metrics(this);
        }
        else
            // Y u do dis? :(
            myLogger(Level.INFO, "Stats disabled, not loading stats :(... Please consider enabling it! I am a simple man, seeing higher user numbers helps me stay motivated!");

        // Load the files for the correct version of Minecraft.
        if (compatibleMCVer())
        {
            Bukkit.getPluginManager().registerEvents(new EventHandlers(this, nbtEditor, is1_9), this);
            getCommand("ArmoredElytra").setExecutor(new CommandHandler(this, nbtEditor));
        }
        else
        {
            Bukkit.getPluginManager().registerEvents(new LoginHandler(this, "The Armored Elytra plugin failed to start correctly! Please send the startup log to pim16aap2!"), this);
            myLogger(Level.WARNING, "Plugin failed to load! Either your version isn't supported or something went horribly wrong! Please contact pim16aap2!");
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
            for (final String s : config.getStringList("allowedEnchantments"))
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
        config = new ConfigLoader(this);

        // Check if the plugin should go into uninstall mode.
        uninstallMode = config.getBool("uninstallMode");

        locale = config.getString("languageFile");
    }

    public Messages getMyMessages()
    {
        return messages;
    }

    private void readMessages()
    {
        // Replace color codes by the corresponding colors.
        usageDeniedMessage    = getMyMessages().getString("MESSAGES.UsageDenied"   ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
        elytraReceivedMessage = getMyMessages().getString("MESSAGES.ElytraReceived").replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
        elytraLore            = getMyMessages().getString("MESSAGES.Lore"          ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");

        leatherName           = getMyMessages().getString("TIER.Leather"           ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
        goldName              = getMyMessages().getString("TIER.Gold"              ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
        chainName             = getMyMessages().getString("TIER.Chain"             ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
        ironName              = getMyMessages().getString("TIER.Iron"              ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
        diamondName           = getMyMessages().getString("TIER.Diamond"           ).replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");

        // Change the string to null if it says "NONE".
        usageDeniedMessage    = (Objects.equals(usageDeniedMessage,    new String("NONE")) ? null : usageDeniedMessage   );
        elytraReceivedMessage = (Objects.equals(elytraReceivedMessage, new String("NONE")) ? null : elytraReceivedMessage);
        elytraLore            = (Objects.equals(elytraLore,            new String("NONE")) ? null : elytraLore           );
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
            final String message = fillInArmorTierInStringNoColor(usageDeniedMessage, armorTier);
            messagePlayer(player, ChatColor.RED, message);
        }
    }

    // Send the elytraReceivedMessage message to the player.
    public void elytraReceivedMessage(Player player, ArmorTier armorTier)
    {
        if (elytraReceivedMessage != null)
        {
            final String message = fillInArmorTierInStringNoColor(elytraReceivedMessage, armorTier);
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
        if (locale == null)
            System.out.println("locale is null!");
        else
            System.out.println("Locale is " + locale);
        return locale == null ? "en_US" : locale;
    }

    // Print a string to the log.
    public void myLogger(Level level, String str)
    {
        Bukkit.getLogger().log(level, "[" + getName() + "] " + str);
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
        nbtEditor = new NBTEditor(this);
        return nbtEditor.succes();
    }

    public String getElytraLore()
    {
        return elytraLore;
    }

    public String getArmoredElytrName(ArmorTier tier)
    {
        String ret;
        switch(tier)
        {
        case LEATHER:
            ret = leatherName;
            break;
        case GOLD:
            ret = goldName;
            break;
        case CHAIN:
            ret = chainName;
            break;
        case IRON:
            ret = ironName;
            break;
        case DIAMOND:
            ret = diamondName;
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