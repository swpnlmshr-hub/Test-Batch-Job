package com.batch.pipeline.port;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Ordered registry of {@link UpstreamSource} plugins for Job 1.
 * Adding another upstream = implement {@link UpstreamSource} and register it here.
 */
public final class UpstreamSourceRegistry {

    private final Map<String, UpstreamSource> sourcesByKey;

    public UpstreamSourceRegistry(Collection<UpstreamSource> sources) {
        Objects.requireNonNull(sources, "sources");
        Map<String, UpstreamSource> map = new LinkedHashMap<>();
        for (UpstreamSource source : sources) {
            Objects.requireNonNull(source, "source");
            String key = Objects.requireNonNull(source.getKey(), "source key");
            UpstreamSource previous = map.put(key, source);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate UpstreamSource key: " + key);
            }
        }
        this.sourcesByKey = Collections.unmodifiableMap(map);
    }

    public List<UpstreamSource> all() {
        return List.copyOf(sourcesByKey.values());
    }

    public Optional<UpstreamSource> find(String key) {
        return Optional.ofNullable(sourcesByKey.get(key));
    }

    /**
     * Run {@link UpstreamSource#prepare()} on every registered source in registration order.
     */
    public void prepareAll() {
        for (UpstreamSource source : sourcesByKey.values()) {
            source.prepare();
        }
    }

    public int size() {
        return sourcesByKey.size();
    }

    /**
     * Convenience factory preserving list order.
     */
    public static UpstreamSourceRegistry of(UpstreamSource... sources) {
        List<UpstreamSource> list = new ArrayList<>();
        Collections.addAll(list, sources);
        return new UpstreamSourceRegistry(list);
    }
}
