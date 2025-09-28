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

            MinecraftServer server = level.getServer();
            if (server != null) {
                ItemStack planeStack = createPlaneStackForLaunch(stack);
                CardboardPlaneEntity plane = new CardboardPlaneEntity(level);
                plane.setPlaneStack(planeStack);
                plane.setUnpack(isPreOpened(stack));
                plane.setPos(player.getX(), player.getEyeY() - 0.1f, player.getZ());
                plane.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.8F, 1.0F);

                String fullAddress = getFullAddress(planeStack);
                String playerAddress = getAddress(planeStack);
                if (trySpawnPlane(server, level, plane, fullAddress, playerAddress)) {
                    stack.shrink(1);
                } else {
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
    public boolean onEject(ItemStack stack, Level level, BlockPos pos, Vec3 launchPos, Vec3 motion) {
        if (level.isClientSide())
            return false;

        return launchPlane(stack, level, pos, launchPos, motion);
    }

    private static boolean tryAssignLocationTarget(CardboardPlaneEntity plane, String address) {
        if (!ServerConfig.planeLocationTargets || address.isBlank())
            return false;
        AddressMarkerHandler.MarkerTarget target = AddressMarkerHandler.getMarkerTarget(address);
        if (target == null)
            return false;
        plane.setTarget(target.pos, target.level);
        return true;
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
        String address = getFullAddress(plane);
        int atIndex = address.lastIndexOf('@');
        if (atIndex != -1 && atIndex + 1 < address.length()) {
            return address.substring(atIndex + 1);
        }
        return address;
    }

    public static String getFullAddress(ItemStack plane) {
        if (plane.getItem() instanceof CardboardPlaneItem) {
            return PackageItem.getAddress(getPackage(plane));
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

    private boolean launchPlane(ItemStack stack, Level level, BlockPos pos, Vec3 launchPos, Vec3 motion) {
        MinecraftServer server = level.getServer();
        if (server == null)
            return false;

        ItemStack planeStack = createPlaneStackForLaunch(stack);
        CardboardPlaneEntity plane = new CardboardPlaneEntity(level);
        plane.setPlaneStack(planeStack);
        plane.setUnpack(isPreOpened(stack));
        plane.setPos(launchPos.x, launchPos.y, launchPos.z);
        if (motion.lengthSqr() > 1.0E-6) {
            float velocity = (float) motion.length();
            if (velocity > 0.0F) {
                plane.shoot(motion.x, motion.y, motion.z, velocity, 0.0F);
            } else {
                plane.setDeltaMovement(motion);
            }
        } else {
            float yaw = switch (level.getBlockState(pos).getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                case NORTH -> 180f;
                case SOUTH -> 0f;
                case WEST -> 90f;
                default -> -90f;
            };
            plane.shootFromRotation(-37.5F, yaw, 0.0F, 0.8F, 1.0F);
        }

        String fullAddress = getFullAddress(planeStack);
        String playerAddress = getAddress(planeStack);
        return trySpawnPlane(server, level, plane, fullAddress, playerAddress);
    }

    private static ItemStack createPlaneStackForLaunch(ItemStack stack) {
        ItemStack planeStack = stack.copy();
        planeStack.setCount(1);
        ItemStack packageItem = getPackage(planeStack);
        if (packageItem.isEmpty()) {
            packageItem = PackageStyles.getRandomBox();
            setPackage(planeStack, packageItem);
        }
        return planeStack;
    }

    private static boolean trySpawnPlane(MinecraftServer server, Level level, CardboardPlaneEntity plane, String fullAddress, String playerAddress) {
        ServerPlayer serverPlayer = null;
        if (ServerConfig.planePlayerTargets && !playerAddress.isBlank()) {
            serverPlayer = server.getPlayerList().getPlayerByName(playerAddress);
        }
        if (serverPlayer != null && ServerConfig.planePlayerTargets) {
            plane.setTarget(serverPlayer);
            level.addFreshEntity(plane);
            return true;
        }
        if (tryAssignLocationTarget(plane, fullAddress)
                || (!playerAddress.equals(fullAddress) && tryAssignLocationTarget(plane, playerAddress))) {
            level.addFreshEntity(plane);
            return true;
        }
        return false;
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
