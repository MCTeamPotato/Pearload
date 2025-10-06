package me.kall.pearload.mixin;

import me.kall.pearload.api.ForceLoader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "setPosRaw", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;<init>(Lnet/minecraft/core/BlockPos;)V"))
    protected void beforeChunkUpdate(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity.level instanceof ServerLevel && ForceLoader.isForceLoader(entity)) {
            ChunkPos pos = entity.chunkPosition();
            ((ServerLevel) entity.level).setChunkForced(pos.x, pos.z, false);
        }
    }

    @Inject(method = "setPosRaw", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;<init>(Lnet/minecraft/core/BlockPos;)V", shift = At.Shift.AFTER))
    protected void afterChunkUpdate(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity.level instanceof ServerLevel && ForceLoader.isForceLoader(entity)) {
            ChunkPos pos = entity.chunkPosition();
            ((ServerLevel) entity.level).setChunkForced(pos.x, pos.z, true);
        }
    }
}