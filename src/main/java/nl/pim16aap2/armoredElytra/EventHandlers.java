package nl.pim16aap2.armoredElytra;

import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.rit.sucy.EnchantmentAPI;

import net.md_5.bungee.api.ChatColor;

public class EventHandlers implements Listener {

	private final ArmoredElytra plugin;

	public EventHandlers(ArmoredElytra plugin) {
		this.plugin = plugin;
	}
	
	// Clear the anvil's inventory (destroy all the items in all 3 slots (second slot is not emptied, when repairing you can safely give multiple items)).
	public void cleanAnvil(AnvilInventory anvilInventory){
		anvilInventory.getItem(0).setAmount(0);
		anvilInventory.getItem(1).setAmount(anvilInventory.getItem(1).getAmount()-1);
		anvilInventory.getItem(2).setAmount(0);
	}
	
	// Check if the elytra being checked is an armored one.
	public boolean isArmoredElytra(ItemStack elytra) {
		if (EnchantmentAPI.itemHasEnchantment(elytra, "Diamond Armor Tier")) {
			return true;
		}
		return false;
	}	
	
	// Handle the anvil related parts.
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();
			if (e.getView().getType() == InventoryType.ANVIL) {
				AnvilInventory anvilInventory = (AnvilInventory) e.getInventory(); // Inventory type
				int slot = e.getRawSlot(); // Get slot 
				
				if (slot == 2 && anvilInventory.getItem(2) != null) {
					// If there is an elytra in the final slot (it is unenchantable by default, so we can reasonably expect it to be an enchanted elytra)
					// and the player selects it, let the player transfer it to their inventory.
					if (anvilInventory.getItem(2).getType() == Material.ELYTRA) {
						if (e.isShiftClick()) {
							p.getInventory().addItem(anvilInventory.getItem(2));
						} else {
							p.setItemOnCursor(anvilInventory.getItem(2));
						}
						// Clean the anvil's inventory after transferring the items.
						cleanAnvil(anvilInventory);
					}
				}
				
		        new BukkitRunnable() {
		            @Override
	                public void run() {
		            	// Check if there are items in both input slots.
		                if (anvilInventory.getItem(0) != null && anvilInventory.getItem(1) != null) {
		                	// Check if the first input slot contains an elytra.
		                	if (anvilInventory.getItem(0).getType() == Material.ELYTRA) {
		                		// Check if the second input slot contains a diamond chestplate.
			                	if (anvilInventory.getItem(1).getType() == Material.DIAMOND_CHESTPLATE) {
			                		
			                		// Get the enchantments of the first and second item in the anvil.
									Map<Enchantment, Integer> enchantments0 = anvilInventory.getItem(0).getEnchantments();
									Map<Enchantment, Integer> enchantments1 = anvilInventory.getItem(1).getEnchantments();

									// Create the resulting item and apply the diamond armor tier enchantment to it.
									ItemStack result = new ItemStack(Material.ELYTRA, 1);
									EnchantmentAPI.getEnchantment("Diamond Armor Tier").addToItem(result, 1);
									
									// Copy enchantments from item0 to result.
									if (enchantments0!=null) {
										for (Map.Entry<Enchantment, Integer> entry : enchantments0.entrySet()) {
											if (entry.getKey().getName().equals("MENDING") || entry.getKey().getName().equals("VANISHING_CURSE") || entry.getKey().getName().equals("VANISHING_CURSE")) {
												result.addEnchantment(entry.getKey(), 1);
											} else {
												EnchantmentAPI.getEnchantment(entry.getKey().getName()).addToItem(result, 1);
											}
										}
									}
									// Copy enchantments from item1 to result.
									if (enchantments1!=null) {
										for (Map.Entry<Enchantment, Integer> entry : enchantments1.entrySet()) {
											if (entry.getKey().getName().equals("MENDING") || entry.getKey().getName().equals("VANISHING_CURSE") || entry.getKey().getName().equals("VANISHING_CURSE"))
												result.addEnchantment(entry.getKey(), 1);
											else {
												Map<Enchantment, Integer> resultEnchantments = result.getEnchantments();
												int enchantLevel = entry.getValue();
												// If item0 and item1 both have the same enchantment at the same level, result has level+1.
												// If item0 and item1 both have the same enchantment at different levels, give the highest level to result.
												if (resultEnchantments != null) {
													for (Map.Entry<Enchantment, Integer> rentry : resultEnchantments.entrySet()) {
														if (entry.getKey().getName() == rentry.getKey().getName()) {
															if (entry.getValue() == rentry.getValue() && entry.getValue()<5) {
																enchantLevel = entry.getValue()+1;
															} else if (entry.getValue() < rentry.getValue()) {
																enchantLevel = rentry.getValue();
															}
														}
													}
												}
												if (!entry.getKey().getName().equals("MENDING") && !entry.getKey().getName().equals("VANISHING_CURSE") && !entry.getKey().getName().equals("VANISHING_CURSE"))
													EnchantmentAPI.getEnchantment(entry.getKey().getName()).addToItem(result, enchantLevel);
											}
										}
									}
									// Resulting item will have full durability.
									result.setDurability((short) 0);
									// Add resulting item in the second slot of the anvil.
									anvilInventory.setItem(2, result);
									
								// If the player tries to repair an armored elytra with diamonds or a regular elytra with leather, repair 52% or 26%.
			                	} else if ((anvilInventory.getItem(1).getType() == Material.LEATHER && !isArmoredElytra(anvilInventory.getItem(0))) || 
			                			   (anvilInventory.getItem(1).getType() == Material.DIAMOND &&  isArmoredElytra(anvilInventory.getItem(0)))) {
									int mult = anvilInventory.getItem(1).getType() == Material.DIAMOND ? 2 : 1;
			                		ItemStack result = anvilInventory.getItem(0).clone();
									int maxDurability = anvilInventory.getItem(0).getType().getMaxDurability();
			                		int durability = anvilInventory.getItem(0).getDurability();
			                		int newDurability = (durability - (int) (maxDurability*0.26*mult));
			                		result.setDurability((short) (newDurability >= 0 ? newDurability : 0) );
									// Add resulting item in the second slot of the anvil.
									anvilInventory.setItem(2, result);
									
			                	// Otherwise, remove the item in the result slot (slot2).
			                	} else {
			                		if (anvilInventory.getItem(2)!=null) {
			                			anvilInventory.getItem(2).setAmount(0);
			                		}
			                	}
			                	// update inventory to show changes made to the anvil's inventory.
								p.updateInventory();
		                	}
		                }
		            }
		        // Do all this with a slight delay, so the resulting item shows up in the final slot properly.
		        }.runTaskLater(this.plugin, 1);
			}
		}
	}
	
	// Calculate the toughness of the player's armor. 2 Points per part of diamond armor (and 2 for armored elytra's... duh...).
	public double getArmorToughness(Player p) {
		double toughness = 0;
		if (p.getInventory().getBoots()!=null)
		if (p.getInventory().getBoots().getType() == Material.DIAMOND_BOOTS) {
			toughness+=2;
		}
		if (p.getInventory().getHelmet()!=null)
		if (p.getInventory().getHelmet().getType() == Material.DIAMOND_HELMET) {
			toughness+=2;
		}
		if (p.getInventory().getLeggings()!=null)
		if (p.getInventory().getLeggings().getType() == Material.DIAMOND_LEGGINGS) {
			toughness+=2;
		}
		if (p.getInventory().getChestplate()!=null)
		if (p.getInventory().getChestplate().getType() == Material.DIAMOND_CHESTPLATE || 
			(p.getInventory().getChestplate().getType() == Material.ELYTRA && isArmoredElytra(p.getInventory().getChestplate()))) {
			toughness+=2;
		}
		return toughness;
	}
	
	
	// Because the armored elytra doesn't actually give any armor, the damage received by players wearing an armored elytra is calculated here.
	@EventHandler
	public void onPlayerDamage (EntityDamageEvent e) {
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (p.getInventory().getChestplate()!=null) {
				if (p.getInventory().getChestplate().getType() == Material.ELYTRA && isArmoredElytra(p.getInventory().getChestplate())) {
					DamageCause cause = e.getCause();
					if (cause!=DamageCause.DROWNING && cause!=DamageCause.LIGHTNING && cause!=DamageCause.STARVATION && cause!=DamageCause.SUFFOCATION && cause!=DamageCause.FALL && cause!=DamageCause.SUICIDE && cause!=DamageCause.FIRE_TICK && cause!=DamageCause.FIRE && cause!=DamageCause.FLY_INTO_WALL && cause!=DamageCause.POISON) {
						double damage = e.getDamage();
						// Get the defense points of the player (armor rating), then add 8 (value of diamond chestplate).
						double defensePoints = p.getAttribute(Attribute.GENERIC_ARMOR).getValue()+8;
						double toughness = getArmorToughness(p);
						// Calculate new damage based on formula found on the wiki.
						double newDamage = e.getDamage() * (1-Math.min(20, Math.max(defensePoints/5, defensePoints-e.getDamage()/(2+toughness/4)))/25);

						e.setDamage(EntityDamageEvent.DamageModifier.ABSORPTION, 0);
						e.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
						e.setDamage(EntityDamageEvent.DamageModifier.BLOCKING, 0);
//						e.setDamage(EntityDamageEvent.DamageModifier.HARD_HAT, 0);
						e.setDamage(EntityDamageEvent.DamageModifier.MAGIC, 0);
						e.setDamage(EntityDamageEvent.DamageModifier.RESISTANCE, 0);
						// Base is now the final damage. Why u no have setFinalDamage?
						e.setDamage(EntityDamageEvent.DamageModifier.BASE, newDamage);
						
						int durability = p.getInventory().getChestplate().getDurability();
						int maxDurability = p.getInventory().getChestplate().getType().getMaxDurability();
						int newDurability = (int) (durability + ((int)(damage/4) > 1 ? (int)(damage/4) : 1));
						
						// If the elytra has the durability enchantment.
						if (p.getInventory().getChestplate().containsEnchantment(Enchantment.DURABILITY)) {
				    		Random r = new Random();
				    		// Get a random int between 0 and 100
				    		int randomInt = r.nextInt(101);
				    		int enchantLevel = p.getInventory().getChestplate().getEnchantmentLevel(Enchantment.DURABILITY);
				    		int durabilityDelta = (100/(enchantLevel+1)) < randomInt ? 0 : 1;
				    		if (durability>=maxDurability) {
								enquipChestPlayer(p);
				    		} else 
				    			newDurability = durability+durabilityDelta;
						}
						// If the item should be broken, make sure it really is broken and unequip it.
						if (newDurability >= maxDurability) {
							newDurability = maxDurability;
							enquipChestPlayer(p);
						}
						p.getInventory().getChestplate().setDurability((short) (newDurability));
					}
				}
			}
		}
	}
	
	// Remove item from player's chestplate slot and puts it in their normal inventory.
	public void enquipChestPlayer(Player p) {
		p.getInventory().addItem(p.getInventory().getChestplate());
		p.getInventory().getChestplate().setAmount(0);
	}
	
	// Check if the player is trying to equip a broken elytra (and prevent that).
	@EventHandler
    public void playerEquipsArmor(InventoryClickEvent e){
		if (e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();
			int slot = e.getRawSlot();
			// Chestplate slot.
			if (slot == 6) {
				new BukkitRunnable() {
		            @Override
	                public void run() {
		            	// If the player equips a new chestplate.
						if (p.getInventory().getChestplate() != null) {
							// If that chestplate is an (armored) elytra.
							if (p.getInventory().getChestplate().getType() == Material.ELYTRA && isArmoredElytra(p.getInventory().getChestplate())) {
								if (p.getInventory().getChestplate().getDurability() >= p.getInventory().getChestplate().getType().getMaxDurability()) {
									p.sendMessage(ChatColor.RED + "You cannot equip this elytra! Please repair it first by combining it with diamonds in an anvil.");
									enquipChestPlayer(p);
								}
							}
						}
		            }
				}.runTaskLater(this.plugin, 1);
			}
        }
    }
}
