package com.shahromali.opl_gg_msa.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FungusBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Green hoe: right-click a sapling and it grows into a full tree on the spot.
 *
 * <p>Works on everything in the saplings tag (all wood types, azaleas, mangrove propagules), crimson/warped fungi on
 * nylium and mushrooms (growing huge ones), and crops and stems (grown to full maturity). It grows the plant by
 * applying bone meal repeatedly without bone meal's random chance of failure, so a tree still needs the usual
 * space and light, and big trees (dark oak, 2x2 spruce/jungle) still need four saplings. On any other block the hoe
 * tills like a normal hoe.
 */
public class GreenHoeItem extends HoeItem {
    /** Upper bound on bone meal applications; saplings need 2, crops at most 8, propagules about 5. */
    private static final int MAX_GROWTH_STEPS = 32;

    public GreenHoeItem(Properties properties) {
        super(Tiers.NETHERITE, properties.attributes(HoeItem.createAttributes(Tiers.NETHERITE, -4.0F, 0.0F)));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!isGrowable(state)) {
            return super.useOn(context); // normal hoe behaviour: till dirt and grass
        }
        if (level instanceof ServerLevel serverLevel && growFully(serverLevel, pos, state)) {
            serverLevel.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 15);
            Player player = context.getPlayer();
            if (player != null) {
                context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static boolean isGrowable(BlockState state) {
        Block block = state.getBlock();
        return block instanceof BonemealableBlock
                && (state.is(BlockTags.SAPLINGS)
                    || block instanceof CropBlock
                    || block instanceof StemBlock
                    || block instanceof FungusBlock
                    || block instanceof MushroomBlock);
    }

    /** Applies bone meal until the plant turns into something else (a tree) or can't grow further. */
    private static boolean growFully(ServerLevel level, BlockPos pos, BlockState original) {
        boolean changed = false;
        for (int i = 0; i < MAX_GROWTH_STEPS; i++) {
            BlockState current = level.getBlockState(pos);
            if (!current.is(original.getBlock())
                    || !(current.getBlock() instanceof BonemealableBlock plant)
                    || !plant.isValidBonemealTarget(level, pos, current)) {
                break;
            }
            plant.performBonemeal(level, level.getRandom(), pos, current);
            changed |= level.getBlockState(pos) != current;
        }
        return changed;
    }
}
