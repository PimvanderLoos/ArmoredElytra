package nl.pim16aap2.armoredElytra.nbtEditor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.ArmorTier;

public class NBTEditor
{
    private final ArmoredElytra plugin;
    private final String NMSbase;
    private final String CraftBase;
    private Method asNMSCopy;
    private Method asBukkitCopy;
    private Class<?> NMSItemStack;
    private Class<?> CraftItemStack;

    private Class<?> NBTTagCompound;
    private Class<?> NBTTagList;
    private Class<?> NBTBase;
    private Class<?> NBTTagString;
    private Class<?> NBTTagByte;
    private Class<?> NBTTagInt;

    private Method hasTag;
    private Method getTag;

    private Method addCompound;

    private Method setTag;

    private Method setCompoundByte;
    private Method setCompoundTagList;

    private Constructor<?> NBTTagStringCtor;
    private Constructor<?> NBTTagByteCtor;
    private Constructor<?> NBTTagIntCtor;

    private boolean success = false;
    private GetArmorValue getArmorValue;

    public NBTEditor(ArmoredElytra plugin)
    {
        this.plugin = plugin;
        final String versionString = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        NMSbase = "net.minecraft.server." + versionString + ".";
        CraftBase = "org.bukkit.craftbukkit." + versionString + ".";

        constructNMSClasses();
        getTagReadingMethod();
    }

    private void getTagReadingMethod()
    {
        if (!success)
            return;

        String version;
        try
        {
            version = Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];
        }
        catch (final ArrayIndexOutOfBoundsException useAVersionMentionedInTheDescriptionPleaseException)
        {
            success = false;
            return;
        }

