package nl.pim16aap2.armoredElytra.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.codingforcookies.armorequip.ArmorListener;
import com.codingforcookies.armorequip.ArmorType;
import com.codingforcookies.armorequip.DispenserArmorListener;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.Action;
import nl.pim16aap2.armoredElytra.util.AllowedToWearEnum;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.Util;
import nl.pim16aap2.armoredElytra.util.XMaterial;

public class EventHandlers implements Listener
{
    private final NBTEditor nbtEditor;
    private final ArmoredElytra plugin;

    private final Consumer<AnvilInventory> cleanAnvilInventory;
    private final Consumer<Player> moveChestplateToInventory;

    public EventHandlers(ArmoredElytra plugin, NBTEditor nbtEditor, boolean is1_9)
    {
        this.plugin = plugin;
        this.nbtEditor = nbtEditor;
        initializeArmorEquipEvent();
        if (is1_9)
        {
            cleanAnvilInventory = this::cleanAnvilOld;
            moveChestplateToInventory = this::moveChestplateToInventoryOld;
        }
        else
        {
            cleanAnvilInventory = this::cleanAnvilNew;
            moveChestplateToInventory = this::moveChestplateToInventoryNew;
        }
    }

    private void initializeArmorEquipEvent()
    {
        Bukkit.getPluginManager().registerEvents(new ArmorListener(new ArrayList<String>()), plugin);
        try
        {
            // Older versions did not have this event. So try to load it if possible, but
            // don't worry
            // about it not working.
            Class.forName("org.bukkit.event.block.BlockDispenseArmorEvent");
            plugin.getServer().getPluginManager().registerEvents(new DispenserArmorListener(), plugin);
        }
        catch (Exception ignored)
        {
        }
    }

    private void moveChestplateToInventoryOld(Player player)
    {
        player.getInventory().getChestplate().setType(XMaterial.AIR.parseMaterial());
    }

    private void moveChestplateToInventoryNew(Player player)
    {
        player.getInventory().addItem(player.getInventory().getChestplate());
        player.getInventory().getChestplate().setAmount(0);
        player.updateInventory();
    }

    // Clean 1.9 inventories.
    private void cleanAnvilOld(AnvilInventory anvilInventory)
    {
        ItemStack air = new ItemStack(XMaterial.AIR.parseMaterial(), 1);
        anvilInventory.setItem(0, air);
        anvilInventory.setItem(1, air);
        anvilInventory.setItem(2, air);
    }

    // Clean >=1.10 inventories.
    private void cleanAnvilNew(AnvilInventory anvilInventory)
    {
        anvilInventory.getItem(0).setAmount(0);
        anvilInventory.getItem(1).setAmount(anvilInventory.getItem(1).getAmount() - 1);
        anvilInventory.getItem(2).setAmount(0);
    }

    // Check if the enchantment is allowed on elytras.
    private boolean isAllowedEnchantment(Enchantment enchant)
    {
        for (String s : plugin.getConfigLoader().allowedEnchantments())
            if (Enchantment.getByName(s) != null)
                if (Enchantment.getByName(s).equals(enchant))
                    return true;
        return false;
    }

