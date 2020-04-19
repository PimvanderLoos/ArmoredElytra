package nl.pim16aap2.armoredElytra.nbtEditor;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NBTEditor
{
    private static final String versionString = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static final MinecraftVersion minecraftVersion = MinecraftVersion.get(versionString);
    private static final String NMSbase = "net.minecraft.server." + versionString + ".";
    private static final String CraftBase = "org.bukkit.craftbukkit." + versionString + ".";

    private static Method asNMSCopy;
    private static Method asBukkitCopy;
    private static Class<?> NMSItemStack;
    private static Class<?> CraftItemStack;

    private static Class<?> NBTTagCompound;
    private static Class<?> NBTTagList;
    private static Class<?> NBTBase;
    private static Class<?> NBTTagString;
    private static Class<?> NBTTagByte;
    private static Class<?> NBTTagInt;

    private static Method hasTag;
    private static Method getTag;

    private static Method addCompound;

    private static Method setTag;

    private static Method setCompoundByte;
    private static Method setCompoundTagList;

    private static Method getCompoundTagList;
    private static Method getTagListSize;
    private static Method getTagListAtIndex;

    private static Constructor<?> NBTTagStringCtor;
    private static Constructor<?> NBTTagByteCtor;
    private static Constructor<?> NBTTagIntCtor;

    private static boolean success;

    private static Function<String, Integer> getArmorValue;


    private static final Pattern pattern_findAmount_double = Pattern.compile("Amount:[0-9]+.[0-9]+d[,}]*");
    private static final Pattern pattern_findAmount_int = Pattern.compile("Amount:[0-9]+[,}]*");
    private static final Pattern pattern_getDouble = Pattern.compile("[0-9]+.[0-9]+");
    private static final Pattern pattern_getInt = Pattern.compile("[0-9]+");
    private static final Pattern pattern_isArmor = Pattern.compile("\"generic.armor\"");


    static
    {
        if (minecraftVersion == null)
            success = false;
        else
            try
            {
                // 1.13 and lower use integer armor values while 1.14 and newer use double armor values.
                getArmorValue = minecraftVersion.isNewerThan(MinecraftVersion.v1_13) ?
                                NBTEditor::getArmorValueDouble : NBTEditor::getArmorValueInt;

                NMSItemStack = getNMSClass("ItemStack");
                hasTag = NMSItemStack.getMethod("hasTag");
                getTag = NMSItemStack.getMethod("getTag");

                CraftItemStack = getCraftClass("inventory.CraftItemStack");
                asNMSCopy = CraftItemStack.getMethod("asNMSCopy", ItemStack.class);
                asBukkitCopy = CraftItemStack.getMethod("asBukkitCopy", NMSItemStack);

                NBTBase = getNMSClass("NBTBase");

                NBTTagString = getNMSClass("NBTTagString");
                NBTTagStringCtor = NBTTagString.getDeclaredConstructor(String.class);
                NBTTagStringCtor.setAccessible(true);

                NBTTagByte = getNMSClass("NBTTagByte");
                NBTTagByteCtor = NBTTagByte.getDeclaredConstructor(byte.class);
                NBTTagByteCtor.setAccessible(true);

                NBTTagInt = getNMSClass("NBTTagInt");
                NBTTagIntCtor = NBTTagInt.getDeclaredConstructor(int.class);
                NBTTagIntCtor.setAccessible(true);

                NBTTagCompound = getNMSClass("NBTTagCompound");
                setTag = NBTTagCompound.getMethod("set", String.class, NBTBase);

                NBTTagList = getNMSClass("NBTTagList");
                getTagListSize = NBTTagList.getMethod("size");
                getTagListAtIndex = NBTTagList.getMethod("get", int.class);
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
                setCompoundByte = NBTTagCompound.getMethod("set", String.class, NBTBase);
                getCompoundTagList = NBTTagCompound.getMethod("getList", String.class, int.class);

                success = true;
            }
            catch (NoSuchMethodException | SecurityException | ClassNotFoundException e)
            {
                e.printStackTrace();
                success = false;
            }
    }

    private static void addCompound(Object instance, Object nbtbase)
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        if (addCompound.getParameterCount() == 2)
            addCompound.invoke(instance, 0, nbtbase);
        else
            addCompound.invoke(instance, nbtbase);
    }

    // Add armor to the supplied item, based on the armorTier.
    public static ItemStack addArmorNBTTags(ItemStack item, ArmorTier armorTier, boolean unbreakable)
    {
        try
        {
            ItemMeta itemmeta = item.getItemMeta();
            int armorProtection = ArmorTier.getArmor(armorTier);
            int armorToughness = ArmorTier.getToughness(armorTier);

            itemmeta.setDisplayName(ArmoredElytra.getInstance().getArmoredElytraName(armorTier));
            if (ArmoredElytra.getInstance().getElytraLore() != null)
                itemmeta
                    .setLore(Arrays.asList(ArmoredElytra.getInstance().fillInArmorTierInStringNoColor(
                        ArmoredElytra.getInstance().getElytraLore(), armorTier)));

            item.setItemMeta(itemmeta);

            Object nmsStack = asNMSCopy.invoke(null, item);
            Object compound = ((boolean) hasTag.invoke(nmsStack) ? getTag.invoke(nmsStack) :
                               NBTTagCompound.newInstance());
            Object modifiers = NBTTagList.newInstance();
            Object armor = NBTTagCompound.newInstance(); // I should be able to simply add custom tags here!
            setTag.invoke(armor, "AttributeName", NBTTagStringCtor.newInstance("generic.armor"));
            setTag.invoke(armor, "Name", NBTTagStringCtor.newInstance("generic.armor"));
            setTag.invoke(armor, "Amount", NBTTagIntCtor.newInstance(armorProtection));
            setTag.invoke(armor, "Operation", NBTTagIntCtor.newInstance(0));
            setTag.invoke(armor, "UUIDLeast", NBTTagIntCtor.newInstance(894654));
            setTag.invoke(armor, "UUIDMost", NBTTagIntCtor.newInstance(2872));
            setTag.invoke(armor, "Slot", NBTTagStringCtor.newInstance("chest"));
            addCompound(modifiers, armor);

            Object armorTough = NBTTagCompound.newInstance();
            setTag.invoke(armorTough, "AttributeName", NBTTagStringCtor.newInstance("generic.armorToughness"));
            setTag.invoke(armorTough, "Name", NBTTagStringCtor.newInstance("generic.armorToughness"));
            setTag.invoke(armorTough, "Amount", NBTTagIntCtor.newInstance(armorToughness));
            setTag.invoke(armorTough, "Operation", NBTTagIntCtor.newInstance(0));
            setTag.invoke(armorTough, "UUIDLeast", NBTTagIntCtor.newInstance(894654));
            setTag.invoke(armorTough, "UUIDMost", NBTTagIntCtor.newInstance(2872));
            setTag.invoke(armorTough, "Slot", NBTTagStringCtor.newInstance("chest"));
            addCompound(modifiers, armorTough);

            if (unbreakable)
                setCompoundByte.invoke(compound, "Unbreakable", NBTTagByteCtor.newInstance((byte) 1));

            setCompoundTagList.invoke(compound, "AttributeModifiers", modifiers);
            item = (ItemStack) asBukkitCopy.invoke(null, nmsStack);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e)
        {
            e.printStackTrace();
        }
        return item;
    }

    /**
     * Gets the armor amount from an NBT attribute.
     *
     * @param string      The NBT attribute as a String.
     * @param findAmount  The {@link Pattern} that finds the amount in a String.
     * @param parseAmount The {@link Pattern} that extracts the amount from the String found by "findAmount".
     * @return The String containing the armor value. This can either be an integer or a double value.
     */
    private static String getArmorAmount(final String string, final Pattern findAmount, final Pattern parseAmount)
    {
        final Matcher amountMatcher = findAmount.matcher(string);
        if (!amountMatcher.find())
        {
            ArmoredElytra.getInstance()
                         .myLogger(Level.SEVERE,
                                   "Failed to obtain armor value from NBT! No armor amount found: " + string);
            return "0";
        }

        final String amountName = amountMatcher.group();
        final Matcher amountString = parseAmount.matcher(amountName);
        if (!amountString.find())
        {
            ArmoredElytra.getInstance()
                         .myLogger(Level.SEVERE,
                                   "Failed to obtain armor value from NBT! Could not parse value: " + amountName);
            return "0";
        }
        return amountString.group();
    }

    /**
     * Gets the amount of an attribute in the format of: "something something, amount:2.0d,". The amount is cast to and
     * returned as an integer value.
     *
     * @param string The nbt attribute as String.
     * @return The integer value of the amount.
     */
    private static int getArmorValueDouble(final String string)
    {
        try
        {
            return (int) Double.parseDouble(getArmorAmount(string, pattern_findAmount_double, pattern_getDouble));
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Gets the amount of an attribute in the format of: "something something, amount:2.0d,". The amount is cast to and
     * returned as an integer value.
     *
     * @param string The nbt attribute as String.
     * @return The integer value of the amount.
     */
    private static int getArmorValueInt(final String string)
    {
        try
        {
            return Integer.parseInt(getArmorAmount(string, pattern_findAmount_int, pattern_getInt));
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    public static ArmorTier getArmorTier(ItemStack item)
    {
//        {
//            double armorValue_2 = 0;
//            net.minecraft.server.v1_15_R1.ItemStack nmsStack_2 = org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack
//                .asNMSCopy(item);
//
//            net.minecraft.server.v1_15_R1.NBTTagCompound compound_2 =
//                nmsStack_2.hasTag() ? nmsStack_2.getTag() : new net.minecraft.server.v1_15_R1.NBTTagCompound();
//
//            net.minecraft.server.v1_15_R1.NBTTagList modifiers_2 = compound_2.getList("AttributeModifiers", 10);
//        }

        try
        {
            Object compound = getTag.invoke(asNMSCopy.invoke(null, item));
            if (compound == null)
                return ArmorTier.NONE;

            Object modifiers = getCompoundTagList.invoke(compound, "AttributeModifiers", 10); // Number 10 = Compound.
            int size = (int) getTagListSize.invoke(modifiers);


            for (int idx = 0; idx < size; ++idx)
            {
//                final String result = modifiers.get(idx).asString();
                final String result = getTagListAtIndex.invoke(modifiers, idx).toString();

                if (!pattern_isArmor.matcher(result).find())
                    continue;

                int armorValue = getArmorValue.apply(result);
                switch (armorValue)
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
            return ArmorTier.NONE;
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            e.printStackTrace();
            return ArmorTier.NONE;
        }
    }

    private static Class<?> getNMSClass(String name)
        throws ClassNotFoundException
    {
        return Class.forName(NMSbase + name);
    }

    private static Class<?> getCraftClass(String name)
        throws ClassNotFoundException
    {
        return Class.forName(CraftBase + name);
    }

    public static boolean success()
    {
        return success;
    }

    private enum MinecraftVersion
    {
        v1_9("1_9", 0),
        v1_10("1_10", 1),
        v1_11("1_11", 2),
        v1_12("1_12", 3),
        v1_13("1_13", 4),
        v1_14("1_14", 5),
        v1_15("1_15", 6);

        private int index;
        private String name;

        MinecraftVersion(String name, int index)
        {
            this.name = name;
            this.index = index;
        }

        /**
         * Checks if this version is newer than the other version.
         *
         * @param other The other version to check against.
         * @return True if this version is newer than the other version.
         */
        public boolean isNewerThan(final MinecraftVersion other)
        {
            return this.index > other.index;
        }

        public static MinecraftVersion get(final String versionName)
        {
            if (versionName == null)
                return null;
            for (final MinecraftVersion mcVersion : MinecraftVersion.values())
                if (versionName.contains(mcVersion.name))
                    return mcVersion;
            return null;
        }
    }
}