        // Old versions use the old format. It is assumed here that all versions from 1.13.2 on will use the new format.
        // Spigot's 1.13.1 uses the old format, but 1.13.2 uses the new format. They share the same version number though.
        if (version.equals("v1_9_R1" ) || version.equals("v1_9_R2" ) || version.equals("v1_10_R1") ||
            version.equals("v1_11_R1") || version.equals("v1_12_R1") || version.equals("v1_13_R1") ||
            version.equals("v1_13_R2") && Bukkit.getVersion().split(" ")[2].equals("1.13.1)"))
            getArmorValue = new GetArmorValueOld(plugin);
        else
            getArmorValue = new GetArmorValueNew(plugin);
    }

    public boolean succes()
    {
        return success;
    }

    private void constructNMSClasses()
    {
        try
        {
            NMSItemStack = getNMSClass("ItemStack");
            hasTag = NMSItemStack.getMethod("hasTag");
            getTag = NMSItemStack.getMethod("getTag");

            CraftItemStack = getCraftClass("inventory.CraftItemStack");
            asNMSCopy      = CraftItemStack.getMethod("asNMSCopy", ItemStack.class);
            asBukkitCopy   = CraftItemStack.getMethod("asBukkitCopy", NMSItemStack);

            NBTBase = getNMSClass("NBTBase");

            NBTTagString     = getNMSClass("NBTTagString");
            NBTTagStringCtor = NBTTagString.getConstructor(String.class);

            NBTTagByte     = getNMSClass("NBTTagByte");
            NBTTagByteCtor = NBTTagByte.getConstructor(byte.class);

            NBTTagInt     = getNMSClass("NBTTagInt");
            NBTTagIntCtor = NBTTagInt.getConstructor(int.class);

            NBTTagCompound = getNMSClass("NBTTagCompound");
            setTag         = NBTTagCompound.getMethod("set", String.class, NBTBase);

            NBTTagList  = getNMSClass("NBTTagList");
            // Starting in 1.14, you also need to provide an int value when adding nbt tags.
            try
            {
                addCompound = NBTTagList.getMethod("add", NBTBase);
            }
            catch (Exception e)
            {
                addCompound = NBTTagList.getMethod("add", int.class, NBTBase);
            }

            setCompoundTagList = NBTTagCompound.getMethod("set", String.class, NBTBase);
            setCompoundByte    = NBTTagCompound.getMethod("set", String.class, NBTBase);

            success = true;
        }
        catch (NoSuchMethodException | SecurityException | ClassNotFoundException e)
        {
            e.printStackTrace();
            success = false;
        }
    }

    private void addCompound(Object instance, Object nbtbase) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        if (addCompound.getParameterCount() == 2)
            addCompound.invoke(instance, 0, nbtbase);
        else
            addCompound.invoke(instance, nbtbase);
    }

    // Add armor to the supplied item, based on the armorTier.
    public ItemStack addArmorNBTTags(ItemStack item, ArmorTier armorTier, boolean unbreakable)
    {
        try
        {
            ItemMeta itemmeta   = item.getItemMeta();
            int armorProtection = ArmorTier.getArmor(armorTier);
            int armorToughness  = ArmorTier.getToughness(armorTier);

            itemmeta.setDisplayName(plugin.getArmoredElytrName(armorTier));
            if (plugin.getElytraLore() != null)
                itemmeta.setLore(Arrays.asList(plugin.fillInArmorTierInStringNoColor(plugin.getElytraLore(), armorTier)));
            item.setItemMeta(itemmeta);

            Object nmsStack   = asNMSCopy.invoke(null, item);
            Object compound   = ((boolean) hasTag.invoke(nmsStack) ? getTag.invoke(nmsStack) : NBTTagCompound.newInstance());
            Object modifiers  = NBTTagList.newInstance();
            Object armor      = NBTTagCompound.newInstance(); // I should be able to simply add custom tags here!
            setTag.invoke     (armor, "AttributeName", NBTTagStringCtor.newInstance("generic.armor"));
            setTag.invoke     (armor, "Name",          NBTTagStringCtor.newInstance("generic.armor"));
            setTag.invoke     (armor, "Amount",        NBTTagIntCtor.newInstance(armorProtection));
            setTag.invoke     (armor, "Operation",     NBTTagIntCtor.newInstance(0));
            setTag.invoke     (armor, "UUIDLeast",     NBTTagIntCtor.newInstance(894654));
            setTag.invoke     (armor, "UUIDMost",      NBTTagIntCtor.newInstance(2872));
            setTag.invoke     (armor, "Slot",          NBTTagStringCtor.newInstance("chest"));
            addCompound(modifiers, armor);

            Object armorTough = NBTTagCompound.newInstance();
            setTag.invoke     (armorTough, "AttributeName", NBTTagStringCtor.newInstance("generic.armorToughness"));
            setTag.invoke     (armorTough, "Name",          NBTTagStringCtor.newInstance("generic.armorToughness"));
            setTag.invoke     (armorTough, "Amount",        NBTTagIntCtor.newInstance(armorToughness));
            setTag.invoke     (armorTough, "Operation",     NBTTagIntCtor.newInstance(0));
            setTag.invoke     (armorTough, "UUIDLeast",     NBTTagIntCtor.newInstance(894654));
            setTag.invoke     (armorTough, "UUIDMost",      NBTTagIntCtor.newInstance(2872));
            setTag.invoke     (armorTough, "Slot",          NBTTagStringCtor.newInstance("chest"));
            addCompound(modifiers, armorTough);

            if (unbreakable)
                setCompoundByte.invoke(compound, "Unbreakable", NBTTagByteCtor.newInstance((byte) 1));

            setCompoundTagList.invoke(compound, "AttributeModifiers", modifiers);
            item = (ItemStack) asBukkitCopy.invoke(null, nmsStack);

        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e)
        {
            // TODO: Log this or something. Pretty serious issue for a plugin based entirely on this code.
            e.printStackTrace();
        }
        return item;
    }

    // Get the armor tier of the supplied item.
    public ArmorTier getArmorTier(ItemStack item)
    {
        try
        {
            if (item == null)
                return ArmorTier.NONE;
            if (item.getType() != Material.ELYTRA)
                return ArmorTier.NONE;

            // Get the NBT tags from the item.
            Object compound = getTag.invoke(asNMSCopy.invoke(null, item));
            if (compound == null)
                return ArmorTier.NONE;

            switch (getArmorValue.armorValueFromNBTString(compound.toString()))
            {
            case 3:
                return ArmorTier.LEATHER;
            case 5:
                return ArmorTier.GOLD;
            case 6:
                return ArmorTier.IRON;
            case 8:
                return ArmorTier.DIAMOND;
            default:
                return ArmorTier.NONE;
            }
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private Class<?> getNMSClass(String name) throws ClassNotFoundException
    {
        return Class.forName(NMSbase + name);
    }

    private Class<?> getCraftClass(String name) throws ClassNotFoundException
    {
        return Class.forName(CraftBase + name);
    }
}
