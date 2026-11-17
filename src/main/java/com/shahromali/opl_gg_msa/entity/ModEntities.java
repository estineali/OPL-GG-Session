package com.shahromali.opl_gg_msa.entity;

import com.shahromali.opl_gg_msa.OplGgMsa;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers the mod's entity types. */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, OplGgMsa.MODID);

    /** Same size and network settings as a thrown trident. */
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownBlueAxe>> THROWN_BLUE_AXE =
            ENTITY_TYPES.register("thrown_blue_axe", () -> EntityType.Builder
                    .<ThrownBlueAxe>of(ThrownBlueAxe::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .eyeHeight(0.13F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("thrown_blue_axe"));

    private ModEntities() {
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
