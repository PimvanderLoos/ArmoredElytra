package nl.pim16aap2.armoredElytra.util;

public enum Action
{
    /**
     * Take no action at all and let vanilla (or some other plugin) handle the process.
     */
    NONE,

    /**
     * Repair an armored elytra.
     */
    REPAIR,

    /**
     * Enchant an armored elytra.
     */
    ENCHANT,

    /**
     * Combines one armored elytra with another one of the same tier.
     */
    COMBINE,

    /**
     * Creates a new armored elytra.
     */
    CREATE,

    /**
     * Blocks an otherwise valid input.
     */
    BLOCK
}
