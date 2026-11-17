package com.shahromali.opl_gg_msa.item;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Red sword: kills any living thing it hits in one strike.
 *
 * <p>Attack damage is set to the game's maximum (2048 including the player's base 1). Armor and enchantments can
 * still shrink that, so after a hit lands, anything left alive is finished off with a "generic kill" damage source,
 * the same one {@code /kill} uses. That bypasses armor, Resistance, totems and invulnerability frames. Players in
 * creative or spectator mode are left alone.
 */
public class RedSwordItem extends SwordItem {
    /** Minecraft caps the attack damage attribute at 2048; players have a base of 1. */
    private static final double BONUS_ATTACK_DAMAGE = 2047.0;
    private static final double ATTACK_SPEED_MODIFIER = -2.4; // same swing speed as vanilla swords

    public RedSwordItem(Properties properties) {
        super(Tiers.NETHERITE, properties.attributes(createMaxDamageAttributes()));
    }

    private static ItemAttributeModifiers createMaxDamageAttributes() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, BONUS_ATTACK_DAMAGE, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, ATTACK_SPEED_MODIFIER, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide() && target.isAlive() && canBeKilled(target)) {
            // Credit the kill to the attacker so loot, XP and death messages behave like a normal kill.
            target.hurt(target.damageSources().source(DamageTypes.GENERIC_KILL, attacker), Float.MAX_VALUE);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    private static boolean canBeKilled(LivingEntity target) {
        return !(target instanceof Player player) || !(player.isCreative() || player.isSpectator());
    }
}
