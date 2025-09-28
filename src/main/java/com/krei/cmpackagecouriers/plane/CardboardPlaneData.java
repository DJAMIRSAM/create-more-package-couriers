package com.krei.cmpackagecouriers.plane;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class CardboardPlaneData {
    private static final String ROOT_TAG = "CardboardPlane";
    private static final String PACKAGE_TAG = "Package";
    private static final String PREOPENED_TAG = "PreOpened";

    private static CompoundTag getOrCreateRootTag(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag root = new CompoundTag();
            tag.put(ROOT_TAG, root);
            return root;
        }
        return tag.getCompound(ROOT_TAG);
    }

    private static CompoundTag getRootTag(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            return tag.getCompound(ROOT_TAG);
        }
        return null;
    }

    public static void setPackage(ItemStack plane, ItemStack box) {
        if (box.isEmpty()) {
            return;
        }
        CompoundTag root = getOrCreateRootTag(plane);
        CompoundTag packageTag = new CompoundTag();
        box.save(packageTag);
        root.put(PACKAGE_TAG, packageTag);
    }

    public static ItemStack getPackage(ItemStack plane) {
        CompoundTag root = getRootTag(plane);
        if (root == null || !root.contains(PACKAGE_TAG, Tag.TAG_COMPOUND)) {
            return ItemStack.EMPTY;
        }
        CompoundTag packageTag = root.getCompound(PACKAGE_TAG);
        return ItemStack.of(packageTag);
    }

    public static void setPreOpened(ItemStack plane, boolean preOpened) {
        CompoundTag root = getOrCreateRootTag(plane);
        root.putBoolean(PREOPENED_TAG, preOpened);
    }

    public static boolean isPreOpened(ItemStack plane) {
        CompoundTag root = getRootTag(plane);
        return root != null && root.getBoolean(PREOPENED_TAG);
    }
}
