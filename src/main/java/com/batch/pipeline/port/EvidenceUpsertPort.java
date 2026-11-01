package com.batch.pipeline.port;

import com.batch.pipeline.model.EntityType;
import com.batch.pipeline.model.Evidence;

import java.util.List;

/**
 * Persistence boundary for evidence rows (entity + parent rule → value JSON).
 * Hides Flowable / JDBC details; prefer chunked transactional upsert on
 * unique {@code (entityId, parentRuleId)}.
 */
public interface EvidenceUpsertPort {

    /**
     * Upsert a batch of evidence payloads in one chunk/transaction.
     */
    void upsertChunk(List<EvidenceWrite> writes);

    /**
     * One evidence row to create-or-modify.
     *
     * @param entityId     entity primary key
     * @param entityType   system or case
     * @param parentRuleId parent rule / list key
     * @param evidence     value JSON object
     */
    record EvidenceWrite(
            String entityId,
            EntityType entityType,
            String parentRuleId,
            Evidence evidence
    ) {
    }
}
