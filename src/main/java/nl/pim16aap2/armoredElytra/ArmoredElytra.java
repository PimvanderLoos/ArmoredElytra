package nl.pim16aap2.armoredElytra;

import nl.pim16aap2.armoredElytra.handlers.CommandHandler;
import nl.pim16aap2.armoredElytra.handlers.EventHandlers;
import nl.pim16aap2.armoredElytra.handlers.FlyDurabilityHandler;
import nl.pim16aap2.armoredElytra.handlers.LoginHandler;
import nl.pim16aap2.armoredElytra.handlers.Uninstaller;
import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ArmorTierName;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.UpdateManager;
import nl.pim16aap2.armoredElytra.util.messages.Message;
import nl.pim16aap2.armoredElytra.util.messages.Messages;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;

// TODO: Figure out if the config really does read the list of enchantments accurately. A bug report with a customized config seemed to load the default settings...
// TODO: Verify enchantments on startup. Remove them from the list if they're invalid.
// TODO: Don't delete the config/translation file. Look at BigDoors.
// TODO: Enchanting should require XP.

public class ArmoredElytra extends JavaPlugin implements Listener
{
    private static ArmoredElytra instance;
    private Messages messages;
    private ConfigLoader config;

    private final Map<ArmorTier, ArmorTierName> armorTierNames = new EnumMap<>(ArmorTier.class);
    private boolean upToDate;
    private boolean is1_9;
    private UpdateManager updateManager;

    @Override
    public void onEnable()
    {
        instance = this;
        config = new ConfigLoader(this);
        messages = new Messages(this);
        readMessages();

        updateManager = new UpdateManager(this, 47136);

        // Check if the user allows checking for updates.
        updateManager.setEnabled(config.checkForUpdates(), config.autoDLUpdate());

        if (config.allowStats())
        {
            myLogger(Level.INFO, "Enabling stats! Thanks, it really helps!");
            @SuppressWarnings("unused") final Metrics metrics = new Metrics(this);
        }
        else
            // Y u do dis? :(
            myLogger(Level.INFO,
                     "Stats disabled, not loading stats :(... Please consider enabling it! I am a simple man, seeing higher user numbers helps me stay motivated!");

        // Load the files for the correct version of Minecraft.
        if (compatibleMCVer())
        {
            Bukkit.getPluginManager().registerEvents(new EventHandlers(this, is1_9), this);
            getCommand("ArmoredElytra").setExecutor(new CommandHandler(this));
        }
        else
        {
            Bukkit.getPluginManager().registerEvents(new LoginHandler(this,
                                                                      "The Armored Elytra plugin failed to start correctly! Please send the startup log to pim16aap2!"),
                                                     this);
            myLogger(Level.WARNING,
                     "Plugin failed to load! Either your version isn't supported or something went horribly wrong! Please contact pim16aap2!");
            return;
        }

        // Load the plugin normally if not in uninstall mode.
        if (!config.uninstallMode())
        {
            // Check if the user wants to disable durability penalty for flying with an armored elytra.
            if (config.noFlightDurability())
            {
                Bukkit.getPluginManager().registerEvents(new FlyDurabilityHandler(), this);
                myLogger(Level.INFO, "Durability penalty for flying disabled!");
            }
            else
                myLogger(Level.INFO, "Durability penalty for flying enabled!");

            // Log all allowed enchantments.
            myLogger(Level.INFO, ("Allowed enchantments:"));
            for (final String s : config.allowedEnchantments())
                myLogger(Level.INFO, " - " + s);
        }
        else
        {
            myLogger(Level.WARNING, "Plugin in uninstall mode!");
            Bukkit.getPluginManager().registerEvents(new Uninstaller(this), this);
        }
    }

    public Messages getMyMessages()
    {
        return messages;
    }

