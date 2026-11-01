package com.batch.pipeline.port;

/**
 * One pluggable upstream open-API source for Job 1 prep.
 * Each implementation typically writes a different local table / dataset.
 * Register another impl in {@link UpstreamSourceRegistry} to add a source.
 */
public interface UpstreamSource {

    /**
     * Stable key / name for this upstream (logging, uniqueness in the registry).
     */
    String getKey();

    /**
     * Fetch from this upstream and store locally for the nightly run.
     * Must not talk to Flowable DO persistence ({@link BatchDataService}).
     */
    void prepare();
}
