package com.krei.cmpackagecouriers;

import com.krei.cmpackagecouriers.compat.Mods;
import com.krei.cmpackagecouriers.plane.*;
import com.krei.cmpackagecouriers.ponder.PonderScenes;
import com.krei.cmpackagecouriers.stock_ticker.PortableStockTickerReg;
import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

@Mod(PackageCouriers.MODID)
public class PackageCouriers {
    public static final String MODID = "cmpackagecouriers";
    public static final String TAG_PLANE_PACKAGE = "PlanePackage";
    public static final String TAG_PLANE_PREOPENED = "PlanePreOpened";
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
        .register();

    public static final ItemEntry<CardboardPlaneItem> CARDBOARD_PLANE_ITEM = REGISTRATE
            .item("cardboard_plane", CardboardPlaneItem::new)
            .register();

    public static final ItemEntry<CardboardPlanePartsItem> CARDBOARD_PLANE_PARTS_ITEM = REGISTRATE
            .item("cardboard_plane_parts", CardboardPlanePartsItem::new)
            .register();

    public PackageCouriers() {
        ModContainer modContainer = FMLJavaModLoadingContext.get().getModContainer();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        if (!Mods.CREATE_MOBILE_PACKAGES.isLoaded())
            PortableStockTickerReg.register();
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        REGISTRATE.registerEventListeners(modEventBus);
        modEventBus.addListener(PackageCouriers::clientInit);
        CardboardPlaneEntity.init();
        modEventBus.addListener(ServerConfig::onLoad);
        modEventBus.addListener(ServerConfig::onReload);
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        CardboardPlaneEntityRenderer.init();
        CardboardPlaneItemRenderer.init();
        PonderIndex.addPlugin(new PonderScenes());
        EntityRenderers.register(
                CARDBOARD_PLANE_ENTITY.get(),
                CardboardPlaneEntityRenderer::new
        );
    }
}
