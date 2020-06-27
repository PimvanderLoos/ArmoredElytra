package nl.pim16aap2.armoredElytra.util;

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Hex_27
 * Copyright (c) 2020 Crypto Morin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import com.google.common.base.Enums;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * <b>XMaterial</b> - Data Values/Pre-flattening<br>
 * 1.13 and above as priority.
 * <p>
 * This class is mainly designed to support ItemStacks. If you want to use it on blocks you'll have to use <a
 * href="https://github.com/CryptoMorin/XSeries/blob/master/XBlock.java">XBlock</a>
 * <p>
 * Pre-flattening: https://minecraft.gamepedia.com/Java_Edition_data_values/Pre-flattening Materials:
 * https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html Materials (1.12):
 * https://helpch.at/docs/1.12.2/index.html?org/bukkit/Material.html Material IDs:
 * https://minecraft-ids.grahamedgecombe.com/ Material Source Code: https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/Material.java
 * XMaterial v1: https://www.spigotmc.org/threads/329630/
 *
 * @author Crypto Morin
 * @version 5.0.0
 * @see Material
 * @see ItemStack
 */
public enum XMaterial
{
    CHAINMAIL_CHESTPLATE,
    DIAMOND_CHESTPLATE,
    GOLDEN_CHESTPLATE("GOLD_CHESTPLATE"),
    IRON_CHESTPLATE,
    LEATHER_CHESTPLATE,
    NETHERITE_CHESTPLATE("1.16"),
    PHANTOM_MEMBRANE("1.13"),
    AIR,
    NETHERITE_INGOT("1.16"),
    ;


    /**
     * An immutable cached set of {@link XMaterial#values()} to avoid allocating memory for calling the method every
     * time.
     *
     * @since 2.0.0
     */
    public static final EnumSet<XMaterial> VALUES = EnumSet.allOf(XMaterial.class);
    /**
     * A set of material names that can be damaged.
     * <p>
     * Most of the names are not complete as this list is intended to be checked with {@link String#contains} for memory
     * usage.
     *
     * @since 1.0.0
     */
    private static final ImmutableSet<String> DAMAGEABLE = ImmutableSet.of(
        "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS",
        "SWORD", "AXE", "PICKAXE", "SHOVEL", "HOE",
        "ELYTRA", "TRIDENT", "HORSE_ARMOR", "BARDING",
        "SHEARS", "FLINT_AND_STEEL", "BOW", "FISHING_ROD",
        "CARROT_ON_A_STICK", "CARROT_STICK", "SPADE", "SHIELD"
    );

    /*
     * A set of all the legacy names without duplicates.
     * <p>
     * It'll help to free up a lot of memory if it's not used.
     * Add it back if you need it.
     *
     * @see #containsLegacy(String)
     * @since 2.2.0
     *
    private static final ImmutableSet<String> LEGACY_VALUES = VALUES.stream().map(XMaterial::getLegacy)
            .flatMap(Arrays::stream)
            .filter(m -> m.charAt(1) == '.')
            .collect(Collectors.collectingAndThen(Collectors.toSet(), ImmutableSet::copyOf));
    */

    /**
     * Guava (Google Core Libraries for Java)'s cache for performance and timed caches. For strings that match a certain
     * XMaterial. Mostly cached for configs.
     *
     * @since 1.0.0
     */
    private static final Cache<String, XMaterial> NAME_CACHE
        = CacheBuilder.newBuilder()
                      .softValues()
                      .expireAfterAccess(15, TimeUnit.MINUTES)
                      .build();
    /**
     * Guava (Google Core Libraries for Java)'s cache for performance and timed caches. For XMaterials that are already
     * parsed once.
     *
     * @since 3.0.0
     */
    private static final Cache<XMaterial, Optional<Material>> PARSED_CACHE
        = CacheBuilder.newBuilder()
                      .softValues()
                      .expireAfterAccess(10, TimeUnit.MINUTES)
                      .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                      .build();

