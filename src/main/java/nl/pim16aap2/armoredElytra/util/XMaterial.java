package nl.pim16aap2.armoredElytra.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/*
 * The MIT License (MIT)
 *
 * Original work Copyright (c) 2018 Hex_27
 * v2.0 Copyright (c) 2019 Crypto Morin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/*
 * References
 *
 * * * GitHub: https://github.com/CryptoMorin/XMaterial/blob/master/XMaterial.java
 * * Thread: https://www.spigotmc.org/threads/378136/
 * https://minecraft.gamepedia.com/Java_Edition_data_values/Pre-flattening
 * https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html
 * http://docs.codelanx.com/Bukkit/1.8/org/bukkit/Material.html
 * https://www.spigotmc.org/threads/1-8-to-1-13-itemstack-material-version-support.329630/
 * https://minecraft-ids.grahamedgecombe.com/
 * v1: https://pastebin.com/Fe65HZnN
 * v2: 6/15/2019
 */

/**
 * XMaterial v2.2 - Data Values/Pre-flattening Supports 1.8-1.14 1.13 and above
 * as priority.
 */
public enum XMaterial
{

    CHAINMAIL_CHESTPLATE(0, ""),
    DIAMOND_CHESTPLATE(0, ""),
    GOLDEN_CHESTPLATE(0, "GOLD_CHESTPLATE"),
    IRON_CHESTPLATE(0, ""),
    LEATHER_CHESTPLATE(0, ""),

    AIR(0, ""),
    ;


    /**
     * A list of material names that can be damaged. Some names are not complete as
     * this list needs to be checked with {@link String#contains}.
     */
    public static final String[] DAMAGEABLE = { "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS", "SWORD", "AXE", "PICKAXE",
                                                "SHOVEL", "HOE", "ELYTRA", "TRIDENT", "HORSE_ARMOR", "BARDING",
                                                "SHEARS", "FLINT_AND_STEEL", "BOW", "FISHING_ROD", "CARROT_ON_A_STICK",
                                                "CARROT_STICK" };

    public static final XMaterial[] VALUES = XMaterial.values();
    private static final HashMap<String, XMaterial> CACHED_SEARCH = new HashMap<>();
    private static MinecraftVersion version;
    private static Boolean isNewVersion;
    private final byte data;
    private final String[] legacy;

    XMaterial(int data, String... legacy)
    {
        this.data = (byte) data;
        this.legacy = legacy;
    }

    /**
     * Checks if the version is 1.13 (Aquatic Update) or higher.
     *
     * @return true if 1.13 or higher.
     */
    public static boolean isNewVersion()
    {
        if (isNewVersion != null)
            return isNewVersion;
        return isNewVersion = isVersionOrHigher(MinecraftVersion.VERSION_1_13);
    }

    public static boolean isOneEight()
    {
        return getVersion() == MinecraftVersion.VERSION_1_8;
    }

    /**
     * Uses newly added materials to minecraft to detect the server version.
     *
     * @return the current server version.
     */
    public static MinecraftVersion getVersion()
    {
        if (version != null)
            return version;
        return version = valueOfVersion(Bukkit.getVersion());
    }

    /**
     * When using newer versions of Minecraft {@link #isNewVersion()} this helps to
     * find the old material name with its data using a cached search for
     * optimization.
     *
     * @see #matchXMaterial(String, byte)
     */
    private static XMaterial requestOldXMaterial(String name, byte data)
    {
        XMaterial cached = CACHED_SEARCH.get(name + "," + data);

        if (cached != null)
            return cached;
        Optional<XMaterial> search = data == -1 ?
            Arrays.stream(XMaterial.VALUES).filter(mat -> mat.matchAnyLegacy(name)).findFirst() :
            Arrays.stream(XMaterial.VALUES).filter(mat -> mat.matchAnyLegacy(name) && mat.data == data).findFirst();

        if (search.isPresent())
        {
            XMaterial found = search.get();
            CACHED_SEARCH.put(found.legacy[0] + "," + found.getData(), found);
            return found;
        }
        return null;
    }

    /**
     * Checks if XMaterial enum contains a material with this name.
     *
     * @param name name of the material
     * @return true if XMaterial enum has this material.
     */
    public static boolean contains(String name)
    {
        String formatted = format(name);
        return Arrays.stream(XMaterial.VALUES).anyMatch(mat -> mat.name().equals(formatted));
    }

    /**
     * Checks if the given material matches any of the legacy names.
     *
     * @param name the material name.
     * @return true if it's a legacy name.
     */
    public static boolean containsLegacy(String name)
    {
        String formatted = format(name);
        return Arrays.stream(Arrays.stream(XMaterial.VALUES).map(m -> m.legacy).toArray(String[]::new))
            .anyMatch(mat -> parseLegacyVersionMaterialName(mat).equals(formatted));
    }

