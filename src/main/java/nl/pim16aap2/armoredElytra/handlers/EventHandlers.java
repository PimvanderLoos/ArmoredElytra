package nl.pim16aap2.armoredElytra.handlers;

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
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.Util;

public class EventHandlers implements Listener 
{
	private int DIAMONDS_TO_FULL;
	private int LEATHER_TO_FULL;
	private int GOLD_TO_FULL;
	private int IRON_TO_FULL;
	private boolean cursesAllowed; 
	private NBTEditor nbtEditor;
	private final ArmoredElytra plugin;
	private List<String> allowedEnchantments;
	private String[] cursedEnchantments  = {"MENDING",
		    								   "VANISHING_CURSE",
                                            "BINDING_CURSE"};
	
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
	
	// Copy enchants of 2 items to one item.
	public ItemStack addEnchants(ItemStack itemOne, ItemStack itemTwo, Player player) 
	{
		ItemStack result = itemOne.clone();
		
		Map<Enchantment, Integer> newEnchantments = itemTwo.getEnchantments();
		
		// Enchants from enchanted books have to be accessed in a different way.
		if (itemTwo.getType() == Material.ENCHANTED_BOOK && (nbtEditor.getArmorTier(itemOne) != ArmorTier.NONE)) 
		{
			EnchantmentStorageMeta meta = (EnchantmentStorageMeta)itemTwo.getItemMeta();
			newEnchantments = meta.getStoredEnchants();
		}
		
		// Copy enchantments from item1 to result.
		if (newEnchantments!=null) 
		{
			// Loop through the enchantments of item1.
			for (Map.Entry<Enchantment, Integer > entry : newEnchantments.entrySet()) 
			{
				// If the enchantment is a curse and if the result does not already have it.
				if (isCursedEnchantment(entry.getKey()) && !result.containsEnchantment(entry.getKey())) 
				{
					// If curses are allowed, apply the curse to the result.
					if (cursesAllowed)
						result.addEnchantment(entry.getKey(), entry.getValue());
				} 
				else if (isAllowedEnchantment(entry.getKey())) 
				{
					int enchantLevel = entry.getValue();
					// If item0 and item1 both have the same enchantment at the same level, result has level+1.
					// If item0 and item1 both have the same enchantment at different levels, give the highest level to result.
					if (newEnchantments != null) 
					{
						// Loop through the enchantments of item0 (which are already on the result).
						for (Map.Entry<Enchantment, Integer > rentry : newEnchantments.entrySet()) 
						{
							if (entry.getKey().getName() == rentry.getKey().getName()) 
							{
								// If they both have the same level of the same enchantment, the result will have that enchantment 1 level higher (if possible).
								if (entry.getValue() == rentry.getValue() && entry.getValue() < entry.getKey().getMaxLevel()) 
									enchantLevel = entry.getValue() + 1;
								else if (entry.getValue() < rentry.getValue()) 
									enchantLevel = rentry.getValue();
							}
						}
					}
					result.addUnsafeEnchantment(entry.getKey(), enchantLevel);
				}
			}
		}
		return result;
	}
	
	// Copy enchants of 2 items to one item.
	public ItemStack repairItem(ItemStack one, ItemStack two) 
	{
		// Create the resulting item.
		ItemStack result = one.clone();
		
		// Get the multiplier for the repair items.
		double mult = 0.01;
		if (two.getType() == Material.LEATHER)
			mult *= (100/LEATHER_TO_FULL);

		else if (two.getType() == Material.GOLD_INGOT)
			mult *= (100/GOLD_TO_FULL);

		else if (two.getType() == Material.IRON_INGOT)
			mult *= (100/IRON_TO_FULL);

		else if (two.getType() == Material.DIAMOND)
			mult *= (100/DIAMONDS_TO_FULL);

		int maxDurability = one.getType().getMaxDurability();
		int durability    = one.getDurability();
		int newDurability = (int) (durability - (maxDurability * mult));
		result.setDurability((short) (newDurability <= 0 ? 0 : newDurability));
		return result;
	}
	
