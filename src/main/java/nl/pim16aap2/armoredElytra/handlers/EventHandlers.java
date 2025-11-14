package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.lib.armorequip.ArmorEquipEvent;
import nl.pim16aap2.armoredElytra.lib.armorequip.ArmorListener;
import nl.pim16aap2.armoredElytra.lib.armorequip.ArmorType;
import nl.pim16aap2.armoredElytra.lib.armorequip.DispenserArmorListener;
import nl.pim16aap2.armoredElytra.nbtEditor.DurabilityManager;
import nl.pim16aap2.armoredElytra.nbtEditor.NBTEditor;
import nl.pim16aap2.armoredElytra.util.AllowedToWearEnum;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.RemappedEnchantment;
import nl.pim16aap2.armoredElytra.util.Util;
import nl.pim16aap2.armoredElytra.util.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class EventHandlers implements Listener
{
    /**
     * A set of damage causes that should not decrease the durability of the elytra.
     */
    // See https://minecraft.fandom.com/wiki/Durability#Armor_durability
    private static final Set<DamageCause> IGNORED_DAMAGE_CAUSES = getDamageCauses(
        "CRAMMING",
        "DRAGON_BREATH",
        "DROWNING",
        "FALL",
        "FIRE_TICK",
        "FLY_INTO_WALL",
        "KILL",
        "MAGIC",
        "POISON",
        "SONIC",
        "STARVATION",
        "SUFFOCATION",
        "SUICIDE",
        "VOID",
        "WITHER",
        "WORLD_BORDER"
    );

    /**
     * A set of fire-related damage causes.
     */
    private static final Set<DamageCause> FIRE_DAMAGE_CAUSES = getDamageCauses(
        "CAMPFIRE",
        "FIRE",
        "FIRE_TICK",
        "HOT_FLOOR",
        "LAVA"
    );

    private final Random random = new Random();
    private final ArmoredElytra plugin;
    private final NBTEditor nbtEditor;
    private final DurabilityManager durabilityManager;

    public EventHandlers(ArmoredElytra plugin, NBTEditor nbtEditor, DurabilityManager durabilityManager)
    {
        this.plugin = plugin;
        this.nbtEditor = nbtEditor;
        this.durabilityManager = durabilityManager;
        initializeArmorEquipEvent();
    }

    private void initializeArmorEquipEvent()
    {
        Bukkit.getPluginManager().registerEvents(new ArmorListener(new ArrayList<>()), plugin);
        Bukkit.getPluginManager().registerEvents(new DispenserArmorListener(), plugin);
    }

    // Make sure the player has the correct permission and that the item is not
    // broken.
    private AllowedToWearEnum isAllowedToWear(ItemStack elytra, Player player, ArmorTier armorTier)
    {
        if (armorTier.equals(ArmorTier.NONE))
            return AllowedToWearEnum.ALLOWED;
        if (Util.isBroken(elytra))
            return AllowedToWearEnum.BROKEN;
        if (!plugin.playerHasWearPerm(player, armorTier))
            return AllowedToWearEnum.NOPERMISSION;
        return AllowedToWearEnum.ALLOWED;
    }

    // Handle armored elytra durability loss.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDamage(EntityDamageEvent e)
    {
        if (!(e.getEntity() instanceof final Player p))
            return;

        final ItemStack elytra = p.getInventory().getChestplate();
        if (elytra == null)
            return;

        final ArmorTier armorTier = nbtEditor.getArmorTierFromElytra(elytra);
        if (armorTier == ArmorTier.NONE)
            return;

        final DamageCause cause = e.getCause();
        // We shouldn't decrease the durability of the elytra for damage causes
        // that do not damage the vanilla elytra in any way.
        if (IGNORED_DAMAGE_CAUSES.contains(cause))
            return;

        // Netherite armor doesn't take durability damage from fire.
        if (armorTier == ArmorTier.NETHERITE && FIRE_DAMAGE_CAUSES.contains(cause))
            return;

        final boolean removeDurability;
        if (elytra.containsEnchantment(RemappedEnchantment.UNBREAKING))
        {
            final int randomInt = random.nextInt(101);
            final int enchantLevel = elytra.getEnchantmentLevel(RemappedEnchantment.UNBREAKING);
            // Formula taken from: https://minecraft.fandom.com/wiki/Unbreaking#Usage
            final float removeDurabilityChance = 60 + 40f / (enchantLevel + 1);
            removeDurability = randomInt <= removeDurabilityChance;
        }
        else
            removeDurability = true;

        // Even when we don't subtract durability, we still want to update the durability, so just subtract 0.
        final int durabilityLoss = removeDurability ? (int) Math.max(1, e.getDamage() / 4) : 0;
        final int newDurability = durabilityManager.removeDurability(elytra, durabilityLoss, armorTier);
        if (durabilityManager.isBroken(newDurability, armorTier))
            Util.moveChestplateToInventory(p);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMending(PlayerItemMendEvent e)
    {
        final ArmorTier armorTier = nbtEditor.getArmorTierFromElytra(e.getItem());
        if (armorTier == ArmorTier.NONE)
            return;
        final int newDurability = durabilityManager.removeDurability(e.getItem(), -e.getRepairAmount(), armorTier);

        // Apply it again a tick later, so we can override the durability of the armored elytra without
        // interfering with the player XP change event that depends on the success of this one.
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
            durabilityManager.setDurability(e.getItem(), newDurability, armorTier), 1);
    }

    @EventHandler
    public void onEquip(ArmorEquipEvent e)
    {
        if (e.getMethod().equals(ArmorEquipEvent.EquipMethod.DEATH) ||
            e.getMethod().equals(ArmorEquipEvent.EquipMethod.BROKE))
            return;

        if (!e.getType().equals(ArmorType.CHESTPLATE) ||
            e.getNewArmorPiece() == null ||
            !e.getNewArmorPiece().getType().equals(Material.ELYTRA))
            return;

        final ArmorTier armorTier = nbtEditor.getArmorTierFromElytra(e.getNewArmorPiece());
        final AllowedToWearEnum allowed = isAllowedToWear(e.getNewArmorPiece(), e.getPlayer(), armorTier);
        switch (allowed)
        {
            case ALLOWED:
                break;
            case BROKEN:
                plugin.messagePlayer(e.getPlayer(), plugin.getMyMessages().getString(Message.MESSAGES_REPAIRNEEDED));
                e.setCancelled(true);
                break;
            case NOPERMISSION:
                plugin.usageDeniedMessage(e.getPlayer(), armorTier);
                e.setCancelled(true);
                break;
            default:
                break;
        }
    }

    private static Set<DamageCause> getDamageCauses(String... names)
    {
        return Arrays.stream(names)
                     .map(EventHandlers::findDamageCause)
                     .filter(Objects::nonNull)
                     .collect(Collectors.toCollection(() -> EnumSet.noneOf(DamageCause.class)));
    }

    private static @Nullable DamageCause findDamageCause(String name)
    {
        try
        {
            return DamageCause.valueOf(name);
        }
        catch (IllegalArgumentException e)
        {
            ArmoredElytra.getInstance().myLogger(Level.INFO, "Failed to find DamageCause with name: " + name);
            return null;
        }
    }
}
