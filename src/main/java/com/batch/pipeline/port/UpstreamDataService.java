package com.batch.pipeline.port;

/**
 * Facade for upstream prep when a single entry point is preferred.
 * Prefer registering pluggable {@link UpstreamSource}s in
 * {@link UpstreamSourceRegistry}; {@link RegistryUpstreamDataService} implements
 * this by running every registered source. Job 1 typically iterates the registry
 * directly. Do not put Flowable DO create/modify here.
 */
public interface UpstreamDataService {

    /**
     * Prefetch and store upstream data needed for this nightly run
     * (all registered upstream sources when using {@link RegistryUpstreamDataService}).
     */
    void prepareUpstream();
}