    /**
     * @see #matchXMaterial(String, byte)
     */
    public static XMaterial matchXMaterial(Material material)
    {
        return matchXMaterial(material.name());
    }

    /**
     * @see #matchXMaterial(String, byte)
     */
    public static XMaterial matchXMaterial(String name)
    {
        // -1 Determines whether the item's data is unknown and only the name is given.
        // Checking if the item is damageable won't do anything as the data is not going
        // to be checked in requestOldMaterial anyway.
        return matchXMaterial(name, (byte) -1);
    }

    /**
     * Parses the material name and data argument as a {@link Material}.
     *
     * @param name name of the material
     * @param data data of the material
     */
    public static Material parseMaterial(String name, byte data)
    {
        return matchXMaterial(name, data).parseMaterial();
    }

    /**
     * @param item the ItemStack to match its material and data.
     * @see #matchXMaterial(String, byte)
     */
    @SuppressWarnings("deprecation")
    public static XMaterial matchXMaterial(ItemStack item)
    {
        return isDamageable(item.getType().name()) ? matchXMaterial(item.getType().name(), (byte) 0) :
            matchXMaterial(item.getType().name(), (byte) item.getDurability());
    }

    /**
     * Matches the argument string and its data with a XMaterial.
     *
     * @param name the name of the material
     * @param data the data byte of the material
     * @return a XMaterial from the enum (with the same legacy name and data if in
     *         older versions.)
     */
    public static XMaterial matchXMaterial(String name, byte data)
    {
        Validate.notEmpty(name, "Material name cannot be null or empty");
        name = format(name);

        if ((contains(name) && data <= 0))
            return valueOf(name);
        return requestOldXMaterial(name, data);
    }

    /**
     * Gets the XMaterial based on the Material's ID and data. You should avoid
     * using this for performance reasons.
     *
     * @param id   the ID (Magic value) of the material.
     * @param data the data of the material.
     * @return some XMaterial, or null.
     */
    public static XMaterial matchXMaterial(int id, byte data)
    {
        // Looping through Material.values() will take longer.
        return Arrays.stream(XMaterial.VALUES).filter(mat -> mat.getId() == id && mat.data == data).findFirst()
            .orElse(null);
    }

    /**
     * Attempts to build the string like an enum name.
     *
     * @param name the material name to modify.
     * @return a Material enum name.
     */
    private static String format(String name)
    {
        return name.toUpperCase().replace("MINECRAFT:", "").replace('-', '_').replaceAll("\\s+", "_").replaceAll("\\W",
                                                                                                                 "");
    }

    /**
     * Parses the material name if the legacy name has a version attached to it.
     *
     * @param name the material name to parse.
     * @return the material name with the version removed.
     */
    private static String parseLegacyVersionMaterialName(String name)
    {
        if (!name.contains("/"))
            return name;
        return name.substring(0, name.indexOf('/'));
    }

    /**
     * Checks if the version argument is the same or higher than the current server
     * version.
     *
     * @param version the version to be checked.
     * @return true of the version is equal or higher than the current version.
     */
    public static boolean isVersionOrHigher(MinecraftVersion version)
    {
        MinecraftVersion current = getVersion();

        if (version == current)
            return true;
        if (version == MinecraftVersion.UNKNOWN)
            return false;
        if (current == MinecraftVersion.UNKNOWN)
            return true;

        int ver = Integer.parseInt(version.name().replace("VERSION_", "").replace("_", ""));
        int currentVer = Integer.parseInt(current.name().replace("VERSION_", "").replace("_", ""));

        return currentVer >= ver;
    }

    /**
     * Converts the material's name to a string with the first letter uppercase.
     *
     * @return a converted string.
     */
    public static String toWord(Material material)
    {
        return material.name().charAt(0) + material.name().substring(1).toLowerCase();
    }

    /**
     * Compares two major versions. The current server version and the given
     * version.
     *
     * @param version the version to check.
     * @return true if is the same as the current version or higher.
     */
    public static boolean isVersionOrHigher(String version)
    {
        int currentVer = Integer.parseInt(getExactMajorVersion(Bukkit.getVersion()).replace(".", ""));
        int versionNumber = Integer.parseInt(version.replace(".", ""));

        return currentVer >= versionNumber;
    }

