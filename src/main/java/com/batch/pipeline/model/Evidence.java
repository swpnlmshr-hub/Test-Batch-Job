package com.batch.pipeline.model;

import java.time.Instant;
import java.util.List;

/**
 * Parent evidence value JSON stored per entity + parent rule.
 *
 * @param isApplicable           {@code false} when the stage gate fails
 * @param sourceDataUpdatedAt    when evidence/source data was updated
 * @param evidenceRules          mapped child rules (always populated on write)
 * @param evidenceItems          gathered source rows (empty when stage gate fails)
 */
public record Evidence(
        boolean isApplicable,
        Instant sourceDataUpdatedAt,
        List<Rule> evidenceRules,
        List<EvidenceItem> evidenceItems
) {
}