    /**
     * Pre-compiled RegEx pattern. Include both replacements to avoid recreating string multiple times with multiple
     * RegEx checks.
     *
     * @since 3.0.0
     */
    private static final Pattern FORMAT_PATTERN = Pattern.compile("\\W+");
    /**
     * The current version of the server in the a form of a major version.
     *
     * @since 1.0.0
     */
    private static final int VERSION = Integer.parseInt(getMajorVersion(Bukkit.getVersion()).substring(2));
    /**
     * Cached result if the server version is after the v1.13 flattening update. Please don't mistake this with
     * flat-chested people. It happened.
     *
     * @since 3.0.0
     */
    private static final boolean ISFLAT = supports(13);
    /**
     * The data value of this material https://minecraft.gamepedia.com/Java_Edition_data_values/Pre-flattening
     *
     * @see #getData()
     */
    private final byte data;
    /**
     * A list of material names that was being used for older verions.
     *
     * @see #getLegacy()
     */
    private final String[] legacy;

    XMaterial(int data, String... legacy)
    {
        this.data = (byte) data;
        this.legacy = legacy;
    }

    XMaterial()
    {
        this(0);
    }

    XMaterial(String... legacy)
    {
        this(0, legacy);
    }

    /**
     * Checks if the version is 1.13 Aquatic Update or higher. An invocation of this method yields the cached result
     * from the expression:
     * <p>
     * <blockquote>
     * {@link #supports(int) 13}}
     * </blockquote>
     *
     * @return true if 1.13 or higher.
     *
     * @see #getVersion()
     * @see #supports(int)
     * @since 1.0.0
     */
    public static boolean isNewVersion()
    {
        return ISFLAT;
    }

    /**
     * This is just an extra method that method that can be used for many cases. It can be used in {@link
     * org.bukkit.event.player.PlayerInteractEvent} or when accessing {@link org.bukkit.entity.Player#getMainHand()}, or
     * other compatibility related methods.
     * <p>
     * An invocation of this method yields exactly the same result as the expression:
     * <p>
     * <blockquote>
     * {@link #getVersion()} == 1.8
     * </blockquote>
     *
     * @since 2.0.0
     */
    public static boolean isOneEight()
    {
        return !supports(9);
    }

    /**
     * The current version of the server.
     *
     * @return the current server version or 0.0 if unknown.
     *
     * @see #isNewVersion()
     * @since 2.0.0
     */
    public static double getVersion()
    {
        return VERSION;
    }

    /**
     * When using newer versions of Minecraft ({@link #isNewVersion()}), helps to find the old material name with its
     * data value using a cached search for optimization.
     *
     * @see #matchDefinedXMaterial(String, byte)
     * @since 1.0.0
     */
    @Nullable
    private static XMaterial requestOldXMaterial(@Nonnull String name, byte data)
    {
        String holder = name + data;
        XMaterial cache = NAME_CACHE.getIfPresent(holder);
        if (cache != null)
            return cache;

        for (XMaterial material : VALUES)
        {
            // Not using material.name().equals(name) check is intended.
            if ((data == -1 || data == material.data) && material.anyMatchLegacy(name))
            {
                NAME_CACHE.put(holder, material);
                return material;
            }
        }

        return null;
    }

    /**
     * Checks if XMaterial enum contains a material with the given name.
     * <p>
     * You should use {@link #matchXMaterial(String)} instead if you're going to get the XMaterial object after checking
     * if it's available in the list by doing a simple {@link Optional#isPresent()} check. This is just to avoid
     * multiple loops for maximum performance.
     *
     * @param name name of the material.
     * @return true if XMaterial enum has this material.
     *
     * @since 1.0.0
     */
    public static boolean contains(@Nonnull String name)
    {
        Validate.notEmpty(name, "Cannot check for null or empty material name");
        name = format(name);

        for (XMaterial materials : VALUES)
            if (materials.name().equals(name))
                return true;
        return false;
    }

    /**
     * Parses the given material name as an XMaterial with unspecified data value.
     *
     * @see #matchXMaterialWithData(String)
     * @since 2.0.0
     */
    @Nonnull
    public static Optional<XMaterial> matchXMaterial(@Nonnull String name)
    {
        Validate.notEmpty(name, "Cannot match a material with null or empty material name");
        Optional<XMaterial> oldMatch = matchXMaterialWithData(name);
        if (oldMatch.isPresent())
            return oldMatch;
        return matchDefinedXMaterial(format(name), (byte) -1);
    }

