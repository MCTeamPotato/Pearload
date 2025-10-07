package me.kall.pearload;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.kall.pearload.api.ForceLoader;
import me.kall.pearload.config.Config;
import me.kall.pearload.config.IConfig;
import me.kall.pearload.data.ForceLoadReasons;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod(Pearload.MOD_ID)
public final class Pearload {
    public static final String MOD_ID = "pearload";

    public static final @Nullable IConfig CONFIG = FMLLoader.getLoadingModList().getModFileById("jsonate") == null ? null : new Config();

    public Pearload(IEventBus modBus, Dist dist, ModContainer container) {
        modBus.addListener(this::setup);
        NeoForge.EVENT_BUS.addListener(this::onEntityLeave);
        NeoForge.EVENT_BUS.addListener(this::onEntityJoin);
    }

    public static boolean projectile() {
        if (CONFIG == null) return false;
        return CONFIG.projectile();
    }

    public void onEntityJoin(@NotNull EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        handleEntityForceLoadChange(entity.chunkPosition(), entity.getUUID(), entity, true);
    }

    public void onEntityLeave(@NotNull EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        handleEntityForceLoadChange(entity.chunkPosition(), entity.getUUID(), entity, false);
    }

    public void setup(FMLCommonSetupEvent event) {
        if (CONFIG != null) {
            Set<ResourceLocation> registryNames = new ObjectOpenHashSet<>();
            Set<String> modIDs = new ObjectOpenHashSet<>();
            for (String forceLoader : CONFIG.getForceLoaders()) {
                if (forceLoader.contains(":")) {
                    registryNames.add(ResourceLocation.parse(forceLoader));
                } else {
                    modIDs.add(forceLoader);
                }
            }
            for (Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
                ResourceLocation id = entry.getKey().location();
                EntityType<?> type = entry.getValue();
                if (registryNames.contains(id) || modIDs.contains(id.getNamespace())) {
                    ForceLoader.setAsForceLoader(type);
                }
            }
        } else {
            ForceLoader.setAsForceLoader(EntityType.ENDER_PEARL);
        }
    }

    public static void handleEntityForceLoadChange(ChunkPos pos, UUID uuid, @NotNull Entity entity, boolean add) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        if (!ForceLoader.isForceLoader(entity)) return;

        ResourceLocation dim = level.dimension().location();

        level.getServer().execute(() -> {
            ForceLoadReasons reasons = ForceLoadReasons.get(level);
            boolean changed = add ? reasons.add(dim, pos.toLong(), uuid) : reasons.remove(dim, pos.toLong(), uuid);
            if (changed) level.setChunkForced(pos.x, pos.z, add);
        });
    }
}
