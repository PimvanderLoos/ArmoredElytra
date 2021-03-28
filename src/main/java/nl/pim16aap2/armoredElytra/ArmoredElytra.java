package nl.pim16aap2.armoredElytra;

import nl.pim16aap2.armoredElytra.handlers.AnvilHandler;
import nl.pim16aap2.armoredElytra.handlers.CommandHandler;
import nl.pim16aap2.armoredElytra.handlers.EventHandlers;
import nl.pim16aap2.armoredElytra.handlers.FlyDurabilityHandler;
import nl.pim16aap2.armoredElytra.handlers.ItemDropListener;
import nl.pim16aap2.armoredElytra.handlers.LoginHandler;
import nl.pim16aap2.armoredElytra.handlers.NetheriteUpgradeListener;
import nl.pim16aap2.armoredElytra.handlers.SmithingTableCraftHandler;
import nl.pim16aap2.armoredElytra.handlers.Uninstaller;
import nl.pim16aap2.armoredElytra.nbtEditor.INBTEditor;
import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ArmorTierName;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.MinecraftVersion;
import nl.pim16aap2.armoredElytra.util.UpdateManager;
import nl.pim16aap2.armoredElytra.util.messages.Message;
import nl.pim16aap2.armoredElytra.util.messages.Messages;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class ArmoredElytra extends JavaPlugin implements Listener
{
    private static final MinecraftVersion minecraftVersion = MinecraftVersion
        .get(Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]);

    private static ArmoredElytra INSTANCE;
    private final Set<Enchantment> allowedEnchantments = new HashSet<>();
    private Messages messages;
    private ConfigLoader config;

    private final Map<ArmorTier, ArmorTierName> armorTierNames = new EnumMap<>(ArmorTier.class);
    private UpdateManager updateManager;

    private INBTEditor nbtEditor;

    @Override
    public void onEnable()
    {
        INSTANCE = this;
        if (minecraftVersion.isOlderThan(MinecraftVersion.v1_15))
        {
            myLogger(Level.SEVERE, "Trying to run this plugin on an unsupported version... ABORT!");
            this.setEnabled(false);
            return;
        }

        if (isBlacklistedVersion())
        {
            myLogger(Level.SEVERE,
                     "You are trying to run this plugin on a blacklisted version of Spigot! Please update Spigot!");
            Bukkit.getPluginManager().registerEvents(
                new LoginHandler(this, ChatColor.RED +
                    "[ArmoredElytra] The plugin failed to start because you are running on a " +
                    "blacklisted version of Spiogt! Please update Spigot!"), this);
            return;
        }
        nbtEditor = new NBTEditor();

        config = new ConfigLoader(this);
        messages = new Messages(this);
        readMessages();

        updateManager = new UpdateManager(this, 47136);

        // Check if the user allows checking for updates.
        updateManager.setEnabled(config.checkForUpdates());

        if (config.allowStats())
        {
            myLogger(Level.INFO, "Enabling stats! Thanks, it really helps!");
            @SuppressWarnings("unused") final Metrics metrics = new Metrics(this);
        }
        else
            // Y u do dis? :(
            myLogger(Level.INFO,
                     "Stats disabled, not loading stats :(... Please consider enabling it! I am a simple man, " +
                         "seeing higher user numbers helps me stay motivated!");

        Bukkit.getPluginManager().registerEvents(new EventHandlers(this), this);
        getCommand("ArmoredElytra").setExecutor(new CommandHandler(this));

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

            final Listener creationListener = config.craftingInSmithingTable() ?
                                              new SmithingTableCraftHandler(this) : new AnvilHandler(this);
            Bukkit.getPluginManager().registerEvents(creationListener, this);
            if (config.allowUpgradeToNetherite())
                Bukkit.getPluginManager().registerEvents(new NetheriteUpgradeListener(this), this);

            if (config.dropNetheriteAsChestplate())
                Bukkit.getPluginManager().registerEvents(new ItemDropListener(this), this);

            // Log all allowed enchantments.
            myLogger(Level.INFO, ("Allowed enchantments:"));
            for (final Enchantment enchantment : config.allowedEnchantments())
                myLogger(Level.INFO, " - " + enchantment.toString());
        }
        else
        {
            myLogger(Level.WARNING, "Plugin in uninstall mode!");
            Bukkit.getPluginManager().registerEvents(new Uninstaller(this), this);
        }
    }

    /**
     * Checks if the current version is blacklisted.
     * <p>
     * This is needed for 1.16, as on the initial release there was a bug with NBT stuff that would crash clients when
     * they saw an Armored Elytra. When they obtained one using a command they wouldn't be able to rejoin the game again
     * until an NBTEditor was used to remove the item from their inventory.
     *
     * @return True if the current version is blacklisted.
     */
    public boolean isBlacklistedVersion()
    {
        if (minecraftVersion != MinecraftVersion.v1_16)
            return false;

        String[] parts = Bukkit.getVersion().substring("git-Spigot-".length()).split("-");
        return parts.length > 0 && parts[0].equals("758abbe");
    }

    public MinecraftVersion getMinecraftVersion()
    {
        return minecraftVersion;
    }

    public Messages getMyMessages()
    {
        return messages;
    }

    public INBTEditor getNbtEditor()
    {
        return nbtEditor;
    }

    private void readMessages()
    {
        // Shouldn't be used.
        armorTierNames.put(ArmorTier.NONE, new ArmorTierName("NONE", "NONE"));

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
        armorTierNames.put(ArmorTier.NETHERITE, new ArmorTierName(messages.getString(Message.TIER_NETHERITE),
                                                                  messages.getString(Message.TIER_SHORT_NETHERITE)));
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

    public static ArmoredElytra getInstance()
    {
        return INSTANCE;
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
}