    /**
     * Parses material name and data value from the specified string. The seperators are: <b>, or :</b> Spaces are
     * allowed. Mostly used when getting materials from config for old school minecrafters.
     * <p>
     * <b>Examples</b>
     * <p><pre>
     *     {@code INK_SACK:1 -> RED_DYE}
     *     {@code WOOL, 14  -> RED_WOOL}
     * </pre>
     *
     * @param name the material string that consists of the material name, data and separator character.
     * @return the parsed XMaterial.
     *
     * @see #matchXMaterial(String)
     * @since 3.0.0
     */
    private static Optional<XMaterial> matchXMaterialWithData(String name)
    {
        for (char separator : new char[]{',', ':'})
        {
            int index = name.indexOf(separator);
            if (index == -1)
                continue;

            String mat = format(name.substring(0, index));
            byte data = Byte.parseByte(StringUtils.deleteWhitespace(name.substring(index + 1)));
            return matchDefinedXMaterial(mat, data);
        }

        return Optional.empty();
    }

    /**
     * Parses the given material as an XMaterial.
     *
     * @throws IllegalArgumentException may be thrown as an unexpected exception.
     * @see #matchDefinedXMaterial(String, byte)
     * @see #matchXMaterial(ItemStack)
     * @since 2.0.0
     */
    @Nonnull
    public static XMaterial matchXMaterial(@Nonnull Material material)
    {
        Objects.requireNonNull(material, "Cannot match null material");
        return matchDefinedXMaterial(material.name(), (byte) -1)
            .orElseThrow(() -> new IllegalArgumentException("Unsupported Material With No Bytes: " + material.name()));
    }

    /**
     * Parses the given item as an XMaterial using its material and data value (durability).
     *
     * @param item the ItemStack to match.
     * @return an XMaterial if matched any.
     *
     * @throws IllegalArgumentException may be thrown as an unexpected exception.
     * @see #matchDefinedXMaterial(String, byte)
     * @since 2.0.0
     */
    @Nonnull
    @SuppressWarnings("deprecation")
    public static XMaterial matchXMaterial(@Nonnull ItemStack item)
    {
        Objects.requireNonNull(item, "Cannot match null ItemStack");
        String material = item.getType().name();
        byte data = (byte) (ISFLAT || isDamageable(material) ? 0 : item.getDurability());

        return matchDefinedXMaterial(material, data)
            .orElseThrow(() -> new IllegalArgumentException("Unsupported Material: " + material + " (" + data + ')'));
    }

    /**
     * Parses the given material name and data value as an XMaterial. All the values passed to this method will not be
     * null or empty and are formatted correctly.
     *
     * @param name the formatted name of the material.
     * @param data the data value of the material.
     * @return an XMaterial (with the same data value if specified)
     *
     * @see #matchXMaterial(Material)
     * @see #matchXMaterial(int, byte)
     * @see #matchXMaterial(ItemStack)
     * @since 3.0.0
     */
    @Nonnull
    private static Optional<XMaterial> matchDefinedXMaterial(@Nonnull String name, byte data)
    {
        boolean duplicated = isDuplicated(name);

        // Do basic number and boolean checks before accessing more complex enum stuff.
        // Maybe we can simplify (ISFLAT || !duplicated) with the (!ISFLAT && duplicated) under it to save a few nanoseconds?
        // if (!Boolean.valueOf(Boolean.getBoolean(Boolean.TRUE.toString())).equals(Boolean.FALSE.booleanValue())) return null;
        if (data <= 0 && !duplicated)
        {
            // Apparently the transform method is more efficient than toJavaUtil()
            // toJavaUtil isn't even supported in older versions.
            Optional<XMaterial> xMat =
                Enums.getIfPresent(XMaterial.class, name).transform(Optional::of).or(Optional.empty());

            if (xMat.isPresent())
                return xMat;
        }

        // XMaterial Paradox (Duplication Check)
        // I've concluded that this is just an infinite loop that keeps
        // going around the Singular Form and the Plural Form materials. A waste of brain cells and a waste of time.
        // This solution works just fine anyway.
        XMaterial xMat = requestOldXMaterial(name, data);
        if (xMat == null)
            return Optional.empty();

        if (!ISFLAT && duplicated && xMat.name().charAt(xMat.name().length() - 1) == 'S')
        {
            // A solution for XMaterial Paradox.
            // Manually parses the duplicated materials to find the exact material based on the server version.
            // If ends with "S" -> Plural Form Material
            return Enums.getIfPresent(XMaterial.class, name).transform(Optional::of).or(Optional.empty());
        }
        return Optional.ofNullable(xMat);
    }

