package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.Action;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.EnchantmentContainer;
import nl.pim16aap2.armoredElytra.util.Util;
import nl.pim16aap2.armoredElytra.util.XMaterial;
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

import java.util.logging.Level;

public class AnvilHandler extends ArmoredElytraHandler implements Listener
{
    public AnvilHandler(final ArmoredElytra plugin, final boolean creationEnabled)
    {
        super(plugin, creationEnabled);
    }

    public AnvilHandler(final ArmoredElytra plugin)
    {
        this(plugin, true);
    }

    // Valid inputs:
    //  - Elytra (armored or not)    + chestplate             -> Create Armored Elytra
    //  - Elytra (armored)           + enchanted book         -> Enchant
    //  - Elytra (armored)           + its repair item        -> Repair
    //  - Elytra (armored)           + other elytra (armored) -> Combine (Enchant + Repair)
    //  ! Elytra (armored, !leather) + leather/membrane       -> Block
    //
    // Ignoring:
    //  - Elytra (not armored)       + !chestplate            -> None
    //  - *                          + *                      -> None
    private Action isValidInput(ItemStack itemOne, ItemStack itemTwo)
    {
        if (itemOne == null || itemTwo == null)
            return Action.NONE;

        // If itemTwo is the elytra, while itemOne isn't, switch itemOne and itemTwo.
        if (itemTwo.getType() == Material.ELYTRA && itemOne.getType() != Material.ELYTRA)
        {
            ItemStack tmp = itemOne;
            itemOne = itemTwo;
            itemTwo = tmp;
        }

        if (itemOne.getType() != Material.ELYTRA)
            return Action.NONE;

        Material matTwo = itemTwo.getType();

        // If the elytra is to be combined with chest armor...
        if (Util.isChestPlate(matTwo))
            return creationEnabled ? Action.CREATE : Action.NONE;

        ArmorTier tier = ArmoredElytra.getInstance().getNbtEditor().getArmorTier(itemOne);

        if (tier != ArmorTier.NONE)
        {
            // If the armored elytra is to be enchanted using an enchanted book...
            if (matTwo == Material.ENCHANTED_BOOK)
                return Action.ENCHANT;

            // If the armored elytra is to be repaired using its repair item...
            if (ArmorTier.getRepairItem(tier) == matTwo)
                return itemOne.getDurability() == 0 ? Action.NONE : Action.REPAIR;

            // If the armored elytra is to be combined with another armored elytra of the
            // same tier...
            if (ArmoredElytra.getInstance().getNbtEditor().getArmorTier(itemTwo) == tier)
                return creationEnabled ? Action.COMBINE : Action.NONE;

            // If the armored elytra is not of the leather tier, but itemTwo is leather,
            // Pick the block action, as that would repair the elytra by default (vanilla).
            // Also block Armored Elytra + Elytra and Elytra + Membrane
            if (tier != ArmorTier.LEATHER && matTwo == Material.LEATHER || matTwo == Material.ELYTRA ||
                matTwo.equals(XMaterial.PHANTOM_MEMBRANE.parseMaterial()))
                return Action.BLOCK;
        }
        return Action.NONE;
    }