    private void readMessages()
    {
        armorTierNames.put(ArmorTier.NONE, new ArmorTierName("NONE", "NONE")); // Shouldn't be used.
        armorTierNames.put(ArmorTier.LEATHER, new ArmorTierName(messages.getString(Message.TIER_LEATHER),
                                                                messages.getString(Message.TIER_SHORT_LEATHER)));
        armorTierNames.put(ArmorTier.GOLD, new ArmorTierName(messages.getString(Message.TIER_GOLD),
                                                             messages.getString(Message.TIER_SHORT_GOLD)));
        armorTierNames.put(ArmorTier.CHAIN, new ArmorTierName(messages.getString(Message.TIER_CHAIN),
                                                              messages.getString(Message.TIER_SHORT_CHAIN)));
        armorTierNames.put(ArmorTier.IRON, new ArmorTierName(messages.getString(Message.TIER_IRON),
                                                             messages.getString(Message.TIER_SHORT_IRON)));
        armorTierNames.put(ArmorTier.DIAMOND, new ArmorTierName(messages.getString(Message.TIER_DIAMOND),
                                                                messages.getString(Message.TIER_SHORT_DIAMOND)));
    }

    public boolean playerHasCraftPerm(Player player, ArmorTier armorTier)
    {
        return getConfigLoader().bypassCraftPerm() ||
            player.hasPermission("armoredelytra.craft." + ArmorTier.getName(armorTier));
    }

    public boolean playerHasWearPerm(Player player, ArmorTier armorTier)
    {
        return getConfigLoader().bypassWearPerm() ||
            player.hasPermission("armoredelytra.wear." + ArmorTier.getName(armorTier));
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

    private String getMessageWithTierNames(final Message message, final ArmorTier armorTier)
    {
        ArmorTierName tierName = armorTierNames.get(armorTier);
        return getMyMessages().getString(message,
                                         tierName.getLongName(),
                                         tierName.getShortName());
    }

    // Send the usageDeniedMessage message to the player.
    public void usageDeniedMessage(Player player, ArmorTier armorTier)
    {
        final String message = getMessageWithTierNames(Message.MESSAGES_USAGEDENIED, armorTier);
        if (!message.equals("NONE"))
            messagePlayer(player, ChatColor.RED, message);
    }

    // Send the elytraReceivedMessage message to the player.
    public void elytraReceivedMessage(Player player, ArmorTier armorTier)
    {
        final String message = getMessageWithTierNames(Message.MESSAGES_ELYTRARECEIVED, armorTier);
        if (!message.equals("NONE"))
            messagePlayer(player, ChatColor.GREEN, message);
    }

    public void sendNoGivePermissionMessage(Player player, ArmorTier armorTier)
    {
        final String message = getMessageWithTierNames(Message.MESSAGES_NOGIVEPERMISSION, armorTier);
        messagePlayer(player, ChatColor.RED, message);
    }

    public String getElytraLore(ArmorTier armorTier)
    {
        final String message = getMessageWithTierNames(Message.MESSAGES_LORE, armorTier);
        Bukkit.broadcastMessage(message);
        return message.equals("NONE") ? null : message;
    }

    // Print a string to the log.
    public void myLogger(Level level, String str)
    {
        Bukkit.getLogger().log(level, "[" + getName() + "] " + str);
    }

    // Log message that only gets printed when debugging is enabled in the config file.
    public void debugMsg(Level level, String str)
    {
        if (config.enableDebug())
            myLogger(level, str);
    }

    // Give the provided player the provided item.
    public void giveArmoredElytraToPlayer(Player player, ItemStack item)
    {
        if (item != null)
            player.getInventory().addItem(item);
    }

    public UpdateManager getUpdateManager()
    {
        return updateManager;
    }

    // Check + initialize for the correct version of Minecraft.
    public boolean compatibleMCVer()
    {
        return NBTEditor.success();
    }

    public static ArmoredElytra getInstance()
    {
        return instance;
    }

    public String getArmoredElytraName(ArmorTier tier)
    {
        if (tier == null)
        {
            getLogger().log(Level.INFO, "ArmorTier was null! Failed to obtain proper string!");
            return "NULL";
        }
        return armorTierNames.get(tier).getLongName();
    }

    public void setUpToDate(boolean upToDate)
    {
        this.upToDate = upToDate;
    }
}
