package nl.pim16aap2.armoredElytra.nbtEditor;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.persistence.PersistentDataType;

final class TrimEditor
{
    private static final NamespacedKey ARMOR_TRIM_KEY = new NamespacedKey(ArmoredElytra.getInstance(), "armor_trim");

    void copyArmorTrim(ItemMeta elytraMeta, ItemStack chestPlate)
    {
        final ArmorMeta chestPlateMeta = (ArmorMeta) chestPlate.getItemMeta();
        if (chestPlateMeta == null)
            return;

        final ArmorTrim trim = chestPlateMeta.getTrim();
        if (trim == null)
            return;

        applyArmorTrim(elytraMeta, trim);
    }

    /**
     * Applies the given {@link ArmorTrim} to the given {@link ItemMeta}.
     *
     * @param elytraMeta
     *     The {@link ItemMeta} to apply the {@link ArmorTrim} to.
     * @param armorTrim
     *     The {@link ArmorTrim} to apply.
     *
     * @return The modified {@link ItemMeta}.
     */
    private void applyArmorTrim(ItemMeta elytraMeta, ArmorTrim armorTrim)
    {
        final ArmorMeta armorMeta = (ArmorMeta) Bukkit.getItemFactory().getItemMeta(Material.IRON_CHESTPLATE);
        if (armorMeta == null)
            throw new IllegalStateException("Failed to create ArmorMeta for iron chest plate!");

        armorMeta.setTrim(armorTrim);

        final var container = elytraMeta.getPersistentDataContainer();
        container.set(ARMOR_TRIM_KEY, PersistentDataType.STRING, armorMeta.getAsString());
    }
}