    // Combine 2 maps of enchantments (and remove any invalid ones).
    private Map<Enchantment, Integer> combineEnchantments(Map<Enchantment, Integer> enchantments0,
                                                          Map<Enchantment, Integer> enchantments1)
    {
        enchantments0 = fixEnchantments(enchantments0);
        Map<Enchantment, Integer> combined = new HashMap<>(fixEnchantments(enchantments0));

        // If the second set of enchantments is null, the combined enchantments are just
        // the first enchantments.
        if (enchantments1 == null)
            return combined;

        enchantments1 = fixEnchantments(enchantments1);
        // Loop through the enchantments of item1.
        for (Map.Entry<Enchantment, Integer> entry : enchantments1.entrySet())
        {
            Integer enchantLevel = enchantments0.get(entry.getKey());
            if (enchantLevel != null)
            {
                if (entry.getValue() == enchantLevel && entry.getValue() < entry.getKey().getMaxLevel())
                    enchantLevel = entry.getValue() + 1;
                else if (entry.getValue() > enchantLevel)
                    enchantLevel = entry.getValue();

                // If the enchantment level has changed,
                if (enchantLevel != enchantments0.get(entry.getKey()))
                {
                    combined.remove(entry.getKey());
                    combined.put(entry.getKey(), enchantLevel);
                }
            }
            else
                combined.put(entry.getKey(), entry.getValue());
        }

        if (!plugin.getConfigLoader().allowMultipleProtectionEnchantments())
        {
            // Get the protection enchantment rating for both enchantment sets.
            int protVal0 = Util.getProtectionEnchantmentsVal(enchantments0);
            int protVal1 = Util.getProtectionEnchantmentsVal(enchantments1);

            // If they have different protection enchantments, keep enchantment1's
            // enchantments
            // And remove the protection enchantment from enchantments0. Yes, this system
            // only works
            // If there is 1 protection enchantment on
            if (protVal0 != 0 && protVal1 != 0 && protVal0 != protVal1)
                switch (protVal0)
                {
                case 1:
                    combined.remove(Enchantment.PROTECTION_ENVIRONMENTAL);
                    break;
                case 2:
                    combined.remove(Enchantment.PROTECTION_EXPLOSIONS);
                    break;
                case 4:
                    combined.remove(Enchantment.PROTECTION_FALL);
                    break;
                case 8:
                    combined.remove(Enchantment.PROTECTION_FIRE);
                    break;
                case 16:
                    combined.remove(Enchantment.PROTECTION_PROJECTILE);
                    break;
                }
        }
        return combined;
    }

    // Repair an Armored Elytra
    private short repairItem(short curDur, ItemStack repairItem)
    {
        // Get the multiplier for the repair items.
        double mult = 0.01;
        if (repairItem.getType().equals(Material.LEATHER))
            mult *= (100.0f / plugin.getConfigLoader().LEATHER_TO_FULL());

        else if (repairItem.getType().equals(Material.GOLD_INGOT))
            mult *= (100.0f / plugin.getConfigLoader().GOLD_TO_FULL());

        else if (repairItem.getType().equals(Material.IRON_INGOT))
            mult *= (100.0f / plugin.getConfigLoader().IRON_TO_FULL());

        else if (repairItem.getType().equals(Material.DIAMOND))
            mult *= (100.0f / plugin.getConfigLoader().DIAMONDS_TO_FULL());

        int maxDurability = Material.ELYTRA.getMaxDurability();
        int newDurability = (int) (curDur - (maxDurability * mult));
        return (short) (newDurability <= 0 ? 0 : newDurability);
    }

