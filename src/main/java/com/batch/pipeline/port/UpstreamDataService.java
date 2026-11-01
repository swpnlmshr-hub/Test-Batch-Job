package com.batch.pipeline.port;

/**
 * Upstream (external system) data prep / fetch boundary — separate from in-app
 * Flowable Data Object persistence on {@link BatchDataService}.
 * <p>
 * Implementations talk to the upstream open API, prefetch nightly inputs, and
 * store them locally so evidence gatherers do not call live upstream during the
 * entity loop. Do not put Flowable DO create/modify here.
 * </p>
 */
public interface UpstreamDataService {

    /**
     * Prefetch and store upstream data needed for this nightly run.
     */
    void prepareUpstream();
}
