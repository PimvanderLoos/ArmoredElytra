package nl.pim16aap2.armoredElytra.nbtEditor;

import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.Util;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class DurabilityManager
{
    private static final int ELYTRA_MAX_DURABILITY = Material.ELYTRA.getMaxDurability();

    private final int[] repairAmounts = new int[ArmorTier.values().length];
    private final int[] maxDurabilities = new int[ArmorTier.values().length];

    private final NBTEditor nbtEditor;
    private final ConfigLoader config;

    public DurabilityManager(NBTEditor nbtEditor, ConfigLoader config)
    {
        this.nbtEditor = nbtEditor;
        this.config = config;
        init();
    }

    /**
     * Combination of {@link #getCombinedDurability(ItemStack, ItemStack, ArmorTier, ArmorTier)} and
     * {@link #setDurability(ItemStack, int, ArmorTier)}.
     * <p>
     * First gets the combined of the input armored elytra and the other item and then applies it to the target armored
     * elytra.
     *
     * @param armoredElytraOut
     *     The output armored elytra item. This is the elytra that will be updated.
     * @param armoredElytraIn
     *     The input armored elytra item.
     * @param other
     *     The other item that will be combined with the armored elytra. This can be another armored elytra, a
     *     chestplate, or any other item.
     * @param currentTier
     *     The current armor tier of the armored elytra.
     * @param targetTier
     *     The target tier of the armored elytra.
     */
    public int setCombinedDurability(ItemStack armoredElytraOut, ItemStack armoredElytraIn, ItemStack other,
                                     ArmorTier currentTier, ArmorTier targetTier)
    {
        if (nbtEditor.isUnbreakable(armoredElytraIn))
            return 0;

        final int combinedDurability = getCombinedDurability(armoredElytraIn, other, currentTier, targetTier);
        setDurability(armoredElytraOut, combinedDurability, targetTier);
        return combinedDurability;
    }

    /**
     * Gets durability value resulting from combining an armored elytra with some other item with durability.
     *
     * @param armoredElytra
     *     The armored elytra item.
     * @param other
     *     The other item that will be combined with the armored elytra. This can be another armored elytra, a
     *     chestplate, or any other item.
     * @param currentTier
     *     The current armor tier of the armored elytra.
     * @param targetTier
     *     The target tier of the armored elytra.
     *
     * @return The new real durability value of the armored elytra if it were to be combined with the other item.
     */
    public int getCombinedDurability(ItemStack armoredElytra, ItemStack other,
                                     ArmorTier currentTier, ArmorTier targetTier)
    {
        if (nbtEditor.isUnbreakable(armoredElytra))
            return 0;

        final ArmorTier otherTier = nbtEditor.getArmorTierFromElytra(other);

        final int currentMaxDurability = getMaxDurability(currentTier);
        final int targetMaxDurability = getMaxDurability(targetTier);
        final int otherMaxDurability = otherTier != ArmorTier.NONE ?
                                       getMaxDurability(otherTier) : other.getType().getMaxDurability();
        final int otherDurability = other.getType().equals(Material.ELYTRA) ?
                                    getRealDurability(other, null) : getItemDurability(other);
        final int currentDurability = getRealDurability(armoredElytra, currentTier);

        final int combinedDurability = targetMaxDurability -
            (otherMaxDurability - otherDurability) -
            (currentMaxDurability - currentDurability);

        return Util.between(combinedDurability, 0, targetMaxDurability);
    }

    /**
     * Removes durability from an armored elytra.
     *
     * @param armoredElytra
     *     The armored elytra item to damage.
     * @param durabilityLoss
     *     The amount of durability to remove from the armored elytra.
     * @param providedTier
     *     The tier of the armored elytra (if this is available). If this is null, it will be retrieved from the item
     *     itself.
     *
     * @return The new durability after removing the provided amount.
     */
    public int removeDurability(ItemStack armoredElytra, int durabilityLoss, @Nullable ArmorTier providedTier)
    {
        if (nbtEditor.isUnbreakable(armoredElytra))
            return 0;

        final ArmorTier currentTier =
            providedTier == null ? nbtEditor.getArmorTierFromElytra(armoredElytra) : providedTier;
        final int currentDurability = getRealDurability(armoredElytra, currentTier);
        final int newDurability = Util.between(currentDurability + durabilityLoss, 0, getMaxDurability(currentTier));
        setDurability(armoredElytra, newDurability, providedTier);
        return newDurability;
    }

    /**
     * Gets the required number of repair items required to fully repair an armored elytra.
     * <p>
     * For example, for an ArmoredElytra that is damaged for 50 durability and its repair item restores 40 durability,
     * this method would return 2.
     *
     * @param armoredElytra
     *     The armored elytra item for which to check how many items are needed to fully repair it.
     * @param providedTier
     *     The tier of the armored elytra (if this is available). If this is null, it will be retrieved from the item
     *     itself.
     *
     * @return The required number of repair items required to fully repair the armored elytra.
     */
    public int getFullRepairItemCount(ItemStack armoredElytra, @Nullable ArmorTier providedTier)
    {
        if (nbtEditor.isUnbreakable(armoredElytra))
            return 0;

        final ArmorTier currentTier =
            providedTier == null ? nbtEditor.getArmorTierFromElytra(armoredElytra) : providedTier;

        final int currentDurability = getRealDurability(armoredElytra, currentTier);

        return (int) Math.ceil((float) currentDurability / getRepairAmount(currentTier));
    }

    /**
     * Gets the new durability of an armored elytra if it were to be repaired right now.
     *
     * @param armoredElytra
     *     The armored elytra item for which to check what the new durability would be after repairing it.
     * @param repairCount
     *     The number of repair items.
     * @param providedTier
     *     The tier of the armored elytra (if this is available). If this is null, it will be retrieved from the item
     *     itself.
     *
     * @return The real durability value of the armored elytra if it were to be repaired.
     */
    public int getRepairedDurability(ItemStack armoredElytra, int repairCount, @Nullable ArmorTier providedTier)
    {
        if (nbtEditor.isUnbreakable(armoredElytra))
            return 0;

        final ArmorTier currentTier =
            providedTier == null ? nbtEditor.getArmorTierFromElytra(armoredElytra) : providedTier;
        final int restoredDurability = repairCount * getRepairAmount(currentTier);
        final int currentDurability = getRealDurability(armoredElytra, currentTier);
        return Math.max(0, currentDurability - restoredDurability);
    }

    /**
     * Gets the real durability of an item.
     * <p>
     * If the item is an armored elytra, and it does not have a real durability yet, it will be upgraded.
     *
     * @param item
     *     The item for which to figure out the real durability.
     * @param providedTier
     *     The tier of the armored elytra (if this is available). If this is null, it will be retrieved from the item
     *     itself.
     *
     * @return The real durability of the item.
     */
    public int getRealDurability(ItemStack item, @Nullable ArmorTier providedTier)
    {
        final ArmorTier currentTier = providedTier == null ? nbtEditor.getArmorTierFromElytra(item) : providedTier;

        if (currentTier == ArmorTier.NONE)
            return getItemDurability(item);

        if (nbtEditor.isUnbreakable(item))
            return 0;

        final int realDurability = nbtEditor.getRealDurability(item, currentTier);

        // If the vanilla durability is 0, we assume that the item was repaired externally.
        // In this case, we need to update the real durability.
        if (getItemDurability(item) == 0 && realDurability > 0)
        {
            nbtEditor.updateDurability(item, 0, 0);
            return 0;
        }

        return realDurability == NBTEditor.HAS_NO_CUSTOM_DURABILITY ?
               upgradeArmoredElytraToDurability(item, currentTier) :
               realDurability;
    }

    /**
     * Sets the durability values (real + shown) of an armored elytra.
     *
     * @param item
     *     The armored elytra item for which to set the durability values.
     * @param durability
     *     The real durability value.
     * @param providedTier
     *     The tier of the armored elytra (if this is available). If this is null, it will be retrieved from the item
     *     itself.
     */
    public void setDurability(ItemStack item, int durability, @Nullable ArmorTier providedTier)
    {
        if (nbtEditor.isUnbreakable(item))
            return;

        final ArmorTier currentTier = providedTier == null ? nbtEditor.getArmorTierFromElytra(item) : providedTier;
        final int oldMaxDurability = getMaxDurability(currentTier);
        final int rawDurability = getRemappedDurability(durability, oldMaxDurability, ELYTRA_MAX_DURABILITY);
        nbtEditor.updateDurability(item, durability, rawDurability);
    }

    /**
     * Sets the real durability NBT data for armored elytras that do not have it.
     * <p>
     * The real durability is calculated from the current 'raw' durability. The real durability will be the same
     * percentage of the max durability for the type as the raw durability is of an elytra's maximum durability.
     *
     * @param armoredElytra
     *     The armored elytra to upgrade to an armored elytra with durability.
     * @param currentTier
     *     The current tier of the armored elytra.
     *
     * @return The real durability of the armored elytra.
     */
    private int upgradeArmoredElytraToDurability(ItemStack armoredElytra, ArmorTier currentTier)
    {
        final int maxDurability = getMaxDurability(currentTier);
        final int rawDurability = getItemDurability(armoredElytra);

        final int realDurability = getRemappedDurability(rawDurability, ELYTRA_MAX_DURABILITY, maxDurability);

        nbtEditor.updateDurability(armoredElytra, realDurability, rawDurability);
        return realDurability;
    }

    /**
     * Gets the maximum durability for an armor tier. This may or may not be
     * {@link ArmorTier#getMaxDurability(ArmorTier)} depending on {@link ConfigLoader#useTierDurability()}.
     *
     * @param armorTier
     *     The armor tier for which to figure out the maximum durability.
     *
     * @return The maximum durability of the armor tier.
     */
    private int calculateMaxDurability(ArmorTier armorTier)
    {
        if (armorTier == ArmorTier.NONE || !config.useTierDurability())
            return ELYTRA_MAX_DURABILITY;
        return ArmorTier.getMaxDurability(armorTier);
    }

    /**
     * Checks if an armored elytra should be considered 'broken'.
     *
     * @param durability
     *     The current 'real' durability. See {@link #getRealDurability(ItemStack, ArmorTier)}.
     * @param armorTier
     *     The armor tier for which to check.
     *
     * @return True if the provided durability should be considered 'broken' for the provided armor tier.
     */
    public boolean isBroken(int durability, ArmorTier armorTier)
    {
        return durability >= getMaxDurability(armorTier);
    }

    /**
     * Checks if an armored elytra should be considered 'broken'.
     *
     * @param armoredElytra
     *     The armored elytra to check.
     * @param armorTier
     *     The armor tier for which to check.
     *
     * @return True if the provided armored elytra should be considered 'broken'.
     */
    public boolean isBroken(ItemStack armoredElytra, @Nullable ArmorTier armorTier)
    {
        final int realDurability = getRealDurability(armoredElytra, armorTier);
        if (realDurability == 0)
            return false;
        return isBroken(realDurability,
                        armorTier == null ? nbtEditor.getArmorTierFromElytra(armoredElytra) : armorTier);
    }

    /**
     * Gets the maximum durability for a given armor tier.
     *
     * @param armorTier
     *     The armor tier for which to get the maximum durability.
     *
     * @return The maximum durability of the given armor tier.
     */
    private int getMaxDurability(ArmorTier armorTier)
    {
        return maxDurabilities[armorTier.ordinal()];
    }

    /**
     * Gets the amount of durability restored per repair step for a given armor tier.
     *
     * @param armorTier
     *     The armor tier.
     *
     * @return The amount of durability restored per repair step for the given armor tier.
     */
    private int getRepairAmount(ArmorTier armorTier)
    {
        return repairAmounts[armorTier.ordinal()];
    }

    /**
     * Remaps a durability value from an old maximum value to a new maximum while maintaining the same durability
     * percentage.
     *
     * @param durability
     *     The current durability value.
     * @param oldMax
     *     The old maximum durability.
     * @param newMax
     *     The new maximum durability.
     *
     * @return The new durability value after remapping it to the new maximum. The value cannot be less than 0 or more
     * than newMax.
     */
    private int getRemappedDurability(int durability, int oldMax, int newMax)
    {
        if (durability == 0)
            return 0;

        final int scaledDurability;
        if (oldMax == newMax)
            scaledDurability = durability;
        else
        {
            final float relativeDurability = (float) durability / oldMax;
            scaledDurability = (int) Math.ceil(relativeDurability * newMax);
        }

        return Util.between(scaledDurability, 0, newMax);
    }

    /**
     * Initializes the {@link #maxDurabilities} and {@link #repairAmounts} arrays.
     */
    private void init()
    {
        repairAmounts[0] = 0;
        maxDurabilities[0] = ELYTRA_MAX_DURABILITY;

        final ArmorTier[] armorTiers = ArmorTier.values();
        for (int idx = 1; idx < armorTiers.length; ++idx)
        {
            final ArmorTier armorTier = armorTiers[idx];

            final int maxDurability = calculateMaxDurability(armorTier);
            maxDurabilities[idx] = maxDurability;

            final int steps = Math.max(1, config.getFullRepairItemCount(armorTier));
            repairAmounts[idx] = (int) Math.ceil((float) maxDurability / steps);
        }
    }

    /**
     * Gets the durability of an item. See {@link ItemStack#getDurability()}.
     *
     * @param item
     *     The item to analyze.
     *
     * @return The durability of the item.
     */
    @SuppressWarnings("deprecation")
    private int getItemDurability(ItemStack item)
    {
        return item.getDurability();
    }
}
