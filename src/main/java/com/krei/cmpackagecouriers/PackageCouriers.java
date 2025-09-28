package com.krei.cmpackagecouriers;

import com.krei.cmpackagecouriers.plane.CardboardPlaneEntity;
import com.krei.cmpackagecouriers.plane.CardboardPlaneEntityRenderer;
import com.krei.cmpackagecouriers.plane.CardboardPlaneItem;
import com.krei.cmpackagecouriers.plane.CardboardPlanePartsItem;
import com.krei.cmpackagecouriers.ServerConfig;
import com.simibubi.create.AllCreativeModeTabs;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(PackageCouriers.MODID)
public class PackageCouriers {
    public static final String MODID = "cmpackagecouriers";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<CardboardPlaneItem> CARDBOARD_PLANE_ITEM = ITEMS.register("cardboard_plane",
            () -> new CardboardPlaneItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<CardboardPlanePartsItem> CARDBOARD_PLANE_PARTS_ITEM = ITEMS.register("cardboard_plane_parts",
            () -> new CardboardPlanePartsItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<EntityType<CardboardPlaneEntity>> CARDBOARD_PLANE_ENTITY = ENTITY_TYPES.register("cardboard_plane",
            () -> EntityType.Builder.<CardboardPlaneEntity>of(CardboardPlaneEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(80)
                    .updateInterval(1)
                    .build(ResourceLocation.fromNamespaceAndPath(MODID, "cardboard_plane").toString()));

    public PackageCouriers() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(this::clientInit);
        modEventBus.addListener(this::fillCreativeTabs);
        modEventBus.addListener(ServerConfig::onLoad);
        modEventBus.addListener(ServerConfig::onReload);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    }

    private void clientInit(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> EntityRenderers.register(CARDBOARD_PLANE_ENTITY.get(), CardboardPlaneEntityRenderer::new));
    }

    private void fillCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == AllCreativeModeTabs.BASE_CREATIVE_TAB.get()) {
            event.accept(CARDBOARD_PLANE_PARTS_ITEM.get());
            event.accept(CARDBOARD_PLANE_ITEM.get());
        }
    }
}
