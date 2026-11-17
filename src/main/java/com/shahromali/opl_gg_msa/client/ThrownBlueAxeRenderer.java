package com.shahromali.opl_gg_msa.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.shahromali.opl_gg_msa.entity.ThrownBlueAxe;
import com.shahromali.opl_gg_msa.item.ModItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Draws the thrown axe using its item sprite: tumbling end over end in flight, blade-first when stuck. */
public class ThrownBlueAxeRenderer extends EntityRenderer<ThrownBlueAxe> {
    private static final float SPIN_DEGREES_PER_TICK = 40.0F;
    /** The sprite's head points up-right (45°); this turns it to point along the direction of travel. */
    private static final float HEAD_FORWARD = -45.0F;

    private final ItemRenderer itemRenderer;
    private final ItemStack stack;

    public ThrownBlueAxeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.stack = new ItemStack(ModItems.BLUE_AXE.get());
    }

    @Override
    public void render(ThrownBlueAxe axe, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        // Turn so local +X points along the horizontal direction of travel, +Y up.
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, axe.yRotO, axe.getYRot()) - 90.0F));
        if (axe.isSpinning()) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-(axe.tickCount + partialTicks) * SPIN_DEGREES_PER_TICK));
        } else {
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, axe.xRotO, axe.getXRot()) + HEAD_FORWARD));
        }
        this.itemRenderer.renderStatic(this.stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, axe.level(), axe.getId());
        poseStack.popPose();
        super.render(axe, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownBlueAxe axe) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
