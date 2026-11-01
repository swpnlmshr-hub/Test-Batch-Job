package com.batch.pipeline.model;

import java.util.Map;

/**
 * One gathered source-table row used later for applicability/evaluation.
 *
 * @param id              primary key of the source table row
 * @param tableName       which table the values come from
 * @param tableAttributes column → value map needed to evaluate rules
 */
public record EvidenceItem(
        String id,
        String tableName,
        Map<String, Object> tableAttributes
) {
}
