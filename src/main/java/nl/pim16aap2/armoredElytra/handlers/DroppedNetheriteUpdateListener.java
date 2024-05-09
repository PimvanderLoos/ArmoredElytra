package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

/**
 * Listens for dropped armored elytras of the netherite tier and updates them to use the new fire resistance attribute.
 */
public class DroppedNetheriteUpdateListener implements Listener
{
    private final NBTEditor nbtEditor;

    public DroppedNetheriteUpdateListener(NBTEditor nbtEditor)
    {
        this.nbtEditor = nbtEditor;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrop(final ItemSpawnEvent event)
    {
        final var entity = event.getEntity();
        final var item = entity.getItemStack();

        if (nbtEditor.updateFireResistance(item))
            entity.setItemStack(item);
    }
}
