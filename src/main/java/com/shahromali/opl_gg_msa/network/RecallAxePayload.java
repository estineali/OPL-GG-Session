package com.shahromali.opl_gg_msa.network;

import com.shahromali.opl_gg_msa.OplGgMsa;
import com.shahromali.opl_gg_msa.entity.ThrownBlueAxe;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → server: "recall my thrown Blue Axe". Sent when the player right-clicks the air with an empty hand, which
 * the server has no way to see on its own. Empty-hand clicks on blocks are seen server-side directly.
 */
public record RecallAxePayload() implements CustomPacketPayload {
    public static final RecallAxePayload INSTANCE = new RecallAxePayload();
    public static final Type<RecallAxePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OplGgMsa.MODID, "recall_axe"));
    public static final StreamCodec<ByteBuf, RecallAxePayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, RecallAxePayload::handle);
    }

    private static void handle(RecallAxePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ThrownBlueAxe.recallAll(player);
            }
        });
    }
}
