package com.batch.pipeline.port;

import java.util.Objects;

/**
 * {@link UpstreamDataService} backed by {@link UpstreamSourceRegistry}.
 * {@link #prepareUpstream()} runs every registered {@link UpstreamSource#prepare()}.
 */
public final class RegistryUpstreamDataService implements UpstreamDataService {

    private final UpstreamSourceRegistry registry;

    public RegistryUpstreamDataService(UpstreamSourceRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public void prepareUpstream() {
        registry.prepareAll();
    }
}
