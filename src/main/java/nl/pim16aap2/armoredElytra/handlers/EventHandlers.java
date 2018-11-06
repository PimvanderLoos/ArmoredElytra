package nl.pim16aap2.armoredElytra.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.scheduler.BukkitRunnable;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nms.NBTEditor;
import nl.pim16aap2.armoredElytra.util.Action;
import nl.pim16aap2.armoredElytra.util.AllowedToWearEnum;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.Util;

public class EventHandlers implements Listener 
{
    private int             DIAMONDS_TO_FULL;
    private int              LEATHER_TO_FULL;
    private int                 GOLD_TO_FULL;
    private int                 IRON_TO_FULL;
    private NBTEditor              nbtEditor;
    private final ArmoredElytra       plugin;
    private List<String> allowedEnchantments;
    private boolean                    is1_9;
    
    public EventHandlers(ArmoredElytra plugin, NBTEditor nbtEditor, boolean is1_9) 
    {
        this.plugin    = plugin;
        this.nbtEditor = nbtEditor;
        this.is1_9     = is1_9;
        
        // Get the values of the config options.
        this.allowedEnchantments = plugin.getConfigLoader().getStringList("allowedEnchantments");
        this.LEATHER_TO_FULL     = plugin.getConfigLoader().getInt("leatherRepair");
        this.GOLD_TO_FULL        = plugin.getConfigLoader().getInt("goldRepair");
        this.IRON_TO_FULL        = plugin.getConfigLoader().getInt("ironRepair");
        this.DIAMONDS_TO_FULL    = plugin.getConfigLoader().getInt("diamondsRepair");
    }

    // Remove item from player's chestplate slot and puts it in their normal inventory.
    private void unenquipChestPlayer(Player p) 
    {
        if (is1_9)
            p.getInventory().getChestplate().setType(Material.AIR);
        else
        {
            p.getInventory().addItem(p.getInventory().getChestplate());
            p.getInventory().getChestplate().setAmount(0);
            p.updateInventory();
        }
    }
    
    // Clear the anvil's inventory (destroy all the items in all 3 slots (second slot is not emptied, when repairing you can safely give multiple items)).
    private void cleanAnvil(AnvilInventory anvilInventory)
    {
        if (is1_9)
        {
            ItemStack air = new ItemStack(Material.AIR, 1);
            anvilInventory.setItem(0, air);
            anvilInventory.setItem(1, air);
            anvilInventory.setItem(2, air);
        }
        else
        {
            anvilInventory.getItem(0).setAmount(0);
            anvilInventory.getItem(1).setAmount(anvilInventory.getItem(1).getAmount() - 1);
            anvilInventory.getItem(2).setAmount(0);
        }
    }
    
    // Check if the enchantment is allowed on elytras.
    private boolean isAllowedEnchantment(Enchantment enchant) 
    {
        for (String s : allowedEnchantments)
            if (Enchantment.getByName(s) != null)
                if (Enchantment.getByName(s).equals(enchant))
                    return true;
        return false;
    }
    
    // Combine 2 maps of enchantments (and remove any invalid ones).
    private Map<Enchantment, Integer> combineEnchantments(Map<Enchantment, Integer> enchantments0, Map<Enchantment, Integer> enchantments1)
    {
        enchantments0 = fixEnchantments(enchantments0);
        Map<Enchantment, Integer> combined = new HashMap<Enchantment, Integer>(fixEnchantments(enchantments0));
        
        // If the second set of enchantments is null, the combined enchantments are just the first enchantments.
        if (enchantments1 == null)
            return combined;
        
        enchantments1  = fixEnchantments(enchantments1);
        // Loop through the enchantments of item1.
        for (Map.Entry<Enchantment, Integer > entry : enchantments1.entrySet()) 
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
            else if (enchantLevel == null)
                combined.put(entry.getKey(), entry.getValue());
        }
        
