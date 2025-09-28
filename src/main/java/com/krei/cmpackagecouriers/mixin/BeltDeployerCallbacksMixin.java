package com.krei.cmpackagecouriers.mixin;

import com.krei.cmpackagecouriers.plane.CardboardPlaneItem;
import com.krei.cmpackagecouriers.PackageCouriers;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.kinetics.deployer.BeltDeployerCallbacks;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(value = BeltDeployerCallbacks.class, remap = false)
public class BeltDeployerCallbacksMixin {

    @Inject(
            method = "activate",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/belt/transport/TransportedItemStack;clearFanProcessingData()V"),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private static void beforeClearFanProcessingData(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler, DeployerBlockEntity blockEntity,
                                                     Recipe<?> recipe, CallbackInfo ci, List<TransportedItemStack> collect) {
        if (collect != null
                && !collect.isEmpty()
                && PackageItem.isPackage(transported.stack)
                && blockEntity.getPlayer().getMainHandItem().is(PackageCouriers.CARDBOARD_PLANE_PARTS_ITEM.get())
                && collect.get(0).stack.getItem() instanceof CardboardPlaneItem) {
            CardboardPlaneItem.setPackage(collect.get(0).stack, transported.stack);
        }

        if (collect != null
                && !collect.isEmpty()
                && transported.stack.getItem() instanceof CardboardPlaneItem
                && blockEntity.getPlayer().getMainHandItem().is(Items.SHEARS)
                && collect.get(0).stack.getItem() instanceof CardboardPlaneItem) {
            collect.get(0).stack = transported.stack.copy();
            CardboardPlaneItem.setPreOpened(collect.get(0).stack, true);
        }
    }
}
