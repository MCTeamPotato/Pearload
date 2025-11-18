package me.kall.pearload.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.kall.pearload.Pearload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "updateChunkPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;removeEntity(Lnet/minecraft/world/entity/Entity;I)V"))
    private void beforeChunkUpdate(Entity entity, CallbackInfo ci) {
        if (Pearload.handleEntityForceLoadChange(new ChunkPos(entity.xChunk, entity.zChunk), entity.getUUID(), entity, false) && Pearload.debug()) {
            System.out.println("ChunkForced (beforeChunkUpdate) (false): " + new ChunkPos(entity.xChunk, entity.zChunk));
        }
    }

    @Inject(method = "updateChunkPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;addEntity(Lnet/minecraft/world/entity/Entity;)V"))
    private void afterChunkUpdate(Entity entity, CallbackInfo ci, @Local(ordinal = 0) int chunkX, @Local(ordinal = 2) int chunkZ) {
        if (Pearload.handleEntityForceLoadChange(new ChunkPos(chunkX, chunkZ), entity.getUUID(), entity, true) && Pearload.debug()) {
            System.out.println("ChunkForced (afterChunkUpdate) (true): " + new ChunkPos(chunkX, chunkZ));
        }
    }
}