        // Get the protection enchantment rating for both enchantment sets.
        int protVal0 = Util.getProtectionEnchantmentsVal(enchantments0);
        int protVal1 = Util.getProtectionEnchantmentsVal(enchantments1);
        
        // If they have different protection enchantments, keep enchantment1's enchantments
        // And remove the protection enchantment from enchantments0. Yes, this system only works
        // If there is 1 protection enchantment on 
        if (protVal0 != 0 && protVal1 != 0 && protVal0 != protVal1)
        {
            switch(protVal0) 
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
        if (     repairItem.getType() == Material.LEATHER)
            mult *= (100 / LEATHER_TO_FULL);

        else if (repairItem.getType() == Material.GOLD_INGOT)
            mult *= (100 / GOLD_TO_FULL);

        else if (repairItem.getType() == Material.IRON_INGOT)
            mult *= (100 / IRON_TO_FULL);

        else if (repairItem.getType() == Material.DIAMOND)
            mult *= (100 / DIAMONDS_TO_FULL);

        int maxDurability = Material.ELYTRA.getMaxDurability();
        int newDurability = (int) (curDur - (maxDurability * mult));
        return (short) (newDurability <= 0 ? 0 : newDurability);
    }

    // Remove any disallowed enchantments in the map.
    private Map<Enchantment, Integer> fixEnchantments(Map<Enchantment, Integer> enchantments) 
    {
        Map<Enchantment, Integer> ret = new HashMap<Enchantment, Integer>(enchantments);
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
    
    /* 
     * Valid inputs: 
     *  - Elytra (armored or not)    + chestplate             -> Create Armored Elytra
     *  - Elytra (armored)           + enchanted book         -> Enchant
     *  - Elytra (armored)           + its repair item        -> Repair
     *  - Elytra (armored)           + other elytra (armored) -> Combine (Enchant + Repair)
     *  ! Elytra (armored, !leather) + leather                -> Block
     * 
     * Ignoring:
     *  - Elytra (not armored)       + !chestplate            -> None
     *  - *                          + *                      -> None
     */
    private Action isValidInput(ItemStack itemOne, ItemStack itemTwo)
    {
        if (itemOne == null || itemTwo == null)
            return Action.NONE;
        
        // If itemTwo is the elytra, while itemOne isn't, switch itemOne and itemTwo.
        if (itemTwo.getType() == Material.ELYTRA && itemOne.getType() != Material.ELYTRA)
        {
            ItemStack tmp = itemOne;
            itemOne       = itemTwo;
            itemTwo       = tmp;
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
            
            // If the armored elytra is to be combined with another armored elytra of the same tier...
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
        Player player    = (Player) event.getView().getPlayer();
        ItemStack itemA  = event.getInventory().getItem(0);
        ItemStack itemB  = event.getInventory().getItem(1);
        ItemStack result = null;
        
        if (itemA != null && itemB != null)
        {
            // If itemB is the elytra, while itemA isn't, switch itemA and itemB.
            if (itemB.getType() == Material.ELYTRA && itemA.getType() != Material.ELYTRA)
            {
                result = itemA;
                itemA  = itemB;
                itemB  = result;
                result = null;
            }
        }
        
        // Check if there are items in both input slots.
        if (itemA != null && itemB != null) 
        {
            Action action        = isValidInput(itemA, itemB);
            ArmorTier newTier    = ArmorTier.NONE;
            ArmorTier curTier    = nbtEditor.getArmorTier(itemA);
            short durability     = 0;
            Map<Enchantment, Integer> enchantments = itemA.getEnchantments();
            enchantments         = fixEnchantments(enchantments);
            
            switch (action)
            {
            case REPAIR:
                newTier          = curTier;
                durability       = repairItem(itemA.getDurability(), itemB);
                break;
            case COMBINE:
                newTier          = curTier;
                durability       = (short) (- itemA.getType().getMaxDurability() - itemA.getDurability() - itemB.getDurability());
                durability       = durability < 0 ? 0 : durability;
                enchantments     = combineEnchantments(enchantments, itemB.getEnchantments());
                break;
            case CREATE:
                newTier          = Util.armorToTier(itemB.getType());
                durability       = 0;
                enchantments     = combineEnchantments(enchantments, itemB.getEnchantments());
                break;
            case ENCHANT:
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) itemB.getItemMeta();
                newTier          = curTier;
                durability       = itemA.getDurability();
                // If there aren't any illegal enchantments on the book, continue as normal.
                // Otherwise... Block.
                if (verifyEnchantments(meta.getStoredEnchants()) != meta.getStoredEnchants().size())
                {
                    enchantments = combineEnchantments(enchantments, meta.getStoredEnchants());
                    break;
                }
            case BLOCK:
                event.setResult(null);
                player.updateInventory();
            case NONE:
                return;
            }
            
            if (Util.playerHasCraftPerm(player, newTier))
            {
                result = new ItemStack(Material.ELYTRA, 1);
                if (enchantments != null)
                    result.addUnsafeEnchantments(enchantments);
                result.setDurability(durability);
                
                result = nbtEditor.addArmorNBTTags(result, newTier, plugin.getConfigLoader().getBool("unbreakable"));
                event.setResult(result);
            }
        }
        
        // Check if either itemA or itemB is unoccupied.
        if ((itemA == null || itemB == null) && nbtEditor.getArmorTier(event.getInventory().getItem(2)) != ArmorTier.NONE) 
            // If Item2 is occupied despite itemA or itemB not being occupied. (only for armored elytra)/
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
            // Print warning to console and exit onInventoryClick event (no support for custom inventories as they are usually used for GUI's).
            plugin.debugMsg(Level.WARNING, "Could not cast inventory to anvilInventory for player " + player.getName() + "! Armored Elytras cannot be crafted!");
            return;
        }
        
        int slot = e.getRawSlot();
        
        if (slot == 2 && anvilInventory.getItem(0) != null && anvilInventory.getItem(1) != null && anvilInventory.getItem(2) != null) 
        {
            ArmorTier armortier = nbtEditor.getArmorTier(anvilInventory.getItem(2));
            // If there's an armored elytra in the final slot...
            if (armortier != ArmorTier.NONE && Util.playerHasCraftPerm(player, armortier)) 
            {
                // Create a new armored elytra and give that one to the player instead of the result.
                // This is done because after putting item0 in AFTER item1, the first letter of the color code shows up, this gets rid of that problem.
                ItemStack result = nbtEditor.addArmorNBTTags(anvilInventory.getItem(2), armortier, plugin.getConfigLoader().getBool("unbreakable"));
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
                this.cleanAnvil(anvilInventory);
                player.updateInventory();
                return;
            }
        }
    }
    
