package nl.pim16aap2.armoredElytra.util.itemInput;

/**
 * Represents the action that should be performed for the input.
 */
public enum InputAction
{
    /**
     * The input is used to create a new armored elytra.
     */
    CREATE,

    /**
     * Upgrade an existing armored elytra.
     */
    UPGRADE,

    /**
     * The input is used to repair an armored elytra.
     */
    REPAIR,

    /**
     * The input is used to enchant an armored elytra.
     */
    ENCHANT,

    /**
     * The input is used to rename an armored elytra.
     */
    RENAME,

    /**
     * The input is used to apply a template to an armored elytra.
     */
    APPLY_TEMPLATE,

    /**
     * The input is used to block an otherwise valid input.
     */
    BLOCK,

    /**
     * The input is not relevant for this plugin and should be ignored.
     */
    IGNORE,
}
