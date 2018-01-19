package nl.pim16aap2.armoredElytra.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.ChatColor;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.scheduler.BukkitRunnable;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.nms.NBTEditor;
import nl.pim16aap2.armoredElytra.util.Action;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.Util;

public class EventHandlers implements Listener 
{
	private int             DIAMONDS_TO_FULL;
	private int              LEATHER_TO_FULL;
	private int                 GOLD_TO_FULL;
	private int                 IRON_TO_FULL;
	private boolean            cursesAllowed; 
	private NBTEditor              nbtEditor;
	private final ArmoredElytra       plugin;
	private List<String> allowedEnchantments;
	private String[]     cursedEnchantments = {	"MENDING"			,
		    								      	"VANISHING_CURSE" 	,
                                               	"BINDING_CURSE"		};
	
	public EventHandlers(ArmoredElytra plugin, NBTEditor nbtEditor) 
	{
		this.plugin    = plugin;
		this.nbtEditor = nbtEditor;
		
		// Get the values of the config options.
		this.cursesAllowed       = plugin.getConfigLoader().getBool("allowCurses");
		this.allowedEnchantments = plugin.getConfigLoader().getStringList("allowedEnchantments");
		this.LEATHER_TO_FULL     = plugin.getConfigLoader().getInt("leatherRepair");
		this.GOLD_TO_FULL        = plugin.getConfigLoader().getInt("goldRepair");
		this.IRON_TO_FULL        = plugin.getConfigLoader().getInt("ironRepair");
		this.DIAMONDS_TO_FULL    = plugin.getConfigLoader().getInt("diamondsRepair");
	}
	
	// Clear the anvil's inventory (destroy all the items in all 3 slots (second slot is not emptied, when repairing you can safely give multiple items)).
	public void cleanAnvil(AnvilInventory anvilInventory)
	{
		anvilInventory.getItem(0).setAmount(0);
		anvilInventory.getItem(1).setAmount(anvilInventory.getItem(1).getAmount() - 1);
		anvilInventory.getItem(2).setAmount(0);
	}
	
	// Accessor for this.plugin
	public ArmoredElytra getPlugin()
	{
		return this.plugin;
	}
	
	// Check if the enchantment is allowed on elytras.
	public boolean isAllowedEnchantment(Enchantment enchant) 
	{
		for (String s : allowedEnchantments)
			if (Enchantment.getByName(s) != null)
				if (Enchantment.getByName(s).equals(enchant))
					return true;
		return false;
	}
	
	// Check if the enchantment is a curse.
	public boolean isCursedEnchantment(Enchantment enchant) 
	{
		for (String s : cursedEnchantments)
			if (Enchantment.getByName(s).equals(enchant))
				return true;
		return false;
	}
	
	// Combine 2 maps of enchantments (and remove any invalid ones).
	public Map<Enchantment, Integer> combineEnchantments(Map<Enchantment, Integer> enchantments0, Map<Enchantment, Integer> enchantments1)
	{
		fixEnchantments(enchantments0);
		Map<Enchantment, Integer> combined = new HashMap<Enchantment, Integer>(enchantments0);
		
		if (enchantments1 != null)
		{
			fixEnchantments(enchantments1);
			// Loop through the enchantments of item1.
			for (Map.Entry<Enchantment, Integer > entry : enchantments1.entrySet()) 
			{
				Integer enchantLevel = enchantments0.get(entry.getKey());
				// If the enchantment is a curse and if the result does not already have it.
				if (isCursedEnchantment(entry.getKey()) && !enchantments0.containsKey(entry.getKey())) 
					combined.put(entry.getKey(), entry.getValue());
				// If the enchantment is already on the list...
				else if (enchantLevel != null)
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
		}
		return combined;
	}
	
	// Repair an Armored Elytra
	public short repairItem(short curDur, ItemStack repairItem) 
	{	
		// Get the multiplier for the repair items.
		double mult = 0.01;
		if (repairItem.getType() == Material.LEATHER)
			mult *= (100/LEATHER_TO_FULL);

		else if (repairItem.getType() == Material.GOLD_INGOT)
			mult *= (100/GOLD_TO_FULL);

		else if (repairItem.getType() == Material.IRON_INGOT)
			mult *= (100/IRON_TO_FULL);

		else if (repairItem.getType() == Material.DIAMOND)
			mult *= (100/DIAMONDS_TO_FULL);

		int maxDurability = Material.ELYTRA.getMaxDurability();
		int newDurability = (int) (curDur - (maxDurability * mult));
		return (short) (newDurability <= 0 ? 0 : newDurability);
	}
	
	// Check if there aren't any disallowed enchantments / curses in the map.
	public boolean verifyEnchants(Map<Enchantment, Integer> enchantments)
	{
		for (Map.Entry<Enchantment, Integer > entry : enchantments.entrySet()) 
		{
			// If it's a cursed enchantment, while it's not allowed, it's false.
			if (isCursedEnchantment(entry.getKey()) && !cursesAllowed)
				return false;
			// If the enchantment is not allowed, it's false.
			else if (!isAllowedEnchantment(entry.getKey())) 
				return false;
		}
		return true;
	}
	
	// Remove any disallowed enchantments / curses in the map.
	public Map<Enchantment, Integer> fixEnchantments(Map<Enchantment, Integer> enchantments) 
	{
		Map<Enchantment, Integer> ret = new HashMap<Enchantment, Integer>(enchantments);
		for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet())
			if (isAllowedEnchantment(entry.getKey()) == false && (cursesAllowed && isCursedEnchantment(entry.getKey())) == false)
				ret.remove(entry.getKey());
		return ret;
	}
	
