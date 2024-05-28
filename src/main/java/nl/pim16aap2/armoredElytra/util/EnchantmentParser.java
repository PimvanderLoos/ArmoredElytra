package nl.pim16aap2.armoredElytra.util;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.logging.Level;

/**
 * A class that parses enchantments from strings.
 * <p>
 * Examples of valid enchantment strings and the resulting enchantments:
 * <ul>
 *     <li>"unbreaking" -> {@link Enchantment#UNBREAKING}</li>
 *     <li>"durability" -> {@link Enchantment#UNBREAKING}</li>
 *     <li>"minecraft:unbreaking" -> {@link Enchantment#UNBREAKING}</li>
 *     <li>"minecraft:durability" -> {@link Enchantment#UNBREAKING}</li>
 * </ul>
 */
@SuppressWarnings({"UnstableApiUsage", "deprecation"})
public class EnchantmentParser
{
    private final ArmoredElytra plugin;

    public EnchantmentParser(ArmoredElytra plugin)
    {
        this.plugin = plugin;
    }

    /**
     * Parses an enchantment from a string.
     *
     * @param input
     *     The string to parse.
     *
     * @return The parsed enchantment or null if the enchantment could not be parsed.
     */
    public @Nullable Enchantment parse(@Nullable String input)
    {
        if (input == null)
            return null;
        final @Nullable Enchantment enchantment = parse0(input.toLowerCase(Locale.ROOT));
        if (enchantment == null)
            plugin.getLogger().warning("Failed to parse enchantment: '" + input + "'");
        return enchantment;
    }

    /**
     * Parses an enchantment without a namespace.
     * <p>
     * See {@link #parse0(String)} for more information.
     *
     * @param name
     *     The name of the enchantment.
     *
     * @return The parsed enchantment or null if the enchantment could not be parsed.
     */
    private @Nullable Enchantment parseWithoutNamespace(String name)
    {
        try
        {
            // getByName already takes care of remapping the enchantment names.
            return Enchantment.getByName(name);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Failed to parse enchantment without namespace: '" + name + "'", e);
        }
    }

    /**
     * Parses an enchantment from the Minecraft namespace.
     * <p>
     * If necessary, the name is remapped to the correct enchantment using the {@link RemappedEnchantment} class.
     * <p>
     * If no remapped enchantment is found, the enchantment is parsed using {@link #parseFromNamespace(String, String)}
     * with the namespace {@link NamespacedKey#MINECRAFT}.
     *
     * @param key
     *     The key of the enchantment.
     *
     * @return The parsed enchantment.
     */
    private @Nullable Enchantment parseFromMinecraftNamespace(String key)
    {
        try
        {
            for (final var remappedEnchantment : RemappedEnchantment.getRemappedEnchantments())
                if (remappedEnchantment.getNames().contains(key))
                    return remappedEnchantment.getEnchantment();
            return parseFromNamespace(NamespacedKey.MINECRAFT, key);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(
                "Failed to parse enchantment from Minecraft namespace: '" + key + "'", e);
        }
    }

    /**
     * Parses an enchantment from a namespace and key combination.
     *
     * @param namespace
     *     The namespace of the enchantment.
     * @param key
     *     The key of the enchantment.
     *
     * @return The parsed enchantment.
     *
     * @throws IllegalArgumentException
     *     If the enchantment could not be parsed.
     */
    private @Nullable Enchantment parseFromNamespace(String namespace, String key)
    {
        try
        {
            final NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
            return Enchantment.getByKey(namespacedKey);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(
                "Failed to parse enchantment from namespace: '" + namespace + ":" + key + "'", e);
        }
    }

    /**
     * Parses an enchantment from a string.
     *
     * @param input
     *     The string to parse. The input is assumed to be lowercased.
     *
     * @return The parsed enchantment or null if the enchantment could not be parsed.
     */
    private @Nullable Enchantment parse0(String input)
    {
        try
        {
            final String stripped = input.strip();
            if (stripped.isEmpty())
                return null;

            final String[] keyParts = stripped.split(":", 2);
            if (keyParts.length < 2)
                return parseWithoutNamespace(stripped);

            final String namespace = keyParts[0];
            final String key = keyParts[1];

            if (NamespacedKey.MINECRAFT.equals(namespace))
                return parseFromMinecraftNamespace(key);

            return parseFromNamespace(namespace, key);
        }
        catch (Exception e)
        {
            plugin.getLogger().log(Level.WARNING, e, () -> "Failed to parse enchantment from input: '" + input + "'");
        }
        return null;
    }
}
