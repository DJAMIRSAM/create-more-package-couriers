package com.krei.cmpackagecouriers.network;

import com.krei.cmpackagecouriers.PackageCouriers;
import com.krei.cmpackagecouriers.stock_ticker.GenericStackListPacket;
import com.krei.cmpackagecouriers.stock_ticker.HiddenCategoriesPacket;
import com.krei.cmpackagecouriers.stock_ticker.OpenPortableStockTicker;
import com.krei.cmpackagecouriers.stock_ticker.RequestStockUpdate;
import com.krei.cmpackagecouriers.stock_ticker.SendPackage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.simple.SimpleChannel;

public class CMPackageCouriersNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(PackageCouriers.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int packetId = 0;

    private static int nextId() {
        return packetId++;
    }

    public static void register() {
        CHANNEL.registerMessage(nextId(), RequestStockUpdate.class, RequestStockUpdate::encode, RequestStockUpdate::decode, RequestStockUpdate::handle);
        CHANNEL.registerMessage(nextId(), GenericStackListPacket.class, GenericStackListPacket::encode, GenericStackListPacket::decode, GenericStackListPacket::handle);
        CHANNEL.registerMessage(nextId(), SendPackage.class, SendPackage::encode, SendPackage::decode, SendPackage::handle);
        CHANNEL.registerMessage(nextId(), HiddenCategoriesPacket.class, HiddenCategoriesPacket::encode, HiddenCategoriesPacket::decode, HiddenCategoriesPacket::handle);
        CHANNEL.registerMessage(nextId(), OpenPortableStockTicker.class, OpenPortableStockTicker::encode, OpenPortableStockTicker::decode, OpenPortableStockTicker::handle);
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
