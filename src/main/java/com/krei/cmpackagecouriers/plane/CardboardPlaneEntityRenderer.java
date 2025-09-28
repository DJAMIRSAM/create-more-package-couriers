package com.krei.cmpackagecouriers.plane;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CardboardPlaneEntityRenderer extends EntityRenderer<CardboardPlaneEntity> {

    public CardboardPlaneEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(CardboardPlaneEntity entity, float yaw, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light) {
        ms.pushPose();
        ms.translate(0, 0.25, 0);
        ms.scale(0.75f, 0.75f, 0.75f);
        ms.mulPose(Axis.YP.rotationDegrees(90 + yaw));
        ms.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, entity.oldDeltaYaw, entity.newDeltaYaw) * -4));
        ms.mulPose(Axis.ZP.rotationDegrees(-Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));

        CardboardPlaneItemRenderer.renderPlane(entity.getPackage(), ms, buffer, light);

        ms.popPose();

        super.render(entity, yaw, partialTicks, ms, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(CardboardPlaneEntity entity) {
        return null;
    }

    public static void init() {}
}