	// Fix enchantments on an item.
	public ItemStack fixEnchantments(ItemStack item) 
	{
		ItemStack result = item.clone();
		for (Map.Entry<Enchantment, Integer> entry : result.getEnchantments().entrySet())
			if (isAllowedEnchantment(entry.getKey()) == false && (cursesAllowed && isCursedEnchantment(entry.getKey())) == false)
				result.removeEnchantment(entry.getKey());
		return result;
	}
	
	// Get the armor tier from a chest plate.
	public ArmorTier armorToTier(Material item)
	{
		ArmorTier ret = ArmorTier.NONE;
		
		switch (item)
		{
		case LEATHER_CHESTPLATE:
			ret = ArmorTier.LEATHER;
			break;
		case GOLD_CHESTPLATE:
			ret = ArmorTier.GOLD;
			break;
		case CHAINMAIL_CHESTPLATE:
			ret = ArmorTier.CHAIN;
			break;
		case IRON_CHESTPLATE:
			ret = ArmorTier.IRON;
			break;
		case DIAMOND_CHESTPLATE:
			ret = ArmorTier.DIAMOND;
			break;
		default:
			break;
		}
		return ret;
	}
	
	// Check if mat is a chest plate.
	public boolean isChestPlate(Material mat)
	{
		if (mat == Material.LEATHER_CHESTPLATE 	|| mat == Material.GOLD_CHESTPLATE || 
			mat == Material.CHAINMAIL_CHESTPLATE	|| mat == Material.IRON_CHESTPLATE || 
			mat == Material.DIAMOND_CHESTPLATE)
			return true;
		return false;
	}
	
	/* 
	 * Valid inputs: 
	 * 	- Elytra (armored or not) 	+ chestplate				-> Create Armored Elytra
	 * 	- Elytra (armored)        	+ enchanted book			-> Enchant
	 * 	- Elytra (armored)        	+ its repair item		-> Repair
	 * 	- Elytra (armored)        	+ other elytra (armored)	-> Combine (Enchant + Repair)
	 * 	! Elytra (armored, !leather)	+ leather				-> Block
	 * 
	 * Ignoring:
	 * 	- Elytra (not armored)		+ !chestplate			-> None
	 * 	- *							+ *						-> None
	 */
	public Action isValidInput(ItemStack itemOne, ItemStack itemTwo)
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
		
		Material matTwo = itemTwo.getType();
		
