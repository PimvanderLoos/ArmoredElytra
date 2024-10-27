package nl.pim16aap2.armoredElytra.nbtEditor;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

final class TrimEditor
{
    @SuppressWarnings("unused")
    private static final Map<Material, TrimPattern> TRIM_PATTERN_MAP = getTrimPatternMap();
    private static final Map<Material, TrimMaterial> TRIM_MATERIAL_MAP = getTrimMaterialMap();

    private static final NamespacedKey ARMOR_TRIM_KEY =
        new NamespacedKey(ArmoredElytra.getInstance(), "armor_trim");

    void copyArmorTrim(ItemMeta elytraMeta, ItemStack chestplate)
    {
        final ArmorMeta chestplateMeta = (ArmorMeta) chestplate.getItemMeta();
        if (chestplateMeta == null)
            return;

        final ArmorTrim trim = chestplateMeta.getTrim();
        if (trim == null)
            return;

        applyArmorTrim(elytraMeta, trim);
    }

    void applyArmorTrim(ItemMeta elytraMeta, @Nullable ArmorTrimData armorTrimData)
    {
        if (armorTrimData == null)
            return;

        final ArmorTrim trim;
        try
        {
            trim = getArmorTrim(armorTrimData);
        }
        catch (Exception e)
        {
            ArmoredElytra.getInstance().myLogger(Level.SEVERE, "Failed to get ArmorTrim from data: " + armorTrimData);
            e.printStackTrace();
            return;
        }

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
            throw new IllegalStateException("Failed to create ArmorMeta for iron chestplate!");

        armorMeta.setTrim(armorTrim);

        final var container = elytraMeta.getPersistentDataContainer();
        container.set(ARMOR_TRIM_KEY, PersistentDataType.STRING, armorMeta.getAsString());
    }

    private static ArmorTrim getArmorTrim(ArmorTrimData armorTrimData)
    {
        final TrimPattern pattern = TRIM_PATTERN_MAP.get(armorTrimData.pattern());
        if (pattern == null)
            throw new IllegalArgumentException(
                "Failed to get TrimPattern for material: '" + armorTrimData.pattern() + "'!");

        final TrimMaterial material = TRIM_MATERIAL_MAP.get(armorTrimData.material());
        if (material == null)
            throw new IllegalArgumentException(
                "Failed to get TrimMaterial for material: '" + armorTrimData.material() + "'!");

        return new ArmorTrim(material, pattern);
    }

    /**
     * Returns a map of all trim templates materials and their corresponding TrimPattern.
     *
     * @return A map of all trim templates materials and their corresponding TrimPattern.
     */
    private static Map<Material, TrimPattern> getTrimPatternMap()
    {
        final var templates = Tag.ITEMS_TRIM_TEMPLATES.getValues();
        final Map<Material, TrimPattern> map = new HashMap<>(templates.size());

        for (final var template : templates)
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
                    Level.SEVERE,
                    "Failed to get TrimPattern with name: '" + name + "' for template: '" + template + "'!"
                );
            }
        }

        return map;
    }

    /**
     * Returns a map of all trim materials and their corresponding TrimMaterial.
     *
     * @return A map of all trim materials and their corresponding TrimMaterial.
     */
    @SuppressWarnings("UnstableApiUsage")
    private static Map<Material, TrimMaterial> getTrimMaterialMap()
    {
        final List<NamedTrimMaterial> namedTrimMaterialsMap = Registry
            .TRIM_MATERIAL
            .stream()
            .sorted(Comparator.comparing(material -> material.getKey().toString()))
            .map(material -> new NamedTrimMaterial(material.getKey().toString().replace("minecraft:", ""), material))
            .toList();

        final List<Material> materialsList = Tag
            .ITEMS_TRIM_MATERIALS
            .getValues()
            .stream()
            .sorted(Comparator.comparing(Enum::name))
            .toList();

        if (namedTrimMaterialsMap.size() != materialsList.size())
            throw new IllegalStateException(
                "Named trim materials map size (" + namedTrimMaterialsMap.size() + ")" +
                    " does not match the size of the tag items trim materials (" + materialsList.size() + ")!");

        final Map<Material, TrimMaterial> map = new HashMap<>(namedTrimMaterialsMap.size());

        // Hardcode all the known materials.
        map.putAll(Map.of(
            Material.AMETHYST_SHARD, TrimMaterial.AMETHYST,
            Material.COPPER_INGOT, TrimMaterial.COPPER,
            Material.DIAMOND, TrimMaterial.DIAMOND,
            Material.EMERALD, TrimMaterial.EMERALD,
            Material.GOLD_INGOT, TrimMaterial.GOLD,
            Material.IRON_INGOT, TrimMaterial.IRON,
            Material.LAPIS_LAZULI, TrimMaterial.LAPIS,
            Material.NETHERITE_INGOT, TrimMaterial.NETHERITE,
            Material.QUARTZ, TrimMaterial.QUARTZ,
            Material.REDSTONE, TrimMaterial.REDSTONE
        ));

        for (int idx = 0; idx < materialsList.size(); idx++)
        {
            final Material material = materialsList.get(idx);

            // First, we try to see if we can rely on the hardcoded values.
            if (map.get(material) != null)
                continue;

            // If we have no hardcoded mapping for this material, we try to derive the mapping from the name.
            // Both the list of named trim materials and the list of materials are sorted alphabetically.
            // The names do not always match exactly, but they usually start with the same characters.
            // For example, TrimMaterial.IRON (key = "iron") should match with Material.IRON_INGOT (key = "iron_ingot").
            final var trimMaterial = namedTrimMaterialsMap.get(idx);
            final String trimMaterialName = trimMaterial.name();

            final String longest =
                (trimMaterialName.length() > material.name().length() ? trimMaterialName : material.name());
            final String shortest =
                (trimMaterialName.length() < material.name().length() ? trimMaterialName : material.name());

            if (!longest.toLowerCase(Locale.ROOT).startsWith(shortest.toLowerCase(Locale.ROOT)))
            {
                ArmoredElytra.getInstance().myLogger(
                    Level.SEVERE, "Found unexpected trim material: '" + trimMaterialName +
                        "' for material: '" + material.name() + "'!");
                continue;
            }

            map.put(material, namedTrimMaterialsMap.get(idx).material());
        }
        return map;
    }

    private record NamedTrimMaterial(String name, TrimMaterial material)
    {}
}
