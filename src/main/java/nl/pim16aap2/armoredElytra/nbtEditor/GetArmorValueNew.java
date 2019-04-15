package nl.pim16aap2.armoredElytra.nbtEditor;

import java.util.logging.Level;

import nl.pim16aap2.armoredElytra.ArmoredElytra;

public class GetArmorValueNew implements GetArmorValue
{
    private final ArmoredElytra plugin;

    public GetArmorValueNew(ArmoredElytra plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public int armorValueFromNBTString(String nbtString)
    {
        int pos = nbtString.indexOf(",Slot:\"chest\",AttributeName:\"generic.armor\"");
        if (pos > 0)
            try
            {
                String stringAtPos = nbtString.substring(pos - 4, pos - 1);
                return (int) Double.parseDouble(stringAtPos);
            }
            catch (Exception e)
            {
                plugin.myLogger(Level.INFO, "Failed to obtain armor value from NBT!");
                return 0;
            }
        else
            return 0;
    }
}
