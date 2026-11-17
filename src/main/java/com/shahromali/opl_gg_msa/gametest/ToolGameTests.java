package com.shahromali.opl_gg_msa.gametest;

import com.shahromali.opl_gg_msa.OplGgMsa;
import com.shahromali.opl_gg_msa.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Automated in-game checks for the three tools. Run with {@code ./gradlew runGameTestServer}.
 *
 * <p>Tests run in {@code data/opl_gg_msa/structure/empty.nbt}, an empty 15x20x15 space.
 */
@GameTestHolder(OplGgMsa.MODID)
@PrefixGameTestTemplate(false)
public class ToolGameTests {
    private static final String EMPTY = "empty";
    private static final BlockPos AREA_MIN = BlockPos.ZERO;
    private static final BlockPos AREA_MAX = new BlockPos(14, 19, 14);

    /** A warden (500 health) dies from a single hit, even with the weakest possible swing. */
    @GameTest(template = EMPTY)
    public static void redSwordKillsWardenInOneHit(GameTestHelper helper) {
        Warden warden = helper.spawnWithNoFreeWill(EntityType.WARDEN, new BlockPos(7, 1, 7));
        Player player = playerHolding(helper, new ItemStack(ModItems.RED_SWORD.get()));

        player.attack(warden);

        helper.assertTrue(warden.isDeadOrDying(), "Warden survived a hit from the red sword");
        helper.succeed();
    }

    /** Right-clicking a sapling grows a tree; breaking one of its logs with the axe removes the whole tree. */
    @GameTest(template = EMPTY)
    public static void greenHoeGrowsTreeAndBlueAxeFellsIt(GameTestHelper helper) {
        BlockPos trunk = new BlockPos(7, 1, 7);
        helper.setBlock(trunk.below(), Blocks.DIRT);
        helper.setBlock(trunk, Blocks.BIRCH_SAPLING);

        Player player = playerHolding(helper, new ItemStack(ModItems.GREEN_HOE.get()));
        BlockPos absTrunk = helper.absolutePos(trunk);
        ModItems.GREEN_HOE.get().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absTrunk), Direction.UP, absTrunk, false)));

        helper.assertBlockPresent(Blocks.BIRCH_LOG, trunk);
        int logs = count(helper, Blocks.BIRCH_LOG);
        helper.assertTrue(logs >= 4, "Expected a grown birch tree, found " + logs + " logs");
        helper.assertTrue(count(helper, Blocks.BIRCH_LEAVES) > 0, "Grown tree has no leaves");

        ItemStack axe = new ItemStack(ModItems.BLUE_AXE.get());
        breakWith(helper, axe, playerHolding(helper, axe), trunk);

        helper.assertTrue(count(helper, Blocks.BIRCH_LOG) == 0, "Logs left after felling");
        helper.assertTrue(count(helper, Blocks.BIRCH_LEAVES) == 0, "Leaves left after felling");
        helper.succeed();
    }

    /** A stack of logs with no leaves (something a player built) only loses the block that was broken. */
    @GameTest(template = EMPTY)
    public static void blueAxeLeavesPlayerBuiltLogsAlone(GameTestHelper helper) {
        BlockPos base = new BlockPos(7, 1, 7);
        for (int y = 0; y < 3; y++) {
            helper.setBlock(base.above(y), Blocks.OAK_LOG);
        }

        ItemStack axe = new ItemStack(ModItems.BLUE_AXE.get());
        breakWith(helper, axe, playerHolding(helper, axe), base);

        helper.assertBlockNotPresent(Blocks.OAK_LOG, base);
        helper.assertBlockPresent(Blocks.OAK_LOG, base.above());
        helper.assertBlockPresent(Blocks.OAK_LOG, base.above(2));
        helper.succeed();
    }

    /** Right-clicking a freshly planted crop grows it to full maturity. */
    @GameTest(template = EMPTY)
    public static void greenHoeMaturesCrops(GameTestHelper helper) {
        BlockPos crop = new BlockPos(7, 1, 7);
        helper.setBlock(crop.below(), Blocks.FARMLAND);
        helper.setBlock(crop, Blocks.WHEAT);

        Player player = playerHolding(helper, new ItemStack(ModItems.GREEN_HOE.get()));
        BlockPos absCrop = helper.absolutePos(crop);
        ModItems.GREEN_HOE.get().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absCrop), Direction.UP, absCrop, false)));

        BlockState state = helper.getBlockState(crop);
        helper.assertTrue(state.getBlock() instanceof net.minecraft.world.level.block.CropBlock c && c.isMaxAge(state),
                "Wheat is not fully grown");
        helper.succeed();
    }

    /** A survival mock player holding {@code stack}, with the item's attack attributes applied. */
    private static Player playerHolding(GameTestHelper helper, ItemStack stack) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        // Mock players don't tick, so held-item attributes (like attack damage) must be applied by hand.
        stack.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            var instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.addOrReplacePermanentModifier(modifier);
            }
        });
        return player;
    }

    /** Breaks the block the way survival mining does: remove it, then let the tool react. */
    private static void breakWith(GameTestHelper helper, ItemStack tool, Player player, BlockPos relativePos) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(relativePos);
        BlockState state = level.getBlockState(pos);
        level.destroyBlock(pos, true, player);
        tool.getItem().mineBlock(tool, level, state, pos, player);
    }

    private static int count(GameTestHelper helper, Block block) {
        int total = 0;
        for (BlockPos pos : BlockPos.betweenClosed(helper.absolutePos(AREA_MIN), helper.absolutePos(AREA_MAX))) {
            if (helper.getLevel().getBlockState(pos).is(block)) {
                total++;
            }
        }
        return total;
    }
}
