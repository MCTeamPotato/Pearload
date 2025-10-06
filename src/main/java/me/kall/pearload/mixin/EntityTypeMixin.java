package me.kall.pearload.mixin;

import me.kall.pearload.api.ForceLoader;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin implements ForceLoader {
    @Unique private boolean pearload$isForceLoader;

    @Override
    public boolean pearload$isForceLoader() {
        return this.pearload$isForceLoader;
    }

    @Override
    public void pearload$setAsForceLoader(boolean isForceLoader) {
        this.pearload$isForceLoader = isForceLoader;
    }
}
