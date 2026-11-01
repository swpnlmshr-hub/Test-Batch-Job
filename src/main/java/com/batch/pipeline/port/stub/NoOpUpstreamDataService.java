package com.batch.pipeline.port.stub;

import com.batch.pipeline.port.UpstreamDataService;

/**
 * No-op {@link UpstreamDataService} for smoke wiring without the upstream open API.
 */
public final class NoOpUpstreamDataService implements UpstreamDataService {

    @Override
    public void prepareUpstream() {
        // no-op: real impl fetches from upstream open API and stores locally
    }
}
