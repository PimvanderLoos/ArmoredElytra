package nl.pim16aap2.armoredElytra.util;

public enum MinecraftVersion
{
    v1_6("1_6", 0),
    v1_7("1_7", 1),
    v1_8("1_8", 2),
    v1_9("1_9", 3),
    v1_10("1_10", 4),
    v1_11("1_11", 5),
    v1_12("1_12", 6),
    v1_13("1_13", 7),
    v1_14("1_14", 8),
    v1_15("1_15", 9),
    v1_16("1_16", 10),
    v1_17("1_17", 11),
    v1_18("1_18", 12),
    v1_19("1_19", 13),
    v1_20("1_20", 14),
    v1_21("1_21", 15),
    v1_22("1_22", 16),
    v1_23("1_23", 17),
    v1_24("1_24", 18),
    UNKNOWN("UNKNOWN", 99999),
    ;

    private int index;
    private String name;

    MinecraftVersion(String name, int index)
    {
        this.name = name;
        this.index = index;
    }

    /**
     * Checks if this version is newer than the other version.
     *
     * @param other The other version to check against.
     * @return True if this version is newer than the other version.
     */
    public boolean isNewerThan(final MinecraftVersion other)
    {
        return this.index > other.index;
    }

    /**
     * Checks if this version is older than the other version.
     *
     * @param other The other version to check against.
     * @return True if this version is older than the other version.
     */
    public boolean isOlderThan(final MinecraftVersion other)
    {
        return this.index < other.index;
    }

    public static MinecraftVersion get(final String versionName)
    {
        if (versionName == null)
            return null;
        for (final MinecraftVersion mcVersion : MinecraftVersion.values())
            if (versionName.contains(mcVersion.name))
                return mcVersion;
        return MinecraftVersion.UNKNOWN;
    }
}
