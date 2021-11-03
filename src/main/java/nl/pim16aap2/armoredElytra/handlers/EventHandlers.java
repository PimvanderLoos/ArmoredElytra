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
import nl.pim16aap2.armoredElytra.util.Util;
import nl.pim16aap2.armoredElytra.util.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Random;

public class EventHandlers implements Listener
{
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
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageEvent e)
    {
        if (!(e.getEntity() instanceof final Player p))
            return;

        final ItemStack elytra = p.getInventory().getChestplate();
        if (elytra == null)
            return;

        final ArmorTier armorTier = nbtEditor.getArmorTier(elytra);
        if (armorTier == ArmorTier.NONE)
            return;

        final DamageCause cause = e.getCause();
        // The elytra doesn't receive any damage for these causes:
        if (cause == DamageCause.DROWNING || cause == DamageCause.STARVATION || cause == DamageCause.SUFFOCATION ||
            cause == DamageCause.SUICIDE || cause == DamageCause.FLY_INTO_WALL || cause == DamageCause.POISON)
            return;

        final boolean removeDurability;
        if (elytra.containsEnchantment(Enchantment.DURABILITY))
        {
            final int randomInt = random.nextInt(101);
            final int enchantLevel = elytra.getEnchantmentLevel(Enchantment.DURABILITY);
            // Formula taken from: https://minecraft.fandom.com/wiki/Unbreaking#Usage
            final float removeDurabilityChance = 60 + 40f / (enchantLevel + 1);
            removeDurability = randomInt <= removeDurabilityChance;
        }
        else
            removeDurability = true;

        // Even when we don't subtract durability, we still want to update the durability, so just subtract 0.
        final int durabilityLoss = removeDurability ? (int) Math.max(1, e.getDamage() / 4) : 0;
        final int newDurability = durabilityManager.removeDurability(elytra, durabilityLoss, armorTier);
        if (newDurability >= durabilityManager.getMaxDurability(armorTier))
            Util.moveChestplateToInventory(p);
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

        final ArmorTier armorTier = nbtEditor.getArmorTier(e.getNewArmorPiece());
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
}
