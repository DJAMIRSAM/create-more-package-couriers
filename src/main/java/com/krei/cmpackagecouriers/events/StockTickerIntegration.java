package com.krei.cmpackagecouriers.events;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.core.BlockPos;

import static com.krei.cmpackagecouriers.PackageCouriers.MODID;
import com.krei.cmpackagecouriers.ServerConfig;

import com.simibubi.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import com.simibubi.create.content.logistics.tableCloth.ShoppingListItem;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StockTickerIntegration {

    private static void rewriteAddressIfNeeded(Player player, ItemStack heldItem) {
        if (heldItem.getItem() instanceof ShoppingListItem) {
            String currentAddress = ShoppingListItem.getAddress(heldItem);
            if (currentAddress.toLowerCase().contains("@player")) {
                String playerIdentifier = player.getDisplayName().getString();
                String newAddress = currentAddress.replaceAll("(?i)@player", "@" + playerIdentifier);
                ShoppingListItem.saveList(heldItem, ShoppingListItem.getList(heldItem), newAddress);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!ServerConfig.shopAddressReplacement) return;
        if (event.getLevel().isClientSide()) {
            return;
        }

        Entity target = event.getTarget();
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);

        if (player == null || target == null || player.isSpectator() || hand != InteractionHand.MAIN_HAND) {
            return;
        }

        BlockPos stockTickerPos = StockTickerInteractionHandler.getStockTickerPosition(target);
        if (stockTickerPos != null) {
            rewriteAddressIfNeeded(player, heldItem);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!ServerConfig.shopAddressReplacement) return;
        if (event.getLevel().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);
        BlockPos pos = event.getPos();

        if (player == null || player.isSpectator() || hand != InteractionHand.MAIN_HAND) {
            return;
        }

        if (event.getLevel().getBlockState(pos).getBlock() instanceof BlazeBurnerBlock) {
            rewriteAddressIfNeeded(player, heldItem);
        }
    }
}
