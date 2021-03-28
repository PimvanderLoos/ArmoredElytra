package nl.pim16aap2.armoredElytra.util;

/**
 * @author Pim
 */
public class ArmorTierName
{
    private String longName, shortName;

    public ArmorTierName(final String longName, final String shortName)
    {
        this.longName = longName;
        this.shortName = shortName;
    }

    public String getLongName()
    {
        return longName;
    }

    public String getShortName()
    {
        return shortName;
    }

    public void setLongName(final String longName)
    {
        this.longName = longName;
    }

    public void setShortName(final String shortName)
    {
        this.shortName = shortName;
    }
}
