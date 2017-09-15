package nl.pim16aap2.armoredElytra;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import nl.pim16aap2.armoredElytra.nms.NBTEditor;
import nl.pim16aap2.armoredElytra.nms.V1_11_R1;
import nl.pim16aap2.armoredElytra.nms.V1_12_R1;
 
public class ArmoredElytra extends JavaPlugin implements Listener 
{
	private NBTEditor nbtEditor;
	private boolean cursesAllowed;
	private int DIAMONDS_TO_FULL;
	private String[] allowedEnchants;
	
	@Override
    public void onEnable() 
	{
		saveDefaultConfig();
		DIAMONDS_TO_FULL = this.getConfig().getInt("diamondsRepair");
		cursesAllowed = this.getConfig().getBoolean("allowCurses");
		List<String> list = this.getConfig().getStringList("allowedEnchantments");
		allowedEnchants = list.toArray(new String[0]);

		Bukkit.getLogger().log(Level.INFO, "["+this.getName()+"] "+"Allowed enchantments:");
		for (String s : allowedEnchants)
		{
			Bukkit.getLogger().log(Level.INFO, "["+this.getName()+"] "+s);
		}
		
		if (compatibleMCVer()) 
		{
			Bukkit.getPluginManager().registerEvents(new EventHandlers(this, nbtEditor, cursesAllowed, DIAMONDS_TO_FULL, allowedEnchants), this);
		} else {
			Bukkit.getLogger().log(Level.WARNING, "Trying to load the plugin on an incompatible version of Minecraft!");
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
        		nbtEditor = new V1_11_R1();

        } else if (version.equals("v1_12_R1")) 
        {
        		nbtEditor = new V1_12_R1();
        }
        // Return true if compatible.
        return nbtEditor != null;
	}
}