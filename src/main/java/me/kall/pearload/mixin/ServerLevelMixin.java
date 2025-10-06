package me.kall.pearload.mixin;

import me.kall.pearload.api.ForceLoader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Shadow public abstract boolean setChunkForced(int chunkX, int chunkZ, boolean add);

    @Inject(method = "updateChunkPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;removeEntity(Lnet/minecraft/world/entity/Entity;I)V"))
    private void beforeChunkUpdate(Entity entity, CallbackInfo ci) {
        if (ForceLoader.isForceLoader(entity)) this.setChunkForced(entity.xChunk, entity.zChunk, false);
    }

    @Inject(method = "updateChunkPos", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;addEntity(Lnet/minecraft/world/entity/Entity;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void afterChunkUpdate(Entity entity, CallbackInfo ci, int chunkX, int y, int chunkZ) {
        if (ForceLoader.isForceLoader(entity)) this.setChunkForced(chunkX, chunkZ, true);
    }
}
