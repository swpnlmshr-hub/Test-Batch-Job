package com.batch.pipeline.port;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Ordered list of {@link UpstreamSource} plugins for Job 1.
 * Job 1 always iterates every entry; adding another upstream = implement and register.
 */
public final class UpstreamSourceRegistry {

    private final List<UpstreamSource> sources;

    public UpstreamSourceRegistry(Collection<UpstreamSource> sources) {
        Objects.requireNonNull(sources, "sources");
        List<UpstreamSource> copy = new ArrayList<>(sources.size());
        for (UpstreamSource source : sources) {
            copy.add(Objects.requireNonNull(source, "source"));
        }
        this.sources = Collections.unmodifiableList(copy);
    }

    public List<UpstreamSource> all() {
        return sources;
    }

    /**
     * Run {@link UpstreamSource#prepare()} on every registered source in order.
     */
    public void prepareAll() {
        for (UpstreamSource source : sources) {
            source.prepare();
        }
    }

    public int size() {
        return sources.size();
    }

    public static UpstreamSourceRegistry of(UpstreamSource... sources) {
        List<UpstreamSource> list = new ArrayList<>();
        Collections.addAll(list, sources);
        return new UpstreamSourceRegistry(list);
    }
}
