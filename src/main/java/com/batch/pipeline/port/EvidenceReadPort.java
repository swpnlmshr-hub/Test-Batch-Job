package com.batch.pipeline.port;

import com.batch.pipeline.model.EntityType;
import com.batch.pipeline.model.Evidence;

import java.util.List;

/**
 * Reads stored evidence rows for the evaluation job (no upstream refetch).
 */
public interface EvidenceReadPort {

    List<EvidenceRow> loadAllEvidence();

    /**
     * @param entityId     entity primary key
     * @param entityType   system or case
     * @param parentRuleId parent rule key
     * @param evidence     stored value JSON
     */
    record EvidenceRow(
            String entityId,
            EntityType entityType,
            String parentRuleId,
            Evidence evidence
    ) {
    }
}
