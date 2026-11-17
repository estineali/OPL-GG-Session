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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FungusBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/**
 * Green hoe: right-click a planted seed and it instantly grows into a tree.
 *
 * <ul>
 *   <li><b>Crops and stems</b> (wheat, carrots, potatoes, beetroot, melon, pumpkin, torchflower) become a tree; each crop
 *       has its own kind of tree, see {@link #CROP_TREES}. Farmland under the crop turns back into dirt so the tree
 *       can root.</li>
 *   <li><b>Saplings</b> (everything in the saplings tag, including azaleas and mangrove propagules) grow into their
 *       own tree; <b>fungi</b> on nylium and <b>mushrooms</b> grow into their huge forms.</li>
 * </ul>
 *
 * <p>Growth works like bone meal applied repeatedly, without bone meal's random chance of failure. A tree still needs
 * room to grow and 2x2 trees still need four saplings. If the tree can't grow, a crop is left exactly as it was.
 * On any other block the hoe tills like a normal hoe.
 */
public class GreenHoeItem extends HoeItem {
    /** Upper bound on bone meal applications; saplings need 2, propagules about 5. */
    private static final int MAX_GROWTH_STEPS = 32;

    /** Which tree each crop becomes. Crops not listed (from other mods, say) become oak. */
    private static final Map<Block, Block> CROP_TREES = Map.of(
            Blocks.WHEAT, Blocks.OAK_SAPLING,
            Blocks.CARROTS, Blocks.ACACIA_SAPLING,
            Blocks.POTATOES, Blocks.BIRCH_SAPLING,
            Blocks.BEETROOTS, Blocks.CHERRY_SAPLING,
            Blocks.MELON_STEM, Blocks.JUNGLE_SAPLING,
            Blocks.PUMPKIN_STEM, Blocks.SPRUCE_SAPLING,
            Blocks.TORCHFLOWER_CROP, Blocks.FLOWERING_AZALEA);
    private static final Block DEFAULT_CROP_TREE = Blocks.OAK_SAPLING;

    public GreenHoeItem(Properties properties) {
        super(Tiers.NETHERITE, properties.attributes(HoeItem.createAttributes(Tiers.NETHERITE, -4.0F, 0.0F)));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        boolean crop = isCrop(state);
        if (!crop && !isTreeSeed(state)) {
            return super.useOn(context); // normal hoe behaviour: till dirt and grass
        }
        if (level instanceof ServerLevel serverLevel) {
            boolean grew = crop ? growCropIntoTree(serverLevel, pos, state) : growFully(serverLevel, pos, state);
            if (grew) {
                serverLevel.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 15);
                Player player = context.getPlayer();
                if (player != null) {
                    context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static boolean isCrop(BlockState state) {
        return state.getBlock() instanceof CropBlock || state.getBlock() instanceof StemBlock;
    }

    private static boolean isTreeSeed(BlockState state) {
        Block block = state.getBlock();
        return block instanceof BonemealableBlock
                && (state.is(BlockTags.SAPLINGS) || block instanceof FungusBlock || block instanceof MushroomBlock);
    }

    /**
     * Swaps the crop for its sapling, turns farmland underneath into dirt, and grows the tree. Puts everything back
     * if the tree doesn't fit.
     */
    private static boolean growCropIntoTree(ServerLevel level, BlockPos pos, BlockState crop) {
        BlockPos groundPos = pos.below();
        BlockState ground = level.getBlockState(groundPos);
        BlockState sapling = CROP_TREES.getOrDefault(crop.getBlock(), DEFAULT_CROP_TREE).defaultBlockState();

        // UPDATE_CLIENTS only: no neighbour updates, so neither the crop nor the sapling pops off mid-swap.
        level.setBlock(pos, sapling, Block.UPDATE_CLIENTS);
        if (ground.is(Blocks.FARMLAND)) {
            level.setBlock(groundPos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_CLIENTS);
        }

        growFully(level, pos, sapling);
        if (!level.getBlockState(pos).is(sapling.getBlock())) {
            return true; // the sapling's spot is now part of a tree
        }

        level.setBlock(groundPos, ground, Block.UPDATE_CLIENTS);
        level.setBlock(pos, crop, Block.UPDATE_ALL);
        return false;
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
