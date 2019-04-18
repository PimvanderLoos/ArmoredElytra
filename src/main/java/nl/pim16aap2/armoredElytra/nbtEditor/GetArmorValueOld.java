package nl.pim16aap2.armoredElytra.nbtEditor;

import nl.pim16aap2.armoredElytra.ArmoredElytra;

public class GetArmorValueOld implements GetArmorValue
{
    private final ArmoredElytra plugin;

    public GetArmorValueOld(ArmoredElytra plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public int armorValueFromNBTString(String nbtString)
    {
        int pos = nbtString.indexOf(",Slot:\"chest\",AttributeName:\"generic.armor\"");
        if (pos > 0)
        {
            pos--;
            String stringAtPos = nbtString.substring(pos, pos + 1);
            return Integer.parseInt(stringAtPos);
        }
        else
            return 0;
    }
}
