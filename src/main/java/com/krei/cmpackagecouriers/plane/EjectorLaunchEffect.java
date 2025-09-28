package com.krei.cmpackagecouriers.plane;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

// NOTE: Might need a better name
public interface EjectorLaunchEffect {
    /**
     * @return true if the item handled the launch itself and the default ejector behaviour should be skipped
     */
    boolean onEject(ItemStack stack, Level level, BlockPos pos, Vec3 launchPos, Vec3 motion);
}