    // Handle all anvil related stuff for this plugin.
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onAnvilInventoryOpen(PrepareAnvilEvent event)
    {
        Player player = (Player) event.getView().getPlayer();
        ItemStack itemA = event.getInventory().getItem(0);
        ItemStack itemB = event.getInventory().getItem(1);
        ItemStack result = null;

        if (itemA != null && itemB != null)
            // If itemB is the (armored) elytra, while itemA isn't, switch itemA and itemB.
            if (itemB.getType() == Material.ELYTRA && itemA.getType() != Material.ELYTRA)
            {
                ItemStack tmp = itemA;
                itemA = itemB;
                itemB = tmp;
            }

        // Check if there are items in both input slots.
        if (itemA != null && itemB != null)
        {
            Action action = isValidInput(itemA, itemB);
            ArmorTier newTier = ArmorTier.NONE;
            ArmorTier curTier = ArmoredElytra.getInstance().getNbtEditor().getArmorTier(itemA);
            short durability = 0;
            EnchantmentContainer enchantments = EnchantmentContainer.getEnchantments(itemA, plugin);

            switch (action)
            {
                case REPAIR:
                    newTier = curTier;
                    durability = repairItem(itemA.getDurability(), itemB);
                    break;
                case COMBINE:
                    newTier = curTier;
                    durability = (short) (-itemA.getType().getMaxDurability() - itemA.getDurability()
                        - itemB.getDurability());
                    durability = durability < 0 ? 0 : durability;
                    enchantments.merge(EnchantmentContainer.getEnchantments(itemB, plugin));
                    break;
                case CREATE:
                    newTier = Util.armorToTier(itemB.getType());
                    durability = 0;
                    enchantments.merge(EnchantmentContainer.getEnchantments(itemB, plugin));
                    break;
                case ENCHANT:
                    newTier = curTier;
                    durability = itemA.getDurability();

                    // If there aren't any illegal enchantments on the book, continue as normal.
                    // Otherwise... Block.
                    EnchantmentContainer enchantmentsB = EnchantmentContainer.getEnchantments(itemB, plugin);
                    if (enchantmentsB.getEnchantmentCount() > 0)
                    {
                        enchantments.merge(enchantmentsB);
                        break;
                    }
                    //$FALL-THROUGH$
                case BLOCK:
                    event.setResult(null);
                    player.updateInventory();
                    //$FALL-THROUGH$
                case NONE:
                    return;
            }

            if (plugin.playerHasCraftPerm(player, newTier))
            {
                result = new ItemStack(Material.ELYTRA, 1);
                enchantments.applyEnchantments(result);
                result.setDurability(durability);

                final String name = getElytraResultName(itemA, action, newTier, event.getInventory().getRenameText());

                result = ArmoredElytra.getInstance().getNbtEditor()
                                      .addArmorNBTTags(result, newTier, plugin.getConfigLoader().unbreakable(), name);

                event.setResult(result);
                return;
            }
        }

        // If one of the input items is null and the other an armored elytra, remove the result.
        // This prevent some naming issues.
        if ((itemA == null ^ itemB == null) &&
            ArmoredElytra.getInstance().getNbtEditor().getArmorTier(itemA == null ? itemB : itemA) != ArmorTier.NONE)
            event.setResult(null);
    }

    private String getElytraResultName(final ItemStack baseItem, final Action action,
                                       final ArmorTier armorTier, final String renameText)
    {
        final String tierName = ArmoredElytra.getInstance().getArmoredElytraName(armorTier);
        if (renameText == null || !plugin.getConfigLoader().allowRenaming())
            return tierName;

        final ItemMeta meta = baseItem.getItemMeta();
        final String currentName = meta == null ? null : meta.getDisplayName();

        // When the rename text is empty, give it the default tier-name when creating a new armored elytra
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
        if (e.getRawSlot() != 2 || !(e.getWhoClicked() instanceof Player))
            return;

        // Check if the event was a player who interacted with an anvil.
        Player player = (Player) e.getWhoClicked();
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

        if (anvilInventory.getItem(0) != null && anvilInventory.getItem(1) != null &&
            anvilInventory.getItem(2) != null && anvilInventory.getItem(2).getType() == Material.ELYTRA)
        {
            ArmorTier armortier = ArmoredElytra.getInstance().getNbtEditor().getArmorTier(anvilInventory.getItem(2));

            // If there's an armored elytra in the final slot...
            if (armortier != ArmorTier.NONE && plugin.playerHasCraftPerm(player, armortier))
            {
                final ItemStack result = anvilInventory.getItem(2);
                // Give the result to the player and clear the anvil's inventory.
                if (!giveItemToPlayer(player, result, e.isShiftClick()))
                    return;

                // Clean the anvil's inventory after transferring the items.
                anvilInventory.clear();
            }
        }
    }
}
