package com.batch.pipeline.port.flowable;

import lombok.experimental.UtilityClass;

/**
 * Concrete Flowable Data Object definition keys for tables in the project design.
 * Values match the placeholder keys from project context.
 */
@UtilityClass
public class FlowableDataObjectKeys {

    /** Parent rules / rule list headers (includes applicable-stage column). */
    public final String RULE_LIST_DO = "ruleListDO";

    /** Child / subrule definitions (applicability and evaluation kinds). */
    public final String RULES_DO = "rulesDO";

    /** Maps rule-list rows to child rules/subrules. */
    public final String RULE_MAPPING_DO = "ruleMappingDO";

    /**
     * Evidence table — per entity + parent rule; value JSON with gathered evidence.
     * Identity: {@code entityId} + {@code parentRuleId}; also stores {@code entityType}.
     */
    public final String EVIDENCE_DO = "evidenceDO";

    /**
     * Flags / result table — per entity; value JSON with parent-level and per-rule outcomes.
     * Identity: {@code entityId}.
     */
    public final String FLAGS_DO = "flagsDO";

    /** Failure codes lookup — code / name / description for failed rule outcomes. */
    public final String FAILURE_CODE_DO = "failureCodeDO";
}
