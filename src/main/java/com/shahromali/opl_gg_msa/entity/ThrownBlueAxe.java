package com.shahromali.opl_gg_msa.entity;

import com.shahromali.opl_gg_msa.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A thrown Blue Axe. Works like a thrown trident.
 *
 * <ul>
 *   <li>It flies, spinning, and damages the first thing it hits.</li>
 *   <li>It sticks where it lands and never despawns. The thrower can walk over it to pick it up.</li>
 *   <li>{@link #recall()} sends it flying back to the thrower like a Loyalty trident. It goes into the inventory, or
 *       drops at their feet if the inventory is full. The thrower recalls it by right-clicking with an empty hand.</li>
 * </ul>
 *
 * <p>Falling out of the world recalls it automatically, so it can't be lost to the void while its owner is online.
 */
public class ThrownBlueAxe extends AbstractArrow {
    private static final EntityDataAccessor<Boolean> RETURNING =
            SynchedEntityData.defineId(ThrownBlueAxe.class, EntityDataSerializers.BOOLEAN);
    private static final float DAMAGE = 12.0F;
    /** Same pull as a Loyalty III trident. */
    private static final double RETURN_SPEED = 3.0;
    private static final float THROW_VELOCITY = 2.5F;

    private boolean dealtDamage;
    private boolean returnSoundPlayed;

    public ThrownBlueAxe(EntityType<? extends ThrownBlueAxe> type, Level level) {
        super(type, level);
    }

    /** Throws {@code stack} from {@code thrower}'s eyes in the direction they are looking. */
    public static ThrownBlueAxe throwFrom(Level level, LivingEntity thrower, ItemStack stack) {
        ThrownBlueAxe axe = new ThrownBlueAxe(ModEntities.THROWN_BLUE_AXE.get(), level);
        axe.setPos(thrower.getX(), thrower.getEyeY() - 0.1F, thrower.getZ());
        axe.setOwner(thrower);
        axe.setPickupItemStack(stack.copy());
        axe.pickup = Pickup.ALLOWED;
        axe.shootFromRotation(thrower, thrower.getXRot(), thrower.getYRot(), 0.0F, THROW_VELOCITY, 1.0F);
        return axe;
    }

    /** Calls back every Blue Axe this player has thrown in their current dimension. Returns how many were recalled. */
    public static int recallAll(Player player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return 0;
        }
        int recalled = 0;
        for (ThrownBlueAxe axe : level.getEntities(ModEntities.THROWN_BLUE_AXE.get(), axe -> axe.getOwner() == player)) {
            if (!axe.isReturning()) {
                axe.recall();
                recalled++;
            }
        }
        return recalled;
    }

    public void recall() {
        this.entityData.set(RETURNING, true);
    }

    public boolean isReturning() {
        return this.entityData.get(RETURNING);
    }

    /** True while flying (out or back); false once stuck in a block. Used by the renderer to spin it. */
    public boolean isSpinning() {
        return !this.inGround || this.isNoPhysics();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(RETURNING, false);
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }
        if (!this.level().isClientSide() && !isReturning() && this.getY() < this.level().getMinBuildHeight() - 16) {
            recall(); // fell out of the world
        }

        Entity owner = this.getOwner();
        if (isReturning() && owner != null) {
            if (!canReturnTo(owner)) {
                if (!this.level().isClientSide()) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }
                this.discard();
            } else {
                // Same homing as a Loyalty trident.
                this.setNoPhysics(true);
                this.dealtDamage = true;
                Vec3 toOwner = owner.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + toOwner.y * 0.015 * RETURN_SPEED, this.getZ());
                if (this.level().isClientSide()) {
                    this.yOld = this.getY();
                }
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(toOwner.normalize().scale(0.05 * RETURN_SPEED)));
                if (!this.returnSoundPlayed) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                    this.returnSoundPlayed = true;
                }
            }
        }
        super.tick();
    }

    private boolean canReturnTo(Entity owner) {
        return owner.isAlive() && !owner.isSpectator() && owner.level() == this.level();
    }

    @Nullable
    @Override
    protected EntityHitResult findHitEntity(Vec3 start, Vec3 end) {
        return this.dealtDamage ? null : super.findHitEntity(start, end);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        DamageSource source = this.damageSources().thrown(this, owner == null ? this : owner);
        this.dealtDamage = true;
        if (target.hurt(source, DAMAGE) && target instanceof LivingEntity living) {
            this.doKnockback(living, source);
            this.doPostHurtEffects(living);
        }
        // Bounce off and drop, like a trident.
        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
    }

    @Override
    protected boolean tryPickup(Player player) {
        return super.tryPickup(player)
                || this.isNoPhysics() && this.ownedBy(player) && player.getInventory().add(this.getPickupItem());
    }

    @Override
    public void playerTouch(Player player) {
        if (this.ownedBy(player) || this.getOwner() == null) {
            super.playerTouch(player);
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.BLUE_AXE.get());
    }

    @Override
    public ItemStack getWeaponItem() {
        return this.getPickupItemStackOrigin();
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    protected void tickDespawn() {
        // Never despawn: this is the player's tool, not a spent arrow.
    }

    @Override
    protected float getWaterInertia() {
        return 0.99F;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("DealtDamage", this.dealtDamage);
        tag.putBoolean("Returning", isReturning());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.dealtDamage = tag.getBoolean("DealtDamage");
        this.entityData.set(RETURNING, tag.getBoolean("Returning"));
    }
}
