package nl.pim16aap2.armoredElytra.util.messages;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import org.bukkit.ChatColor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class Messages
{
    private static final String DEFAULT_FILENAME = "en_US.txt";

    /**
     * The map of all messages.
     * <p>
     * Key: The {@link Message} enum entry.
     * <p>
     * Value: The translated message.
     */
    private final Map<Message, String> messageMap = new EnumMap<>(Message.class);

    private final ArmoredElytra plugin;
    private Path textFile;

    private static final Pattern matchDots = Pattern.compile("\\.");
    private static final Pattern matchNewLines = Pattern.compile("\\\\n");
    private static final Pattern matchColorCodes = Pattern.compile("&((?i)[0-9a-fk-or])");

    public Messages(final ArmoredElytra plugin)
    {
        this.plugin = plugin;
        writeDefaultFiles(plugin);

        String fileName = plugin.getConfigLoader().languageFile();
        fileName = fileName.endsWith(".txt") ? fileName : (fileName + ".txt");

        textFile = Path.of(plugin.getDataFolder().toURI()).resolve(fileName);

        if (!Files.exists(textFile))
        {
            plugin.myLogger(Level.WARNING, "Failed to load language file: \"" + textFile +
                "\": File not found! Using default file (\"" + DEFAULT_FILENAME + "\") instead!");
            textFile = Path.of(plugin.getDataFolder().toURI()).resolve(DEFAULT_FILENAME);
        }
        populateMessageMap();
    }

    /**
     * Processes the contents of a file. Each valid line will be split up in the message key and the message value. It
     * then
     *
     * @param br
     *     The {@link BufferedReader} that supplies the text.
     * @param action
     *     The action to take for every message and value combination that is encountered.
     *
     * @throws IOException
     */
    private void processFile(final BufferedReader br, final BiConsumer<Message, String> action)
        throws IOException
    {
        String sCurrentLine;

        while ((sCurrentLine = br.readLine()) != null)
        {
            // Ignore comments.
            if (sCurrentLine.startsWith("#") || sCurrentLine.isEmpty())
                continue;

            String[] parts = sCurrentLine.split("=", 2);
            try
            {
                final Message msg = Message.valueOf(matchDots.matcher(parts[0]).replaceAll("_").toUpperCase());
                final String value = matchNewLines.matcher(matchColorCodes.matcher(parts[1]).replaceAll("\u00A7$1"))
                                                  .replaceAll("\n");
                action.accept(msg, value);
            }
            catch (IllegalArgumentException e)
            {
                plugin.myLogger(Level.WARNING, "Failed to identify Message corresponding to key: \"" + parts[0] +
                    "\". Its value will be ignored!");

                System.out.println(
                    "Trying to find enum value of: " + matchDots.matcher(parts[0]).replaceAll("_").toUpperCase());
            }
        }
    }

    /**
     * Adds a message to the {@link #messageMap}.
     *
     * @param message
     *     The {@link Message}.
     * @param value
     *     The value of the message.
     */
    private void addMessage(final Message message, final String value)
    {
        messageMap.put(message, message.getDefaultColor() + ChatColor.translateAlternateColorCodes('&', value));
    }

    /**
     * Adds a message to the {@link #messageMap} if it isn't on the map already.
     *
     * @param message
     *     The {@link Message}.
     * @param value
     *     The value of the message.
     */
    private void addBackupMessage(final Message message, final String value)
    {
        if (messageMap.containsKey(message))
            return;

        plugin.myLogger(Level.WARNING,
                        "Could not find translation of key: \"" + message.name() + "\". Using default value instead!");
        addMessage(message, value);
    }

    /**
     * Reads the translations from the provided translations file.
     * <p>
     * Missing translations will use their default value.
     */
    private void populateMessageMap()
    {
        try (BufferedReader br = Files.newBufferedReader(textFile, StandardCharsets.UTF_8))
        {
            processFile(br, this::addMessage);
        }
        catch (FileNotFoundException e)
        {
            plugin.myLogger(Level.SEVERE, "Locale file \"" + textFile + "\" does not exist!");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            plugin.myLogger(Level.SEVERE, "Could not read locale file! \"" + textFile + "\"");
            e.printStackTrace();
        }

        final URL defaultFileUrl = Objects.requireNonNull(getClass().getClassLoader().getResource(DEFAULT_FILENAME));
        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(defaultFileUrl.openStream(), StandardCharsets.UTF_8)))
        {
            processFile(br, this::addBackupMessage);
        }
        catch (FileNotFoundException e)
        {
            plugin.myLogger(Level.SEVERE, "Failed to load internal locale file!");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            plugin.myLogger(Level.SEVERE, "Could not read internal locale file!");
            e.printStackTrace();
        }

        for (final Message msg : Message.values())
            if (!msg.equals(Message.EMPTY) && !messageMap.containsKey(msg))
            {
                plugin.myLogger(Level.WARNING, "Could not find translation of key: " + msg.name());
                messageMap.put(msg, getFailureString(msg.name()));
            }
    }

    /**
     * Gets the default String to return in case a value could not be found for a given String.
     *
     * @param key
     *     The key that could not be resolved.
     *
     * @return The default String to return in case a value could not be found for a given String.
     */
    private String getFailureString(final String key)
    {
        return "Translation for key \"" + key + "\" not found! Contact server admin!";
    }

    /**
     * Gets the translated message of the provided {@link Message} and substitutes its variables for the provided
     * values.
     *
     * @param msg
     *     The {@link Message} to translate.
     * @param values
     *     The values to substitute for the variables in the message.
     *
     * @return The translated message of the provided {@link Message} and substitutes its variables for the provided
     * values.
     */
    public String getString(final Message msg, final String... values)
    {
        if (msg.equals(Message.EMPTY))
            return "";

        if (values.length != Message.getVariableCount(msg))
        {
            plugin.myLogger(Level.SEVERE,
                            "Expected " + Message.getVariableCount(msg) + " variables for key " + msg.name() +
                                " but only got " + values.length + ". This is a bug. Please contact pim16aap2!");
            return getFailureString(msg.name());
        }

        String value = messageMap.get(msg);
        if (value != null)
        {
            for (int idx = 0; idx != values.length; ++idx)
                value = insertVariable(value, Message.getVariableName(msg, idx), values[idx]);
            return value;
        }

        plugin.myLogger(Level.WARNING, "Failed to get the translation for key " + msg.name());
        return getFailureString(msg.name());
    }

    /**
     * Inserts a variable into a base string. The variable is inserted at the first occurrence of the variable name.
     * <p>
     * This method uses {@link ChatColor#getLastColors(String)} to determine the color of the string before the variable
     * is inserted. This color is then reinserted after the variable is inserted to ensure the color of the string is
     * consistent.
     *
     * @param base
     *     The base string.
     * @param variable
     *     The name of the variable to insert. E.g. "%ARMOR_TIER%".
     * @param value
     *     The value to insert. E.g. "Diamond".
     *
     * @return The base string with the variable inserted.
     */
    static String insertVariable(String base, String variable, String value)
    {
        final int idx = base.indexOf(variable);
        if (idx == -1)
            return base;

        String color = ChatColor.getLastColors(base.substring(0, idx));
        if (color.isEmpty())
            color = ChatColor.RESET.toString();

        return base.replaceFirst(variable, value + color);
    }

    private static void writeDefaultFiles(ArmoredElytra plugin)
    {
        try
        {
            getPackagedLocalizations(plugin).forEach(file -> saveResource(plugin, file, false));
        }
        catch (IOException e)
        {
            plugin.myLogger(Level.SEVERE, "Failed to write default localization files!");
            e.printStackTrace();
        }
    }

    /**
     * Saves a resource embedded in the jar to the plugin's data folder.
     * <p>
     * If the resource already exists and replace is false, no action is taken. This is different from
     * {@link ArmoredElytra#saveResource(String, boolean)} because that method will print a warning if the file already
     * exists and replace is false.
     *
     * @param plugin
     *     The plugin instance.
     * @param resourcePath
     *     The path to the resource in the jar.
     * @param replace
     *     Whether to replace the file if it already exists.
     */
    private static void saveResource(ArmoredElytra plugin, String resourcePath, boolean replace)
    {
        File outFile = new File(plugin.getDataFolder(), resourcePath);
        if (!replace && outFile.exists())
            return;
        plugin.saveResource(resourcePath, replace);
    }

    private static List<String> getPackagedLocalizations(ArmoredElytra plugin)
        throws IOException
    {
        final List<String> files = new ArrayList<>();

        final Enumeration<URL> enumeration = plugin.getClass().getClassLoader().getResources("META-INF");
        while (enumeration.hasMoreElements())
        {
            final URL url = enumeration.nextElement();
            final JarURLConnection connection = (JarURLConnection) url.openConnection();
            try (JarFile jarFile = connection.getJarFile())
            {
                final Enumeration<JarEntry> jarEntries = jarFile.entries();
                while (jarEntries.hasMoreElements())
                {
                    final String name = jarEntries.nextElement().getName();
                    if (!name.contains("/") && (name.endsWith(".txt")))
                        files.add(name);
                }
            }
        }
        return files;
    }
}
