package com.krei.cmpackagecouriers.plane;

import com.krei.cmpackagecouriers.PackageCouriers;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.depot.DepotBlock;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.UUID;

// NOTE: Maybe replace synced entity data with a normal field
// TODO: sync targetPos to increase entity update interval
// TODO: NonNull targetPos and targetPosLevel
// TODO: Use the Ender Pearl chunk loading mechanic
// NOTE: We could implement this by adding functionality to ejectors
// NOTE: We could implement this by using still entities and rendering ghosts
public class CardboardPlaneEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData
            .defineId(CardboardPlaneEntity.class, EntityDataSerializers.ITEM_STACK);

    @Nullable private UUID targetEntityUUID = null;
    @Nullable protected Entity targetEntityCached = null;
    @Nullable protected Vec3 targetPos = null;
    @Nullable protected ResourceKey<Level> targetPosLevel = null;
    protected double speed = 0.8;
    public float newDeltaYaw = 0;
    public float oldDeltaYaw = 0;
    public boolean unpack = false;

    public CardboardPlaneEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public CardboardPlaneEntity(Level level) {
        super(PackageCouriers.CARDBOARD_PLANE_ENTITY.get(), level);
    }

    // NOTE: Might require own implementation for noclip and to avoid unnecessary nbt data
    @Override
    public void tick() {
        super.tick();

        Vec3 vec3 = this.getDeltaMovement();
        double d0 = this.getX() + vec3.x;
        double d1 = this.getY() + vec3.y;
        double d2 = this.getZ() + vec3.z;
        this.updateRotation();
        this.setPos(d0, d1, d2);
        this.oldDeltaYaw = this.newDeltaYaw;
        this.newDeltaYaw = this.yRotO-this.getYRot();

        if (level().isClientSide()) {
            if (this.tickCount%3 == 0) {
                Vec3 lookAngle = this.getLookAngle().scale(0.5);
                this.level()
                        .addParticle(
                                ParticleTypes.FIREWORK,
                                this.getX() + lookAngle.x(),
                                this.getY() + lookAngle.y(),
                                this.getZ() + lookAngle.z(),
                                this.random.nextGaussian() * 0.05,
                                -this.getDeltaMovement().y * 0.5,
                                this.random.nextGaussian() * 0.05
                        );
            }
            return; // Don't do flight calculations on clientside
        }

        if (targetEntityUUID != null && targetEntityCached == null) {
            if (this.level() instanceof ServerLevel serverLevel) {
                targetEntityCached = serverLevel.getEntity(targetEntityUUID);
                PackageCouriers.LOGGER.debug("cachedEntity");
            }
        }
        if (targetEntityCached != null) {
            if (targetEntityCached instanceof LivingEntity)
                targetPos = targetEntityCached.getEyePosition();
            else
                targetPos = targetEntityCached.position();
            targetPosLevel = targetEntityCached.level().dimension();
        }
        if (targetPos == null) {
            remove(RemovalReason.DISCARDED);
            // TODO: Drop package, invalid state
            return;
        }

        if (targetPos.closerThan(this.position(), 40)
                && targetEntityCached instanceof Player player) {
            player.displayClientMessage(Component.translatable(PackageCouriers.MODID + ".message.inbound"), true);
        }

        if (targetPos.closerThan(this.position(), 1.5)) {
            onReachedTarget();
            remove(RemovalReason.DISCARDED);
            return;
        }

        Vec3 vecFrom = this.getDeltaMovement().normalize();
        Vec3 vecTo;
        if (targetPosLevel != level().dimension()) {  // Target not in the same dimension
            vecTo = this.getDeltaMovement().normalize();
        } else if (!targetPos.closerThan(this.position(), 80)) {  // Target is far, fly upwards in the general direction
            vecTo = targetPos.subtract(this.position());
            vecTo = new Vec3(vecTo.x(), vecTo.y() + vecTo.length()/2, vecTo.z()).normalize();
        } else {
            vecTo = targetPos.subtract(this.position()).normalize();
        }
        float augmentedDistance = (float)targetPos.subtract(this.position()).length() + Math.max(0, 80 - this.tickCount);
        float clampedDistance = Mth.clamp(augmentedDistance, 5, 60);
        float curveAmount = Mth.lerp((clampedDistance - 5f) / 55f, 0.4f, 0.06f);
        this.setDeltaMovement(vecFrom.lerp(vecTo, curveAmount).normalize().scale(this.speed));

        Vec3 posAhead = this.position().add(this.getDeltaMovement().normalize().scale(20));
        if (!isChunkTicking(level(), posAhead)
                || this.tickCount > 120) {

            if (level() instanceof ServerLevel serverLevel
                    && this.targetPosLevel != null) {
                ServerLevel tpLevel = serverLevel.getServer().getLevel(this.targetPosLevel);

                if (!targetPos.closerThan(this.position(), 80) || targetPosLevel != level().dimension()) {
                    Vec3 dirVec = this.position().subtract(targetPos);
                    dirVec = new Vec3(dirVec.x(), 0, dirVec.z()).normalize();
                    dirVec = new Vec3(dirVec.x(), 0.5, dirVec.z()).normalize();
                    Vec3 tpVec;
                    if (isChunkTicking(tpLevel, targetPos.add(dirVec.scale(70)))) {
                        tpVec = targetPos.add(dirVec.scale(70));
                    } else if (isChunkTicking(tpLevel, targetPos.add(dirVec.scale(50)))) {
                        tpVec = targetPos.add(dirVec.scale(50));
                    } else if (isChunkTicking(tpLevel, targetPos.add(dirVec.scale(30)))) {
                        tpVec = targetPos.add(dirVec.scale(30));
                    } else if (isChunkTicking(tpLevel, targetPos.add(dirVec.scale(10)))) {
                        tpVec = targetPos.add(dirVec.scale(10));
                    } else if (isChunkTicking(tpLevel, targetPos)) {
                        tpVec = targetPos;
                    } else {
//                        PackageCouriers.LOGGER.debug("Target Not Loaded");
                        remove(RemovalReason.DISCARDED);
                        return;
                    }

                    if (targetPosLevel != level().dimension()) {  // Target not in the same dimension
                        // NOTE: Maybe set the proper rotations?
                        teleportTo(tpLevel, tpVec.x(), tpVec.y(), tpVec.z(), Collections.emptySet(), this.getYRot(), this.getXRot());
//                        PackageCouriers.LOGGER.debug("TP: " + tpLevel + " " + tpVec);
                    } else {
                        teleportTo(tpVec.x(), tpVec.y(), tpVec.z());
//                        PackageCouriers.LOGGER.debug("TP: " + tpVec);
                    }
                    this.setDeltaMovement(targetPos.subtract(this.position()).normalize().scale(this.speed));
                }
            }
        }

        if (this.tickCount > 400) {
            if (level() instanceof ServerLevel serverLevel && this.targetPosLevel != null) {
                ServerLevel tpLevel = serverLevel.getServer().getLevel(this.targetPosLevel);
                teleportTo(tpLevel, targetPos.x(), targetPos.y(), targetPos.z(), Collections.emptySet(), this.getYRot(), this.getXRot());
                PackageCouriers.LOGGER.debug("Timeout: " + targetPos);
            } else {
                remove(RemovalReason.DISCARDED);  // Illegal State, break
            }
        }
//
//        if (this.tickCount%10 == 0) {
//            PackageCouriers.LOGGER.debug(this.targetPos+"");
//        }
    }

    protected void onReachedTarget() {
        if (targetEntityCached != null  // Assumes entity is cached
                && targetEntityCached instanceof Player player) {
            if (!level().isClientSide()) {
                if (unpack) {
                    ItemStackHandler stacks = PackageItem.getContents(this.getPackage());
                    for (int slot = 0; slot < stacks.getSlots(); slot++) {
                        ItemStack stack = stacks.getStackInSlot(slot);
                        player.getInventory().placeItemBackInInventory(stack);
                    }
                } else {
                    player.getInventory().placeItemBackInInventory(this.getPackage());
                }
            }
        } else if (targetPos != null) {
            if (!level().isClientSide()) {
                BlockPos blockPos = new BlockPos((int)Math.floor(this.targetPos.x()), (int)Math.floor(this.targetPos.y()), (int)Math.floor(this.targetPos.z()));
                if (level().getBlockState(blockPos).getBlock() instanceof DepotBlock
                        && level().getBlockEntity(blockPos) instanceof DepotBlockEntity depot
                        && depot.getHeldItem().is(Items.AIR)) {
                    depot.setHeldItem(this.getPackage());
                    depot.notifyUpdate();
                    //TODO: Belts and hoppers as targets
                } else {
                    level().addFreshEntity(PackageEntity.fromItemStack(level(), this.position(), this.getPackage()));
                }
            }
        }

        ParticleOptions particleOption = new ItemParticleOption(ParticleTypes.ITEM, AllItems.CARDBOARD.asStack());

        // TODO: particles spawned at client instead to avoid graphics latency
        // NOTE: figure out why Explosion.finalizeExplosion is invoked at client but Explosion.explode doesn't
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    particleOption,
                    this.position().x(),
                    this.position().y(),
                    this.position().z(),
                    20,
                    0.3, 0.3, 0.3,
                    0.1
            );
        }

        level().playSound(
                null,
                this.position().x(), this.position().y(), this.position().z(),
                SoundEvents.PLAYER_LEVELUP,
                SoundSource.NEUTRAL,
                1.0F,
                0.75F
        );
    }

    public void setTarget(@Nullable Entity targetEntity) {
        if (targetEntity != null) {
            this.targetEntityUUID = targetEntity.getUUID();
            this.targetEntityCached = targetEntity;
        }
    }

    public void setTarget(BlockPos targetBlock, Level level) {
        this.targetPos = Vec3.atCenterOf(targetBlock);
        this.targetPosLevel = level.dimension();
        this.targetEntityCached = null;
        this.targetEntityUUID = null;
    }

    public void shootFromRotation(float x, float y, float z, float velocity, float inaccuracy) {
        float f = -Mth.sin(y * (float) (Math.PI / 180.0)) * Mth.cos(x * (float) (Math.PI / 180.0));
        float f1 = -Mth.sin((x + z) * (float) (Math.PI / 180.0));
        float f2 = Mth.cos(y * (float) (Math.PI / 180.0)) * Mth.cos(x * (float) (Math.PI / 180.0));
        this.shoot(f, f1, f2, velocity, inaccuracy);
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected Item getDefaultItem() {
        return PackageCouriers.CARDBOARD_PLANE_ITEM.get();
    }

    @Override
    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        ItemStack box = ItemStack.of(compoundTag.getCompound("Box"));
        this.setPackage(box);

        if (compoundTag.hasUUID("TargetEntity")) {
            targetEntityUUID = compoundTag.getUUID("TargetEntity");
        } else if (compoundTag.contains("TargetPosX")) {
            double x = compoundTag.getDouble("TargetPosX");
            double y = compoundTag.getDouble("TargetPosY");
            double z = compoundTag.getDouble("TargetPosZ");
            targetPos = new Vec3(x, y, z);
            if (compoundTag.contains("TargetPosDim")) {
                ResourceLocation dimensionId = ResourceLocation.tryParse(compoundTag.getString("TargetPosDim"));
                if (dimensionId != null) {
                    targetPosLevel = ResourceKey.create(Registries.DIMENSION, dimensionId);
                }
            }
        } else {
            // Illegal state
        }

        if (compoundTag.contains("Unpack")) {
            unpack = compoundTag.getBoolean("Unpack");
        }

        if (targetPos != null && targetPosLevel == null) {
            targetPosLevel = level().dimension();
        }

        refreshDimensions();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        ItemStack box = this.getPackage();
        compoundTag.put("Box", box.save(new CompoundTag()));
        compoundTag.putBoolean("Unpack", unpack);

        if (targetEntityUUID != null) {
            compoundTag.putUUID("TargetEntity", targetEntityUUID);
        } else if (targetPos != null) {
            compoundTag.putDouble("TargetPosX", targetPos.x());
            compoundTag.putDouble("TargetPosY", targetPos.y());
            compoundTag.putDouble("TargetPosZ", targetPos.z());
            if (targetPosLevel != null) {
                compoundTag.putString("TargetPosDim", targetPosLevel.location().toString());
            }
        } else {
            // Illegal State
        }
    }

    public ItemStack getPackage() {
        return this.getEntityData().get(DATA_ITEM);
    }

    public void setPackage(ItemStack stack) {
        if (stack.getItem() instanceof PackageItem)
            this.getEntityData().set(DATA_ITEM, stack.copy());
    }

    public boolean isUnpack() {
        return unpack;
    }

    public void setUnpack(boolean unpack) {
        this.unpack = unpack;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public static boolean isChunkTicking(Level level, Vec3 pos) {
        if (level instanceof ServerLevel serverLevel) {
            BlockPos blockPos = new BlockPos((int) pos.x(),(int)  pos.y(),(int)  pos.z());
            return serverLevel.getChunkSource().chunkMap.getDistanceManager()
                    .inEntityTickingRange(ChunkPos.asLong(blockPos));
        }
        return false;
    }

    public static void init() {}
}
