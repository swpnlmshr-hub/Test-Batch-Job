package com.batch.pipeline.model;

/**
 * Child / subrule definition referenced from evidence and mapping tables.
 *
 * @param ruleCode    stable rule identifier
 * @param ruleType    optional classifier (may be ignored for now)
 * @param ruleVersion rule version string
 */
public record Rule(
        String ruleCode,
        String ruleType,
        String ruleVersion
) {
}
