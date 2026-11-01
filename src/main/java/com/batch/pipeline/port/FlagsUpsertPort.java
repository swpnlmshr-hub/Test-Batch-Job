package com.batch.pipeline.port;

import com.batch.pipeline.model.ParentEvaluationOutcome;

import java.util.List;

/**
 * Persistence boundary for flags (entity → value JSON with parent-level outcomes).
 * Hides Flowable / JDBC details behind this port.
 */
public interface FlagsUpsertPort {

    /**
     * Upsert flags for a chunk of entities.
     */
    void upsertChunk(List<FlagsWrite> writes);

    /**
     * Flags payload for one entity (all parent outcomes for that entity).
     *
     * @param entityId entity primary key
     * @param parents  parent-level rollups that form the flags value JSON
     */
    record FlagsWrite(
            String entityId,
            List<ParentEvaluationOutcome> parents
    ) {
    }
}
