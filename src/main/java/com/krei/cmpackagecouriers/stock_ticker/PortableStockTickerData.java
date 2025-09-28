package com.krei.cmpackagecouriers.stock_ticker;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PortableStockTickerData {
    private static final String ROOT_TAG = "PortableStockTicker";
    private static final String FREQ_TAG = "Frequency";
    private static final String ADDRESS_TAG = "Address";
    private static final String CATEGORIES_TAG = "Categories";
    private static final String HIDDEN_CATEGORIES_TAG = "HiddenCategories";

    private static CompoundTag getOrCreateRoot(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag root = new CompoundTag();
            tag.put(ROOT_TAG, root);
            return root;
        }
        return tag.getCompound(ROOT_TAG);
    }

    private static CompoundTag getRoot(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            return tag.getCompound(ROOT_TAG);
        }
        return null;
    }

    public static boolean hasFrequency(ItemStack stack) {
        CompoundTag root = getRoot(stack);
        return root != null && root.hasUUID(FREQ_TAG);
    }

    public static UUID getFrequency(ItemStack stack) {
        CompoundTag root = getRoot(stack);
        if (root == null || !root.hasUUID(FREQ_TAG)) {
            return null;
        }
        return root.getUUID(FREQ_TAG);
    }

    public static void setFrequency(ItemStack stack, UUID frequency) {
        CompoundTag root = getOrCreateRoot(stack);
        root.putUUID(FREQ_TAG, frequency);
    }

    public static void clearFrequency(ItemStack stack) {
        CompoundTag root = getRoot(stack);
        if (root != null) {
            root.remove(FREQ_TAG);
        }
    }

    public static void setAddress(ItemStack stack, String address) {
        CompoundTag root = getOrCreateRoot(stack);
        root.putString(ADDRESS_TAG, address);
    }

    public static String getAddress(ItemStack stack) {
        CompoundTag root = getRoot(stack);
        if (root == null || !root.contains(ADDRESS_TAG, Tag.TAG_STRING)) {
            return null;
        }
        String address = root.getString(ADDRESS_TAG);
        return address.isEmpty() ? null : address;
    }

    public static void setCategories(ItemStack stack, List<ItemStack> categories) {
        CompoundTag root = getOrCreateRoot(stack);
        ListTag list = new ListTag();
        for (ItemStack category : categories) {
            if (category.isEmpty()) {
                continue;
            }
            CompoundTag itemTag = new CompoundTag();
            category.save(itemTag);
            list.add(itemTag);
        }
        root.put(CATEGORIES_TAG, list);
    }

    public static List<ItemStack> getCategories(ItemStack stack) {
        CompoundTag root = getRoot(stack);
        if (root == null || !root.contains(CATEGORIES_TAG, Tag.TAG_LIST)) {
            return List.of();
        }
        ListTag listTag = root.getList(CATEGORIES_TAG, Tag.TAG_COMPOUND);
        List<ItemStack> result = new ArrayList<>(listTag.size());
        for (Tag tag : listTag) {
            if (tag instanceof CompoundTag compound) {
                result.add(ItemStack.of(compound));
            }
        }
        return result;
    }

    public static void setHiddenCategories(ItemStack stack, Map<UUID, List<Integer>> hidden) {
        CompoundTag root = getOrCreateRoot(stack);
        CompoundTag hiddenTag = new CompoundTag();
        hidden.forEach((uuid, indices) -> {
            if (uuid == null || indices == null) {
                return;
            }
            ListTag list = new ListTag();
            for (Integer index : indices) {
                if (index != null) {
                    list.add(IntTag.valueOf(index));
                }
            }
            hiddenTag.put(uuid.toString(), list);
        });
        root.put(HIDDEN_CATEGORIES_TAG, hiddenTag);
    }

    public static Map<UUID, List<Integer>> getHiddenCategories(ItemStack stack) {
        Map<UUID, List<Integer>> result = new HashMap<>();
        CompoundTag root = getRoot(stack);
        if (root == null || !root.contains(HIDDEN_CATEGORIES_TAG, Tag.TAG_COMPOUND)) {
            return result;
        }
        CompoundTag hiddenTag = root.getCompound(HIDDEN_CATEGORIES_TAG);
        for (String key : hiddenTag.getAllKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                ListTag list = hiddenTag.getList(key, Tag.TAG_INT);
                List<Integer> indices = new ArrayList<>(list.size());
                for (Tag element : list) {
                    if (element instanceof IntTag intTag) {
                        indices.add(intTag.getAsInt());
                    }
                }
                result.put(uuid, indices);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return result;
    }
}
