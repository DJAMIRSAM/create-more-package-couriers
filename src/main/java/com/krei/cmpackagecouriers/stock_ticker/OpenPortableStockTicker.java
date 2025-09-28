package com.krei.cmpackagecouriers.stock_ticker;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

// Shamelessly copied from Create: Mobile Packages
public class OpenPortableStockTicker {
    public OpenPortableStockTicker() {
    }

    public static void encode(OpenPortableStockTicker packet, FriendlyByteBuf buffer) {
    }

    public static OpenPortableStockTicker decode(FriendlyByteBuf buffer) {
        return new OpenPortableStockTicker();
    }

    public static void handle(OpenPortableStockTicker packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                context.getSender().openMenu(new SimpleMenuProvider(
                        (id, inv, ply) -> new PortableStockTickerMenu(id, inv),
                        Component.translatable("item.cmpackagecouriers.portable_stock_ticker")
                ));
            }
        });
        context.setPacketHandled(true);
    }
}
