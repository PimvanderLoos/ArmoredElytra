package nl.pim16aap2.armoredElytra.util;

public enum MinecraftVersion
{
    v1_6,
    v1_7,
    v1_8,
    v1_9,
    v1_10,
    v1_11,
    v1_12,
    v1_13,
    v1_14,
    v1_15,
    v1_16,
    v1_17,
    v1_18,
    v1_19,
    v1_20,
    v1_21,
    v1_22,
    v1_23,
    v1_24,
    v1_25,
    UNKNOWN,
    ;

    private final String versionName;

    MinecraftVersion()
    {
        versionName = name().substring(1);
    }

    /**
     * Checks if this version is newer than the other version.
     *
     * @param other The other version to check against.
     * @return True if this version is newer than the other version.
     */
    public boolean isNewerThan(final MinecraftVersion other)
    {
        return ordinal() > other.ordinal();
    }

    /**
     * Checks if this version is older than the other version.
     *
     * @param other The other version to check against.
     * @return True if this version is older than the other version.
     */
    public boolean isOlderThan(final MinecraftVersion other)
    {
        return ordinal() < other.ordinal();
    }

    public static MinecraftVersion get(final String versionName)
    {
        if (versionName == null)
            return null;
        for (final MinecraftVersion mcVersion : MinecraftVersion.values())
            if (versionName.contains(mcVersion.versionName))
                return mcVersion;
        return MinecraftVersion.UNKNOWN;
    }
}
