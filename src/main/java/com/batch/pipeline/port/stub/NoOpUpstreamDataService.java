package com.batch.pipeline.port.stub;

import com.batch.pipeline.port.UpstreamDataService;

/**
 * No-op {@link UpstreamDataService} facade for smoke wiring.
 * Prefer registering {@link ExampleUpstreamSource} (and more) in
 * {@link com.batch.pipeline.port.UpstreamSourceRegistry} for Job 1.
 */
public final class NoOpUpstreamDataService implements UpstreamDataService {

    @Override
    public void prepareUpstream() {
        // no-op
    }
}
