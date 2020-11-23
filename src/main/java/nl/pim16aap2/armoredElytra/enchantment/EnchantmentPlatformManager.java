package nl.pim16aap2.armoredElytra.enchantment;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentPlatformManager
{
    private static final EnchantmentPlatformManager INSTANCE = new EnchantmentPlatformManager();
    private final List<IEnchantmentPlatform> platforms = new ArrayList<>();

    private EnchantmentPlatformManager()
    {
        addPlatform(new VanillaEnchantmentPlatform());
    }

    public static EnchantmentPlatformManager get()
    {
        return INSTANCE;
    }

    public void addPlatform(final IEnchantmentPlatform platform)
    {
        platforms.add(platform);
    }

    public List<IEnchantmentPlatform> getPlatforms()
    {
        return platforms;
    }
}