	public boolean verifyEnchants(Map<Enchantment, Integer> enchantments)
	{
		for (Map.Entry<Enchantment, Integer > entry : enchantments.entrySet()) 
		{
			// If it's a cursed enchantment, while it's not allowed, it's false.
			if (isCursedEnchantment(entry.getKey()))
				if (!cursesAllowed)
					return false;
			// If the enchantment is not allowed, it's false.
			else if (!isAllowedEnchantment(entry.getKey())) 
				return false;
		}
		return true;
	}
	
	public ItemStack fixEnchants(ItemStack item) 
	{
		ItemStack result = item.clone();
		for (Map.Entry<Enchantment, Integer> entry : result.getEnchantments().entrySet())
			if (isAllowedEnchantment(entry.getKey()) == false && (cursesAllowed && isCursedEnchantment(entry.getKey())) == false)
				result.removeEnchantment(entry.getKey());
		return result;
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
				
				if (slot == 2 && anvilInventory.getItem(2) != null) 
				{
					// If there is an elytra in the final slot (it is unenchantable by default, so we can reasonably expect it to be an enchanted elytra)
					// and the player selects it, let the player transfer it to their inventory.
					// Verify the end result first, to prevent glitches. If the end result is invalid, remove the item and update the player's inventory.
					if (anvilInventory.getItem(2).getType() == Material.ELYTRA && 
							anvilInventory.getItem(0) != null && 
							anvilInventory.getItem(1) != null && 
							verifyEnchants(anvilInventory.getItem(2).getEnchantments())) 
					{
						// If the elytra is armored with any tier other than leather and the other item is leather, remove the elytra.
						if ((nbtEditor.getArmorTier(anvilInventory.getItem(0))   	!= ArmorTier.LEATHER 	|| 
								nbtEditor.getArmorTier(anvilInventory.getItem(1))	!= ArmorTier.LEATHER)	&&
							(anvilInventory.getItem(0).getType() 	== Material.LEATHER              	|| 
								anvilInventory.getItem(1).getType() 	== Material.LEATHER)                	&&
							(nbtEditor.getArmorTier(anvilInventory.getItem(0)) 	!= ArmorTier.NONE     	|| 
								nbtEditor.getArmorTier(anvilInventory.getItem(1))	!= ArmorTier.NONE))
						{
							anvilInventory.getItem(2).setAmount(0);
							p.updateInventory();
							return;
						}
						else if (e.isShiftClick()) 
							p.getInventory().addItem(anvilInventory.getItem(2));
						else 
							p.setItemOnCursor(anvilInventory.getItem(2));
						// Clean the anvil's inventory after transferring the items.
						cleanAnvil(anvilInventory);
					}
					else
					{
						anvilInventory.getItem(2).setAmount(0);
						p.updateInventory();
					}
				}
				
		        new BukkitRunnable() 
		        {
		            @Override
	                public void run() 
	                {
		            		ItemStack itemA = anvilInventory.getItem(0);
						ItemStack itemB = anvilInventory.getItem(1);
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
			                	// Check if the first input slot contains an elytra.
			                	if (itemA.getType() == Material.ELYTRA) 
			                	{
			                		ArmorTier armorTier = ArmorTier.NONE;
			                		ArmorTier currentArmorTier = nbtEditor.getArmorTier(itemA);
			                		
			                		if (currentArmorTier == ArmorTier.NONE && itemB.getType() == Material.LEATHER)
			                			return;
			                		
			                		// Check if the second input slot contains a diamond chestplate.
			                		else if (itemB.getType()	== Material.LEATHER_CHESTPLATE  	||
			                				 itemB.getType()	== Material.GOLD_CHESTPLATE     	||
			                				 itemB.getType()	== Material.CHAINMAIL_CHESTPLATE	||
			                				 itemB.getType()	== Material.IRON_CHESTPLATE     	||
			                				 itemB.getType()	== Material.DIAMOND_CHESTPLATE) 
				                	{
				                		// Combine the enchantments of the two items in the input slots.
				                		result = addEnchants(itemA, itemB, p);
				                		
				                		if (itemB.getType() == Material.LEATHER_CHESTPLATE)
				                			armorTier = ArmorTier.LEATHER;
				                		else if (itemB.getType() == Material.GOLD_CHESTPLATE)
				                			armorTier = ArmorTier.GOLD;
				                		else if (itemB.getType() == Material.CHAINMAIL_CHESTPLATE)
				                			armorTier = ArmorTier.CHAIN;
				                		else if (itemB.getType() == Material.IRON_CHESTPLATE)
				                			armorTier = ArmorTier.IRON;
				                		else if (itemB.getType() == Material.DIAMOND_CHESTPLATE)
				                			armorTier = ArmorTier.DIAMOND;
				                		
				                		short durability = (short) (- itemA.getType().getMaxDurability() - itemA.getDurability() - itemB.getDurability());
				                		durability = durability < 0 ? 0 : durability;
			                			result.setDurability(durability);
				                	} 
								// If the player tries to repair an armored elytra. Check if the armor tier and the repair item match.
				                	// If the repair item is leather it can only repair 
				                	else if ((itemB.getType() == Material.LEATHER    && currentArmorTier == ArmorTier.LEATHER) || 
			                			    	 (itemB.getType() == Material.GOLD_INGOT && currentArmorTier == ArmorTier.GOLD   ) || 
			                			    	 (itemB.getType() == Material.IRON_INGOT && currentArmorTier == ArmorTier.IRON   ) || 
			                			    	 (itemB.getType() == Material.IRON_INGOT && currentArmorTier == ArmorTier.CHAIN  ) || 
			                			    	 (itemB.getType() == Material.DIAMOND    && currentArmorTier == ArmorTier.DIAMOND)) 
				                	{
				                		// Repair the item in the first input slot with items from the second input slot.
				                		result = repairItem(itemA, itemB);
				                	}
				                	// Check if it is an enchanted book for itemB.
				                	else if (itemB.getType() == Material.ENCHANTED_BOOK)
				                		result = addEnchants(itemA, itemB, p);
				                	
				                	// If itemA and itemB are both armored elytras of the same armor tier, repair + share enchantments
				                	else if (itemB.getType() == Material.ELYTRA)
				                	{
				                		if (nbtEditor.getArmorTier(itemB) != ArmorTier.NONE && nbtEditor.getArmorTier(itemA) == nbtEditor.getArmorTier(itemB))
				                		{
				                			result = addEnchants(itemA, itemB, p);
					                		short durability = (short) (- itemA.getType().getMaxDurability() - itemA.getDurability() - itemB.getDurability());
					                		durability = durability < 0 ? 0 : durability;
				                			result.setDurability(durability);
				                		}
				                	}

				                	// Otherwise, remove the item in the result slot (slot2).
				                	else if (anvilInventory.getItem(2) != null)
			                			anvilInventory.getItem(2).setAmount(0);
				                	
								// Put the created item in the second slot of the anvil.
				                	if (result!=null) 
				                	{
					                	if (itemB.getType() 		== Material.LEATHER_CHESTPLATE  	||
					                			itemB.getType() 	== Material.GOLD_CHESTPLATE     	||
					                			itemB.getType() 	== Material.CHAINMAIL_CHESTPLATE	||
					                			itemB.getType() 	== Material.IRON_CHESTPLATE     	||
					                			itemB.getType() 	== Material.DIAMOND_CHESTPLATE)
				                			// Add the NBT Tags for the elytra, to give it armorTier tier of armor protection.
					                		result = nbtEditor.addArmorNBTTags(result, armorTier, plugin.getConfigLoader().getBool("unbreakable"));
					                	else if ((nbtEditor.getArmorTier(itemA) 	!= ArmorTier.NONE) && 
					                			 (nbtEditor.getArmorTier(result)	!= ArmorTier.NONE))
				                		{
				                			armorTier = nbtEditor.getArmorTier(itemA);
				                			result = nbtEditor.addArmorNBTTags(result, armorTier, plugin.getConfigLoader().getBool("unbreakable"));
				                		}
					                	result = fixEnchants(result);
									anvilInventory.setItem(2, result);
				                	}
				                	else if (anvilInventory.getItem(2) != null)
			                			anvilInventory.getItem(2).setAmount(0);
			                	}
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
						    		int durabilityDelta = (100/(enchantLevel+1)) < randomInt ? 0 : 1;
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