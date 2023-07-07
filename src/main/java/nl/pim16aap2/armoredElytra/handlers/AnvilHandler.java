package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nbtEditor.DurabilityManager;
import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.Action;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.ConfigLoader;
import nl.pim16aap2.armoredElytra.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.logging.Level;

public class AnvilHandler extends ArmoredElytraHandler implements Listener
{
    protected AnvilHandler(ArmoredElytra plugin, boolean creationEnabled,
                           NBTEditor nbtEditor, DurabilityManager durabilityManager, ConfigLoader config)
    {
        super(plugin, creationEnabled, nbtEditor, durabilityManager, config);
    }

    public AnvilHandler(ArmoredElytra plugin, NBTEditor nbtEditor,
                        DurabilityManager durabilityManager, ConfigLoader config)
    {
        super(plugin, true, nbtEditor, durabilityManager, config);
    }

    // Valid inputs:
    //  - Elytra (armored or not)    + chestplate             -> Create Armored Elytra
    //  - Elytra (armored)           + enchanted book         -> Enchant
    //  - Elytra (armored)           + its repair item        -> Repair
    //  - Elytra (armored)           + other elytra (armored) -> Combine (Enchant + Repair)
    //  - Elytra (armored, !leather) + leather/membrane       -> Block
    //
    // Ignoring:
    //  - Elytra (not armored)       + !chestplate            -> None
    //  - *                          + *                      -> None
    private Action isValidInput(ArmorTier itemOneTier, ItemStack itemOne, ItemStack itemTwo)
    {
        if (itemOne == null || itemTwo == null)
            return Action.NONE;

        if (itemOne.getType() != Material.ELYTRA)
            return Action.NONE;

        final Material matTwo = itemTwo.getType();

        // If the elytra is to be combined with chest armor...
        if (Util.isChestPlate(matTwo))
            return creationEnabled ? Action.CREATE : Action.NONE;

        if (itemOneTier != ArmorTier.NONE)
        {
            // If the armored elytra is to be enchanted using an enchanted book...
            if (matTwo == Material.ENCHANTED_BOOK)
                return config.allowAddingEnchantments() ? Action.ENCHANT : Action.BLOCK;

            // If the armored elytra is to be repaired using its repair item...
            if (ArmorTier.getRepairItem(itemOneTier) == matTwo)
                return durabilityManager.getRealDurability(itemOne, itemOneTier) == 0 ? Action.BLOCK : Action.REPAIR;

            // If the armored elytra is to be combined with another armored elytra of the same tier...
            if (nbtEditor.getArmorTier(itemTwo) == itemOneTier)
                return creationEnabled ? Action.COMBINE : Action.NONE;

            // If the armored elytra is not of the leather tier, but itemTwo is leather,
            // Pick the block action, as that would repair the elytra by default (vanilla).
            // Also block Armored Elytra + Elytra and Elytra + Membrane
            if (itemOneTier != ArmorTier.LEATHER && matTwo == Material.LEATHER || matTwo == Material.ELYTRA ||
                matTwo.equals(Material.PHANTOM_MEMBRANE))
                return Action.BLOCK;
        }
        return Action.NONE;
    }

