package nl.pim16aap2.armoredElytra.util.messages;

import nl.pim16aap2.armoredElytra.util.Util;
import org.bukkit.ChatColor;

import javax.annotation.Nullable;

/**
 * Represents a localizable message.
 *
 * @author Pim
 */
public enum Message implements IMessageVariable
{
    EMPTY,

    TIER_LEATHER,
    TIER_GOLD,
    TIER_CHAIN,
    TIER_IRON,
    TIER_DIAMOND,
    TIER_NETHERITE,
    TIER_COPPER,

    TIER_SHORT_LEATHER,
    TIER_SHORT_GOLD,
    TIER_SHORT_CHAIN,
    TIER_SHORT_IRON,
    TIER_SHORT_DIAMOND,
    TIER_SHORT_NETHERITE,
    TIER_SHORT_COPPER,

    MESSAGES_UNINSTALLMODE(ChatColor.RED),
    MESSAGES_UNSUPPORTEDTIER(ChatColor.RED),

    MESSAGES_REPAIRNEEDED(ChatColor.RED),
    MESSAGES_LORE(ChatColor.DARK_PURPLE, VAR_TIER_NAME, VAR_TIER_NAME_SHORT),
    MESSAGES_NOGIVEPERMISSION(ChatColor.RED, VAR_TIER_NAME, VAR_TIER_NAME_SHORT),
    MESSAGES_USAGEDENIED(ChatColor.RED, VAR_TIER_NAME, VAR_TIER_NAME_SHORT),
    MESSAGES_ELYTRARECEIVED(ChatColor.GREEN, VAR_TIER_NAME, VAR_TIER_NAME_SHORT),

    ;

    private final String defaultColor;

    /**
     * The list of names that can be used as variables in this message.
     * <p>
     * For example: "This door will move %BLOCKSTOMOVE% blocks." Would contain at least "%BLOCKSTOMOVE%".
     */
    private final String[] variableNames;

    /**
     * Constructs a message.
     *
     * @param variableNames
     *     The names of the variables in the value that can be replaced.
     */
    Message(@Nullable ChatColor defaultColor, final String... variableNames)
    {
        this.defaultColor = Util.nullableColorToString(defaultColor);
        this.variableNames = variableNames;
    }

    Message()
    {
        this(null);
    }

    /**
     * Gets the default color of this message.
     *
     * @return The default color of this message.
     */
    public String getDefaultColor()
    {
        return defaultColor;
    }

    /**
     * Gets the name of the variable at the given position for the given message.
     *
     * @param msg
     *     The message for which to retrieve the variable name.
     * @param idx
     *     The index of the variable name.
     *
     * @return The name of the variable at the given position of this message.
     */
    public static String getVariableName(final Message msg, final int idx)
    {
        return msg.variableNames[idx];
    }

    /**
     * Gets the names of the variables for the given message..
     *
     * @param msg
     *     The message for which to retrieve the variable names.
     *
     * @return The names of the variables of this message.
     */
    public static String[] getVariableNames(final Message msg)
    {
        return msg.variableNames;
    }

    /**
     * Gets the number of variables in this message that can be substituted.
     *
     * @param msg
     *     The message to retrieve the variable count for.
     *
     * @return The number of variables in this message that can be substituted.
     */
    public static int getVariableCount(final Message msg)
    {
        return msg.variableNames.length;
    }
}