    /**
     * <b>XMaterial Paradox (Duplication Check)</b>
     * Checks if the material has any duplicates.
     * <p>
     * <b>Example:</b>
     * <p>{@code MELON, CARROT, POTATO, BEETROOT -> true}
     *
     * @param name the name of the material to check.
     * @return true if there's a duplicated material for this material, otherwise false.
     *
     * @see #isDuplicated()
     * @since 2.0.0
     */
    private static boolean isDuplicated(@Nonnull String name)
    {
        return false;
    }

    /**
     * Gets the XMaterial based on the material's ID (Magic Value) and data value.<br> You should avoid using this for
     * performance issues.
     *
     * @param id   the ID (Magic value) of the material.
     * @param data the data value of the material.
     * @return a parsed XMaterial with the same ID and data value.
     *
     * @see #matchXMaterial(ItemStack)
     * @since 2.0.0
     */
    @Nonnull
    public static Optional<XMaterial> matchXMaterial(int id, byte data)
    {
        if (id < 0 || data < 0) return Optional.empty();

        // Looping through Material.values() will take longer.
        for (XMaterial materials : VALUES)
            if (materials.data == data && materials.getId() == id) return Optional.of(materials);
        return Optional.empty();
    }

    /**
     * Attempts to build the string like an enum name. Removes all the spaces, numbers and extra non-English characters.
     * Also removes some config/in-game based strings.
     *
     * @param name the material name to modify.
     * @return a Material enum name.
     *
     * @since 2.0.0
     */
    @Nonnull
    private static String format(@Nonnull String name)
    {
        return FORMAT_PATTERN.matcher(
            name.trim().replace('-', '_').replace(' ', '_')).replaceAll("").toUpperCase(Locale.ENGLISH);
    }

    /**
     * Checks if the specified version is the same version or higher than the current server version.
     *
     * @param version the major version to be checked. "1." is ignored. E.g. 1.12 = 12 | 1.9 = 9
     * @return true of the version is equal or higher than the current version.
     *
     * @since 2.0.0
     */
    public static boolean supports(int version)
    {
        return VERSION >= version;
    }

    /**
     * Converts the enum names to a more friendly and readable string.
     *
     * @return a formatted string.
     *
     * @see #toWord(String)
     * @since 2.1.0
     */
    @Nonnull
    public static String toWord(@Nonnull Material material)
    {
        Objects.requireNonNull(material, "Cannot translate a null material to a word");
        return toWord(material.name());
    }

    /**
     * Parses an enum name to a normal word. Normal names have underlines removed and each word capitalized.
     * <p>
     * <b>Examples:</b>
     * <pre>
     *     EMERALD                 -> Emerald
     *     EMERALD_BLOCK           -> Emerald Block
     *     ENCHANTED_GOLDEN_APPLE  -> Enchanted Golden Apple
     * </pre>
     *
     * @param name the name of the enum.
     * @return a cleaned more readable enum name.
     *
     * @since 2.1.0
     */
    @Nonnull
    private static String toWord(@Nonnull String name)
    {
        return WordUtils.capitalize(name.replace('_', ' ').toLowerCase(Locale.ENGLISH));
    }

    /**
     * Gets the exact major version (..., 1.9, 1.10, ..., 1.14)
     *
     * @param version Supports {@link Bukkit#getVersion()}, {@link Bukkit#getBukkitVersion()} and normal formats such as
     *                "1.14"
     * @return the exact major version.
     *
     * @since 2.0.0
     */
    @Nonnull
    public static String getMajorVersion(@Nonnull String version)
    {
        Validate.notEmpty(version, "Cannot get major Minecraft version from null or empty string");

        // getVersion()
        int index = version.lastIndexOf("MC:");
        if (index != -1)
            version = version.substring(index + 4, version.length() - 1);
        else if (version.endsWith("SNAPSHOT"))
        {
            // getBukkitVersion()
            index = version.indexOf('-');
            version = version.substring(0, index);
        }

        // 1.13.2, 1.14.4, etc...
        int lastDot = version.lastIndexOf('.');
        if (version.indexOf('.') != lastDot)
            version = version.substring(0, lastDot);

        return version;
    }

