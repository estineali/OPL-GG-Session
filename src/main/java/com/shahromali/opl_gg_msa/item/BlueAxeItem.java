package com.shahromali.opl_gg_msa.item;

import com.shahromali.opl_gg_msa.entity.ThrownBlueAxe;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Blue axe: breaking one log of a tree brings down the whole tree.
 *
 * <p>When a log is broken, every log connected to it (including diagonally) is felled too, along with the leaves that
 * would otherwise be left floating, i.e. leaves no longer within 6 blocks of a remaining log, the same rule vanilla
 * leaf decay uses. Leaves held up by a neighbouring tree stay put. For huge nether fungi, attached wart blocks and
 * shroomlights are removed as well.
 *
 * <p>Only natural trees are felled: the connected logs must touch natural (non-player-placed) leaves or wart blocks.
 * Player-built log structures break one block at a time, as does any log broken while sneaking.
 *
 * <p>It can also be thrown; see {@link #use} and {@link ThrownBlueAxe}.
 */
public class BlueAxeItem extends AxeItem {
    private static final int MAX_LOGS = 1024;
    private static final int MAX_FOLIAGE = 4096;
    /** Vanilla leaves decay when more than 6 blocks (through other leaves) from a log. */
    private static final int LEAF_REACH = LeavesBlock.DECAY_DISTANCE - 1;

    public BlueAxeItem(Properties properties) {
        super(Tiers.NETHERITE, properties.attributes(AxeItem.createAttributes(Tiers.NETHERITE, 5.0F, -3.0F)));
    }

    /**
     * Right-click (in the air, or on a block the axe can't strip) throws the axe. It costs 1 durability and leaves the
     * hand empty, even in creative, so an empty-hand right-click can call it back.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            if (stack.isEmpty()) {
                return InteractionResultHolder.consume(stack); // broke on this throw
            }
            ThrownBlueAxe axe = ThrownBlueAxe.throwFrom(level, player, stack);
            level.addFreshEntity(axe);
            level.playSound(null, axe, SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
            player.awardStat(Stats.ITEM_USED.get(this));
            player.setItemInHand(hand, ItemStack.EMPTY);
            return InteractionResultHolder.consume(ItemStack.EMPTY);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean result = super.mineBlock(stack, level, state, pos, miner);
        if (level instanceof ServerLevel serverLevel && isLog(state) && !miner.isShiftKeyDown()) {
            fellTree(serverLevel, pos, miner);
        }
        return result;
    }

    private static void fellTree(ServerLevel level, BlockPos brokenLog, LivingEntity miner) {
        Set<BlockPos> logs = collectConnectedLogs(level, brokenLog);
        Set<BlockPos> felled = new HashSet<>(logs);
        felled.add(brokenLog); // already air, but counts as removed when working out which leaves fall

        List<BlockPos> leaves = findOrphanedLeaves(level, felled);
        List<BlockPos> wart = findAttachedWart(level, felled);
        if (leaves.isEmpty() && wart.isEmpty() && !touchesNaturalLeaves(level, felled)) {
            return; // no foliage: player-built, not a tree
        }

        for (BlockPos pos : logs) {
            level.destroyBlock(pos, true, miner);
        }
        for (BlockPos pos : leaves) {
            level.destroyBlock(pos, true, miner);
        }
        for (BlockPos pos : wart) {
            level.destroyBlock(pos, true, miner);
        }
    }

    /** All logs connected to {@code start} through the 26 surrounding blocks, not including {@code start}. */
    private static Set<BlockPos> collectConnectedLogs(Level level, BlockPos start) {
        Set<BlockPos> found = new LinkedHashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty() && found.size() < MAX_LOGS) {
            BlockPos current = queue.poll();
            for (BlockPos next : BlockPos.betweenClosed(current.offset(-1, -1, -1), current.offset(1, 1, 1))) {
                if (found.size() >= MAX_LOGS) {
                    break;
                }
                if (!next.equals(start) && !found.contains(next) && isLog(level.getBlockState(next))) {
                    BlockPos immutable = next.immutable();
                    found.add(immutable);
                    queue.add(immutable);
                }
            }
        }
        return found;
    }

    /**
     * Natural leaves that would be left with no log within reach once {@code felled} is gone.
     *
     * <p>Explores natural leaves up to twice the reach from the felled logs, so a leaf held up by another tree is
     * seen along with the path to that tree's logs. Then walks outward from the remaining logs to mark every leaf
     * still within reach; the rest of the nearby leaves are orphans.
     */
    private static List<BlockPos> findOrphanedLeaves(Level level, Set<BlockPos> felled) {
        Map<BlockPos, Integer> depthFromFelled = new HashMap<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> remainingLogs = new HashSet<>();

        for (BlockPos log : felled) {
            for (Direction dir : Direction.values()) {
                BlockPos next = log.relative(dir);
                if (!depthFromFelled.containsKey(next) && isNaturalLeaf(level.getBlockState(next))) {
                    depthFromFelled.put(next, 1);
                    queue.add(next);
                }
            }
        }
        while (!queue.isEmpty() && depthFromFelled.size() < MAX_FOLIAGE) {
            BlockPos current = queue.poll();
            int depth = depthFromFelled.get(current);
            for (Direction dir : Direction.values()) {
                BlockPos next = current.relative(dir);
                BlockState nextState = level.getBlockState(next);
                if (isLog(nextState) && !felled.contains(next)) {
                    remainingLogs.add(next);
                } else if (depth < LEAF_REACH * 2 && !depthFromFelled.containsKey(next) && isNaturalLeaf(nextState)) {
                    depthFromFelled.put(next, depth + 1);
                    queue.add(next);
                }
            }
        }

        // Leaves still within reach of a log that is not being felled.
        queue.clear(); // the search above may stop early at MAX_FOLIAGE
        Map<BlockPos, Integer> depthFromRemaining = new HashMap<>();
        for (BlockPos log : remainingLogs) {
            for (Direction dir : Direction.values()) {
                BlockPos next = log.relative(dir);
                if (depthFromFelled.containsKey(next) && !depthFromRemaining.containsKey(next)) {
                    depthFromRemaining.put(next, 1);
                    queue.add(next);
                }
            }
        }
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            int depth = depthFromRemaining.get(current);
            if (depth >= LEAF_REACH) {
                continue;
            }
            for (Direction dir : Direction.values()) {
                BlockPos next = current.relative(dir);
                if (depthFromFelled.containsKey(next) && !depthFromRemaining.containsKey(next)) {
                    depthFromRemaining.put(next, depth + 1);
                    queue.add(next);
                }
            }
        }

        List<BlockPos> orphans = new ArrayList<>();
        depthFromFelled.forEach((pos, depth) -> {
            if (depth <= LEAF_REACH && !depthFromRemaining.containsKey(pos)) {
                orphans.add(pos);
            }
        });
        return orphans;
    }

    /** Nether wart blocks and shroomlights connected to the felled stems (huge crimson/warped fungi). */
    private static List<BlockPos> findAttachedWart(Level level, Set<BlockPos> felled) {
        Set<BlockPos> found = new LinkedHashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        for (BlockPos log : felled) {
            for (Direction dir : Direction.values()) {
                BlockPos next = log.relative(dir);
                if (!found.contains(next) && isWart(level.getBlockState(next))) {
                    found.add(next);
                    queue.add(next);
                }
            }
        }
        while (!queue.isEmpty() && found.size() < MAX_FOLIAGE) {
            BlockPos current = queue.poll();
            for (Direction dir : Direction.values()) {
                BlockPos next = current.relative(dir);
                if (!found.contains(next) && isWart(level.getBlockState(next))) {
                    found.add(next);
                    queue.add(next);
                }
            }
        }
        return new ArrayList<>(found);
    }

    /** True when any felled log has natural leaves next to it (also covers leaves still held by another tree). */
    private static boolean touchesNaturalLeaves(Level level, Set<BlockPos> felled) {
        for (BlockPos log : felled) {
            for (Direction dir : Direction.values()) {
                if (isNaturalLeaf(level.getBlockState(log.relative(dir)))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isLog(BlockState state) {
        return state.is(BlockTags.LOGS);
    }

    private static boolean isNaturalLeaf(BlockState state) {
        return state.is(BlockTags.LEAVES)
                && state.hasProperty(LeavesBlock.PERSISTENT)
                && !state.getValue(LeavesBlock.PERSISTENT);
    }

    private static boolean isWart(BlockState state) {
        return state.is(BlockTags.WART_BLOCKS) || state.is(Blocks.SHROOMLIGHT);
    }
}
