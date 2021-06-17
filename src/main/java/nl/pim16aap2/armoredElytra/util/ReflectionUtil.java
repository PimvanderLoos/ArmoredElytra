package nl.pim16aap2.armoredElytra.util;

import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public final class ReflectionUtil
{
    private static final String PACKAGE_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static final String CRAFTBUKKIT_PACKAGE = "org.bukkit.craftbukkit." + PACKAGE_VERSION;

    private ReflectionUtil()
    {
    }

    /**
     * Retrieves a class in the CraftBukkit package.
     *
     * @param name The name of the class to retrieve.
     * @return The class in the CraftBukkit if it could be found, otherwise null.
     */
    public static @Nullable Class<?> getCraftBukkitClass(String name)
        throws ClassNotFoundException
    {
        return Class.forName(CRAFTBUKKIT_PACKAGE + "." + name);
    }

    /**
     * Retrieves a method.
     *
     * @param clz            The class for which to retrieve the method.
     * @param name           The name of the method to retrieve.
     * @param makeAccessible Whether or not to make the method accessible.
     * @param args           The input arguments of the method.
     * @return The method.
     */
    public static @Nullable Method getMethod(Class<?> clz, String name, boolean makeAccessible, Class<?>... args)
        throws NoSuchMethodException
    {
        Method ret = clz.getDeclaredMethod(name, args);
        if (makeAccessible)
            ret.setAccessible(true);
        return ret;
    }

    /**
     * Attempts to find a method with a specific return type in a class.
     * <p>
     * If more than one match is found, only the first match is returned.
     *
     * @param clz            The target class whose methods to look through.
     * @param type           The return type of the method to look for.
     * @param makeAccessible Whether or not to set the found method to accessible.
     * @param args           The input arguments of the method. When empty, this method will look for a method without
     *                       any input arguments. When this is null, this method will ignore the input arguments and
     *                       only look for the return type.
     * @return The (first) method that was found, or null if no methods could be found.
     */
    public static @Nullable Method getTypedMethod(Class<?> clz, Class<?> type, boolean makeAccessible,
                                                  @Nullable Class<?>... args)
    {
        for (Method m : clz.getDeclaredMethods())
        {
            if (!m.getReturnType().equals(type))
                continue;

            if (args != null && !Arrays.equals(args, m.getParameterTypes()))
                continue;

            if (makeAccessible)
                m.setAccessible(true);
            return m;
        }
        return null;
    }

    /**
     * Looks for a field in a class of a specific type.
     * <p>
     * When more than one field of the given type is found, only the first match is returned.
     *
     * @param clz            The target class whose fields will be inspected.
     * @param type           The type of the member field to look for.
     * @param makeAccessible Whether or not to set the field to accessible.
     * @return The field that is a member of clz of type type if one could be found. Otherwise null.
     */
    public static @Nullable Field getTypedField(Class<?> clz, Class<?> type, boolean makeAccessible)
    {
        for (Field field : clz.getDeclaredFields())
            if (field.getType().equals(type))
            {
                if (makeAccessible)
                    field.setAccessible(true);
                return field;
            }
        return null;
    }

}