    /**
     * Checks if the material can be damaged by using it. Names going through this method are not formatted.
     *
     * @param name the name of the material.
     * @return true of the material can be damaged.
     *
     * @see #isDamageable()
     * @since 1.0.0
     */
    public static boolean isDamageable(@Nonnull String name)
    {
        Objects.requireNonNull(name, "Material name cannot be null");
        for (String damageable : DAMAGEABLE)
            if (name.contains(damageable))
                return true;
        return false;
    }

    /**
     * Checks if the list of given material names matches the given base material. Mostly used for configs.
     * <p>
     * Supports {@link String#contains} {@code CONTAINS:NAME} and Regular Expression {@code REGEX:PATTERN} formats.
     * <p>
     * <b>Example:</b>
     * <blockquote><pre>
     *     XMaterial material = {@link #matchXMaterial(ItemStack)};
     *     if (material.isOneOf(plugin.getConfig().getStringList("disabled-items")) return;
     * </pre></blockquote>
     * <br>
     * <b>{@code CONTAINS} Examples:</b>
     * <pre>
     *     {@code "CONTAINS:CHEST" -> CHEST, ENDERCHEST, TRAPPED_CHEST -> true}
     *     {@code "cOnTaINS:dYe" -> GREEN_DYE, YELLOW_DYE, BLUE_DYE, INK_SACK -> true}
     * </pre>
     * <p>
     * <b>{@code REGEX} Examples</b>
     * <pre>
     *     {@code "REGEX:^.+_.+_.+$" -> Every Material with 3 underlines or more: SHULKER_SPAWN_EGG, SILVERFISH_SPAWN_EGG, SKELETON_HORSE_SPAWN_EGG}
     *     {@code "REGEX:^.{1,3}$" -> Material names that have 3 letters only: BED, MAP, AIR}
     * </pre>
     * <p>
     * The reason that there are tags for {@code CONTAINS} and {@code REGEX} is for the performance. Please avoid using
     * the {@code REGEX} tag if you can use the {@code CONTAINS} tag. It'll have a huge impact on performance. Please
     * avoid using {@code (capturing groups)} there's no use for them in this case. If you want to use groups, use
     * {@code (?: non-capturing groups)}. It's faster.
     * <p>
     * You can make a cache for pre-compiled RegEx patterns from your config. It's better, but not much faster since
     * these patterns are not that complex.
     * <p>
     * Want to learn RegEx? You can mess around in <a href="https://regexr.com/">RegExr</a> website.
     *
     * @param material  the base material to match other materials with.
     * @param materials the material names to check base material on.
     * @return true if one of the given material names is similar to the base material.
     *
     * @since 3.1.1
     */
    public static boolean isOneOf(@Nonnull Material material, @Nullable List<String> materials)
    {
        if (materials == null || materials.isEmpty())
            return false;
        Objects.requireNonNull(material, "Cannot match materials with a null material");
        String name = material.name();

        for (String comp : materials)
        {
            comp = comp.toUpperCase();
            if (comp.startsWith("CONTAINS:"))
            {
                comp = format(comp.substring(9));
                if (name.contains(comp))
                    return true;
                continue;
            }
            if (comp.startsWith("REGEX:"))
            {
                comp = comp.substring(6);
                if (name.matches(comp))
                    return true;
                continue;
            }

            // Direct Object Equals
            Optional<XMaterial> mat = matchXMaterial(comp);
            if (mat.isPresent() && mat.get().parseMaterial() == material)
                return true;
        }
        return false;
    }

    /**
     * Gets the version which this material was added in. If the material doesn't have a version it'll return 0;
     *
     * @return the Minecraft version which tihs material was added in.
     *
     * @since 3.0.0
     */
    public int getMaterialVersion()
    {
        if (this.legacy.length == 0)
            return 0;
        String version = this.legacy[0];
        if (version.charAt(1) != '.')
            return 0;

        return Integer.parseInt(version.substring(2));
    }

    /**
     * Sets the {@link Material} (and data value on older versions) of an item. Damageable materials will not have their
     * durability changed.
     * <p>
     * Use {@link #parseItem()} instead when creating new ItemStacks.
     *
     * @param item the item to change its type.
     * @see #parseItem()
     * @since 3.0.0
     */
    @Nonnull
    @SuppressWarnings("deprecation")
    public ItemStack setType(@Nonnull ItemStack item)
    {
        Objects.requireNonNull(item, "Cannot set material for null ItemStack");

        item.setType(this.parseMaterial());
        if (!ISFLAT && !this.isDamageable())
            item.setDurability(this.data);
        return item;
    }