    /**
     * Gets the exact major version (..., 1.9, 1.10, ..., 1.14)
     *
     * @param version Supports {@link Bukkit#getVersion()},
     *                {@link Bukkit#getBukkitVersion()} and normal formats such as
     *                "1.14"
     * @return the exact major version.
     */
    public static String getExactMajorVersion(String version)
    {
        // getBukkitVersion()
        if (version.contains("SNAPSHOT") || version.contains("-R"))
            version = version.substring(0, version.indexOf("-"));
        // getVersion()
        if (version.contains("git"))
            version = version.substring(version.indexOf("MC:") + 4).replace(")", "");
        if (version.split(Pattern.quote(".")).length > 2)
            version = version.substring(0, version.lastIndexOf("."));
        return version;
    }

    /**
     * Parses the string arugment to a version. Supports
     * {@link Bukkit#getVersion()}, {@link Bukkit#getBukkitVersion()} and normal
     * formats such as "1.14"
     *
     * @param version the server version.
     * @return the Minecraft version represented by the string.
     */
    private static MinecraftVersion valueOfVersion(String version)
    {
        version = getExactMajorVersion(version);
        if (version.equals("1.10") || version.equals("1.11") || version.equals("1.12"))
            return MinecraftVersion.VERSION_1_9;
        version = version.replace(".", "_");
        if (!version.startsWith("VERSION_"))
            version = "VERSION_" + version;
        String check = version;
        return Arrays.stream(MinecraftVersion.VALUES).anyMatch(v -> v.name().equals(check)) ?
            MinecraftVersion.valueOf(version) : MinecraftVersion.UNKNOWN;
    }

    /**
     * Checks if the material can be damaged from {@link #DAMAGEABLE}.
     *
     * @param name the name of the material.
     * @return true of the material can be damaged.
     */
    public static boolean isDamageable(String name)
    {
        return Arrays.stream(DAMAGEABLE).anyMatch(name::contains);
    }

    /**
     * Gets the ID (Magic value) of the material. If an
     * {@link IllegalArgumentException} was thrown from this method, you should
     * definitely report it.
     *
     * @return the ID of the material. -1 if it's a new block.
     */
    @SuppressWarnings("deprecation")
    public int getId()
    {
        return isNew() ? -1 : this.parseMaterial().getId();
    }

    /**
     * Checks if the given string matches any of this material's legacy material
     * names.
     *
     * @param name the name to check
     * @return true if it's one of the legacy names.
     */
    public boolean matchAnyLegacy(String name)
    {
        String formatted = format(name);
        return Arrays.asList(legacy).contains(formatted);
    }

