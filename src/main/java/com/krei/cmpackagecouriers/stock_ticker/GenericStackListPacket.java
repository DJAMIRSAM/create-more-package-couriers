package com.krei.cmpackagecouriers.stock_ticker;

import com.krei.cmpackagecouriers.compat.factory_abstractions.FactoryAbstractionsCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.NetworkEvent;
import ru.zznty.create_factory_abstractions.api.generic.stack.GenericStack;

import java.util.List;
import java.util.function.Supplier;

// Shamelessly copied from Create: Mobile Packages
public class GenericStackListPacket {

    private final List<GenericStack> stacks;

    // Standard constructor
    public GenericStackListPacket(List<GenericStack> stacks) {
        this.stacks = stacks;
    }

    public static void encode(GenericStackListPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.stacks.size());
        for (GenericStack stack : packet.stacks) {
            FactoryAbstractionsCompat.writeGenericStack(buffer, stack);
        }
    }

    public static GenericStackListPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<GenericStack> stacks = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            stacks.add(FactoryAbstractionsCompat.readGenericStack(buffer));
        }
        return new GenericStackListPacket(stacks);
    }

    public static void handle(GenericStackListPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                ClientScreenStorage.stacks = packet.stacks;
            }
        });
        context.setPacketHandled(true);
    }
}