    /**
     * Checks if the list of given material names matches the given base material. Mostly used for configs.
     *
     * @param materials the material names to check base material on.
     * @return true if one of the given material names is similar to the base material.
     *
     * @see #isOneOf(Material, List)
     * @since 3.0.0
     */
    public boolean isOneOf(@Nullable List<String> materials)
    {
        Material material = this.parseMaterial();
        if (material == null)
            return false;
        return isOneOf(material, materials);
    }

    /**
     * Checks if the given string matches any of this material's legacy material names. All the values passed to this
     * method will not be null or empty and are formatted correctly.
     *
     * @param name the name to check
     * @return true if it's one of the legacy names.
     *
     * @since 2.0.0
     */
    private boolean anyMatchLegacy(@Nonnull String name)
    {
        for (String legacy : this.legacy)
        {
            if (legacy.isEmpty())
                break; // Left-side suggestion list
            if (name.equals(legacy))
                return true;
        }
        return false;
    }

    /**
     * User-friendly readable name for this material In most cases you should be using {@link #name()} instead.
     *
     * @return string of this object.
     *
     * @see #toWord(String)
     * @since 3.0.0
     */
    @Override
    public String toString()
    {
        return toWord(this.name());
    }

    /**
     * Gets the ID (Magic value) of the material.
     *
     * @return the ID of the material or <b>-1</b> if it's a new block or the material is not supported.
     *
     * @see #matchXMaterial(int, byte)
     * @since 2.2.0
     */
    @SuppressWarnings("deprecation")
    public int getId()
    {
        if (this.data != 0 || (this.legacy.length != 0 && Integer.parseInt(this.legacy[0].substring(2)) >= 13))
            return -1;
        Material material = this.parseMaterial();
        return material == null ? -1 : material.getId();
    }

    /**
     * Checks if the material has any duplicates.
     *
     * @return true if there is a duplicated name for this material, otherwise false.
     *
     * @see #isDuplicated(String)
     * @since 2.0.0
     */
    public boolean isDuplicated()
    {
        return false;
    }

    /**
     * Checks if the material can be damaged by using it. Names going through this method are not formatted.
     *
     * @return true if the item can be damaged (have its durability changed), otherwise false.
     *
     * @see #isDamageable(String)
     * @since 1.0.0
     */
    public boolean isDamageable()
    {
        return isDamageable(this.name());
    }

    /**
     * The data value of this material <a href="https://minecraft.gamepedia.com/Java_Edition_data_values/Pre-flattening">pre-flattening</a>.
     * <p>
     * Can be accessed with {@link ItemStack#getData()} then {@code MaterialData#getData()} or {@link
     * ItemStack#getDurability()} if not damageable.
     *
     * @return data of this material, or 0 if none.
     *
     * @since 1.0.0
     */
    @SuppressWarnings("deprecation")
    public byte getData()
    {
        return data;
    }

    /**
     * Get a list of materials names that was previously used by older versions. If the material was added in a new
     * version {@link #isNewVersion()}, then the first element will indicate which version the material was added in.
     *
     * @return a list of legacy material names and the first element as the version the material was added in if new.
     *
     * @since 1.0.0
     */
    @Nonnull
    public String[] getLegacy()
    {
        return legacy;
    }

    /**
     * Parses an item from this XMaterial. Uses data values on older versions.
     *
     * @return an ItemStack with the same material (and data value if in older versions.)
     *
     * @see #parseItem(boolean)
     * @see #setType(ItemStack)
     * @since 1.0.0
     */
    @Nullable
    public ItemStack parseItem()
    {
        return parseItem(false);
    }

    /**
     * Parses an item from this XMaterial. Uses data values on older versions.
     *
     * @param suggest if true {@link #parseMaterial(boolean)} true will be used.
     * @return an ItemStack with the same material (and data value if in older versions.)
     *
     * @see #setType(ItemStack)
     * @since 2.0.0
     */
    @Nullable
    @SuppressWarnings("deprecation")
    public ItemStack parseItem(boolean suggest)
    {
        Material material = this.parseMaterial(suggest);
        if (material == null)
            return null;
        return ISFLAT ? new ItemStack(material) : new ItemStack(material, 1, this.data);
    }

