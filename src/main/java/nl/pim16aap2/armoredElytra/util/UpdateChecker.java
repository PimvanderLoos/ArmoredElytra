package nl.pim16aap2.armoredElytra.util;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import nl.pim16aap2.armoredElytra.ArmoredElytra;
import org.bukkit.plugin.java.JavaPlugin;
import org.semver4j.Semver;
import org.semver4j.SemverException;
import org.semver4j.internal.Coerce;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * A utility class to assist in checking for updates for plugins uploaded to
 * <a href="https://spigotmc.org/resources/">SpigotMC</a>. Before any members of
 * this class are accessed, {@link #init(ArmoredElytra, int)} must be invoked by the plugin, preferably in its
 * {@link JavaPlugin#onEnable()} method, though that is not a requirement.
 * <p>
 * This class performs asynchronous queries to
 * <a href="https://spiget.org">SpiGet</a>, an REST server which is updated
 * periodically. If the results of {@link #requestUpdateCheck()} are inconsistent with what is published on SpigotMC, it
 * may be due to SpiGet's cache. Results will be updated in due time.
 * <p>
 * Some modifications were made to support downloading of updates and storing the age of an update.
 *
 * @author Parker Hawke - 2008Choco
 */
public final class UpdateChecker
{
    private static final String USER_AGENT = "ArmoredElytra-update-checker";
    private static final String UPDATE_URL = "https://api.spiget.org/v2/resources/%d/versions?size=1&sort=-releaseDate";

    private static UpdateChecker instance;

    private UpdateResult lastResult = null;

    private final ArmoredElytra plugin;
    private final int pluginID;

    private UpdateChecker(final ArmoredElytra plugin, final int pluginID)
    {
        this.plugin = plugin;
        this.pluginID = pluginID;
    }

    /**
     * Requests an update check to SpiGet. This request is asynchronous and may not complete immediately as an HTTP GET
     * request is published to the SpiGet API.
     *
     * @return a future update result
     */
    public CompletableFuture<UpdateResult> requestUpdateCheck()
    {
        return CompletableFuture.supplyAsync(
            () ->
            {
                int responseCode;
                try
                {
                    URL url = new URL(String.format(UPDATE_URL, pluginID));
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.addRequestProperty("User-Agent", USER_AGENT);

                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    responseCode = connection.getResponseCode();

                    if (responseCode != 200)
                        return new UpdateResult(responseCode == 401 ?
                                                UpdateReason.UNAUTHORIZED_QUERY : UpdateReason.UNKNOWN_ERROR);

                    JsonElement element = JsonParser.parseReader(reader);
                    if (!element.isJsonArray())
                        return new UpdateResult(UpdateReason.INVALID_JSON);

                    reader.close();

                    JsonObject versionObject = element.getAsJsonArray().get(0).getAsJsonObject();

                    long age = -1;
                    String ageString = versionObject.get("releaseDate").getAsString();
                    try
                    {
                        age = getAge(Long.parseLong(ageString));
                    }
                    catch (NumberFormatException e)
                    {
                        plugin.myLogger(Level.WARNING,
                                        "Failed to obtain age of update from ageString: \"" + ageString + "\"");
                    }

                    final String currentString = plugin.getDescription().getVersion();
                    final String latestAvailableString = versionObject.get("name").getAsString();

                    final Semver current = parseVersion(currentString);
                    final Semver latestAvailable = parseVersion(latestAvailableString);

                    if (current == null || latestAvailable == null)
                        return new UpdateResult(UpdateReason.UNSUPPORTED_VERSION_SCHEME);

                    if (latestAvailable.isGreaterThan(current))
                        return new UpdateResult(UpdateReason.NEW_UPDATE, latestAvailableString, age);

                    return new UpdateResult(current.equals(latestAvailable) ?
                                            UpdateReason.UP_TO_DATE :
                                            UpdateReason.UNRELEASED_VERSION, currentString, age);
                }
                catch (IOException e)
                {
                    return new UpdateResult(UpdateReason.COULD_NOT_CONNECT);
                }
                catch (JsonSyntaxException e)
                {
                    return new UpdateResult(UpdateReason.INVALID_JSON);
                }
            });
    }

    /**
     * Gets the difference in seconds between a given time and the current time.
     *
     * @param updateTime
     *     A moment in time to compare the current time to.
     *
     * @return The difference in seconds between a given time and the current time.
     */
    private long getAge(final long updateTime)
    {
        long currentTime = Instant.now().getEpochSecond();
        return currentTime - updateTime;
    }

    /**
     * Gets the last update result that was queried by {@link #requestUpdateCheck()}. If no update check was performed
     * since this class' initialization, this method will return null.
     *
     * @return the last update check result. null if none.
     */
    public UpdateResult getLastResult()
    {
        return lastResult;
    }

    /**
     * Initializes this update checker with the specified values and return its instance. If an instance of
     * UpdateChecker has already been initialized, this method will act similarly to {@link #get()} (which is
     * recommended after initialization).
     *
     * @param plugin
     *     the plugin for which to check updates. Cannot be null
     * @param pluginID
     *     the ID of the plugin as identified in the SpigotMC resource link. For example,
     *     "https://www.spigotmc.org/resources/veinminer.<b>12038</b>/" would expect "12038" as a value. The value must
     *     be greater than 0
     *
     * @return the UpdateChecker instance
     */
    public static synchronized UpdateChecker init(final ArmoredElytra plugin, final int pluginID)
    {
        Preconditions.checkArgument(pluginID > 0, "Plugin ID must be greater than 0");

        return (instance == null) ? instance = new UpdateChecker(plugin, pluginID) : instance;
    }

    /**
     * Gets the initialized instance of UpdateChecker. If {@link #init(ArmoredElytra, int)} has not yet been invoked,
     * this method will throw an exception.
     *
     * @return the UpdateChecker instance
     */
    public static UpdateChecker get()
    {
        Preconditions.checkState(instance != null,
                                 "Instance has not yet been initialized. Be sure #init() has been invoked");
        return instance;
    }

    /**
     * Checks whether the UpdateChecker has been initialized or not (if {@link #init(ArmoredElytra, int)} has been
     * invoked) and {@link #get()} is safe to use.
     *
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized()
    {
        return instance != null;
    }

    static Semver parseVersion(String version)
    {
        String suffix = null;
        int dashIndex = version.indexOf('-');
        if (dashIndex != -1)
        {
            suffix = version.substring(dashIndex + 1);
            version = version.substring(0, dashIndex);
        }

        String coerced = Coerce.coerce(version);
        if (coerced == null)
            return null;

        if (suffix != null)
            coerced = coerced + "-" + suffix;

        try
        {
            return new Semver(coerced);
        }
        catch (SemverException e)
        {
            return null;
        }
    }

    /**
     * A constant reason for the result of {@link UpdateResult}.
     */
    public enum UpdateReason
    {

        /**
         * A new update is available for download.
         * <p>
         * This is the only reason that requires an update.
         */
        NEW_UPDATE,

        /**
         * A successful connection to the SpiGet API could not be established.
         */
        COULD_NOT_CONNECT,

        /**
         * The JSON retrieved from SpiGet was invalid or malformed.
         */
        INVALID_JSON,

        /**
         * A 401 error was returned by the SpiGet API.
         */
        UNAUTHORIZED_QUERY,

        /**
         * The version of the plugin installed on the server is greater than the one uploaded to SpigotMC's resources
         * section.
         */
        UNRELEASED_VERSION,

        /**
         * An unknown error occurred.
         */
        UNKNOWN_ERROR,

        /**
         * The plugin uses an unsupported version scheme, therefore a proper comparison between versions could not be
         * made.
         */
        UNSUPPORTED_VERSION_SCHEME,

        /**
         * The plugin is up to date with the version released on SpigotMC's resources section.
         */
        UP_TO_DATE

    }

    /**
     * Represents a result for an update query performed by {@link UpdateChecker#requestUpdateCheck()}.
     */
    public final class UpdateResult
    {
        private final UpdateReason reason;
        private final String newestVersion;
        private final long age;

        {
            lastResult = this;
        }

        private UpdateResult(final UpdateReason reason, final String newestVersion, final long age)
        {
            this.reason = reason;
            this.newestVersion = newestVersion;
            this.age = age;
        }

        private UpdateResult(final UpdateReason reason)
        {
            Preconditions
                .checkArgument(reason != UpdateReason.NEW_UPDATE && reason != UpdateReason.UP_TO_DATE,
                               "Reasons that might require updates must also provide the latest version String");
            this.reason = reason;
            newestVersion = plugin.getDescription().getVersion();
            age = -1;
        }

        /**
         * Gets the constant reason of this result.
         *
         * @return the reason
         */
        public UpdateReason getReason()
        {
            return reason;
        }

        /**
         * Checks whether or not this result requires the user to update.
         *
         * @return true if requires update, false otherwise
         */
        public boolean requiresUpdate()
        {
            return reason == UpdateReason.NEW_UPDATE;
        }

        /**
         * Gets the latest version of the plugin. This may be the currently installed version, it may not be. This
         * depends entirely on the result of the update.
         *
         * @return the newest version of the plugin
         */
        public String getNewestVersion()
        {
            return newestVersion;
        }

        /**
         * Gets the number of seconds since the last update was released.
         *
         * @return The number of seconds since the last update was released or -1 if unavailable.
         */
        public long getAge()
        {
            return age;
        }
    }
}
