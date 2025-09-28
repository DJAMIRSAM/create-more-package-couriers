package com.krei.cmpackagecouriers.stock_ticker;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

// Shamelessly copied from Create: Mobile Packages
public class HiddenCategoriesPacket {

    private final List<Integer> indices;

    public HiddenCategoriesPacket(List<Integer> indices) {
        this.indices = indices;
    }

    public static void encode(HiddenCategoriesPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.indices.size());
        for (int index : packet.indices) {
            buffer.writeVarInt(index);
        }
    }

    public static HiddenCategoriesPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<Integer> indices = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            indices.add(buffer.readVarInt());
        }
        return new HiddenCategoriesPacket(indices);
    }

    public static void handle(HiddenCategoriesPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() == null) {
                return;
            }
            ItemStack stack = PortableStockTicker.find(context.getSender().getInventory());
            if (stack == null) {
                return;
            }
            if (stack.getItem() instanceof PortableStockTicker pst) {
                Map<UUID, List<Integer>> hiddenCategories = new HashMap<>();
                hiddenCategories.put(context.getSender().getUUID(), new ArrayList<>(packet.indices));
                pst.hiddenCategoriesByPlayer = hiddenCategories;
                pst.saveHiddenCategoriesByPlayerToStack(stack, hiddenCategories);
            }
        });
        context.setPacketHandled(true);
    }
}