    /**
     * Parses the material of this XMaterial.
     *
     * @return the material related to this XMaterial based on the server version.
     *
     * @see #parseMaterial(boolean)
     * @since 1.0.0
     */
    @Nullable
    public Material parseMaterial()
    {
        return parseMaterial(false);
    }

    /**
     * Parses the material of this XMaterial and accepts suggestions.
     *
     * @param suggest use a suggested material (from older materials) if the material is added in a later version of
     *                Minecraft.
     * @return the material related to this XMaterial based on the server version.
     *
     * @since 2.0.0
     */
    @SuppressWarnings("OptionalAssignedToNull")
    @Nullable
    public Material parseMaterial(boolean suggest)
    {
        Optional<Material> cache = PARSED_CACHE.getIfPresent(this);
        if (cache != null)
            return cache.orElse(null);
        Material mat;

        if (!ISFLAT && this.isDuplicated())
            mat = requestOldMaterial(suggest);
        else
        {
            mat = Material.getMaterial(this.name());
            if (mat == null)
                mat = requestOldMaterial(suggest);
        }

        if (mat != null)
            PARSED_CACHE.put(this, Optional.ofNullable(mat));
        return mat;
    }

    /**
     * Parses a material for older versions of Minecraft. Accepts suggestions if specified.
     *
     * @param suggest if true suggested materials will be considered for old versions.
     * @return a parsed material suitable for the current Minecraft version.
     *
     * @see #parseMaterial(boolean)
     * @since 2.0.0
     */
    @Nullable
    private Material requestOldMaterial(boolean suggest)
    {
        for (int i = this.legacy.length - 1; i >= 0; i--)
        {
            String legacy = this.legacy[i];

            // Check if we've reached the end and the last string is our
            // material version.
            if (i == 0 && legacy.charAt(1) == '.')
                return null;

            // According to the suggestion list format, all the other names continuing
            // from here are considered as a "suggestion"
            // The empty string is an indicator for suggestion list on the left side.
            if (legacy.isEmpty())
            {
                if (suggest) continue;
                break;
            }

            Material material = Material.getMaterial(legacy);
            if (material != null)
                return material;
        }
        return null;
    }

    /**
     * Checks if an item has the same material (and data value on older versions).
     *
     * @param item item to check.
     * @return true if the material is the same as the item's material (and data value if on older versions), otherwise
     * false.
     *
     * @since 1.0.0
     */
    @SuppressWarnings("deprecation")
    public boolean isSimilar(@Nonnull ItemStack item)
    {
        Objects.requireNonNull(item, "Cannot compare with null ItemStack");
        if (item.getType() != this.parseMaterial())
            return false;
        return ISFLAT || this.isDamageable() || item.getDurability() == this.data;
    }

    /**
     * Gets the suggested material names that can be used if the material is not supported in the current version.
     *
     * @return a list of suggested material names.
     *
     * @see #parseMaterial(boolean)
     * @since 2.0.0
     */
    @Nonnull
    public List<String> getSuggestions()
    {
        if (this.legacy.length == 0 || this.legacy[0].charAt(1) != '.')
            return new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        for (String legacy : this.legacy)
        {
            if (legacy.isEmpty())
                break;
            suggestions.add(legacy);
        }
        return suggestions;
    }

    /**
     * Checks if this material is supported in the current version. Suggested materials will be ignored.
     * <p>
     * Note that you should use {@link #parseMaterial()} and check if it's null if you're going to parse and use the
     * material later.
     *
     * @return true if the material exists in {@link Material} list.
     *
     * @since 2.0.0
     */
    public boolean isSupported()
    {
        int version = this.getMaterialVersion();
        if (version != 0)
            return supports(version);

        Material material = Material.getMaterial(this.name());
        if (material != null)
            return true;
        return requestOldMaterial(false) != null;
    }

    /**
     * Checks if the material is newly added after the 1.13 Aquatic Update.
     *
     * @return true if the material was newly added, otherwise false.
     *
     * @see #getMaterialVersion()
     * @since 2.0.0
     */
    public boolean isFromNewSystem()
    {
        return this.legacy.length != 0 && Integer.parseInt(this.legacy[0].substring(2)) > 13;
    }
}




