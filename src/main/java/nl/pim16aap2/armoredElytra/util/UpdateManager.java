package nl.pim16aap2.armoredElytra.util;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
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

    private final UpdateChecker updater;
    private BukkitTask updateRunner = null;

    public UpdateManager(final ArmoredElytra plugin, final int pluginID)
    {
        this.plugin = plugin;
        updater = UpdateChecker.init(plugin, pluginID);
    }

    public void setEnabled(final boolean newCheckForUpdates)
    {
        checkForUpdates = newCheckForUpdates;
        initUpdater();
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

        return updater.getLastResult().requiresUpdate();
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
                }.runTaskTimer(plugin, 0L, 864000L); // Run immediately, then every 12 hours.
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
