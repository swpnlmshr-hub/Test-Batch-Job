package com.batch.pipeline.source;

import com.batch.pipeline.model.Evidence;

import java.util.List;
import java.util.Map;

/**
 * Evidence gatherer bound to one parent rule key.
 * Implementations build evidence JSON only; they do not persist or run rules.
 */
public interface Source {

    /**
     * Parent-rule key this source applies to (registry lookup key).
     */
    String getKey();

    /**
     * Declares which source tables/attributes this gatherer needs.
     *
     * @return map of table name → attribute (column) names to fetch
     */
    Map<String, List<String>> getSourceAttributesMap();

    /**
     * Gather parent evidence for the given entity.
     * Callers are expected to have already stage-gated; stage-fail paths are owned by the orchestrator.
     *
     * @param entityId entity primary key
     * @return full parent {@link Evidence} value object
     */
    Evidence fetchEvidence(String entityId);
}
