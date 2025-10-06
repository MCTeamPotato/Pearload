package me.kall.pearload.api;

import me.kall.pearload.Pearload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import org.jetbrains.annotations.NotNull;

public interface ForceLoader {
    boolean pearload$isForceLoader();
    void pearload$setAsForceLoader(boolean isForceLoader);

    static void setAsForceLoader(EntityType<?> type) {
        ((ForceLoader)type).pearload$setAsForceLoader(true);
    }

    static boolean isForceLoader(@NotNull Entity entity) {
        return ((ForceLoader)entity.getType()).pearload$isForceLoader() || (Pearload.projectile() && entity instanceof Projectile);
    }
}
