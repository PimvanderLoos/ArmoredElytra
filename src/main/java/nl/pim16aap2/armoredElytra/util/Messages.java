package nl.pim16aap2.armoredElytra.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import nl.pim16aap2.armoredElytra.ArmoredElytra;

public class Messages
{
    private Map<String, String> messageMap = new HashMap<>();
    private ArmoredElytra plugin;
    private File textFile;

    public Messages(ArmoredElytra plugin)
    {
        this.plugin = plugin;
        textFile = new File(plugin.getDataFolder(), plugin.getConfigLoader().languageFile() + ".txt");
        if (!textFile.exists())
        {
            plugin.myLogger(Level.WARNING, "Failed to load language file: \"" + textFile + "\": File not found! Using default file instead!");
            textFile = new File(plugin.getDataFolder(), "en_US.txt");
        }
        readFile();
    }

    private void writeDefaultFile()
    {
        File defaultFile = new File(plugin.getDataFolder(), "en_US.txt");
        if (!defaultFile.setWritable(true))
            plugin.myLogger(Level.SEVERE, "Failed to make file \"" + defaultFile + "\" writable!");

        // Load the default en_US from the resources.
        plugin.saveResource("en_US.txt", true);
        if (!defaultFile.setWritable(false))
        {
            plugin.myLogger(Level.WARNING, "Could not set default language file to read-only!");
            plugin.myLogger(Level.WARNING, "This is not a big problem. Just remember not to edit the file!");
        }
    }

    // Read locale file.
    private void readFile()
    {
        writeDefaultFile();

        try (BufferedReader br = new BufferedReader(new FileReader(textFile)))
        {
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null)
            {
                // Ignore comments.
                if (sCurrentLine.startsWith("#"))
                    continue;
                String key, value;
                String[] parts = sCurrentLine.split("=", 2);
                key = parts[0];
                value = parts[1].replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
                String[] newLineSplitter = value.split("\\\\n");

                String values = newLineSplitter[0];

                for (int idx = 1; idx < newLineSplitter.length; ++idx)
                    values += "\n" + newLineSplitter[idx];

                messageMap.put(key, values);
            }
            br.close();
        }
        catch (FileNotFoundException e)
        {
            plugin.myLogger(Level.SEVERE, "Locale file \"" + textFile + "\" does not exist!");
        }
        catch (IOException e)
        {
            plugin.myLogger(Level.SEVERE, "Could not read locale file: \"" + textFile + "\"");
            e.printStackTrace();
        }
    }

    // Get a string from a key. Returns "null" if null.
    public String getString(String key)
    {
        String value = messageMap.get(key);
        if (value != null)
            return value;

        plugin.myLogger(Level.WARNING, "Failed to get the translation for key " + key);
        return "Translation for key \"" + key + "\" not found! Contact server admin!";
    }
}
