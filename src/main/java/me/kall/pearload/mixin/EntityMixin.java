package me.kall.pearload.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.kall.pearload.Pearload;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "setPosRaw", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ChunkPos;<init>(Lnet/minecraft/core/BlockPos;)V"))
    protected void beforeChunkUpdate(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (Pearload.handleEntityForceLoadChange(entity.chunkPosition(), entity.getUUID(), entity, false) && Pearload.debug()) System.out.println("ChunkForced (beforeChunkUpdate) (false): " + entity.chunkPosition());
    }

    @WrapMethod(method = "setPosRaw")
    protected void afterChunkUpdate(double x, double y, double z, Operation<Void> original) {
        Entity entity = (Entity) (Object) this;
        long before = entity.chunkPosition().toLong();
        original.call(x, y, z);
        long after = entity.chunkPosition().toLong();
        if (before == after) return;
        if (Pearload.handleEntityForceLoadChange(entity.chunkPosition(), entity.getUUID(), entity, true) && Pearload.debug()) System.out.println("ChunkForced (afterChunkUpdate) (true): " + entity.chunkPosition());
    }
}