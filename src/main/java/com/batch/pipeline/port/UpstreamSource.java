package com.batch.pipeline.port;

/**
 * One pluggable upstream open-API source for Job 1 prep.
 * Each implementation typically writes a different local table / dataset.
 * Job 1 always runs every registered source — register another impl in
 * {@link UpstreamSourceRegistry} to add one. Do not put Flowable DO work here.
 */
public interface UpstreamSource {

    /**
     * Fetch from this upstream and store locally for the nightly run.
     */
    void prepare();
}
