package nl.pim16aap2.armoredElytra;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import com.rit.sucy.EnchantPlugin;

import nl.pim16aap2.armoredElytra.nms.NBTEditor;
import nl.pim16aap2.armoredElytra.nms.V1_11_R1;
import nl.pim16aap2.armoredElytra.nms.V1_12_R1;
 
public class ArmoredElytra extends EnchantPlugin implements Listener 
{
	private NBTEditor nbtEditor;
	
	@Override
    public void onEnable() 
	{
		if (compatibleMCVer()) 
		{
			Bukkit.getPluginManager().registerEvents(new EventHandlers(this, nbtEditor), this);
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