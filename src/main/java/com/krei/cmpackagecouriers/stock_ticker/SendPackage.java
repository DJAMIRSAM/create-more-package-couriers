package com.krei.cmpackagecouriers.stock_ticker;

import com.krei.cmpackagecouriers.compat.factory_abstractions.FactoryAbstractionsCompat;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.WiFiEffectPacket;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.utility.AdventureUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.NetworkEvent;
import ru.zznty.create_factory_abstractions.generic.support.GenericOrder;

// Shamelessly copied from Create: Mobile Packages
public class SendPackage {

    private final GenericOrder order;
    private final String address;

    public SendPackage(GenericOrder order, String address) {
        this.order = order;
        this.address = address;
    }

    public static void encode(SendPackage packet, FriendlyByteBuf buffer) {
        FactoryAbstractionsCompat.writeGenericOrder(buffer, packet.order);
        buffer.writeUtf(packet.address);
    }

    public static SendPackage decode(FriendlyByteBuf buffer) {
        GenericOrder order = FactoryAbstractionsCompat.readGenericOrder(buffer);
        String address = buffer.readUtf();
        return new SendPackage(order, address);
    }

    protected void applySettings(ServerPlayer player) {

        if (!order.isEmpty()) {
            AllSoundEvents.STOCK_TICKER_REQUEST.playOnServer(player.level(), player.blockPosition());
            AllAdvancements.STOCK_TICKER.awardTo(player);
            WiFiEffectPacket.send(player.level(), player.blockPosition());
        }

        ItemStack pstStack = PortableStockTicker.find(player.getInventory());
        PortableStockTicker pst = pstStack != null ? (PortableStockTicker) pstStack.getItem() : null;
        if (pstStack != null) {
            pst.broadcastPackageRequest(LogisticallyLinkedBehaviour.RequestType.PLAYER, order, null, address, player);
        }
    }

    public static void handle(SendPackage packet, java.util.function.Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || player.isSpectator() || AdventureUtil.isAdventure(player))
                return;
            Level world = player.level();
            if (!world.isLoaded(player.blockPosition()))
                return;
            packet.applySettings(player);
        });
        context.setPacketHandled(true);
    }
}
