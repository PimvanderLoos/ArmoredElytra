package nl.pim16aap2.armoredElytra;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import nl.pim16aap2.armoredElytra.nms.NBTEditor;
import nl.pim16aap2.armoredElytra.nms.NBTEditor_V1_11_R1;
import nl.pim16aap2.armoredElytra.nms.NBTEditor_V1_12_R1;
 
public class ArmoredElytra extends JavaPlugin implements Listener 
{
	private NBTEditor nbtEditor;
	private boolean cursesAllowed;
	private int LEATHER_TO_FULL;
	private int GOLD_TO_FULL;
	private int IRON_TO_FULL;
	private int DIAMONDS_TO_FULL;
	private String[] allowedEnchants;
	
	@Override
    public void onEnable() 
	{	
		saveDefaultConfig();
		LEATHER_TO_FULL = this.getConfig().getInt("leatherRepair", 6);
		GOLD_TO_FULL = this.getConfig().getInt("goldRepair", 5);
		IRON_TO_FULL = this.getConfig().getInt("ironRepair", 4);
		DIAMONDS_TO_FULL = this.getConfig().getInt("diamondsRepair", 3);
		cursesAllowed = this.getConfig().getBoolean("allowCurses", true);
		List<String> list = this.getConfig().getStringList("allowedEnchantments");
		allowedEnchants = list.toArray(new String[0]);

		Bukkit.getLogger().log(Level.INFO, "["+this.getName()+"] "+"Allowed enchantments:");
		for (String s : allowedEnchants)
		{
			Bukkit.getLogger().log(Level.INFO, "["+this.getName()+"] "+s);
		}
		
		if (compatibleMCVer()) 
		{
			Bukkit.getPluginManager().registerEvents(new EventHandlers(this, nbtEditor, cursesAllowed, LEATHER_TO_FULL, GOLD_TO_FULL, IRON_TO_FULL, DIAMONDS_TO_FULL, allowedEnchants), this);
		} else {
			Bukkit.getLogger().log(Level.WARNING, "Trying to load the plugin on an incompatible version of Minecraft!");
		}
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
    {
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			if (cmd.getName().equalsIgnoreCase("ArmoredElytra"))
			{
				if (args.length == 1) 
				{
					ItemStack newElytra = null;
					
					String tier = args[0];
					// Leather armor.
					if (tier.equalsIgnoreCase("leather"))
					{
						if (player.hasPermission("armoredelytra.give.leather")) 
						{
							player.sendMessage("Giving you an armored elytra of the leather armor tier!");
							newElytra = nbtEditor.addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), 1);
						} else 
						{
							player.sendMessage("You do not have the required permission node for this armor tier!");
						}
						
					// Gold armor.
					} else if (tier.equalsIgnoreCase("gold"))
					{
						if (player.hasPermission("armoredelytra.give.gold")) 
						{
							player.sendMessage("Giving you an armored elytra of the gold armor tier!");
							newElytra = nbtEditor.addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), 2);
						} else 
						{
							player.sendMessage("You do not have the required permission node for this armor tier!");
						}
						
					// Chain armor.
					} else if (tier.equalsIgnoreCase("chain"))
					{
						if (player.hasPermission("armoredelytra.give.chain")) 
						{
							player.sendMessage("Giving you an armored elytra of the chain armor tier!");
							newElytra = nbtEditor.addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), 3);
						} else 
						{
							player.sendMessage("You do not have the required permission node for this armor tier!");
						}
					
					// Iron armor.
					} else if (tier.equalsIgnoreCase("iron"))
					{
						if (player.hasPermission("armoredelytra.give.iron")) 
						{
							player.sendMessage("Giving you an armored elytra of the iron armor tier!");
							newElytra = nbtEditor.addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), 4);
						} else 
						{
							player.sendMessage("You do not have the required permission node for this armor tier!");
						}
					
					// Diamond armor.
					} else if (tier.equalsIgnoreCase("diamond"))
					{
						if (player.hasPermission("armoredelytra.give.diamond")) 
						{
							player.sendMessage("Giving you an armored elytra of the diamond armor tier!");
							newElytra = nbtEditor.addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), 5);
						} else 
						{
							player.sendMessage("You do not have the required permission node for this armor tier!");
						}
					
					} else 
					{
						player.sendMessage("Not a supported armor tier! Try one of these: leather, gold, chain, iron, diamond.");
					}
					giveArmoredElytraToPlayer(player, newElytra);
					return true;
				}
			}
		}
		return false;
    }
	
	
	// Give the provided player the provided item.
	public void giveArmoredElytraToPlayer(Player player, ItemStack item)
	{
		if (item != null)
		{
			player.getInventory().addItem(item);
		}
	}
	
	
	// Check + initialize for the correct version of Minecraft.
	public boolean compatibleMCVer()
	{
        String version;

        try 
        {
            version = Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];
        } catch (ArrayIndexOutOfBoundsException whatVersionAreYouUsingException) 
        {
            return false;
        }

        if (version.equals("v1_11_R1")) 
        {
        		nbtEditor = new NBTEditor_V1_11_R1();

        } else if (version.equals("v1_12_R1")) 
        {
        		nbtEditor = new NBTEditor_V1_12_R1();
        }
        // Return true if compatible.
        return nbtEditor != null;
	}
}