    /**
     * Converts the material's name to a string with the first letter uppercase.
     *
     * @return a converted string.
     */
    public String toWord()
    {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    /**
     * @return true if the item can be damaged.
     * @see #isDamageable(String)
     */
    public boolean isDamageable()
    {
        return isDamageable(name());
    }

    /**
     * Get the {@link ItemStack} data of this material in older versions. Which can
     * be accessed with {@link ItemStack#getData()} then MaterialData#getData() or
     * {@link ItemStack#getDurability()} if not damageable.
     *
     * @return data of this material.
     */
    public int getData()
    {
        return data;
    }

    /**
     * Get a list of materials names that was previously used by older versions.
     *
     * @return a list of string of legacy material names.
     */
    public String[] getLegacy()
    {
        return legacy;
    }

    /**
     * Parses the XMaterial as an {@link ItemStack}.
     *
     * @return an ItemStack with the same material (and data if in older versions.)
     */
    public ItemStack parseItem()
    {
        return parseItem(false);
    }

    /**
     * Parses the XMaterial as an {@link ItemStack}.
     *
     * @param suggest if true {@link #parseMaterial(boolean)}
     * @return an ItemStack with the same material (and data if in older versions.)
     */
    @SuppressWarnings("deprecation")
    public ItemStack parseItem(boolean suggest)
    {
        Material material = this.parseMaterial(suggest);
        return isNewVersion() ? new ItemStack(material) : new ItemStack(material, 1, data);
    }

    /**
     * Parses the XMaterial as a {@link Material}.
     *
     * @return the Material related to this XMaterial based on the server version.
     */
    public Material parseMaterial()
    {
        return parseMaterial(false);
    }

    /**
     * Parses the XMaterial as a {@link Material}.
     *
     * @param suggest Use a suggested material if the material is added in the new
     *                version.
     * @return the Material related to this XMaterial based on the server version.
     * @see #matchXMaterial(String, byte)
     */
    public Material parseMaterial(boolean suggest)
    {
        Material newMat = Material.getMaterial(name());

        // If the name is not null it's probably the new version.
        // So you can still use this name even if it's a duplicated name.
        // Since duplicated names only apply to older versions.
        if (newMat != null && (isNewVersion()))
            return newMat;
        return requestOldMaterial(suggest);
    }

    /**
     * Parses from old material names and can accept suggestions.
     *
     * @param suggest Accept suggestions for newly added blocks
     * @return A parsed Material suitable for this minecraft version.
     */
    private Material requestOldMaterial(boolean suggest)
    {
        Material oldMat;
        boolean isNew = getVersionIfNew() != MinecraftVersion.UNKNOWN;
        for (int i = legacy.length - 1; i >= 0; i--)
        {
            String legacyName = legacy[i];
            // Slash means it's just another name for the material in another version.
            if (legacyName.contains("/"))
            {
                oldMat = Material.getMaterial(parseLegacyVersionMaterialName(legacyName));

                if (oldMat != null)
                    return oldMat;
                else
                    continue;
            }
            if (isNew)
            {
                if (suggest)
                {
                    oldMat = Material.getMaterial(legacyName);
                    if (oldMat != null)
                        return oldMat;
                }
                else
                    return null;
                // According to the suggestion format list, all the other names continuing
                // from here are considered as a "suggestion" if there's no slash anymore.
            }
            oldMat = Material.getMaterial(legacyName);
            if (oldMat != null)
                return oldMat;
        }
        return null;
    }

    /**
     * Checks if an item is similar to the material and its data (if in older
     * versions.)
     *
     * @param item item to check.
     * @return true if the material is the same as the item's material (and data if
     *         in older versions.)
     */
    @SuppressWarnings("deprecation")
    public boolean isSimilar(ItemStack item)
    {
        Objects.requireNonNull(item, "ItemStack cannot be null");
        Objects.requireNonNull(item.getType(), "ItemStack's material cannot be null");
        return (isNewVersion() || this.isDamageable()) ? item.getType() == this.parseMaterial() :
            item.getType() == this.parseMaterial() && item.getDurability() == data;
    }

    /**
     * Get the suggested material names that can be used instead of this material.
     *
     * @return a list of suggested material names.
     */
    public String[] getSuggestions()
    {
        if (!legacy[0].contains("."))
            return new String[0];
        return Arrays.stream(legacy).filter(mat -> !mat.contains(".")).toArray(String[]::new);
    }

    /**
     * Checks if this material is supported in the current version. It'll check both
     * the newest matetrial name and for legacy names.
     *
     * @return true if the material exists in {@link Material} list.
     */
    public boolean isSupported()
    {
        return Arrays.stream(Material.values())
            .anyMatch(mat -> mat.name().equals(name()) || matchAnyLegacy(mat.name()));
    }

    /**
     * Gets the added version if the material is newly added after the 1.13 Aquatic
     * Update and higher.
     *
     * @return the version which the material was added in.
     *         {@link MinecraftVersion#UNKNOWN} if not new.
     * @see #isNew()
     */
    public MinecraftVersion getVersionIfNew()
    {
        return isNew() ? valueOfVersion(legacy[0]) : MinecraftVersion.UNKNOWN;
    }

    /**
     * Checks if the material is newly added after the 1.13 Aquatic Update.
     *
     * @return true if it was newly added.
     */
    public boolean isNew()
    {
        return legacy[0].contains(".");
    }

    /**
     * Gets the suggested material instead of returning null for unsupported
     * versions. This is somehow similar to what ProtcolSupport and ViaVersion are
     * doing to new materials. Don't use this if you want to parse to a
     * {@link Material}
     *
     * @return The suggested material that is similar.
     * @see #parseMaterial()
     */
    public XMaterial suggestOldMaterialIfNew()
    {
        if (getVersionIfNew() == MinecraftVersion.UNKNOWN || legacy.length == 1)
            return null;

        // We need a loop because: Newest -> Oldest
        for (int i = legacy.length - 1; i >= 0; i--)
        {
            String legacyName = legacy[i];

            if (legacyName.contains("/"))
                continue;
            XMaterial mat = matchXMaterial(parseLegacyVersionMaterialName(legacyName), data);
            if (mat != null && this != mat)
                return mat;
        }
        return null;
    }

    /**
     * Only major versions related to material changes.
     */
    public enum MinecraftVersion
    {
        /**
         * Bountiful Update
         */
        VERSION_1_8,
        /**
         * Combat Update (Pitiful Update?)
         */
        VERSION_1_9,
        /**
         * Aquatic Update
         */
        VERSION_1_13,
        /**
         * Village Pillage Update
         */
        VERSION_1_14,
        /**
         * 1.7 or below. Using {@link #getVersionIfNew()} it means 1.12 or below.
         */
        UNKNOWN;

        public static final MinecraftVersion[] VALUES = MinecraftVersion.values();
    }
}