		// If the elytra is to be combined with chest armor...
		if (isChestPlate(matTwo))
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
			if (tier != ArmorTier.LEATHER && matTwo == Material.LEATHER)
				return Action.BLOCK;
		}
		return Action.NONE;
	}
	
	// Handle the anvil related parts.
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) 
	{
		if (e.getWhoClicked() instanceof Player) 
		{
			// Check if the event was a player who interacted with an anvil.
			Player p = (Player) e.getWhoClicked();
			if (e.getView().getType() == InventoryType.ANVIL) 
			{
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
					plugin.debugMsg(Level.WARNING, "Could not cast inventory to anvilInventory for player " + p.getName() + "! Armored Elytras cannot be crafted!");
					return;
				}
				
				int slot = e.getRawSlot(); // Get slot 				
				
				if (slot == 2 && anvilInventory.getItem(0) != null && anvilInventory.getItem(1) != null && anvilInventory.getItem(2) != null) 
				{
					// If there's an armored elytra in the final slot...
					if (anvilInventory.getItem(2).getType() == Material.ELYTRA && nbtEditor.getArmorTier(anvilInventory.getItem(2)) != ArmorTier.NONE) 
					{
						Action action = isValidInput(anvilInventory.getItem(0), anvilInventory.getItem(1));
						// If there is no action to be taken, take no action...
						if (action == Action.NONE) 
							return;
						// If the action is to block... Well, Block! (get rid of the end result!)
						// Also check if there are any disallowed enchantments/curses on the result, delete the result.
						if (action == Action.BLOCK)
						{
							// Send message to player that the recipe is invalid, as clearing the inventory doesn't work properly.
							plugin.messagePlayer(p, ChatColor.RED, "Invalid recipe!");
							anvilInventory.setItem(2, null);
							anvilInventory.clear(2);
							p.updateInventory();
							p.updateInventory();
							p.updateInventory();
							return;
						}
						
						// Give the result to the player and clear the anvil's inventory.
						if (e.isShiftClick()) 
							p.getInventory().addItem(fixEnchantments(anvilInventory.getItem(2)));
						else 
							p.setItemOnCursor(fixEnchantments(anvilInventory.getItem(2)));
						// Clean the anvil's inventory after transferring the items.
						cleanAnvil(anvilInventory);
						return;
					}
				}
				
		        new BukkitRunnable() 
		        {
					@Override
	                public void run() 
	                {
		            		ItemStack itemA  = anvilInventory.getItem(0);
						ItemStack itemB  = anvilInventory.getItem(1);
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
				        		Action action     	= isValidInput(itemA, itemB);
				        		ArmorTier newTier 	= ArmorTier.NONE;
				        		ArmorTier curTier 	= nbtEditor.getArmorTier(itemA);
				        		short durability  	= 0;
				        		Map<Enchantment, Integer> enchantments = itemA.getEnchantments();
				        		enchantments			= fixEnchantments(enchantments);
				        		
				        		switch (action)
				        		{
				        		case REPAIR:
				        			newTier 			= curTier;
				        			durability		= repairItem(itemA.getDurability(), itemB);
				        			break;
				        		case ENCHANT:
				        			EnchantmentStorageMeta meta 	= (EnchantmentStorageMeta) itemB.getItemMeta();
				        			newTier 			= curTier;
				        			durability		= itemA.getDurability();
				        			enchantments 	= combineEnchantments(enchantments, meta.getStoredEnchants());
				        			break;
				        		case COMBINE:
				        			newTier 			= curTier;
				        			durability    	= (short) (- itemA.getType().getMaxDurability() - itemA.getDurability() - itemB.getDurability());
				        			durability 		= durability < 0 ? 0 : durability;
			                		enchantments 	= combineEnchantments(enchantments, itemB.getEnchantments());
				        			break;
				        		case CREATE:
				        			newTier 			= armorToTier(itemB.getType());
				        			durability 		= 0;
				        			enchantments 	= combineEnchantments(enchantments, itemB.getEnchantments());
				        			break;
				        		case BLOCK:
				        			if (anvilInventory.getItem(2) != null)
				        				anvilInventory.getItem(2).setAmount(0);
								p.updateInventory();
				        		case NONE:
				        			return;
				        		}
				        		
					        	result = new ItemStack(Material.ELYTRA, 1);
					        	if (enchantments != null)
					        		result.addUnsafeEnchantments(enchantments);
					        	result.setDurability(durability);
					        	result = nbtEditor.addArmorNBTTags(result, newTier, plugin.getConfigLoader().getBool("unbreakable"));
					        	anvilInventory.setItem(2, result);
				        }
				        
				        // Check if either itemA or itemB is unoccupied.
				        if (itemA == null || itemB == null) 
				        		// If Item2 is occupied despite Item1 not being occupied.
				        		if (anvilInventory.getItem(2) != null)
				        			// Then set the amount to 0.
				        			anvilInventory.getItem(2).setAmount(0);
						p.updateInventory();
		            }
		        }.runTaskLater(this.plugin, 1);
			}
		}
	}
	
	// Check if the player tries to equip armor by richt clicking it.
	@SuppressWarnings("deprecation")
	@EventHandler
    public void onRightClick(PlayerInteractEvent event) 
	{
        Player player = event.getPlayer();    
    
        ItemStack item = player.getItemInHand();
    
        if (item != null)
            	if (item.getType() == Material.ELYTRA && (nbtEditor.getArmorTier(item) != ArmorTier.NONE)) 
            	{
            		ArmorTier armorTier = nbtEditor.getArmorTier(item);
            		if ((armorTier == ArmorTier.LEATHER 	&& !player.hasPermission("armoredelytra.wear.leather"))  || 
		            (armorTier == ArmorTier.GOLD 	&& !player.hasPermission("armoredelytra.wear.gold"))     || 
		            (armorTier == ArmorTier.CHAIN 	&& !player.hasPermission("armoredelytra.wear.chain"))    || 
		            (armorTier == ArmorTier.IRON 	&& !player.hasPermission("armoredelytra.wear.iron"))     || 
		            (armorTier == ArmorTier.DIAMOND 	&& !player.hasPermission("armoredelytra.wear.diamond")))
            		{
            			plugin.usageDeniedMessage(player, armorTier);
		        		event.setCancelled(true);
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
	public void verifyArmorInChestSlot(Player player)
	{
		ItemStack chestplate = player.getInventory().getChestplate();
		// If the player equips a new chestplate.
		if (player.getInventory().getChestplate() != null) 
		{
			// If that chestplate is an (armored) elytra.
			if (chestplate.getType() == Material.ELYTRA && (nbtEditor.getArmorTier(chestplate) != ArmorTier.NONE)) 
			{
				ArmorTier armorTier = nbtEditor.getArmorTier(chestplate);
				if ((chestplate.getDurability() >= chestplate.getType().getMaxDurability())) 
				{
					plugin.messagePlayer(player, ChatColor.RED + "You cannot equip this elytra! Please repair it in an anvil first.");
					Util.unenquipChestPlayer(player);
				} 
				else if ((armorTier == ArmorTier.LEATHER	&& !player.hasPermission("armoredelytra.wear.leather")) || 
						 (armorTier == ArmorTier.GOLD 	&& !player.hasPermission("armoredelytra.wear.gold"   )) || 
						 (armorTier == ArmorTier.CHAIN 	&& !player.hasPermission("armoredelytra.wear.chain"  )) || 
						 (armorTier == ArmorTier.IRON 	&& !player.hasPermission("armoredelytra.wear.iron"   )) || 
						 (armorTier == ArmorTier.DIAMOND	&& !player.hasPermission("armoredelytra.wear.diamond")))
				{
					plugin.usageDeniedMessage(player, armorTier);
					Util.unenquipChestPlayer(player);
				}
				player.updateInventory();
			}
		}
	}
	
	// Because the armored elytra doesn't actually give any armor, the damage received by players wearing an armored elytra is calculated here.
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e) 
	{
		if(e.getEntity() instanceof Player) 
		{
			Player p = (Player) e.getEntity();
			// If the player didn't die from the damage.
			if ((p.getHealth() - e.getFinalDamage()) > 0)
			{
				if (p.getInventory().getChestplate()!=null) 
				{
					if (p.getInventory().getChestplate().getType() == Material.ELYTRA && 
						nbtEditor.getArmorTier(p.getInventory().getChestplate()) != ArmorTier.NONE) 
					{
						ItemStack elytra = p.getInventory().getChestplate();
						DamageCause cause = e.getCause();
						
						// The elytra doesn't receive any damage for these causes:
						if (cause != DamageCause.DROWNING  &&  cause != DamageCause.STARVATION     &&  cause != DamageCause.SUFFOCATION && 
						    cause != DamageCause.SUICIDE   &&  cause != DamageCause.FLY_INTO_WALL  &&  cause != DamageCause.POISON) 
						{
							int durability    = p.getInventory().getChestplate().getDurability();
							int maxDurability = p.getInventory().getChestplate().getType().getMaxDurability();
							int newDurability = (int) (durability + ((int) (e.getDamage() / 4) > 1 ? (int) (e.getDamage() / 4) : 1));
							
							// If the elytra has the durability enchantment, we calculate the durability loss ourselves.
							if (p.getInventory().getChestplate().containsEnchantment(Enchantment.DURABILITY)) {
					    		
								// Get a random int between 0 and 100
						    		Random r            = new Random();
						    		int randomInt       = r.nextInt(101);
						    		int enchantLevel    = p.getInventory().getChestplate().getEnchantmentLevel(Enchantment.DURABILITY);
						    		int durabilityDelta = (100 / (enchantLevel + 1)) < randomInt ? 0 : 1;
						    		if (durability >= maxDurability) 
									Util.unenquipChestPlayer(p);
						    		else 
						    			newDurability = durability + durabilityDelta;
							}
							// If the item should be broken, make sure it really is broken and unequip it.
							if (newDurability >= maxDurability) 
							{
								newDurability = maxDurability;
								Util.unenquipChestPlayer(p);
							}
							elytra.setDurability((short) (newDurability));
						}
					}
				}
			}
		}
	}
	
	// Check if the player is trying to equip a broken elytra (and prevent that).
	@EventHandler
    public void playerEquipsArmor(InventoryClickEvent e)
	{
		if (e.getWhoClicked() instanceof Player) 
		{
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
}