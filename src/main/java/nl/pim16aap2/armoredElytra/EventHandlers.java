package nl.pim16aap2.armoredElytra;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
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
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.server.v1_11_R1.*;

import com.rit.sucy.EnchantmentAPI;

import net.md_5.bungee.api.ChatColor;

public class EventHandlers implements Listener {

	private int DIAMONDS_TO_FULL = 3;
	private int  LEATHER_TO_FULL = 4;
	private final ArmoredElytra plugin;
	private String[] allowedEnchantments = {"DURABILITY",
										    "PROTECTION_FIRE",
										    "PROTECTION_EXPLOSIONS",
										    "PROTECTION_PROJECTILE",
										    "PROTECTION_ENVIRONMENTAL",
											"DIAMOND_ARMOR_ITEMS",
											"THORNS"};
	private String[] specialEnchantments = {"MENDING",
		    								"VANISHING_CURSE",
		    								"BINDING_CURSE"};

	public EventHandlers(ArmoredElytra plugin) {
		this.plugin = plugin;
	}
	
	
	// Clear the anvil's inventory (destroy all the items in all 3 slots (second slot is not emptied, when repairing you can safely give multiple items)).
	public void cleanAnvil(AnvilInventory anvilInventory){
		anvilInventory.getItem(0).setAmount(0);
		anvilInventory.getItem(1).setAmount(anvilInventory.getItem(1).getAmount()-1);
		anvilInventory.getItem(2).setAmount(0);
	}
	
	
	// Check if the enchantment is allowed on elytras.
	public boolean isAllowedEnchantment(Enchantment enchant) {
		for (String s : allowedEnchantments) {
			if (Enchantment.getByName(s).equals(enchant)) {
				return true;
			}
		}
		return false;
	}
	
	
	// Check if the enchantment is "special", i.e. cannot be enchanted by the EnchantmentAPI.
	public boolean isSpecialEnchantment(Enchantment enchant) {
		for (String s : specialEnchantments) {
			if (Enchantment.getByName(s).equals(enchant)) {
				return true;
			}
		}
		return false;
	}
	
	
	// Check if the elytra being checked is an armored one.
	public boolean isArmoredElytra(ItemStack elytra) {
	if (elytra.hasItemMeta() && elytra.getType() == Material.ELYTRA) 
		if (elytra.getItemMeta().hasLore())
			if (elytra.getItemMeta().getLore().toString().equals("[This is an armored Elytra.]")) {
				return true;
			}
		return false;
	}
	
	
	// Copy enchants of 2 items to one item.
	public ItemStack addEnchants(ItemStack itemOne, ItemStack itemTwo, Player p) {
		// Create the resulting item and apply the diamond armor tier enchantment to it.
		ItemStack result = itemOne.clone();
		// Get the enchantments of the first and second item in the anvil.
		Map<Enchantment, Integer> enchantments0 = itemOne.getEnchantments();
		Map<Enchantment, Integer> enchantments1 = itemTwo.getEnchantments();
		// Enchants from enchanted books have to be access in a different way.
		if (itemTwo.getType() == Material.ENCHANTED_BOOK && isArmoredElytra(itemOne)) {
			EnchantmentStorageMeta meta = (EnchantmentStorageMeta)itemTwo.getItemMeta();
			enchantments1 = meta.getStoredEnchants();
		}
		// Copy enchantments from item1 to result.
		if (enchantments1!=null) {
			for (Map.Entry<Enchantment, Integer> entry : enchantments1.entrySet()) {
				if (isSpecialEnchantment(entry.getKey()) && !result.containsEnchantment(entry.getKey())) {
					result.addEnchantment(entry.getKey(), entry.getValue());
				} else if (isAllowedEnchantment(entry.getKey())) {
					int enchantLevel = entry.getValue();
					// If item0 and item1 both have the same enchantment at the same level, result has level+1.
					// If item0 and item1 both have the same enchantment at different levels, give the highest level to result.
					if (enchantments0 != null) {
						for (Map.Entry<Enchantment, Integer> rentry : enchantments0.entrySet()) {
							if (entry.getKey().getName() == rentry.getKey().getName()) {
								if (entry.getValue() == rentry.getValue() && entry.getValue() < entry.getKey().getMaxLevel()) {
									enchantLevel = entry.getValue()+1;
								} else if (entry.getValue() < rentry.getValue()) {
									enchantLevel = rentry.getValue();
								}
							}
						}
					}
					EnchantmentAPI.getEnchantment(entry.getKey().getName()).addToItem(result, enchantLevel);
				} else {
					p.sendMessage(ChatColor.RED+"This enchantment is not allowed on this item!");
				}
			}
		}
		return result;
	}
	
	
	// Copy enchants of 2 items to one item.
	public ItemStack repairItem(ItemStack one, ItemStack two) {
		// Create the resulting item.
		ItemStack result = one.clone();
		
		int mult          = two.getType() == Material.DIAMOND ? 100/DIAMONDS_TO_FULL : 100/LEATHER_TO_FULL;
		int maxDurability = one.getType().getMaxDurability();
		int durability    = one.getDurability();
		int newDurability = (durability - (int) (maxDurability*mult));
		result.setDurability((short) (newDurability >= 0 ? newDurability : 0) );
		
		return result;
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
		                		ItemStack result = null;
		                		// Check if the second input slot contains a diamond chestplate.
			                	if (anvilInventory.getItem(1).getType() == Material.DIAMOND_CHESTPLATE) {
			                		// Combine the enchantments of the two items in the input slots.
			                		result = addEnchants(anvilInventory.getItem(0), anvilInventory.getItem(1), p);
			                		if (anvilInventory.getItem(1).getType() == Material.DIAMOND_CHESTPLATE) {
			                			result.setDurability((short)0);
			                		}
			                	} 
								// If the player tries to repair an armored elytra with diamonds or a regular elytra with leather, repair 52% or 26%.
			                	else if ((anvilInventory.getItem(1).getType() == Material.LEATHER && !isArmoredElytra(anvilInventory.getItem(0))) || 
		                			     (anvilInventory.getItem(1).getType() == Material.DIAMOND &&  isArmoredElytra(anvilInventory.getItem(0)))) {
			                		// Repair the item in the first input slot with items from the second input slot.
			                		result = repairItem(anvilInventory.getItem(0), anvilInventory.getItem(1));
			                	}
			                	// Otherwise, remove the item in the result slot (slot2).
			                	else {
			                		if (anvilInventory.getItem(2)!=null) {
			                			anvilInventory.getItem(2).setAmount(0);
			                		}
			                	}
								// Put the created item in the second slot of the anvil.
			                	if (result!=null) {
			                		if (anvilInventory.getItem(1).getType() == Material.DIAMOND_CHESTPLATE) {
				                		ItemMeta itemmeta = result.getItemMeta();
				                		itemmeta.setDisplayName(ChatColor.AQUA+"Armored Elytra");
				                		itemmeta.setLore(Arrays.asList("This is an armored Elytra."));
				                		result.setItemMeta(itemmeta);
				                		net.minecraft.server.v1_11_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(result);
				                		NBTTagCompound compound = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();
				                		NBTTagList modifiers = new NBTTagList();
				                		NBTTagCompound armor = new NBTTagCompound();
				                		armor.set("AttributeName", new NBTTagString("generic.armor"));
				                	    armor.set("Name", new NBTTagString("generic.armor"));
				                		armor.set("Amount", new NBTTagInt(8));
				                		armor.set("Operation", new NBTTagInt(0));
				                		armor.set("UUIDLeast", new NBTTagInt(894654));
				                		armor.set("UUIDMost", new NBTTagInt(2872));
				                		armor.set("Slot", new NBTTagString("chest"));
				                		modifiers.add(armor);			
				                		NBTTagCompound armorTough = new NBTTagCompound();
				                		armorTough.set("AttributeName", new NBTTagString("generic.armorToughness"));
				                		armorTough.set("Name", new NBTTagString("generic.armorToughness"));
				                		armorTough.set("Amount", new NBTTagInt(2));
				                		armorTough.set("Operation", new NBTTagInt(0));
				                		armorTough.set("UUIDLeast", new NBTTagInt(894654));
				                		armorTough.set("UUIDMost", new NBTTagInt(2872));
				                		armorTough.set("Slot", new NBTTagString("chest"));
				                		modifiers.add(armorTough);
				                		compound.set("AttributeModifiers", modifiers);
				                		result = CraftItemStack.asBukkitCopy(nmsStack);
			                		}
									anvilInventory.setItem(2, result);
			                	}
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
	public void onPlayerDamage (EntityDamageEvent e) {
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (p.getInventory().getChestplate()!=null) {
				if (p.getInventory().getChestplate().getType() == Material.ELYTRA && isArmoredElytra(p.getInventory().getChestplate())) {
					DamageCause cause = e.getCause();
					if (cause!=DamageCause.DROWNING    && cause!=DamageCause.STARVATION     && cause!=DamageCause.SUFFOCATION && 
					    cause!=DamageCause.SUICIDE     && cause!=DamageCause.FLY_INTO_WALL  && cause!=DamageCause.POISON) {
						
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
