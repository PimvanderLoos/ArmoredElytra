package nl.pim16aap2.armoredElytra.handlers;

import nl.pim16aap2.armoredElytra.ArmoredElytra;
import nl.pim16aap2.armoredElytra.enchantment.EnchantmentManager;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import nl.pim16aap2.armoredElytra.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

public class SmithingTableHandler extends ArmoredElytraHandler implements Listener
{
    public SmithingTableHandler(final ArmoredElytra plugin, final boolean creationEnabled)
    {
        super(plugin, creationEnabled);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSmithingTableUsage(final PrepareSmithingEvent event)
    {
        final SmithingInventory inventory = event.getInventory();
        final ItemStack[] contents = inventory.getContents();

        final ItemStack itemStackA = contents[0];
        final ItemStack itemStackB = contents[1];

        if (itemStackA == null || itemStackB == null ||
            itemStackA.getType() != Material.ELYTRA || !Util.isChestPlate(itemStackB))
            return;

        final ArmorTier newTier = Util.armorToTier(itemStackB.getType());
        final EnchantmentManager enchantments = new EnchantmentManager(itemStackA);
        final Player player = (Player) event.getView().getPlayer();

        final ItemStack result;
        if (plugin.playerHasCraftPerm(player, newTier))
        {
            result = ArmoredElytra.getInstance().getNbtEditor()
                                  .addArmorNBTTags(new ItemStack(Material.ELYTRA, 1), newTier,
                                                   plugin.getConfigLoader().unbreakable());
            enchantments.apply(result);
            event.setResult(result);
        }
    }
}
