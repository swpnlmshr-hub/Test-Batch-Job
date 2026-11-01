package com.batch.pipeline.source;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Maps parent-rule key ({@link Source#getKey()}) → {@link Source} implementation.
 */
public final class SourceRegistry {

    private final Map<String, Source> sourcesByKey;

    public SourceRegistry(Collection<Source> sources) {
        Objects.requireNonNull(sources, "sources");
        Map<String, Source> map = new LinkedHashMap<>();
        for (Source source : sources) {
            Objects.requireNonNull(source, "source");
            String key = Objects.requireNonNull(source.getKey(), "source key");
            Source previous = map.put(key, source);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate Source key: " + key);
            }
        }
        this.sourcesByKey = Collections.unmodifiableMap(map);
    }

    public Optional<Source> find(String parentRuleKey) {
        return Optional.ofNullable(sourcesByKey.get(parentRuleKey));
    }

    public Source require(String parentRuleKey) {
        return find(parentRuleKey)
                .orElseThrow(() -> new IllegalArgumentException("No Source registered for key: " + parentRuleKey));
    }

    public Map<String, Source> asMap() {
        return sourcesByKey;
    }
}
