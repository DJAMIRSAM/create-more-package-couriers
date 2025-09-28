package com.krei.cmpackagecouriers.stock_ticker;

import com.krei.cmpackagecouriers.network.CMPackageCouriersNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

import static com.krei.cmpackagecouriers.stock_ticker.StockCheckingItem.getAccurateSummary;

// Shamelessly copied from Create: Mobile Packages
public class RequestStockUpdate {
    private final UUID networkId;

    public RequestStockUpdate(UUID networkId) {
        if (networkId == null) {
            this.networkId = UUID.randomUUID();
            return;
        }
        this.networkId = networkId;
    }

    public static void encode(RequestStockUpdate packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.networkId);
    }

    public static RequestStockUpdate decode(FriendlyByteBuf buffer) {
        return new RequestStockUpdate(buffer.readUUID());
    }

    public static void handle(RequestStockUpdate packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() == null) {
                return;
            }
            ItemStack stack = PortableStockTicker.find(context.getSender().getInventory());
            if (stack == null || stack.isEmpty()) {
                return;
            }

            GenericStackListPacket responsePacket = new GenericStackListPacket(getAccurateSummary(stack).get());
            CMPackageCouriersNetwork.sendToPlayer(responsePacket, context.getSender());
        });
        context.setPacketHandled(true);
    }
}