    // Handle all anvil related stuff for this plugin.
    @EventHandler(priority = EventPriority.LOWEST)
    private void onAnvilInventoryOpen(PrepareAnvilEvent event)
    {
        if (!(event.getView().getPlayer() instanceof Player player))
            return;

        ItemStack itemA = event.getInventory().getItem(0);
        ItemStack itemB = event.getInventory().getItem(1);

        if (itemA != null && itemB != null)
            // If itemB is the (armored) elytra, while itemA isn't, switch itemA and itemB.
            if (itemB.getType() == Material.ELYTRA && itemA.getType() != Material.ELYTRA)
            {
                ItemStack tmp = itemA;
                itemA = itemB;
                itemB = tmp;
            }

        // If one of the input items is null and the other an armored elytra, remove the result.
        // This prevents some naming issues.
        if ((itemA == null ^ itemB == null) &&
            nbtEditor.getArmorTier(itemA == null ? itemB : itemA) != ArmorTier.NONE)
            event.setResult(null);

        if (itemA == null || itemB == null)
            return;

        final ArmorTier currentArmorTier = nbtEditor.getArmorTier(itemA);
        final Action action = isValidInput(currentArmorTier, itemA, itemB);

        if (action == Action.NONE)
            return;

        final ArmorTier newArmorTier;
        if (action == Action.CREATE)
            newArmorTier = Util.armorToTier(itemB);
        else if (action == Action.COMBINE)
            newArmorTier = nbtEditor.getArmorTier(itemB);
        else
            newArmorTier = currentArmorTier;

        final @Nullable String name = getElytraResultName(itemA, action, currentArmorTier, newArmorTier,
                                                          event.getInventory().getRenameText());

        final @Nullable ItemStack result =
            !plugin.playerHasCraftPerm(player, newArmorTier) ? null :
            switch (action)
            {
                case REPAIR -> armoredElytraBuilder.repair(itemA, itemB, name);
                case ENCHANT -> armoredElytraBuilder.enchant(itemA, itemB, name);
                case COMBINE, CREATE -> armoredElytraBuilder.combine(itemA, itemB, newArmorTier, name);
                case BLOCK -> null;
                //noinspection ConstantConditions
                case NONE -> itemA;
            };

        event.setResult(result);
        player.updateInventory();
    }

    private @Nullable String getElytraResultName(final ItemStack baseItem, final Action action,
                                                 final ArmorTier currentArmorTier, final ArmorTier newArmorTier,
                                                 final String renameText)
    {
        final String currentTierName = plugin.getArmoredElytraName(currentArmorTier);
        final String tierName = plugin.getArmoredElytraName(newArmorTier);

        if (renameText == null || !config.allowRenaming() ||
            ChatColor.stripColor(currentTierName).equals(ChatColor.stripColor(renameText)))
            return tierName;

        final ItemMeta meta = baseItem.getItemMeta();
        final String currentName = meta == null ? null : meta.getDisplayName();

        // When the renameText is empty, give it the default tier-name when creating a new armored elytra
        // (so it's named properly) or when the current name is already the tier name (just returning the current
        // name would strip the tier's color in this case).
        if ((action == Action.CREATE && renameText.equals("")) ||
            ChatColor.stripColor(tierName).equals(ChatColor.stripColor(renameText)))
            return tierName;

        return renameText.equals("") ? currentName : renameText;
    }

    // Let the player take items out of the anvil.
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e)
    {
        if (e.getRawSlot() != 2 || !(e.getWhoClicked() instanceof Player player))
            return;

        // Check if the event was a player who interacted with an anvil.
        if (e.getView().getType() != InventoryType.ANVIL)
            return;

        AnvilInventory anvilInventory;
        // Try to cast inventory being used in the event to an anvil inventory.
        // This will throw a ClassCastException when a CraftInventoryCustom is used.
        try
        {
            anvilInventory = (AnvilInventory) e.getInventory();
        }
        catch (ClassCastException exception)
        {
            // Print warning to console and exit onInventoryClick event (no support for
            // custom inventories as they are usually used for GUI's).
            plugin.debugMsg(Level.WARNING, "Could not cast inventory to anvilInventory for player " + player.getName()
                + "! Armored Elytras cannot be crafted!");
            exception.printStackTrace();
            return;
        }

        final @Nullable ItemStack item0 = anvilInventory.getItem(0);
        final @Nullable ItemStack item1 = anvilInventory.getItem(1);
        final @Nullable ItemStack item2 = anvilInventory.getItem(2);

        if (item0 != null && item1 != null && item2 != null && item2.getType() == Material.ELYTRA)
        {
            final ArmorTier armortier = nbtEditor.getArmorTier(anvilInventory.getItem(2));

            // If there's an armored elytra in the final slot...
            if (armortier != ArmorTier.NONE && plugin.playerHasCraftPerm(player, armortier))
            {
                if (!giveItemToPlayer(player, item2, e.isShiftClick()))
                    return;

                // Clean the anvil's inventory after transferring the items.
                anvilInventory.clear();
            }
        }
    }
}
