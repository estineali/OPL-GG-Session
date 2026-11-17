package com.shahromali.opl_gg_msa.gametest;

import com.shahromali.opl_gg_msa.OplGgMsa;
import com.shahromali.opl_gg_msa.entity.ModEntities;
import com.shahromali.opl_gg_msa.entity.ThrownBlueAxe;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import java.util.List;
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

    /** Right-clicking freshly planted wheat turns it into an oak tree; the farmland under it becomes soil. */
    @GameTest(template = EMPTY)
    public static void greenHoeTurnsWheatIntoOakTree(GameTestHelper helper) {
        BlockPos crop = new BlockPos(7, 1, 7);
        helper.setBlock(crop.below(), Blocks.FARMLAND);
        helper.setBlock(crop, Blocks.WHEAT);

        useHoeOn(helper, crop);

        helper.assertBlockPresent(Blocks.OAK_LOG, crop);
        helper.assertBlockNotPresent(Blocks.FARMLAND, crop.below());
        helper.assertTrue(count(helper, Blocks.OAK_LEAVES) > 0, "Oak tree has no leaves");
        helper.succeed();
    }

    /** With no room for a tree (a ceiling two blocks up), the crop and farmland are left exactly as they were. */
    @GameTest(template = EMPTY)
    public static void greenHoeLeavesCropAloneWithoutRoom(GameTestHelper helper) {
        BlockPos crop = new BlockPos(7, 1, 7);
        helper.setBlock(crop.below(), Blocks.FARMLAND);
        helper.setBlock(crop, Blocks.WHEAT);
        helper.setBlock(crop.above(2), Blocks.STONE);

        useHoeOn(helper, crop);

        helper.assertBlockPresent(Blocks.WHEAT, crop);
        helper.assertBlockPresent(Blocks.FARMLAND, crop.below());
        helper.succeed();
    }

    /**
     * Throwing the axe empties the hand and launches it; it sticks in the ground; recalling it brings it back to
     * the thrower, who gets it back in their inventory.
     */
    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void blueAxeThrowsSticksAndReturns(GameTestHelper helper) {
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(4, 0, 4), new BlockPos(10, 0, 10))) {
            helper.setBlock(pos, Blocks.DIRT);
        }
        Player player = playerHolding(helper, new ItemStack(ModItems.BLUE_AXE.get()));
        Vec3 standAt = helper.absoluteVec(new Vec3(7.5, 4.0, 7.5));
        player.moveTo(standAt.x, standAt.y, standAt.z, 0.0F, 90.0F); // looking straight down

        ModItems.BLUE_AXE.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

        helper.assertTrue(player.getMainHandItem().isEmpty(), "Hand still holds the axe after throwing");
        var thrown = helper.getLevel().getEntities(ModEntities.THROWN_BLUE_AXE.get(), axe -> axe.getOwner() == player);
        helper.assertTrue(thrown.size() == 1, "Expected 1 thrown axe, found " + thrown.size());
        ThrownBlueAxe axe = thrown.get(0);

        helper.runAfterDelay(30, () -> {
            helper.assertFalse(axe.isSpinning(), "Thrown axe did not stick in the ground");
            helper.assertTrue(ThrownBlueAxe.recallAll(player) == 1, "Recall did not find the thrown axe");
            helper.succeedWhen(() -> {
                helper.assertTrue(axe.distanceTo(player) < 1.5, "Axe has not flown back yet");
                axe.playerTouch(player); // mock players don't tick, so trigger the pickup a real player would
                helper.assertTrue(player.getInventory().hasAnyMatching(stack -> stack.is(ModItems.BLUE_AXE.get())),
                        "Axe did not return to the inventory");
                helper.assertTrue(axe.isRemoved(), "Thrown axe entity still exists after pickup");
            });
        });
    }

    /** Each tool's crafting recipe loads, produces the tool, and has a recipe-book unlock. */
    @GameTest(template = EMPTY)
    public static void toolRecipesCraftTheTools(GameTestHelper helper) {
        ItemStack redstone = new ItemStack(Items.REDSTONE_BLOCK);
        ItemStack lapis = new ItemStack(Items.LAPIS_BLOCK);
        ItemStack emerald = new ItemStack(Items.EMERALD_BLOCK);
        ItemStack bone = new ItemStack(Items.BONE_BLOCK);

        assertCrafts(helper, "red_sword", ModItems.RED_SWORD.get(),
                redstone, new ItemStack(Items.NETHER_STAR), redstone,
                redstone, new ItemStack(Items.NETHERITE_SWORD), redstone,
                redstone, redstone, redstone);
        assertCrafts(helper, "blue_axe", ModItems.BLUE_AXE.get(),
                lapis, lapis, lapis,
                lapis, new ItemStack(Items.NETHERITE_AXE), lapis,
                lapis, lapis, lapis);
        assertCrafts(helper, "green_hoe", ModItems.GREEN_HOE.get(),
                bone, emerald, bone,
                emerald, new ItemStack(Items.NETHERITE_HOE), emerald,
                bone, emerald, bone);
        helper.succeed();
    }

    private static void assertCrafts(GameTestHelper helper, String name, Item expected, ItemStack... grid) {
        ServerLevel level = helper.getLevel();
        CraftingInput input = CraftingInput.of(3, 3, List.of(grid));
        ItemStack result = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
                .map(recipe -> recipe.value().assemble(input, level.registryAccess()))
                .orElse(ItemStack.EMPTY);
        helper.assertTrue(result.is(expected), "Recipe for " + name + " produced " + result);

        String category = expected == ModItems.RED_SWORD.get() ? "combat" : "tools";
        ResourceLocation unlock = ResourceLocation.fromNamespaceAndPath(OplGgMsa.MODID, "recipes/" + category + "/" + name);
        helper.assertTrue(level.getServer().getAdvancements().get(unlock) != null, "Missing recipe unlock " + unlock);
    }

    private static void useHoeOn(GameTestHelper helper, BlockPos relativePos) {
        Player player = playerHolding(helper, new ItemStack(ModItems.GREEN_HOE.get()));
        BlockPos pos = helper.absolutePos(relativePos);
        ModItems.GREEN_HOE.get().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)));
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