    // Remove any disallowed enchantments in the map.
    private Map<Enchantment, Integer> fixEnchantments(Map<Enchantment, Integer> enchantments)
    {
        Map<Enchantment, Integer> ret = new HashMap<>(enchantments);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet())
            if (!isAllowedEnchantment(entry.getKey()))
                ret.remove(entry.getKey());
        return ret;
    }

    // Verify there aren't any disallowed enchantments in the map.
    private int verifyEnchantments(Map<Enchantment, Integer> enchantments)
    {
        int ret = 0;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet())
            if (!isAllowedEnchantment(entry.getKey()))
                ++ret;
        return ret;
    }

    // Valid inputs:
    //  - Elytra (armored or not)    + chestplate             -> Create Armored Elytra
    //  - Elytra (armored)           + enchanted book         -> Enchant
    //  - Elytra (armored)           + its repair item        -> Repair
    //  - Elytra (armored)           + other elytra (armored) -> Combine (Enchant + Repair)
    //  ! Elytra (armored, !leather) + leather                -> Block
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
            return Action.CREATE;

        ArmorTier tier = nbtEditor.getArmorTier(itemOne);

        if (tier != ArmorTier.NONE)
        {
            // If the armored elytra is to be enchanted using an enchanted book...
            if (matTwo == Material.ENCHANTED_BOOK)
                return Action.ENCHANT;

            // If the armored elytra is to be repaired using its repair item...
            if (ArmorTier.getRepairItem(tier) == matTwo)
                return Action.REPAIR;

            // If the armored elytra is to be combined with another armored elytra of the
            // same tier...
            if (nbtEditor.getArmorTier(itemTwo) == tier)
                return Action.COMBINE;

            // If the armored elytra is not of the leather tier, but itemTwo is leather,
            // Pick the block action, as that would repair the elytra by default (vanilla).
            // Also block Armored Elytra + Elytra.
            if (tier != ArmorTier.LEATHER && matTwo == Material.LEATHER || matTwo == Material.ELYTRA)
                return Action.BLOCK;
        }
        return Action.NONE;
    }

    // Handle all anvil related stuff for this plugin.
    @EventHandler
    private void onAnvilInventoryOpen(PrepareAnvilEvent event)
    {
        Player player = (Player) event.getView().getPlayer();
        ItemStack itemA = event.getInventory().getItem(0);
        ItemStack itemB = event.getInventory().getItem(1);
        ItemStack result = null;

        if (itemA != null && itemB != null)
            // If itemB is the elytra, while itemA isn't, switch itemA and itemB.
            if (itemB.getType() == Material.ELYTRA && itemA.getType() != Material.ELYTRA)
            {
                result = itemA;
                itemA = itemB;
                itemB = result;
                result = null;
            }

        // Check if there are items in both input slots.
        if (itemA != null && itemB != null)
        {
            Action action = isValidInput(itemA, itemB);
            ArmorTier newTier = ArmorTier.NONE;
            ArmorTier curTier = nbtEditor.getArmorTier(itemA);
            short durability = 0;
            Map<Enchantment, Integer> enchantments = itemA.getEnchantments();
            enchantments = fixEnchantments(enchantments);

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
                enchantments = combineEnchantments(enchantments, itemB.getEnchantments());
                break;
            case CREATE:
                newTier = Util.armorToTier(itemB.getType());
                durability = 0;
                enchantments = combineEnchantments(enchantments, itemB.getEnchantments());
                break;
            case ENCHANT:
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) itemB.getItemMeta();
                newTier = curTier;
                durability = itemA.getDurability();
                // If there aren't any illegal enchantments on the book, continue as normal.
                // Otherwise... Block.
                if (verifyEnchantments(meta.getStoredEnchants()) != meta.getStoredEnchants().size())
                {
                    enchantments = combineEnchantments(enchantments, meta.getStoredEnchants());
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
                if (enchantments != null)
                    result.addUnsafeEnchantments(enchantments);
                result.setDurability(durability);

                result = nbtEditor.addArmorNBTTags(result, newTier, plugin.getConfigLoader().unbreakable());
                event.setResult(result);
            }
        }

        // Check if either itemA or itemB is unoccupied.
        if ((itemA == null || itemB == null) &&
            nbtEditor.getArmorTier(event.getInventory().getItem(2)) != ArmorTier.NONE)
            // If Item2 is occupied despite itemA or itemB not being occupied. (only for
            // armored elytra)/
            event.setResult(null);
        player.updateInventory();
    }

    // Let the player take items out of the anvil.
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e)
    {
        if (!(e.getWhoClicked() instanceof Player))
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
            return;
        }

        int slot = e.getRawSlot();

        if (slot == 2 && anvilInventory.getItem(0) != null && anvilInventory.getItem(1) != null &&
            anvilInventory.getItem(2) != null)
        {
            ArmorTier armortier = nbtEditor.getArmorTier(anvilInventory.getItem(2));
            // If there's an armored elytra in the final slot...
            if (armortier != ArmorTier.NONE && plugin.playerHasCraftPerm(player, armortier))
            {
                // Create a new armored elytra and give that one to the player instead of the
                // result.
                // This is done because after putting item0 in AFTER item1, the first letter of
                // the color code shows up, this gets rid of that problem.
                ItemStack result = nbtEditor.addArmorNBTTags(anvilInventory.getItem(2), armortier,
                                                             plugin.getConfigLoader().unbreakable());
                // Give the result to the player and clear the anvil's inventory.
                if (e.isShiftClick())
                {
                    // If the player's inventory is full, don't do anything.
                    if (player.getInventory().firstEmpty() == -1)
                        return;
                    player.getInventory().addItem(result);
                }
                else
                    player.setItemOnCursor(result);
                // Clean the anvil's inventory after transferring the items.
                cleanAnvilInventory.accept(anvilInventory);
                player.updateInventory();
                return;
            }
        }
    }

    // Make sure the player has the correct permission and that the item is not
    // broken.
    private AllowedToWearEnum isAllowedToWear(ItemStack elytra, Player player, ArmorTier armorTier)
    {
        if (armorTier.equals(ArmorTier.NONE))
            return AllowedToWearEnum.ALLOWED;
        if (Util.isBroken(elytra))
            return AllowedToWearEnum.BROKEN;
        if (!plugin.playerHasWearPerm(player, armorTier))
            return AllowedToWearEnum.NOPERMISSION;
        return AllowedToWearEnum.ALLOWED;
    }

    // Handle armored elytra durability loss.
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e)
    {
        if (!(e.getEntity() instanceof Player))
            return;

        if (plugin.getConfigLoader().unbreakable())
            return;

        Player p = (Player) e.getEntity();
        // If the player didn't die from the damage.
        if ((p.getHealth() - e.getFinalDamage()) > 0)
        {
            if (p.getInventory().getChestplate() == null)
                return;

            if (nbtEditor.getArmorTier(p.getInventory().getChestplate()) == ArmorTier.NONE)
                return;

            ItemStack elytra = p.getInventory().getChestplate();
            DamageCause cause = e.getCause();

            // The elytra doesn't receive any damage for these causes:
            if (cause != DamageCause.DROWNING && cause != DamageCause.STARVATION    && cause != DamageCause.SUFFOCATION &&
                cause != DamageCause.SUICIDE  && cause != DamageCause.FLY_INTO_WALL && cause != DamageCause.POISON)
            {
                int durability = p.getInventory().getChestplate().getDurability();
                int maxDurability = p.getInventory().getChestplate().getType().getMaxDurability();
                int newDurability = durability + ((int) (e.getDamage() / 4) > 1 ? (int) (e.getDamage() / 4) : 1);

                // If the elytra has the durability enchantment, we calculate the durability
                // loss ourselves.
                if (p.getInventory().getChestplate().containsEnchantment(Enchantment.DURABILITY))
                {
                    // Get a random int between 0 and 100 to use in deciding if the durability
                    // enchantment will take effect.
                    Random r = new Random();
                    int randomInt = r.nextInt(101);
                    int enchantLevel = p.getInventory().getChestplate().getEnchantmentLevel(Enchantment.DURABILITY);
                    int durabilityDelta = (100 / (enchantLevel + 1)) < randomInt ? 0 : 1;
                    // If the durability equals/exceeds maxDurability, it's broken (0 = full item
                    // durability).
                    if (durability >= maxDurability)
                        moveChestplateToInventory.accept(p);
                    else
                        newDurability = durability + durabilityDelta;
                }
                // If the item should be broken, make sure it really is broken and unequip it.
                if (newDurability >= maxDurability)
                {
                    newDurability = maxDurability;
                    moveChestplateToInventory.accept(p);
                }
                elytra.setDurability((short) (newDurability));
            }
        }
    }

    @EventHandler
    public void onEquip(ArmorEquipEvent e)
    {
        if (!e.getType().equals(ArmorType.CHESTPLATE) ||
            !e.getNewArmorPiece().getType().equals(Material.ELYTRA) ||
            e.getNewArmorPiece() == null)
            return;

        ArmorTier armorTier = nbtEditor.getArmorTier(e.getNewArmorPiece());
        AllowedToWearEnum allowed = isAllowedToWear(e.getNewArmorPiece(), e.getPlayer(), armorTier);
        switch(allowed)
        {
        case ALLOWED:
            break;
        case BROKEN:
            plugin.messagePlayer(e.getPlayer(), plugin.getMyMessages().getString("MESSAGES.RepairNeeded"));
            e.setCancelled(true);
            break;
        case NOPERMISSION:
            plugin.usageDeniedMessage(e.getPlayer(), armorTier);
            e.setCancelled(true);
            break;
        default:
            break;
        }
    }
}