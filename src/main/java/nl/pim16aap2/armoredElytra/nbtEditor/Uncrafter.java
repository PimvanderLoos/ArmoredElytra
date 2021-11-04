package nl.pim16aap2.armoredElytra.nbtEditor;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.enchantmentcontainer.EnchantmentContainer;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.Util;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Uncrafter
{
    private final ArmoredElytra plugin;
    private final NBTEditor nbtEditor;
    private final DurabilityManager durabilityManager;

    public Uncrafter(ArmoredElytra plugin, NBTEditor nbtEditor, DurabilityManager durabilityManager)
    {
        this.plugin = plugin;
        this.nbtEditor = nbtEditor;
        this.durabilityManager = durabilityManager;
    }

    /**
     * See {@link #uncraft(ItemStack, ArmorTier)}.
     */
    public UncraftingResult uncraft(ItemStack armoredElytra)
    {
        final ArmorTier armorTier = nbtEditor.getArmorTier(armoredElytra);
        if (armorTier == ArmorTier.NONE)
            throw new IllegalArgumentException("Non-armored elytras cannot be uncrafted!");
        return uncraft(armoredElytra, armorTier);
    }

    /**
     * Uncrafts and armored elytra back into its original components.
     *
     * @param armoredElytra The armored elytra to uncraft.
     * @param armorTier     The current tier of the armored elytra. If this is not known, use {@link
     *                      #uncraft(ItemStack)} instead.
     * @return The result of uncrafting the armored elytra.
     */
    public UncraftingResult uncraft(ItemStack armoredElytra, ArmorTier armorTier)
    {
        final NBTEditor.OriginalEnchantments<EnchantmentContainer, EnchantmentContainer> enchantments =
            nbtEditor.getOriginalEnchantments(armoredElytra);

        final ItemStack elytra = newItem(armoredElytra, armorTier, Material.ELYTRA, enchantments.enchantmentsElytra());
        final ItemStack chestplate = newItem(armoredElytra, armorTier, Util.tierToChestplate(armorTier),
                                             enchantments.enchantmentsChestplate());

        final EnchantmentContainer remainingEnchantments = getRemainingEnchantments(armoredElytra, enchantments);

        return new UncraftingResult(elytra, chestplate, remainingEnchantments);
    }

    private ItemStack newItem(ItemStack armoredElytra, ArmorTier armorTier,
                              Material mat, EnchantmentContainer enchantments)
    {
        final ItemStack ret = new ItemStack(mat);

        enchantments.applyEnchantments(ret);

        final int newDurability = durabilityManager.getRemappedDurability(armoredElytra, armorTier,
                                                                          mat.getMaxDurability());
        //noinspection deprecation
        ret.setDurability((short) newDurability);

        return ret;
    }

    private EnchantmentContainer getRemainingEnchantments(
        ItemStack armoredElytra,
        NBTEditor.OriginalEnchantments<EnchantmentContainer, EnchantmentContainer> enchantments)
    {
        final EnchantmentContainer currentEnchantments = EnchantmentContainer.getEnchantments(armoredElytra, plugin);
        final EnchantmentContainer combinedOriginalEnchantments =
            EnchantmentContainer.merge(enchantments.enchantmentsElytra(), enchantments.enchantmentsChestplate());

        // Remove all the original enchantments from the current set of
        // enchantments to get the ones we cannot place anywhere.
        return EnchantmentContainer.except(currentEnchantments, combinedOriginalEnchantments);
    }

    /**
     * Represents the result of uncrafting an armored elytra.
     *
     * @param elytra                The elytra retrieved from the armored elytra.
     * @param chestplate            The chestplate retrieved from the armored elytra.
     * @param remainingEnchantments The enchantments that could not be placed on either the elytra or the chestplate.
     */
    public record UncraftingResult(ItemStack elytra, ItemStack chestplate, EnchantmentContainer remainingEnchantments)
    {}
}
