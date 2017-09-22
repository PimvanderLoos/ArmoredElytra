package nl.pim16aap2.armoredElytra;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.scheduler.BukkitRunnable;

import nl.pim16aap2.armoredElytra.nms.NBTEditor;

import net.md_5.bungee.api.ChatColor;

public class EventHandlers implements Listener 
{
	private int DIAMONDS_TO_FULL;
	private int LEATHER_TO_FULL;
	private int GOLD_TO_FULL;
	private int IRON_TO_FULL;
	private boolean cursesAllowed; 
	private NBTEditor nbtEditor;
	private final ArmoredElytra plugin;
	private String[] allowedEnchantments;
	private String[] cursedEnchantments  = {"MENDING",
		    								   "VANISHING_CURSE",
                                            "BINDING_CURSE"};
	
	public EventHandlers(ArmoredElytra plugin, NBTEditor nbtEditor, boolean allowCurses, int LEATHER_TO_FULL, int GOLD_TO_FULL, int IRON_TO_FULL, int DIAMONDS_TO_FULL, String[] allowedEnchantments) 
	{
		this.plugin = plugin;
		this.nbtEditor = nbtEditor;
		this.cursesAllowed = allowCurses;
		this.DIAMONDS_TO_FULL = DIAMONDS_TO_FULL;
		this.allowedEnchantments = allowedEnchantments;
		this.LEATHER_TO_FULL = LEATHER_TO_FULL;
		this.GOLD_TO_FULL = GOLD_TO_FULL;
		this.IRON_TO_FULL = IRON_TO_FULL;
	}
	
	
	// Clear the anvil's inventory (destroy all the items in all 3 slots (second slot is not emptied, when repairing you can safely give multiple items)).
	public void cleanAnvil(AnvilInventory anvilInventory)
	{
		anvilInventory.getItem(0).setAmount(0);
		anvilInventory.getItem(1).setAmount(anvilInventory.getItem(1).getAmount()-1);
		anvilInventory.getItem(2).setAmount(0);
	}
	
	
	// Check if the enchantment is allowed on elytras.
	public boolean isAllowedEnchantment(Enchantment enchant) 
	{
		for (String s : allowedEnchantments) 
		{
			if (Enchantment.getByName(s) != null) 
			{
				if (Enchantment.getByName(s).equals(enchant)) 
				{
					return true;
				}
			}
		}
		return false;
	}
	
	
	// Check if the enchantment is a curse.
	public boolean isCursedEnchantment(Enchantment enchant) 
	{
		for (String s : cursedEnchantments) 
		{
			if (Enchantment.getByName(s).equals(enchant)) 
			{
				return true;
			}
		}
		return false;
	}
	
	
	// Check if the elytra being checked is an armored one.
	public boolean isArmoredElytra(ItemStack elytra) 
	{
	if (elytra.hasItemMeta() && elytra.getType() == Material.ELYTRA) 
		if (elytra.getItemMeta().hasLore())
			if (elytra.getItemMeta().getLore().toString().equals("[This is an armored Elytra.]")) 
			{
				return true;
			}
		return false;
	}
	
	
	// Copy enchants of 2 items to one item.
	public ItemStack addEnchants(ItemStack itemOne, ItemStack itemTwo, Player p) 
	{
		// Create the resulting item;
		ItemStack result = new ItemStack(Material.ELYTRA, 1);
		
		// Get the enchantments of the first and second item in the anvil.
		Map<Enchantment, Integer> enchantmentsTemp = itemOne.getEnchantments();
		Map<Enchantment, Integer> enchantments0 = new HashMap<Enchantment, Integer>();
		Map<Enchantment, Integer> enchantments1 = itemTwo.getEnchantments();
		
		for (Map.Entry<Enchantment, Integer> entry : enchantmentsTemp.entrySet()) 
		{
			// Check if the enchantment is allowed or if it is a cursed enchantment while it's allowed.
			if (isAllowedEnchantment(entry.getKey()) || (cursesAllowed && isCursedEnchantment(entry.getKey())))
			{
				enchantments0.put(entry.getKey(), entry.getValue());
			}
		}
		// Add the enchantments copied from itemOne to the resulting item.
		result.addUnsafeEnchantments(enchantments0);
		
		// Enchants from enchanted books have to be access in a different way.
		if (itemTwo.getType() == Material.ENCHANTED_BOOK && isArmoredElytra(itemOne)) 
		{
			EnchantmentStorageMeta meta = (EnchantmentStorageMeta)itemTwo.getItemMeta();
			enchantments1 = meta.getStoredEnchants();
		}
		
		// Copy enchantments from item1 to result.
		if (enchantments1!=null) 
		{
			// Loop through the enchantments of item1.
			for (Map.Entry<Enchantment, Integer> entry : enchantments1.entrySet()) 
			{
				// If the enchantment is a curse and if the result does not already have it.
				if (isCursedEnchantment(entry.getKey()) && !result.containsEnchantment(entry.getKey())) 
				{
					// If curses are allowed, apply the curse to the result.
					if (cursesAllowed)
					{
						result.addEnchantment(entry.getKey(), entry.getValue());
					}
				} else if (isAllowedEnchantment(entry.getKey())) 
				{
					int enchantLevel = entry.getValue();
					// If item0 and item1 both have the same enchantment at the same level, result has level+1.
					// If item0 and item1 both have the same enchantment at different levels, give the highest level to result.
					if (enchantments0 != null) 
					{
						// Loop through the enchantments of item0 (which are already on the result).
						for (Map.Entry<Enchantment, Integer> rentry : enchantments0.entrySet()) 
						{
							if (entry.getKey().getName() == rentry.getKey().getName()) 
							{
								// If they both have the same level of the same enchantment, the result will have that enchantment 1 level higher (if possible).
								if (entry.getValue() == rentry.getValue() && entry.getValue() < entry.getKey().getMaxLevel()) 
								{
									enchantLevel = entry.getValue()+1;
								} else if (entry.getValue() < rentry.getValue()) 
								{
									enchantLevel = rentry.getValue();
								}
							}
						}
					}
					result.addUnsafeEnchantment(entry.getKey(), enchantLevel);
				} else 
				{
//					p.sendMessage(ChatColor.RED+"This enchantment is not allowed on this item!");
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
		{
			mult *= (100/LEATHER_TO_FULL);
			
		} else if (two.getType() == Material.GOLD_INGOT)
		{
			mult *= (100/GOLD_TO_FULL);
			
		} else if (two.getType() == Material.IRON_INGOT)
		{
			mult *= (100/IRON_TO_FULL);
			
		} else if (two.getType() == Material.DIAMOND)
		{
			mult *= (100/DIAMONDS_TO_FULL);
		}

		int maxDurability = one.getType().getMaxDurability();
		int durability    = one.getDurability();
		int newDurability = (int) (durability - (maxDurability*mult));
		result.setDurability((short) (newDurability <= 0 ? 0 : newDurability));
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
				AnvilInventory anvilInventory = (AnvilInventory) e.getInventory(); // Inventory type
				int slot = e.getRawSlot(); // Get slot 
				
				if (slot == 2 && anvilInventory.getItem(2) != null) 
				{
					// If there is an elytra in the final slot (it is unenchantable by default, so we can reasonably expect it to be an enchanted elytra)
					// and the player selects it, let the player transfer it to their inventory.
					if (anvilInventory.getItem(2).getType() == Material.ELYTRA) 
					{
						if (e.isShiftClick()) 
						{
							p.getInventory().addItem(anvilInventory.getItem(2));
						} else 
						{
							p.setItemOnCursor(anvilInventory.getItem(2));
						}
						// Clean the anvil's inventory after transferring the items.
						cleanAnvil(anvilInventory);
					}
				}
		        new BukkitRunnable() 
		        {
		            @Override
	                public void run() 
		            {
            				ItemStack result = null;
		            		// Check if there are items in both input slots.
				        if (anvilInventory.getItem(0) != null && anvilInventory.getItem(1) != null) 
				        {
			                	// Check if the first input slot contains an elytra.
			                	if (anvilInventory.getItem(0).getType() == Material.ELYTRA) 
			                	{
			                		int armorTier = 0;
			                		int currentArmorTier = 0;
			                		if (isArmoredElytra(anvilInventory.getItem(0)))
			                		{
			                			currentArmorTier = nbtEditor.getArmorTier(anvilInventory.getItem(0));
			                		}
			                		/* 0 = No Armor.
			                		 * 1 = Leather Armor.
			                		 * 2 = Gold Armor.
			                		 * 3 = Chain Armor.
			                		 * 4 = Iron Armor.
			                		 * 5 = Diamond Armor.
			                		 */
			                		// Check if the second input slot contains a diamond chestplate.
				                	if (anvilInventory.getItem(1).getType() == Material.LEATHER_CHESTPLATE   ||
			                			anvilInventory.getItem(1).getType() == Material.GOLD_CHESTPLATE      ||
			                			anvilInventory.getItem(1).getType() == Material.CHAINMAIL_CHESTPLATE ||
			                			anvilInventory.getItem(1).getType() == Material.IRON_CHESTPLATE      ||
			                			anvilInventory.getItem(1).getType() == Material.DIAMOND_CHESTPLATE) 
				                	{
				                		// Combine the enchantments of the two items in the input slots.
				                		result = addEnchants(anvilInventory.getItem(0), anvilInventory.getItem(1), p);
				                		if (anvilInventory.getItem(1).getType() == Material.LEATHER_CHESTPLATE) 
				                		{
				                			armorTier = 1;
				                		} else if (anvilInventory.getItem(1).getType() == Material.GOLD_CHESTPLATE) 
				                		{
				                			armorTier = 2;
				                		} else if (anvilInventory.getItem(1).getType() == Material.CHAINMAIL_CHESTPLATE) 
				                		{
				                			armorTier = 3;
				                		} else if (anvilInventory.getItem(1).getType() == Material.IRON_CHESTPLATE) 
				                		{
				                			armorTier = 4;
				                		} else if (anvilInventory.getItem(1).getType() == Material.DIAMOND_CHESTPLATE) 
				                		{
				                			armorTier = 5;
				                		}
				                		short durability = (short) (-anvilInventory.getItem(0).getType().getMaxDurability() - anvilInventory.getItem(0).getDurability() - anvilInventory.getItem(1).getDurability());
				                		durability = durability < 0 ? 0 : durability;
			                			result.setDurability(durability);
				                	} 
								// If the player tries to repair an armored elytra. Check if the armor tier and the repair item match.
				                	// If the repair item is leather it can only repair 
				                	else if ((anvilInventory.getItem(1).getType() == Material.LEATHER    && (!isArmoredElytra(anvilInventory.getItem(0))) || currentArmorTier == 1) || 
			                			    	 	(anvilInventory.getItem(1).getType() == Material.GOLD_INGOT &&  isArmoredElytra(anvilInventory.getItem(0))   && currentArmorTier == 2) || 
			                			    	 	(anvilInventory.getItem(1).getType() == Material.IRON_INGOT &&  isArmoredElytra(anvilInventory.getItem(0))   && currentArmorTier == 3) || 
			                			    	 	(anvilInventory.getItem(1).getType() == Material.IRON_INGOT &&  isArmoredElytra(anvilInventory.getItem(0))   && currentArmorTier == 4) || 
			                			    	 	(anvilInventory.getItem(1).getType() == Material.DIAMOND    &&  isArmoredElytra(anvilInventory.getItem(0))   && currentArmorTier == 5)) 
				                	{
				                		// Repair the item in the first input slot with items from the second input slot.
				                		result = repairItem(anvilInventory.getItem(0), anvilInventory.getItem(1));
				                	}
				                	// Otherwise, remove the item in the result slot (slot2).
				                	else 
				                	{
				                		if (anvilInventory.getItem(2)!=null) 
				                		{
				                			anvilInventory.getItem(2).setAmount(0);
				                		}
				                	}
								// Put the created item in the second slot of the anvil.
				                	if (result!=null) 
				                	{
					                	if (anvilInventory.getItem(1).getType() == Material.LEATHER_CHESTPLATE       ||
					                			anvilInventory.getItem(1).getType() == Material.GOLD_CHESTPLATE      ||
					                			anvilInventory.getItem(1).getType() == Material.CHAINMAIL_CHESTPLATE ||
					                			anvilInventory.getItem(1).getType() == Material.IRON_CHESTPLATE      ||
					                			anvilInventory.getItem(1).getType() == Material.DIAMOND_CHESTPLATE) 
				                		{
				                			// Add the NBT Tags for the elytra, to give it diamond_chestplate tier of armor protection.
					                		result = nbtEditor.addArmorNBTTags(result, armorTier);
				                		}
									anvilInventory.setItem(2, result);
				                	}
			                	}
				        }
				        // Check if Item0 is occupied, but Item1 isn't.
				        if (anvilInventory.getItem(0) != null && anvilInventory.getItem(1) == null) 
				        {
				        		// If Item2 is occupied despite Item1 not being occupied.
				        		if (anvilInventory.getItem(2) != null)
				        		{
				        			// Then set the amount to 0.
				        			anvilInventory.getItem(2).setAmount(0);
				        		}
				        }
						p.updateInventory();
		            }
		        }.runTaskLater(this.plugin, 1);
			}
		}
	}
	
	
	// Because the armored elytra doesn't actually give any armor, the damage received by players wearing an armored elytra is calculated here.
	@EventHandler
	public void onPlayerDamage (EntityDamageEvent e) 
	{
		if(e.getEntity() instanceof Player) 
		{
			Player p = (Player) e.getEntity();
			if (p.getInventory().getChestplate()!=null) 
			{
				if (p.getInventory().getChestplate().getType() == Material.ELYTRA && isArmoredElytra(p.getInventory().getChestplate())) 
				{
					DamageCause cause = e.getCause();
					if (cause!=DamageCause.DROWNING    && cause!=DamageCause.STARVATION     && cause!=DamageCause.SUFFOCATION && 
					    cause!=DamageCause.SUICIDE     && cause!=DamageCause.FLY_INTO_WALL  && cause!=DamageCause.POISON) 
					{
						
						int durability    = p.getInventory().getChestplate().getDurability();
						int maxDurability = p.getInventory().getChestplate().getType().getMaxDurability();
						int newDurability = (int) (durability + ((int)(e.getDamage()/4) > 1 ? (int)(e.getDamage()/4) : 1));
						// If the elytra has the durability enchantment.
						if (p.getInventory().getChestplate().containsEnchantment(Enchantment.DURABILITY)) {
				    		Random r = new Random();
				    		// Get a random int between 0 and 100
				    		int randomInt = r.nextInt(101);
				    		int enchantLevel = p.getInventory().getChestplate().getEnchantmentLevel(Enchantment.DURABILITY);
				    		int durabilityDelta = (100/(enchantLevel+1)) < randomInt ? 0 : 1;
				    		if (durability>=maxDurability) 
				    		{
								unenquipChestPlayer(p);
				    		} else 
				    			newDurability = durability+durabilityDelta;
						}
						// If the item should be broken, make sure it really is broken and unequip it.
						if (newDurability >= maxDurability) 
						{
							newDurability = maxDurability;
							unenquipChestPlayer(p);
						}
						p.getInventory().getChestplate().setDurability((short) (newDurability));
					}
				}
			}
		}
	}
	
	
	// Remove item from player's chestplate slot and puts it in their normal inventory.
	public void unenquipChestPlayer(Player p) 
	{
		p.getInventory().addItem(p.getInventory().getChestplate());
		p.getInventory().getChestplate().setAmount(0);
	}
	
	
	// Check if the player tries to equip armor by richt clicking it.
	@SuppressWarnings("deprecation")
	@EventHandler
    public void onRightClick(PlayerInteractEvent event) 
	{
        Player player = event.getPlayer();    
    
        ItemStack item = player.getItemInHand();
    
        if (item != null) 
        {
            	if (item.getType() == Material.ELYTRA && isArmoredElytra(item)) 
            	{
            		int armorTier = nbtEditor.getArmorTier(item);
            		if ((armorTier == 1 && !player.hasPermission("armoredelytra.wear.leather"))     || 
			           (armorTier == 2 && !player.hasPermission("armoredelytra.wear.gold"))     || 
			           (armorTier == 3 && !player.hasPermission("armoredelytra.wear.chain"))    || 
			           (armorTier == 4 && !player.hasPermission("armoredelytra.wear.iron"))     || 
			           (armorTier == 5 && !player.hasPermission("armoredelytra.wear.diamond")))
            		{
		        		player.sendMessage(ChatColor.RED + "You do not have the required permission to wear this armor tier!.");
		        		event.setCancelled(true);
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
	            		ItemStack chestplate = player.getInventory().getChestplate();
	            		// If the player equips a new chestplate.
					if (player.getInventory().getChestplate() != null) 
					{
						// If that chestplate is an (armored) elytra.
						if (chestplate.getType() == Material.ELYTRA && isArmoredElytra(chestplate)) 
						{
							int armorTier = nbtEditor.getArmorTier(chestplate);
							if ((chestplate.getDurability() >= chestplate.getType().getMaxDurability())) 
							{
								player.sendMessage(ChatColor.RED + "You cannot equip this elytra! Please repair it in an anvil first.");
								unenquipChestPlayer(player);
							} else if ((armorTier == 1 && !player.hasPermission("armoredelytra.wear.leather")) || 
						           (armorTier == 2 && !player.hasPermission("armoredelytra.wear.gold"))        || 
						           (armorTier == 3 && !player.hasPermission("armoredelytra.wear.chain"))       || 
						           (armorTier == 4 && !player.hasPermission("armoredelytra.wear.iron"))        || 
						           (armorTier == 5 && !player.hasPermission("armoredelytra.wear.diamond")))
							{
								player.sendMessage(ChatColor.RED + "You do not have the required permission to wear this armor tier!.");
								unenquipChestPlayer(player);
							}
							player.updateInventory();
							e.setCancelled(true);
						}
					}
	            }
			}.runTaskLater(this.plugin, 1);
		}
    }
}