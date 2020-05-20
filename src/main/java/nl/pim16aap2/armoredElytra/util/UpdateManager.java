package nl.pim16aap2.armoredElytra.util;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.UpdateChecker.UpdateReason;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Level;

/**
 * @author Pim
 */
public final class UpdateManager
{
    private final ArmoredElytra plugin;
    private boolean checkForUpdates = false;
    private boolean downloadUpdates = false;
    private boolean updateDownloaded = false;

    private UpdateChecker updater;
    private BukkitTask updateRunner = null;

    public UpdateManager(final ArmoredElytra plugin, final int pluginID)
    {
        this.plugin = plugin;
        updater = UpdateChecker.init(plugin, pluginID);
    }

    public void setEnabled(final boolean newCheckForUpdates, final boolean newDownloadUpdates)
    {
        checkForUpdates = newCheckForUpdates;
        downloadUpdates = newDownloadUpdates;
        initUpdater();
    }

    public boolean hasUpdateBeenDownloaded()
    {
        return updateDownloaded;
    }

    public String getNewestVersion()
    {
        if (!checkForUpdates || updater.getLastResult() == null)
            return null;
        return updater.getLastResult().getNewestVersion();
    }

    public boolean updateAvailable()
    {
        // Updates disabled, so no new updates available by definition.
        if (!checkForUpdates || updater.getLastResult() == null)
            return false;

        // There's a newer version available.
        if (updater.getLastResult().requiresUpdate())
            return true;

        // The plugin is "up-to-date", but this is a dev-build, so it must be newer.
        if (updater.getLastResult().getReason().equals(UpdateReason.UP_TO_DATE))
            return true;

        return false;
    }

    public void checkForUpdates()
    {
        updater.requestUpdateCheck().whenComplete(
            (result, throwable) ->
            {
                boolean updateAvailable = updateAvailable();
                if (updateAvailable)
                    plugin.myLogger(Level.INFO,
                                    "A new update is available: " + plugin.getUpdateManager().getNewestVersion());

                if (downloadUpdates && updateAvailable)
                {
                    updateDownloaded = updater.downloadUpdate();
                    if (updateDownloaded)
                        plugin.myLogger(Level.INFO, "Update downloaded! Restart to apply it! " +
                            "New version is " + updater.getLastResult().getNewestVersion() +
                            ", Currently running " + plugin.getDescription().getVersion());
                    else
                        plugin.myLogger(Level.INFO,
                                        "Failed to download latest version! You can download it manually at: " +
                                            updater.getDownloadUrl());
                }
            });
    }

    private void initUpdater()
    {
        if (checkForUpdates)
        {
            // Run the UpdateChecker regularly.
            if (updateRunner == null)
                updateRunner = new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        checkForUpdates();
                    }
                }.runTaskTimer(plugin, 0L, 288000L); // Run immediately, then every 4 hours.
        }
        else
        {
            plugin.myLogger(Level.INFO,
                            "Plugin update checking not enabled! You will not receive any messages about new updates " +
                                "for this plugin. Please consider turning this on in the config.");
            if (updateRunner != null)
            {
                updateRunner.cancel();
                updateRunner = null;
            }
        }
    }
}
