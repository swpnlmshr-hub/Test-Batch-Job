package com.batch.pipeline.port;

/**
 * Upstream data-prep hook used at the start of the evidence-gathering job.
 * Implementations fetch upstream data and persist it locally so gatherers
 * read local copies rather than live upstream during the entity loop.
 */
public interface UpstreamPrepPort {

    /**
     * Prefetch and store upstream data needed for this nightly run.
     */
    void prepare();
}
