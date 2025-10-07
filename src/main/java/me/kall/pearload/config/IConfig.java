package me.kall.pearload.config;

import java.util.Collections;
import java.util.Set;

public interface IConfig {
    default Set<String> getForceLoaders() {
        return Collections.emptySet();
    }

    default boolean projectile() {
        return false;
    }

    default boolean debug() {
        return false;
    }
}
