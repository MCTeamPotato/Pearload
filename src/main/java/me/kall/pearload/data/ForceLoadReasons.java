package me.kall.pearload.data;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class ForceLoadReasons extends SavedData {
    private static final String ID = "pearload_force_load_reasons";

    private final Object2ObjectMap<ResourceLocation, Long2ObjectMap<Set<UUID>>> data = new Object2ObjectOpenHashMap<>();

    public boolean add(@NotNull ResourceLocation dimId, long chunkPos, @NotNull UUID uuid) {
        Long2ObjectMap<Set<UUID>> dimMap = data.computeIfAbsent(dimId, k -> new Long2ObjectOpenHashMap<>());
        Set<UUID> uuids = dimMap.computeIfAbsent(chunkPos, k -> new ObjectOpenHashSet<>());
        boolean changed = uuids.add(uuid);
        if (changed) setDirty();
        return changed;
    }

    public boolean remove(@NotNull ResourceLocation dimId, long chunkPos, @NotNull UUID uuid) {
        Long2ObjectMap<Set<UUID>> dimMap = data.get(dimId);
        if (dimMap == null) return true;
        Set<UUID> uuids = dimMap.get(chunkPos);
        if (uuids == null) return true;

        uuids.remove(uuid);
        if (uuids.isEmpty()) {
            dimMap.remove(chunkPos);
            if (dimMap.isEmpty()) data.remove(dimId);
            setDirty();
            return true;
        }
        setDirty();
        return false;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag dimensions = new ListTag();

        for (var dimEntry : data.object2ObjectEntrySet()) {
            CompoundTag dimTag = new CompoundTag();
            dimTag.putString("dim", dimEntry.getKey().toString());

            ListTag chunkList = new ListTag();
            for (var chunkEntry : dimEntry.getValue().long2ObjectEntrySet()) {
                CompoundTag chunkTag = new CompoundTag();
                chunkTag.putLong("pos", chunkEntry.getLongKey());

                ListTag uuidList = new ListTag();
                for (UUID uuid : chunkEntry.getValue()) {
                    uuidList.add(StringTag.valueOf(uuid.toString()));
                }
                chunkTag.put("uuids", uuidList);
                chunkList.add(chunkTag);
            }

            dimTag.put("chunks", chunkList);
            dimensions.add(dimTag);
        }

        tag.put("dimensions", dimensions);
        return tag;
    }

    public static @NotNull ForceLoadReasons load(@NotNull CompoundTag tag) {
        ForceLoadReasons reasons = new ForceLoadReasons();

        ListTag dimensions = tag.getList("dimensions", Tag.TAG_COMPOUND);
        for (Tag dimTag0 : dimensions) {
            CompoundTag dimTag = (CompoundTag) dimTag0;
            ResourceLocation dimId = ResourceLocation.parse(dimTag.getString("dim"));
            Long2ObjectMap<Set<UUID>> dimMap = new Long2ObjectOpenHashMap<>();

            ListTag chunkList = dimTag.getList("chunks", Tag.TAG_COMPOUND);
            for (Tag chunkTag0 : chunkList) {
                CompoundTag chunkTag = (CompoundTag) chunkTag0;
                long pos = chunkTag.getLong("pos");
                Set<UUID> uuids = new ObjectOpenHashSet<>();
                ListTag uuidList = chunkTag.getList("uuids", Tag.TAG_STRING);
                for (Tag uuidTag : uuidList) {
                    uuids.add(UUID.fromString(uuidTag.getAsString()));
                }
                dimMap.put(pos, uuids);
            }

            reasons.data.put(dimId, dimMap);
        }

        return reasons;
    }


    public static @NotNull ForceLoadReasons get(@NotNull ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ForceLoadReasons::load, ForceLoadReasons::new, ID);
    }
}
