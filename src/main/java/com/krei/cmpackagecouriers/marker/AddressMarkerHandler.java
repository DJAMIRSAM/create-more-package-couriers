package com.krei.cmpackagecouriers.marker;

import com.krei.cmpackagecouriers.PackageCouriers;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber(modid = PackageCouriers.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AddressMarkerHandler {

    public static Map<MarkerTarget, MarkerTarget> markerMap = new HashMap<>();

    public static void addOrUpdateTarget(Level level, BlockPos pos, String address) {
        MarkerTarget marker = new MarkerTarget(level, pos, address);
        if (markerMap.containsKey(marker)) {
            marker = markerMap.get(marker);
            marker.resetTimeout();
        } else {
            markerMap.put(marker, marker);
        }
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        Iterator<Map.Entry<MarkerTarget, MarkerTarget>> iterator = markerMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<MarkerTarget, MarkerTarget> entry = iterator.next();
            MarkerTarget marker = entry.getValue();

            if (marker.tickAndCheckTimeout()) {
                iterator.remove();
            }
        }
    }

    @Nullable
    public static MarkerTarget getMarkerTarget(String address) {
        for (MarkerTarget marker : markerMap.values()) {
            if (PackageItem.matchAddress(address, marker.address)) {
                return marker;
            }
        }
        return null;
    }

    public static class MarkerTarget {
        public static final int TIMEOUT_TICKS = 20;
        public final BlockPos pos;
        public final Level level;
        public final String address;
        private int timeout = TIMEOUT_TICKS;

        public MarkerTarget(@Nonnull Level level, @Nonnull  BlockPos pos, @Nonnull  String address) {
            this.level = level;
            this.pos = pos;
            this.address = address;
        }

        public boolean tickAndCheckTimeout() {
            this.timeout--;
            return this.timeout < 0;
        }

        public void resetTimeout() {
            this.timeout = TIMEOUT_TICKS;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof MarkerTarget other)) return false;

            return Objects.equals(pos, other.pos) &&
                    Objects.equals(level.dimension(), other.level.dimension()) &&
                    Objects.equals(address, other.address);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos, level.dimension(), address);
        }

        @Override
        public String toString() {
            return "(" + this.address + ": " + this.pos + ")";
        }
    }
}
