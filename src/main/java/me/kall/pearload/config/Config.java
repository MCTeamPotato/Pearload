package me.kall.pearload.config;

import com.google.common.collect.Lists;
import me.kall.jsonate.api.JsonConfig;
import me.kall.pearload.Pearload;

import java.util.Set;

public class Config implements IConfig {
    private final JsonConfig config = JsonConfig.create(Pearload.MOD_ID, "1")
            .put("ForceLoaders", Lists.newArrayList("minecraft:ender_pearl"))
            .put("InitializeAllTheProjectilesAsChunkLoader", false)
            .initialize();

    private final Set<String> forceLoaders = config.getSet("ForceLoaders", String.class);
    private final boolean projectile = config.getBoolean("InitializeAllTheProjectilesAsChunkLoader");

    @Override
    public Set<String> getForceLoaders() {
        return this.forceLoaders;
    }

    @Override
    public boolean projectile() {
        return this.projectile;
    }
}
