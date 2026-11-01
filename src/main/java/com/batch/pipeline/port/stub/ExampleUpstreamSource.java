package com.batch.pipeline.port.stub;

import com.batch.pipeline.port.UpstreamSource;

/**
 * Example / stub {@link UpstreamSource}. Register additional impls alongside this
 * in {@link com.batch.pipeline.port.UpstreamSourceRegistry} to fetch more tables.
 */
public final class ExampleUpstreamSource implements UpstreamSource {

    public static final String KEY = "example-upstream";

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void prepare() {
        // stub: real impl fetches from upstream open API and writes a local table
    }
}
