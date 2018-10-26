package nl.pim16aap2.armoredElytra.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import nl.pim16aap2.armoredElytra.ArmoredElytra;

public class Update
{

    // The project's unique ID
    private final int projectID;

    // An optional API key to use, will be null if not submitted
    private final String apiKey;

    // Keys for extracting file information from JSON response
    private static final String API_NAME_VALUE = "name";

    // Static information for querying the API
    private static final String API_QUERY = "/servermods/files?projectIds=";
    private static final String API_HOST = "https://api.curseforge.com";

    private String versionName;
    ArmoredElytra plugin;

    /**
     * Check for updates anonymously (keyless)
     *
     * @param projectID The BukkitDev Project ID, found in the "Facts" panel on the
     *                  right-side of your project page.
     */
    public Update(int projectID, ArmoredElytra plugin)
    {
        this(projectID, null, plugin);
    }

    /**
     * Check for updates using your Curse account (with key)
     *
     * @param projectID The BukkitDev Project ID, found in the "Facts" panel on the
     *                  right-side of your project page.
     * @param apiKey    Your ServerMods API key, found at
     *                  https://dev.bukkit.org/home/servermods-apikey/
     */
    public Update(int projectID, String apiKey, ArmoredElytra plugin)
    {
        this.projectID = projectID;
        this.apiKey = apiKey;
        this.plugin = plugin;

        query();
    }

    public int versionCompare(String str1, String str2)
    {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i]))
            i++;
        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length)
        {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        return Integer.signum(vals1.length - vals2.length);
    }

    // Get the latest version of the plugin.
    public String getLatestVersion()
    {
        if (versionName == null)
            return null;
        return versionName.replaceAll("Armored Elytra ", "");
    }

    /**
     * Query the API to find the latest approved file's details.
     */
    public void query()
    {
        URL url = null;

        try
        {
            // Create the URL to query using the project's ID
            url = new URL(API_HOST + API_QUERY + projectID);
        }
        catch (MalformedURLException e)
        {
            // There was an error creating the URL

            e.printStackTrace();
            return;
        }

        try
        {
            // Open a connection and query the project
            URLConnection conn = url.openConnection();

            if (apiKey != null)
                // Add the API key to the request if present
                conn.addRequestProperty("X-API-Key", apiKey);

            // Add the user-agent to identify the program
            conn.addRequestProperty("User-Agent", "ServerModsAPI-Example (by Gravity)");

            // Read the response of the query
            // The response will be in a JSON format, so only reading one line is necessary.
            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = reader.readLine();

            // Parse the array of files from the query's response
            JSONArray array = (JSONArray) JSONValue.parse(response);

            if (array.size() > 0)
            {
                // Get the newest file's details
                JSONObject latest = (JSONObject) array.get(array.size() - 1);

                // Get the version's title
                this.versionName = (String) latest.get(API_NAME_VALUE);
            }
        }
        catch (IOException e)
        {
            // There was an error reading the query.
            // Does not print stacktrace, so people won't see any errors from this plugin
            // when Bukkit Dev's servers are down,
            // So people won't think the plugin is broken, while the actualy problem is
            // much, much smaller. latestVersion will be null, though, which will prompt a
            // warning in the log instead.

//            e.printStackTrace();
            return;
        }
    }
}