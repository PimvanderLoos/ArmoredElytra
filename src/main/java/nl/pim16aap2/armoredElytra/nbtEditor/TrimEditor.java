package nl.pim16aap2.armoredElytra.nbtEditor;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

final class TrimEditor
{
    @SuppressWarnings("unused")
    private static final Map<Material, TrimPattern> TRIM_PATTERN_MAP = getTrimPatternMap();

    private static final NamespacedKey ARMOR_TRIM_KEY =
        new NamespacedKey(ArmoredElytra.getInstance(), "armor_trim");

    ItemMeta copyArmorTrim(ItemMeta elytraMeta, ItemStack chestplate)
    {
        final ArmorMeta chestplateMeta = (ArmorMeta) chestplate.getItemMeta();
        if (chestplateMeta == null)
            return elytraMeta;

        final ArmorTrim trim = chestplateMeta.getTrim();
        if (trim == null)
            return elytraMeta;

        final ArmorMeta armorMeta = (ArmorMeta) Bukkit.getItemFactory().getItemMeta(Material.IRON_CHESTPLATE);
        if (armorMeta == null)
            throw new IllegalStateException("Failed to create ArmorMeta for iron chestplate!");

        armorMeta.setTrim(trim);

        final var container = elytraMeta.getPersistentDataContainer();
        container.set(ARMOR_TRIM_KEY, PersistentDataType.STRING, armorMeta.getAsString());
        return elytraMeta;
    }

    /**
     * Returns a map of all trim templates materials and their corresponding TrimPattern.
     *
     * @return A map of all trim templates materials and their corresponding TrimPattern.
     */
    private static Map<Material, TrimPattern> getTrimPatternMap()
    {
        final var templates = Tag.ITEMS_TRIM_TEMPLATES;
        final Map<Material, TrimPattern> map = new HashMap<>(templates.getValues().size());

        for (final var template : templates.getValues())
        {
            final String name = template.name().replace("_ARMOR_TRIM_SMITHING_TEMPLATE", "");
            try
            {
                final Field patternField = TrimPattern.class.getField(name);
                final TrimPattern pattern = (TrimPattern) patternField.get(null);
                map.put(template, pattern);
            }
            catch (NoSuchFieldException | IllegalAccessException | ClassCastException e)
            {
                ArmoredElytra.getInstance().myLogger(
                    Level.SEVERE, "Failed to get TrimPattern for template: '" + name + "'!");
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }

        return map;
    }
}
