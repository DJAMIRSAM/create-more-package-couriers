package com.krei.cmpackagecouriers.stock_ticker;

import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static com.krei.cmpackagecouriers.stock_ticker.PortableStockTickerReg.TAG_FREQUENCY;

public class LogisticallyLinkedItem extends Item {

    public LogisticallyLinkedItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack pStack) {
        return isTuned(pStack);
    }

    public static boolean isTuned(ItemStack pStack) {
        CompoundTag tag = pStack.getTag();
        return tag != null && tag.contains(TAG_FREQUENCY, Tag.TAG_COMPOUND) && tag.getCompound(TAG_FREQUENCY).hasUUID("Freq");
    }

    @Nullable
    public static UUID networkFromStack(ItemStack pStack) {
        CompoundTag tag = pStack.getTag();
        if (tag == null || !tag.contains(TAG_FREQUENCY, Tag.TAG_COMPOUND))
            return null;
        CompoundTag freqTag = tag.getCompound(TAG_FREQUENCY);
        if (!freqTag.hasUUID("Freq"))
            return null;
        return freqTag.getUUID("Freq");
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext tooltipContext,
                            @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, tooltipContext, tooltipComponents, tooltipFlag);

        if (!isTuned(stack))
            return;

        CreateLang.translate("logistically_linked.tooltip")
                .style(ChatFormatting.GOLD)
                .addTo(tooltipComponents);

        CreateLang.translate("logistically_linked.tooltip_clear")
                .style(ChatFormatting.GRAY)
                .addTo(tooltipComponents);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext pContext) {
        ItemStack stack = pContext.getItemInHand();
        BlockPos pos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        Player player = pContext.getPlayer();

        if (player == null)
            return InteractionResult.FAIL;

        LogisticallyLinkedBehaviour link = BlockEntityBehaviour.get(level, pos, LogisticallyLinkedBehaviour.TYPE);
        boolean tuned = isTuned(stack);

        if (link != null) {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;
            if (!link.mayInteractMessage(player))
                return InteractionResult.SUCCESS;

            assignFrequency(stack, player, link.freqId);
            return InteractionResult.SUCCESS;
        }

        InteractionResult useOn = super.useOn(pContext);
        if (level.isClientSide || useOn == InteractionResult.FAIL)
            return useOn;

        player.displayClientMessage(tuned ? CreateLang.translateDirect("logistically_linked.connected")
                : CreateLang.translateDirect("logistically_linked.new_network_started"), true);
        return useOn;
    }

    public static void assignFrequency(ItemStack stack, Player player, UUID frequency) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag freqTag = tag.contains(TAG_FREQUENCY, Tag.TAG_COMPOUND) ? tag.getCompound(TAG_FREQUENCY) : new CompoundTag();
        freqTag.putUUID("Freq", frequency);
        tag.put(TAG_FREQUENCY, freqTag);

        player.displayClientMessage(CreateLang.translateDirect("logistically_linked.tuned"), true);
    }
}
