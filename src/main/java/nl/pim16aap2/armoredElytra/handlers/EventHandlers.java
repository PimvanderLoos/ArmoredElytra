package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.lib.armorequip.ArmorEquipEvent;
import nl.pim16aap2.armoredElytra.lib.armorequip.ArmorListener;
import nl.pim16aap2.armoredElytra.lib.armorequip.ArmorType;
import nl.pim16aap2.armoredElytra.lib.armorequip.DispenserArmorListener;
import nl.pim16aap2.armoredElytra.util.AllowedToWearEnum;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.Util;
import nl.pim16aap2.armoredElytra.util.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Random;

public class EventHandlers implements Listener
{
    private final ArmoredElytra plugin;

    public EventHandlers(ArmoredElytra plugin)
    {
        this.plugin = plugin;
        initializeArmorEquipEvent();
    }

    private void initializeArmorEquipEvent()
    {
        Bukkit.getPluginManager().registerEvents(new ArmorListener(new ArrayList<>()), plugin);
        Bukkit.getPluginManager().registerEvents(new DispenserArmorListener(), plugin);
    }

    private void moveChestplateToInventory(Player player)
    {
        player.getInventory().addItem(player.getInventory().getChestplate());
        player.getInventory().getChestplate().setAmount(0);
        player.updateInventory();
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
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e)
    {
        if (!(e.getEntity() instanceof Player))
            return;

        if (plugin.getConfigLoader().unbreakable())
            return;

        Player p = (Player) e.getEntity();
        // If the player didn't die from the damage.
        if ((p.getHealth() - e.getFinalDamage()) > 0)
        {
            if (p.getInventory().getChestplate() == null)
                return;

            if (ArmoredElytra.getInstance().getNbtEditor().getArmorTier(p.getInventory().getChestplate()) ==
                ArmorTier.NONE)
                return;

            ItemStack elytra = p.getInventory().getChestplate();
            DamageCause cause = e.getCause();

            // The elytra doesn't receive any damage for these causes:
            if (cause != DamageCause.DROWNING && cause != DamageCause.STARVATION && cause != DamageCause.SUFFOCATION &&
                cause != DamageCause.SUICIDE && cause != DamageCause.FLY_INTO_WALL && cause != DamageCause.POISON)
            {
                int durability = p.getInventory().getChestplate().getDurability();
                int maxDurability = p.getInventory().getChestplate().getType().getMaxDurability();
                int newDurability = durability + ((int) (e.getDamage() / 4) > 1 ? (int) (e.getDamage() / 4) : 1);

                // If the elytra has the durability enchantment, we calculate the durability
                // loss ourselves.
                if (p.getInventory().getChestplate().containsEnchantment(Enchantment.DURABILITY))
                {
                    // Get a random int between 0 and 100 to use in deciding if the durability
                    // enchantment will take effect.
                    Random r = new Random();
                    int randomInt = r.nextInt(101);
                    int enchantLevel = p.getInventory().getChestplate().getEnchantmentLevel(Enchantment.DURABILITY);
                    int durabilityDelta = (100 / (enchantLevel + 1)) < randomInt ? 0 : 1;
                    // If the durability equals/exceeds maxDurability, it's broken (0 = full item
                    // durability).
                    if (durability >= maxDurability)
                        moveChestplateToInventory(p);
                    else
                        newDurability = durability + durabilityDelta;
                }
                // If the item should be broken, make sure it really is broken and unequip it.
                if (newDurability >= maxDurability)
                {
                    newDurability = maxDurability;
                    moveChestplateToInventory(p);
                }
                elytra.setDurability((short) (newDurability));
            }
        }
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

        ArmorTier armorTier = ArmoredElytra.getInstance().getNbtEditor().getArmorTier(e.getNewArmorPiece());
        AllowedToWearEnum allowed = isAllowedToWear(e.getNewArmorPiece(), e.getPlayer(), armorTier);
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
