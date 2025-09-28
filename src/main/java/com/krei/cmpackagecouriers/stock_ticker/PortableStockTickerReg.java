package com.krei.cmpackagecouriers.stock_ticker;

import com.krei.cmpackagecouriers.PackageCouriers;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.MenuEntry;
import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

import static com.krei.cmpackagecouriers.PackageCouriers.REGISTRATE;

public class PortableStockTickerReg {

    public static final String TAG_FREQUENCY = "CmpFreq";
    public static final String TAG_ADDRESS = "CmpAddress";
    public static final String TAG_CATEGORIES = "CmpCategories";
    public static final String TAG_HIDDEN_CATEGORIES = "CmpHiddenCategories";

    public static final ItemEntry<PortableStockTicker> PORTABLE_STOCK_TICKER =
            REGISTRATE.item("portable_stock_ticker", PortableStockTicker::new)
                    .register();

    public static final MenuEntry<PortableStockTickerMenu> PORTABLE_STOCK_TICKER_MENU =
            REGISTRATE.menu(
                    "portable_stock_ticker_menu",
                    (menuType, containerId, playerInventory) -> new PortableStockTickerMenu(containerId, playerInventory),
                    () -> PortableStockTickerScreen::new
            ).register();


    public enum PortableStockTickerPackets implements BasePacketPayload.PacketTypeProvider {
        // Client to Server
        LOGISTICS_PACKAGE_REQUEST(SendPackage.class, SendPackage.STREAM_CODEC),
        REQUEST_STOCK_UPDATE(RequestStockUpdate.class, RequestStockUpdate.STREAM_CODEC),
        HIDDEN_CATEGORIES(HiddenCategoriesPacket.class, HiddenCategoriesPacket.STREAM_CODEC),
        OPEN_PORTABLE_STOCK_TICKER(OpenPortableStockTicker.class, OpenPortableStockTicker.STREAM_CODEC),

        // Server to Client
        BIG_ITEM_STACK_LIST(GenericStackListPacket.class, GenericStackListPacket.STREAM_CODEC);


        private final CatnipPacketRegistry.PacketType<?> type;

        <T extends BasePacketPayload> PortableStockTickerPackets(Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
            String name = this.name().toLowerCase(Locale.ROOT);
            this.type = new CatnipPacketRegistry.PacketType<>(
                    new CustomPacketPayload.Type<>(new ResourceLocation(PackageCouriers.MODID, name)),
                    clazz, codec
            );
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
            return (CustomPacketPayload.Type<T>) this.type.type();
        }

        public static void register() {
            CatnipPacketRegistry packetRegistry = new CatnipPacketRegistry(PackageCouriers.MODID, 1);
            for (PortableStockTickerPackets packet : PortableStockTickerPackets.values()) {
                packetRegistry.registerPacket(packet.type);
            }
            packetRegistry.registerAllPackets();
        }
    }

    public static void register() {
        PortableStockTickerPackets.register();
    }
}
