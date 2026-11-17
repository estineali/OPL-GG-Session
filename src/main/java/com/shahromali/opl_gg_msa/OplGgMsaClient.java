package com.shahromali.opl_gg_msa;

import com.shahromali.opl_gg_msa.client.ThrownBlueAxeRenderer;
import com.shahromali.opl_gg_msa.entity.ModEntities;
import com.shahromali.opl_gg_msa.network.RecallAxePayload;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

// Client-only entrypoint. Not loaded on dedicated servers, so client classes are safe here.
@Mod(value = OplGgMsa.MODID, dist = Dist.CLIENT)
public class OplGgMsaClient {
    public OplGgMsaClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(OplGgMsaClient::onClientSetup);
        modEventBus.addListener(OplGgMsaClient::registerRenderers);
        NeoForge.EVENT_BUS.addListener(OplGgMsaClient::onRightClickEmpty);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        OplGgMsa.LOGGER.info("{} client setup", OplGgMsa.MODID);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.THROWN_BLUE_AXE.get(), ThrownBlueAxeRenderer::new);
    }

    /** Right-clicking the air with an empty main hand recalls thrown Blue Axes; only the client sees this click. */
    private static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        if (event.getHand() == InteractionHand.MAIN_HAND) {
            PacketDistributor.sendToServer(RecallAxePayload.INSTANCE);
        }
    }
}
