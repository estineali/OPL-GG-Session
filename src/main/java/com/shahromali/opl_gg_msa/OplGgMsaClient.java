package com.shahromali.opl_gg_msa;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

// Client-only entrypoint. Not loaded on dedicated servers, so client classes are safe here.
@Mod(value = OplGgMsa.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = OplGgMsa.MODID, value = Dist.CLIENT)
public class OplGgMsaClient {
    public OplGgMsaClient(ModContainer container) {
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        OplGgMsa.LOGGER.info("{} client setup", OplGgMsa.MODID);
    }
}
