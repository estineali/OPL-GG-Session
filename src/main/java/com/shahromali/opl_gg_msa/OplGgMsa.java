package com.shahromali.opl_gg_msa;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

// The value here must match an entry in the META-INF/neoforge.mods.toml file.
@Mod(OplGgMsa.MODID)
public class OplGgMsa {
    public static final String MODID = "opl_gg_msa";
    public static final Logger LOGGER = LogUtils.getLogger();

    // NeoForge injects the mod event bus and container into the constructor.
    public OplGgMsa(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("{} common setup", MODID);
    }
}
