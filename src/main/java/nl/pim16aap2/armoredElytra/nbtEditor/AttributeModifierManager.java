package nl.pim16aap2.armoredElytra.nbtEditor;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import nl.pim16aap2.armoredElytra.util.ArmorTier;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.meta.ItemMeta;
import org.semver4j.Semver;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;

/**
 * Manages the attribute modifiers of an item.
 */
abstract class AttributeModifierManager
{
    /**
     * The attribute setters to apply when setting the attributes of an armored elytra.
     */
    private static final List<AttributeCreator> ATTRIBUTE_SETTERS = List.of(
        new AttributeCreator(
            Attribute.GENERIC_ARMOR,
            ArmorTier::getArmor,
            AttributeModifierManager::newGenericArmorModifier
        ),
        new AttributeCreator(
            Attribute.GENERIC_ARMOR_TOUGHNESS,
            ArmorTier::getToughness,
            AttributeModifierManager::newGenericArmorToughnessModifier
        ),
        new AttributeCreator(
            Attribute.GENERIC_KNOCKBACK_RESISTANCE,
            ArmorTier::getKnockbackResistance,
            AttributeModifierManager::newGenericKnockbackResistanceModifier
        )
    );

    /**
     * Creates an instance of the correct subclass of AttributeModifierManager.
     *
     * @param version
     *     The version of Minecraft to create the correct subclass for.
     */
    static AttributeModifierManager create(Semver version)
    {
        if (version.isGreaterThanOrEqualTo(Semver.of(1, 21, 0)))
            return new AttributeModifierManagerNamespacedKey();
        else
            return new AttributeModifierManagerUUID();
    }

    /**
     * Overwrites the attribute modifiers of the item meta with the values from the armor tier.
     *
     * @param meta
     *     The item meta to overwrite the attribute modifiers of.
     * @param armorTier
     *     The armor tier to get the values from.
     */
    public final void overwriteAttributeModifiers(ItemMeta meta, ArmorTier armorTier)
    {
        final @Nullable Multimap<Attribute, AttributeModifier> existingModifiers = meta.getAttributeModifiers();

        final Multimap<Attribute, AttributeModifier> updatedModifiers =
            existingModifiers == null ? LinkedHashMultimap.create() : LinkedHashMultimap.create(existingModifiers);

        ATTRIBUTE_SETTERS.forEach(setter -> setter.apply(this, updatedModifiers, armorTier));

        meta.setAttributeModifiers(updatedModifiers);
    }

    protected abstract AttributeModifier newGenericArmorModifier(double value);

    protected abstract AttributeModifier newGenericArmorToughnessModifier(double value);

    protected abstract AttributeModifier newGenericKnockbackResistanceModifier(double value);

    /**
     * The AttributeModifierManager for versions 1.21.0 and higher.
     * <p>
     * This class uses NamespacedKeys to identify the attribute modifiers.
     */
    @SuppressWarnings("UnstableApiUsage")
    private static final class AttributeModifierManagerNamespacedKey extends AttributeModifierManager
    {
        @Override
        protected AttributeModifier newGenericArmorModifier(double value)
        {
            return addNumberToChestModifier(Attribute.GENERIC_ARMOR.getKey(), value);
        }

        @Override
        protected AttributeModifier newGenericArmorToughnessModifier(double value)
        {
            return addNumberToChestModifier(Attribute.GENERIC_ARMOR_TOUGHNESS.getKey(), value);
        }

        @Override
        protected AttributeModifier newGenericKnockbackResistanceModifier(double value)
        {
            return addNumberToChestModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE.getKey(), value);
        }

        private static AttributeModifier addNumberToChestModifier(NamespacedKey key, double value)
        {
            return new AttributeModifier(
                key,
                value,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.CHEST
            );
        }
    }

    /**
     * The AttributeModifierManager for versions prior to 1.21.0.
     * <p>
     * This class uses UUIDs to identify the attribute modifiers.
     */
    private static final class AttributeModifierManagerUUID extends AttributeModifierManager
    {
        private static final UUID UUID_GENERIC_ARMOR =
            UUID.fromString("afa5a6a1-f3bc-4cbb-aeb4-13383b5fcd5a");

        private static final UUID UUID_GENERIC_ARMOR_TOUGHNESS =
            UUID.fromString("a334b502-88c0-47ec-9adc-738036bf3e27");

        private static final UUID UUID_GENERIC_KNOCKBACK_RESISTANCE =
            UUID.fromString("4a66cb1c-f606-4baf-b04b-6fb5d6bcfb88");

        @Override
        protected AttributeModifier newGenericArmorModifier(double value)
        {
            return addNumberToChestModifier(
                UUID_GENERIC_ARMOR,
                Attribute.GENERIC_ARMOR,
                value
            );
        }

        @Override
        protected AttributeModifier newGenericArmorToughnessModifier(double value)
        {
            return addNumberToChestModifier(
                UUID_GENERIC_ARMOR_TOUGHNESS,
                Attribute.GENERIC_ARMOR_TOUGHNESS,
                value
            );
        }

        @Override
        protected AttributeModifier newGenericKnockbackResistanceModifier(double value)
        {
            return addNumberToChestModifier(
                UUID_GENERIC_KNOCKBACK_RESISTANCE,
                Attribute.GENERIC_KNOCKBACK_RESISTANCE,
                value
            );
        }

        @SuppressWarnings("deprecation") // It's not deprecated on the version this is used on.
        private AttributeModifier addNumberToChestModifier(UUID key, Attribute attribute, double value)
        {
            return new AttributeModifier(
                key,
                attribute.getKey().getKey(),
                value,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlot.CHEST
            );
        }
    }

    /**
     * A record to store the attribute, the value predicate and the creator of the attribute modifier.
     *
     * @param attribute
     *     The attribute to set.
     * @param valuePredicate
     *     The predicate to get the value of the attribute from the ArmorTier.
     * @param creator
     *     The creator of the attribute modifier.
     */
    private record AttributeCreator(
        Attribute attribute,
        ToDoubleFunction<ArmorTier> valuePredicate,
        BiFunction<AttributeModifierManager, Double, AttributeModifier> creator
    )
    {
        /**
         * Applies the attribute modifier to the existing attribute modifiers of the item meta.
         * <p>
         * If the attribute modifier already exists, it will be removed first.
         *
         * @param manager
         *     The manager to apply the attribute modifier with.
         * @param modifiers
         *     The existing attribute modifiers of the item meta.
         * @param armorTier
         *     The armor tier to get the value from.
         */
        void apply(
            AttributeModifierManager manager,
            Multimap<Attribute, AttributeModifier> modifiers,
            ArmorTier armorTier)
        {
            try
            {
                modifiers.removeAll(attribute);

                final double value = valuePredicate.applyAsDouble(armorTier);
                final AttributeModifier modifier = creator.apply(manager, value);

                modifiers.put(attribute, modifier);
            }
            catch (Exception e)
            {
                throw new RuntimeException(
                    "Failed to apply attribute modifier!" +
                        "\n  For attribute:  " + attribute +
                        "\n  For armor tier: " + armorTier +
                        "\n  With current modifiers: " + modifiers,
                    e
                );
            }
        }
    }
}