    // Make sure the player has the correct permission and that the item is not broken.
    private AllowedToWearEnum isAllowedToWear(ItemStack elytra, Player player, ArmorTier armorTier)
    {
        if (Util.isBroken(elytra))
            return AllowedToWearEnum.BROKEN;
        if (!Util.playerHasWearPerm(player, armorTier))
            return AllowedToWearEnum.NOPERMISSION;
        return AllowedToWearEnum.ALLOWED;
    }
    
    // Check if the player tries to equip armor by richt clicking it.
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) 
    {
        if (!event.getAction().equals(org.bukkit.event.block.Action.RIGHT_CLICK_AIR) &&
            !event.getAction().equals(org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK))
            return;
        
        ItemStack item = event.getItem();
        if (item == null)
            return;
        
        Player player  = event.getPlayer();  
        
        if (item.getType() == Material.ELYTRA) 
        {
            ArmorTier armorTier = nbtEditor.getArmorTier(item);
            if (nbtEditor.getArmorTier(item) == ArmorTier.NONE)
                return;
            AllowedToWearEnum allowed = this.isAllowedToWear(item, player, armorTier);
            if (allowed == AllowedToWearEnum.BROKEN) 
            {
                plugin.messagePlayer(player, plugin.getMyMessages().getString("MESSAGES.RepairNeeded"));
                event.setCancelled(true);
                player.updateInventory();
            } 
            else if (allowed == AllowedToWearEnum.NOPERMISSION)
            {
                plugin.usageDeniedMessage(player, armorTier);
                event.setCancelled(true);
                player.updateInventory();
            }
        }
    }
    
    // Player closes their inventory. Also checks for whether they are allowed to wear the armored elytra they are wearing.
    // This is done again here because there are ways to  bypass permission check when equipping.
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e)
    {
        verifyArmorInChestSlot((Player) e.getPlayer());
    }
    
    // Check if the player is allowed to wear the armored elytra based on their permissions.
    private void verifyArmorInChestSlot(Player player)
    {
        ItemStack chestplate = player.getInventory().getChestplate();
        // If the player equips a new chestplate.
        if (player.getInventory().getChestplate() == null)
            return;

        ArmorTier armorTier = nbtEditor.getArmorTier(chestplate);
        // If that chestplate is an (armored) elytra.
        if (armorTier == ArmorTier.NONE)
            return;
        
        AllowedToWearEnum allowed = this.isAllowedToWear(chestplate, player, armorTier);
        if (allowed == AllowedToWearEnum.BROKEN) 
        {
            plugin.messagePlayer(player, plugin.getMyMessages().getString("MESSAGES.RepairNeeded"));
            this.unenquipChestPlayer(player);
        } 
        else if (allowed == AllowedToWearEnum.NOPERMISSION)
        {
            plugin.usageDeniedMessage(player, armorTier);
            this.unenquipChestPlayer(player);
        }
    }
    
    // Because the armored elytra doesn't actually give any armor, the damage received by players wearing an armored elytra is calculated here.
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e) 
    {
        if(!(e.getEntity() instanceof Player))
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
                int durability    = p.getInventory().getChestplate().getDurability();
                int maxDurability = p.getInventory().getChestplate().getType().getMaxDurability();
                int newDurability = (int) (durability + ((int) (e.getDamage() / 4) > 1 ? (int) (e.getDamage() / 4) : 1));
                
                // If the elytra has the durability enchantment, we calculate the durability loss ourselves.
                if (p.getInventory().getChestplate().containsEnchantment(Enchantment.DURABILITY)) 
                {
                    // Get a random int between 0 and 100 to use in deciding if the durability enchantment will take effect.
                    Random r            = new Random();
                    int randomInt       = r.nextInt(101);
                    int enchantLevel    = p.getInventory().getChestplate().getEnchantmentLevel(Enchantment.DURABILITY);
                    int durabilityDelta = (100 / (enchantLevel + 1)) < randomInt ? 0 : 1;
                    // If the durability equals/exceeds maxDurability, it's broken (0 = full item durability).
                    if (durability >= maxDurability)
                        this.unenquipChestPlayer(p);
                    else 
                        newDurability = durability + durabilityDelta;
                }
                // If the item should be broken, make sure it really is broken and unequip it.
                if (newDurability >= maxDurability) 
                {
                    newDurability = maxDurability;
                    this.unenquipChestPlayer(p);
                }
                elytra.setDurability((short) (newDurability));
            }
        }
    }
    
    // Check if the player is trying to equip a broken elytra (and prevent that).
    @EventHandler
    public void playerEquipsArmor(InventoryClickEvent e)
    {
        if (!(e.getWhoClicked() instanceof Player))
            return;
        
        Player player = (Player) e.getWhoClicked();
        new BukkitRunnable() 
        {
            @Override
            public void run() 
            {
                verifyArmorInChestSlot(player);
            }
        }.runTaskLater(this.plugin, 1);
    }
}