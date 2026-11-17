package com.shahromali.opl_gg_msa;

import com.mojang.logging.LogUtils;
import com.shahromali.opl_gg_msa.entity.ModEntities;
import com.shahromali.opl_gg_msa.entity.ThrownBlueAxe;
import com.shahromali.opl_gg_msa.item.ModItems;
import com.shahromali.opl_gg_msa.network.RecallAxePayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;

// The value here must match an entry in the META-INF/neoforge.mods.toml file.
@Mod(OplGgMsa.MODID)
public class OplGgMsa {
    public static final String MODID = "opl_gg_msa";
    public static final Logger LOGGER = LogUtils.getLogger();

    // NeoForge injects the mod event bus and container into the constructor.
    public OplGgMsa(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(RecallAxePayload::register);
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(OplGgMsa::onRightClickBlock);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("{} common setup", MODID);
    }

    /** Right-clicking a block with an empty main hand recalls thrown Blue Axes (the click still does its usual thing). */
    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() == InteractionHand.MAIN_HAND
                && event.getItemStack().isEmpty()
                && event.getEntity() instanceof ServerPlayer player) {
            ThrownBlueAxe.recallAll(player);
        }
    }
}
