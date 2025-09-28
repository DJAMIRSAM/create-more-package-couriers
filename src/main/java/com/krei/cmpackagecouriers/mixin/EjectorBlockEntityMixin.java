package com.krei.cmpackagecouriers.mixin;

import com.krei.cmpackagecouriers.plane.EjectorLaunchEffect;
import com.simibubi.create.content.logistics.depot.EjectorBlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EjectorBlockEntity.class, remap = false)
public abstract class EjectorBlockEntityMixin {

    @Redirect(method = "launchItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean cmpackagecouriers$spawnPlanes(Level level, Entity entity) {
        if (level.isClientSide) {
            return level.addFreshEntity(entity);
        }

        if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if (stack.getItem() instanceof EjectorLaunchEffect ejectable) {
                Vec3 launchPos = itemEntity.position();
                Vec3 motion = itemEntity.getDeltaMovement();
                EjectorBlockEntity ejector = (EjectorBlockEntity) (Object) this;
                if (ejectable.onEject(stack, level, ejector.getBlockPos(), launchPos, motion)) {
                    return true;
                }
            }
        }

        return level.addFreshEntity(entity);
    }
}
