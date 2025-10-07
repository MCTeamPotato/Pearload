package me.kall.pearload;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.kall.pearload.api.ForceLoader;
import me.kall.pearload.config.Config;
import me.kall.pearload.config.IConfig;
import me.kall.pearload.data.ForceLoadReasons;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod(Pearload.MOD_ID)
public final class Pearload {
    public static final String MOD_ID = "pearload";

    public static final @Nullable IConfig CONFIG = FMLLoader.getLoadingModList().getModFileById("jsonate") == null ? null : new Config();

    public Pearload(@NotNull FMLJavaModLoadingContext context) {
        context.getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.addListener(this::onEntityLeave);
        MinecraftForge.EVENT_BUS.addListener(this::onEntityJoin);
    }

    public static boolean projectile() {
        return CONFIG != null && CONFIG.projectile();
    }

    public static boolean debug() {
        return CONFIG != null && CONFIG.debug();
    }

    public void onEntityJoin(@NotNull EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (handleEntityForceLoadChange(entity.chunkPosition(), entity.getUUID(), entity, true) && debug()) System.out.println("ChunkForced (EntityJoinLevelEvent) (true): " + entity.chunkPosition());
    }

    public void onEntityLeave(@NotNull EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        if (handleEntityForceLoadChange(entity.chunkPosition(), entity.getUUID(), entity, false) && debug()) System.out.println("ChunkForced (EntityLeaveLevelEvent) (false): " + entity.chunkPosition());
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
            for (Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : ForgeRegistries.ENTITY_TYPES.getEntries()) {
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

    public static boolean handleEntityForceLoadChange(ChunkPos pos, UUID uuid, @NotNull Entity entity, boolean add) {
        if (!(entity.level() instanceof ServerLevel level)) return false;
        if (!ForceLoader.isForceLoader(entity)) return false;

        ResourceLocation dim = level.dimension().location();
        ForceLoadReasons reasons = ForceLoadReasons.get(level);

        boolean anyChanged = false;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int chunkX = pos.x + dx;
                int chunkZ = pos.z + dz;
                long chunkKey = ChunkPos.asLong(chunkX, chunkZ);

                boolean changed = add ? reasons.add(dim, chunkKey, uuid) : reasons.remove(dim, chunkKey, uuid);
                if (changed) {
                    level.setChunkForced(chunkX, chunkZ, add);
                    anyChanged = true;
                }
            }
        }

        return anyChanged;
    }
}
