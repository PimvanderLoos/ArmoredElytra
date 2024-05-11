package nl.pim16aap2.armoredElytra;

import nl.pim16aap2.armoredElytra.handlers.AnvilHandler;
import nl.pim16aap2.armoredElytra.handlers.CommandHandler;
import nl.pim16aap2.armoredElytra.handlers.DroppedNetheriteConversionListener;
import nl.pim16aap2.armoredElytra.handlers.DroppedNetheriteUpdateListener;
import nl.pim16aap2.armoredElytra.handlers.EventHandlers;
import nl.pim16aap2.armoredElytra.handlers.FlyDurabilityHandler;
import nl.pim16aap2.armoredElytra.handlers.NetheriteUpgradeListener;
import nl.pim16aap2.armoredElytra.handlers.SmithingTableCraftHandler;
import nl.pim16aap2.armoredElytra.handlers.Uninstaller;
import nl.pim16aap2.armoredElytra.nbtEditor.DurabilityManager;
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.semver4j.Semver;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class ArmoredElytra extends JavaPlugin implements Listener
{
    public static final Semver SERVER_VERSION =
        Objects.requireNonNull(Semver.parse(Bukkit.getServer().getBukkitVersion()));

    private static ArmoredElytra INSTANCE;
    private Messages messages;
    private ConfigLoader config;

    private final Map<ArmorTier, ArmorTierName> armorTierNames = new EnumMap<>(ArmorTier.class);
    private UpdateManager updateManager;

    private NBTEditor nbtEditor;

    public ArmoredElytra()
    {
        INSTANCE = this;
    }

    @Override
    public void onEnable()
    {
        if (SERVER_VERSION.isLowerThan(Semver.of(1, 16, 0)))
        {
            myLogger(Level.SEVERE, "Trying to run this plugin on an unsupported version... ABORT!");
            setEnabled(false);
            return;
        }

        nbtEditor = new NBTEditor();

        config = new ConfigLoader(this);

        DurabilityManager durabilityManager = new DurabilityManager(nbtEditor, config);

        messages = new Messages(this);
        readMessages();

        updateManager = new UpdateManager(this, 47136);

        // Check if the user allows checking for updates.
        updateManager.setEnabled(config.checkForUpdates());

        if (config.allowStats())
        {
            myLogger(Level.INFO, "Enabling stats! Thanks, it really helps!");
            @SuppressWarnings("unused") final Metrics metrics = new Metrics(this, 1656);
        }
        else
            // Y u do dis? :(
            myLogger(Level.INFO,
                     "Stats disabled, not loading stats :(... Please consider enabling it! I am a simple man, " +
                         "seeing higher user numbers helps me stay motivated!");

        Bukkit.getPluginManager().registerEvents(new EventHandlers(this, nbtEditor, durabilityManager), this);
        Objects.requireNonNull(getCommand("ArmoredElytra"), "ArmoredElytra base command not found!")
               .setExecutor(new CommandHandler(this, nbtEditor, durabilityManager));

        // Load the plugin normally if not in uninstall mode.
        if (!config.uninstallMode())
        {
            Bukkit.getPluginManager().registerEvents(new FlyDurabilityHandler(config.noFlightDurability(),
                                                                              nbtEditor, durabilityManager), this);
            final Listener creationListener =
                config.allowCraftingInSmithingTable() ?
                new SmithingTableCraftHandler(this, nbtEditor, durabilityManager, config) :
                new AnvilHandler(this, nbtEditor, durabilityManager, config);

            Bukkit.getPluginManager().registerEvents(creationListener, this);
            if (config.allowUpgradeToNetherite())
                Bukkit.getPluginManager()
                      .registerEvents(
                          new NetheriteUpgradeListener(this, nbtEditor, durabilityManager, config), this);

            if (NBTEditor.HAS_FIRE_RESISTANT_METHOD)
                Bukkit.getPluginManager().registerEvents(new DroppedNetheriteUpdateListener(nbtEditor), this);
            else if (config.dropNetheriteAsChestplate())
                Bukkit.getPluginManager().registerEvents(new DroppedNetheriteConversionListener(nbtEditor), this);

            // Log all allowed enchantments.
            myLogger(Level.INFO, ("Allowed enchantments:"));
            for (final Enchantment enchantment : config.allowedEnchantments())
                myLogger(Level.INFO, " - " + enchantment.getKey());
        }
        else
        {
            myLogger(Level.WARNING, "Plugin in uninstall mode!");
            Bukkit.getPluginManager().registerEvents(new Uninstaller(this, nbtEditor), this);
        }
    }

    public Messages getMyMessages()
    {
        return messages;
    }

    public NBTEditor getNbtEditor()
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

    public boolean playerHasCraftPerm(HumanEntity player, ArmorTier armorTier)
    {
        return getConfigLoader().bypassCraftPerm() ||
            player.hasPermission("armoredelytra.craft." + ArmorTier.getName(armorTier));
    }

    public boolean playerHasWearPerm(HumanEntity player, ArmorTier armorTier)
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
    public void messagePlayer(HumanEntity player, ChatColor color, String str)
    {
        player.sendMessage(color + str);
    }

    // Send a message to a player.
    public void messagePlayer(HumanEntity player, String str)
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
    public void usageDeniedMessage(HumanEntity player, ArmorTier armorTier)
    {
        final String message = getMessageWithTierNames(Message.MESSAGES_USAGEDENIED, armorTier);
        if (!message.equals("NONE"))
            messagePlayer(player, ChatColor.RED, message);
    }

    // Send the elytraReceivedMessage message to the player.
    public void elytraReceivedMessage(HumanEntity player, ArmorTier armorTier)
    {
        final String message = getMessageWithTierNames(Message.MESSAGES_ELYTRARECEIVED, armorTier);
        if (!message.equals("NONE"))
            messagePlayer(player, ChatColor.GREEN, message);
    }

    public void sendNoGivePermissionMessage(HumanEntity player, ArmorTier armorTier)
    {
        final String message = getMessageWithTierNames(Message.MESSAGES_NOGIVEPERMISSION, armorTier);
        messagePlayer(player, ChatColor.RED, message);
    }

    public @Nullable List<String> getElytraLore(ArmorTier armorTier)
    {
        final String message = ChatColor.stripColor(getMessageWithTierNames(Message.MESSAGES_LORE, armorTier));
        return message.equals("NONE") ? null : Collections.singletonList(message);
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
    public void giveArmoredElytraToPlayer(HumanEntity player, ItemStack item)
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
