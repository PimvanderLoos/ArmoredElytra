package nl.pim16aap2.armoredElytra.util;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class ConfigLoader
{
    private static final String HEADER =
        "Config file for ArmoredElytra. Don't forget to make a backup before making changes!";

    private final ArmoredElytra plugin;

    private final EnchantmentParser enchantmentParser;

    private final int[] repairCounts = new int[ArmorTier.values().length];
    private final ArrayList<ConfigOption<?>> configOptionsList = new ArrayList<>();

    private boolean allowStats;
    private boolean unbreakable;
    private boolean enableDebug;
    private String languageFile;
    private boolean uninstallMode;
    private boolean checkForUpdates;
    private boolean noFlightDurability;
    private boolean useTierDurability;
    private LinkedHashSet<Enchantment> allowedEnchantments;
    private List<List<Enchantment>> mutuallyExclusiveEnchantments;
    private boolean craftingInSmithingTable;
    private boolean allowUpgradeToNetherite;
    private boolean bypassWearPerm;
    private boolean bypassCraftPerm;
    private boolean allowRenaming;
    private boolean allowAddingEnchantments;

    public ConfigLoader(ArmoredElytra plugin)
    {
        this.plugin = plugin;
        this.enchantmentParser = new EnchantmentParser(plugin);
        makeConfig();
    }

    // Read the current config, the make a new one based on the old one or default values, whichever is applicable.
    private void makeConfig()
    {
        // All the comments for the various config options.
        String[] unbreakableComment =
            {
                "Setting this to true will cause armored elytras to be unbreakable.",
                "Changing this to false will NOT make unbreakable elytras breakable again!"
            };
        String[] flyDurabilityComment =
            {
                "Setting this to true will cause armored elytras to not lose any durability while flying.",
                "This is not a permanent option and will affect ALL elytras."
            };
        String[] useTierDurabilityComment =
            {
                "Use the maximum durability of the armor tier of armored elytras.",
                "For example, when this is true, a diamond armored elytra would have a durability of 528.",
                "When this is false, all armored elytras have the maximum durability of a regular elytra."
            };
        String[] repairComment =
            {
                "Amount of items it takes to fully repair an armored elytra",
                "Repair cost for every tier of armored elytra in number of items to repair 100%.",
                "Note that this value cannot be less than 1."
            };
        String[] enchantmentsComment =
            {
                "List of enchantments that are allowed to be put on an armored elytra.",
                "If you do not want to allow any enchantments at all, remove them all and add \"NONE\"",
                "You can find supported enchantments by running the command:",
                "\"armoredelytra listAvailableEnchantments\" in console",
                "If you install additional enchantment plugins, you can add their enchantments as well.",
                "Just add their 'NamespacedKey'. Ask the enchantment plugin dev for more info if you need it."
            };
        String[] mutuallyExclusiveEnchantmentsComment =
            {
                "The lists of enchantments that are mutually exclusive.",
                "Each group [] on this list is treated as mutually exclusive, " +
                    "so only one of them can be on an ArmoredElytra.",
                "The default follows modern vanilla rules by making the different " +
                    "types of protection mutually exclusive.",
                "If you do not want any enchantments to be mutually exclusive, " +
                    "replace all the entries in this list with \"[]\"",
                "You can find supported enchantments by running the command:",
                "\"armoredelytra listAvailableEnchantments\" in console",
                "If you install additional enchant plugins, " +
                    "you can make their enchantments mutually exclusive as well.",
                "Just add their 'NamespacedKey'. Ask the enchantment plugin dev for more info if you need it."
            };
        String[] updateComment =
            {
                "Allow this plugin to check for updates on startup. It will not download new versions on its own!"
            };
        String[] bStatsComment =
            {
                "Allow this plugin to send (anonymous) stats using bStats. Please consider keeping it enabled.",
                "It has a negligible impact on performance and more users on stats keeps me more motivated " +
                    "to support this plugin!"
            };
        String[] debugComment =
            {
                "Print debug messages to console. You will most likely never need this."
            };
        String[] uninstallComment =
            {
                "Setting this to true will disable this plugin and remove any armored elytras it can find.",
                "It will check player's inventories and their end chest upon login and any regular" +
                    " chest when it is opened.",
                "This means it will take a while for all armored elytras to be removed from your server, " +
                    "but it doesn't take up ",
                "a lot of resources, so you can just leave the plugin enabled and ignore it.",
                "Please do not forget to MAKE A BACKUP before enabling this option!"
            };
        String[] languageFileComment =
            {
                "Specify a language file to be used."
            };
        String[] permissionsComment =
            {
                "Globally bypass permissions for wearing and/or crafting armored elytras.",
                "Useful if permissions are unavailable."
            };
        String[] craftingInSmithingTableComment =
            {
                "When enabled, armored elytra creation in anvils is disabled. ",
                "Instead, you will have to craft them in a smithy. Enchanting/repairing them still works via the anvil."
            };
        String[] allowUpgradeToNetheriteComment =
            {
                "Whether or not to allow upgrading diamond armored elytras to netherite ones is possible.",
                "When allowed, you can combine a diamond one with a netherite ingot in a smithing table",
                "and you'll receive a netherite one."
            };
        String[] allowRenamingComment =
            {
                "Whether or not to allow renaming of armored elytras in anvils."
            };
        String[] allowAddingEnchantmentsComment =
            {
                "Allow new enchantments to be added to armored elytras.",
                "When false, no enchantments can be added to armored elytras, even those on the allowed list.",
                "When true, only enchantments from the allowed list can be added."
            };

        // Set default list of allowed enchantments.
        List<String> defaultAllowedEnchantments = new ArrayList<>(
            Arrays.asList("minecraft:unbreaking", "minecraft:fire_protection", "minecraft:blast_protection",
                          "minecraft:projectile_protection", "minecraft:protection",
                          "minecraft:thorns", "minecraft:binding_curse", "minecraft:vanishing_curse",
                          "minecraft:mending"));

        // Set a default list of lists of mutually exclusive enchantments
        // Default only has a list for the protection enchantments
        List<List<String>> defaultMutuallyExclusiveEnchantments = new ArrayList<>();
        defaultMutuallyExclusiveEnchantments.add(List.of("minecraft:protection",
                                                         "minecraft:projectile_protection",
                                                         "minecraft:blast_protection",
                                                         "minecraft:fire_protection"));

        FileConfiguration config = plugin.getConfig();


        unbreakable = addNewConfigOption(config, "unbreakable", false, unbreakableComment);
        noFlightDurability = addNewConfigOption(config, "noFlightDurability", false, flyDurabilityComment);
        useTierDurability = addNewConfigOption(config, "useTierDurability", true, useTierDurabilityComment);

        final ArmorTier[] armorTiers = ArmorTier.values();
        for (int idx = 1; idx < armorTiers.length; ++idx)
        {
            final ArmorTier armorTier = armorTiers[idx];

            // IRON uses the same repair item as the preceding tier; CHAIN.
            // We don't want to have duplicate entries in the config file.
            if (armorTier == ArmorTier.IRON)
            {
                repairCounts[idx] = repairCounts[idx - 1];
                continue;
            }

            // Only the first one should have the comment.
            final @Nullable String[] comment = idx == 1 ? repairComment : null;
            final Material repairItem = ArmorTier.getRepairItem(armorTier);
            final String name = Util.snakeToCamelCase(repairItem == null ? "None" : repairItem.name());
            final int defaultRepairCount = ArmorTier.getDefaultRepairCount(armorTier);

            repairCounts[idx] = addNewConfigOption(config, name, defaultRepairCount, comment);
        }

        final int armorTierCount = ArmorTier.values().length;
        if (repairCounts.length != armorTierCount)
            throw new IllegalStateException("Incorrect repair counts array size! Expected size " +
                                                armorTierCount + " but got size " + repairCounts.length);

        craftingInSmithingTable = addNewConfigOption(config, "craftingInSmithingTable", true,
                                                     craftingInSmithingTableComment);
        allowUpgradeToNetherite = addNewConfigOption(config, "allowUpgradeToNetherite", true,
                                                     allowUpgradeToNetheriteComment);

        defaultAllowedEnchantments = addNewConfigOption(config, "allowedEnchantments", defaultAllowedEnchantments,
                                                        enchantmentsComment);
        allowedEnchantments = new LinkedHashSet<>();
        defaultAllowedEnchantments.forEach(this::addNameSpacedKey);

        defaultMutuallyExclusiveEnchantments =
            addNewConfigOption(config, "mutuallyExclusiveEnchantments",
                               defaultMutuallyExclusiveEnchantments, mutuallyExclusiveEnchantmentsComment);
        mutuallyExclusiveEnchantments = new LinkedList<>();
        defaultMutuallyExclusiveEnchantments.forEach(this::addMutuallyExclusiveEnchantments);

        allowAddingEnchantments = addNewConfigOption(config, "allowAddingEnchantments", true,
                                                     allowAddingEnchantmentsComment);
        allowRenaming = addNewConfigOption(config, "allowRenaming", true, allowRenamingComment);

        checkForUpdates = addNewConfigOption(config, "checkForUpdates", true, updateComment);
        allowStats = addNewConfigOption(config, "allowStats", true, bStatsComment);
        enableDebug = addNewConfigOption(config, "enableDebug", false, debugComment);
        uninstallMode = addNewConfigOption(config, "uninstallMode", false, uninstallComment);
        languageFile = addNewConfigOption(config, "languageFile", "en_US", languageFileComment);
        bypassWearPerm = addNewConfigOption(config, "bypassWearPermissions", true, permissionsComment);
        bypassCraftPerm = addNewConfigOption(config, "bypassCraftPermissions", true, null);

        writeConfig();
    }


    private void addNameSpacedKey(String fullKey)
    {
        final @Nullable Enchantment enchantment = enchantmentParser.parse(fullKey);
        if (enchantment != null)
            allowedEnchantments.add(enchantment);
    }

    private void addMutuallyExclusiveEnchantments(List<String> fullKeys)
    {
        final List<Enchantment> enchantments = new LinkedList<>();
        for (String fullKey : fullKeys)
        {
            final @Nullable Enchantment enchantment = enchantmentParser.parse(fullKey);
            if (enchantment != null)
                enchantments.add(enchantment);
        }
        mutuallyExclusiveEnchantments.add(enchantments);
    }

    private <T> T addNewConfigOption(FileConfiguration config, String optionName, T defaultValue, String[] comment)
    {
        ConfigOption<T> option = new ConfigOption<>(plugin, config, optionName, defaultValue, comment);
        configOptionsList.add(option);
        return option.getValue();
    }

    // Write new config file.
    private void writeConfig()
    {
        // Write all the config options to the config.yml.
        try
        {
            final Path dataDir = plugin.getDataFolder().toPath();
            if (!Files.exists(dataDir))
                Files.createDirectory(dataDir);

            final Path configFile = dataDir.resolve("config.yml");

            final StringBuilder sb = new StringBuilder(6000);
            sb.append("# ").append(HEADER).append('\n');
            for (final ConfigOption<?> configOption : configOptionsList)
            {
                if (configOption.hasComment())
                    sb.append('\n');
                sb.append(configOption).append('\n');
            }

            Files.write(configFile, sb.toString().getBytes(),
                        StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        }
        catch (IOException e)
        {
            plugin.getLogger().log(Level.SEVERE, "Could not save config.yml! " +
                "Please contact pim16aap2 and show him the following code:");
            e.printStackTrace();
        }
    }

    public boolean allowStats()
    {
        return allowStats;
    }

    /**
     * Whether to allow crafting in a smithing table.
     * <p>
     * This is the inverse of {@link #allowCraftingInAnvil()}.
     *
     * @return True if crafting in a smithing table is allowed, false otherwise.
     */
    public boolean allowCraftingInSmithingTable()
    {
        return craftingInSmithingTable;
    }

    /**
     * Whether to allow crafting in an anvil.
     * <p>
     * This is the inverse of {@link #allowCraftingInSmithingTable()}.
     *
     * @return True if crafting in an anvil is allowed, false otherwise.
     */
    public boolean allowCraftingInAnvil()
    {
        return !craftingInSmithingTable;
    }

    public boolean allowUpgradeToNetherite()
    {
        return allowUpgradeToNetherite;
    }

    public boolean unbreakable()
    {
        return unbreakable;
    }

    public boolean enableDebug()
    {
        return enableDebug;
    }

    public String languageFile()
    {
        return languageFile;
    }

    public int getFullRepairItemCount(ArmorTier armorTier)
    {
        return repairCounts[armorTier.ordinal()];
    }

    public boolean allowRenaming()
    {
        return allowRenaming;
    }

    public boolean allowAddingEnchantments()
    {
        return allowAddingEnchantments;
    }

    public boolean uninstallMode()
    {
        return uninstallMode;
    }

    public boolean checkForUpdates()
    {
        return checkForUpdates;
    }

    public boolean noFlightDurability()
    {
        return noFlightDurability;
    }

    public LinkedHashSet<Enchantment> allowedEnchantments()
    {
        return allowedEnchantments;
    }

    public List<List<Enchantment>> getMutuallyExclusiveEnchantments()
    {
        return mutuallyExclusiveEnchantments;
    }

    public boolean bypassWearPerm()
    {
        return bypassWearPerm;
    }

    public boolean bypassCraftPerm()
    {
        return bypassCraftPerm;
    }

    public boolean useTierDurability()
    {
        return useTierDurability;
    }
}
