package com.krei.cmpackagecouriers.plane;

import com.krei.cmpackagecouriers.PackageCouriers;
import com.krei.cmpackagecouriers.ServerConfig;
import com.krei.cmpackagecouriers.marker.AddressMarkerHandler;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.List;

// Copied and Altered from TridentItem
// NOTE: Might need to remove projectileItem interface
// NOTE: Using a compass with target in an item frame or placard to set a coordinate address
public class CardboardPlaneItem extends Item implements EjectorLaunchEffect {

    private static final String TAG_PACKAGE = "PlanePackage";
    private static final String TAG_PREOPENED = "PlanePreOpened";

    public CardboardPlaneItem(Properties p) {
        super(p.stacksTo(1));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player player
                && this.getUseDuration(stack) - timeLeft >= 10
                && !level.isClientSide()) {

            String address = getAddress(stack);
            ItemStack packageItem = getPackage(stack);
            if (packageItem.isEmpty()) {
                packageItem = PackageStyles.getRandomBox();
                // TODO: Do some exception because this shouldn't happen
            }

            MinecraftServer server = level.getServer();
            if (server != null) {
                CardboardPlaneEntity plane = new CardboardPlaneEntity(level);
                plane.setPos(player.getX(), player.getEyeY()-0.1f, player.getZ());
                plane.setPackage(packageItem);
                plane.setUnpack(isPreOpened(stack));
                plane.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.8F, 1.0F);

                ServerPlayer serverPlayer = server.getPlayerList().getPlayerByName(address);
                boolean launched = false;
                if (serverPlayer != null && ServerConfig.planePlayerTargets) {
                    plane.setTarget(serverPlayer);
                    level.addFreshEntity(plane);
                    stack.shrink(1);
                    launched = true;
                } else {
                    AddressMarkerHandler.MarkerTarget target = AddressMarkerHandler.getMarkerTarget(address);
                    if (target != null && ServerConfig.planeLocationTargets) {
                        plane.setTarget(target.pos, target.level);
                        level.addFreshEntity(plane);
                        stack.shrink(1);
                        launched = true;
                    }
                }
                if (!launched) {
                    player.displayClientMessage(Component.translatable(PackageCouriers.MODID + ".message.no_address"), true);
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isCrouching()) {
            if (!level.isClientSide()) {
                ItemStack stack = player.getItemInHand(hand);
                ItemStack box = CardboardPlaneItem.getPackage(stack);
                stack.shrink(1);
                player.getInventory().placeItemBackInInventory(box);
                player.getInventory().placeItemBackInInventory(new ItemStack(PackageCouriers.CARDBOARD_PLANE_PARTS_ITEM.get()));
            }
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean onEject(ItemStack stack, Level level, BlockPos pos) {
        if (level.isClientSide())
            return false;

        float yaw = switch (level.getBlockState(pos).getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> 180f;
            case SOUTH -> 0f;
            case WEST  -> 90f;
            default    -> -90f;
        };

        String address = getAddress(stack);
        ItemStack packageItem = getPackage(stack);
        if (packageItem.isEmpty()) {
            packageItem = PackageStyles.getRandomBox();
            // TODO: Do some exception because this shouldn't happen
        }

        MinecraftServer server = level.getServer();
        if (server != null) {
            CardboardPlaneEntity plane = new CardboardPlaneEntity(level);
            plane.setPos(Vec3.atCenterOf(pos).add(0,1,0));
            plane.setPackage(packageItem);
            plane.setUnpack(isPreOpened(stack));
            plane.shootFromRotation(-37.5F, yaw, 0.0F, 0.8F, 1.0F);

            ServerPlayer serverPlayer = server.getPlayerList().getPlayerByName(address);
            if (serverPlayer != null && ServerConfig.planePlayerTargets) {
                plane.setTarget(serverPlayer);
                level.addFreshEntity(plane);
                return true;
            } else {
                AddressMarkerHandler.MarkerTarget target = AddressMarkerHandler.getMarkerTarget(address);
                if (target != null && ServerConfig.planeLocationTargets) {
                    plane.setTarget(target.pos, target.level);
                    level.addFreshEntity(plane);
                    return true;
                }
            }
        }
        return false;
    }

    public static ItemStack withPackage(ItemStack box) {
        ItemStack plane = new ItemStack(PackageCouriers.CARDBOARD_PLANE_ITEM.get());
        setPackage(plane, box);
        return plane;
    }

    public static void setPackage(ItemStack plane, ItemStack box) {
        if (!(plane.getItem() instanceof CardboardPlaneItem))
            return;
        if (box.getItem() instanceof PackageItem) {
            CompoundTag tag = plane.getOrCreateTag();
            tag.put(TAG_PACKAGE, box.save(new CompoundTag()));
        } else {
            plane.removeTagKey(TAG_PACKAGE);
        }
    }

    public static ItemStack getPackage(ItemStack plane) {
        if (!(plane.getItem() instanceof CardboardPlaneItem))
            return ItemStack.EMPTY;
        CompoundTag tag = plane.getTag();
        if (tag == null || !tag.contains(TAG_PACKAGE))
            return ItemStack.EMPTY;
        CompoundTag packageTag = tag.getCompound(TAG_PACKAGE);
        return ItemStack.of(packageTag);
    }

    public static String getAddress(ItemStack plane) {
        if (plane.getItem() instanceof CardboardPlaneItem) {
            // added handling of @ in address to alow adress chaining and identifying player names
            String address = PackageItem.getAddress(getPackage(plane));
            int atIndex = address.indexOf('@');
            if (atIndex != -1) {
                address = address.substring(atIndex + 1);
            }
            return address;
        }
        return "";
    }

    public static void setPreOpened(ItemStack plane, boolean preopened) {
        if (plane.getItem() instanceof CardboardPlaneItem) {
            plane.getOrCreateTag().putBoolean(TAG_PREOPENED, preopened);
        }
    }

    public static boolean isPreOpened(ItemStack plane) {
        if (!(plane.getItem() instanceof CardboardPlaneItem))
            return false;
        CompoundTag tag = plane.getTag();
        return tag != null && tag.getBoolean(TAG_PREOPENED);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ItemStack box = getPackage(stack);
        if (!box.isEmpty()) {
            if (isPreOpened(stack))
                tooltipComponents.add(Component.translatable("tooltip.cmpackagecouriers.cardboard_plane.preopened")
                        .withStyle(ChatFormatting.AQUA));
            box.getItem().appendHoverText(box, level, tooltipComponents, tooltipFlag);
        }
        else
            super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
