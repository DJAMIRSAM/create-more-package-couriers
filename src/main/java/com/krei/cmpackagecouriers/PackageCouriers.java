package com.krei.cmpackagecouriers;

import com.krei.cmpackagecouriers.compat.Mods;
import com.krei.cmpackagecouriers.plane.*;
import com.krei.cmpackagecouriers.stock_ticker.PortableStockTickerReg;
import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(PackageCouriers.MODID)
public class PackageCouriers {
    public static final String MODID = "cmpackagecouriers";
    @SuppressWarnings("unused")
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate
            .create(MODID)
            .defaultCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey());

    static {
        REGISTRATE.setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                .andThen(TooltipModifier.mapNull(KineticStats.create(item))));
    }

    public static final EntityEntry<CardboardPlaneEntity> CARDBOARD_PLANE_ENTITY = REGISTRATE
        .entity("cardboard_plane", CardboardPlaneEntity::createEmpty, MobCategory.MISC)
        .properties(p -> p
                .sized(0.5f, 0.5f)
                .eyeHeight(0.25f)
                .clientTrackingRange(80)
                .updateInterval(1))
//        .renderer(() -> DeliveryPlaneRenderer::new)
        .register();

    public static final ItemEntry<CardboardPlaneItem> CARDBOARD_PLANE_ITEM = REGISTRATE
            .item("cardboard_plane", CardboardPlaneItem::new)
            .register();

    public static final ItemEntry<CardboardPlanePartsItem> CARDBOARD_PLANE_PARTS_ITEM = REGISTRATE
            .item("cardboard_plane_parts", CardboardPlanePartsItem::new)
            .register();

    public PackageCouriers(IEventBus modEventBus, ModContainer modContainer) {
        if (!Mods.CREATE_MOBILE_PACKAGES.isLoaded())
            PortableStockTickerReg.register();
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        REGISTRATE.registerEventListeners(modEventBus);
        modEventBus.addListener(PackageCouriers::clientInit);
        CardboardPlaneEntity.init();
        modEventBus.addListener(ServerConfig::onLoad);
        modEventBus.addListener(ServerConfig::onReload);
        // Event Handler Class: AddressMarkerHandler
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        CardboardPlaneEntityRenderer.init();
        CardboardPlaneItemRenderer.init();
        // Somethings wrong with registrate that makes me wanna commit seppuku
        EntityRenderers.register(
                CARDBOARD_PLANE_ENTITY.get(),
                CardboardPlaneEntityRenderer::new
        );
    }

    // TODO: Replace depot sign based targeting with a new sign block
}
