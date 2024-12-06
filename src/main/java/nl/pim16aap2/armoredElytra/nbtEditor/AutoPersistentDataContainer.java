package nl.pim16aap2.armoredElytra.nbtEditor;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Represents a PersistentDataContainer that automatically saves the data to the item meta when closed.
 * <p>
 * When the object is closed, the data is saved to the item meta.
 * <p>
 * It can be used as follows:
 * <pre>
 * {@code
 * try (var apdc = new AutoPersistentDataContainer(item)) {
 *     apdc.set(key, type, value);
 *     // Do stuff with the data.
 * } // The data is now saved to the item.
 * }</pre>
 */
public class AutoPersistentDataContainer implements AutoCloseable, PersistentDataContainer
{
    private final ItemStack itemStack;
    private final ItemMeta meta;
    private final PersistentDataContainer pdc;

    public AutoPersistentDataContainer(ItemStack itemStack)
    {
        this.itemStack = itemStack;
        this.meta = NBTEditor.getOrCreateItemMeta(itemStack);
        this.pdc = meta.getPersistentDataContainer();
    }

    @Override
    public void close()
    {
        this.itemStack.setItemMeta(this.meta);
    }

    @Override
    public <P, C> void set(@Nonnull NamespacedKey key, @Nonnull PersistentDataType<P, C> type, @Nonnull C value)
    {
        pdc.set(key, type, value);
    }

    @Override
    public <P, C> boolean has(@Nonnull NamespacedKey key, @Nonnull PersistentDataType<P, C> type)
    {
        return pdc.has(key, type);
    }

    @Override
    public boolean has(@Nonnull NamespacedKey key)
    {
        return pdc.has(key);
    }

    @Override
    public @Nullable <P, C> C get(@Nonnull NamespacedKey key, @Nonnull PersistentDataType<P, C> type)
    {
        return pdc.get(key, type);
    }

    @Override
    public @Nonnull <P, C> C getOrDefault(
        @Nonnull NamespacedKey key, @Nonnull PersistentDataType<P, C> type, @Nonnull C defaultValue)
    {
        return pdc.getOrDefault(key, type, defaultValue);
    }

    @Override
    public @Nonnull Set<NamespacedKey> getKeys()
    {
        return pdc.getKeys();
    }

    @Override
    public void remove(@Nonnull NamespacedKey key)
    {
        pdc.remove(key);
    }

    @Override
    public boolean isEmpty()
    {
        return pdc.isEmpty();
    }

    @Override
    public void copyTo(@Nonnull PersistentDataContainer other, boolean replace)
    {
        pdc.copyTo(other, replace);
    }

    @Override
    public @Nonnull PersistentDataAdapterContext getAdapterContext()
    {
        return pdc.getAdapterContext();
    }
}
