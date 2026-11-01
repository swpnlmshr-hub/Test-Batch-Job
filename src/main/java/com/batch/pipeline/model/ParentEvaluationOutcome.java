package com.batch.pipeline.model;

import java.util.List;

/**
 * Parent-level rollup used to build flags value JSON for one parent rule on an entity.
 *
 * @param parentRuleKey            parent list / parent-rule key
 * @param applicabilityRuleOutcome aggregate applicability
 * @param evaluationRuleOutcome    aggregate evaluation ({@code null} when short-circuited)
 * @param applicabilityResults     per-rule applicability outcomes
 * @param evaluationResults        per-rule evaluation outcomes (empty when notApplicable)
 */
public record ParentEvaluationOutcome(
        String parentRuleKey,
        ApplicabilityAggregate applicabilityRuleOutcome,
        EvaluationAggregate evaluationRuleOutcome,
        List<RuleEvaluationResult> applicabilityResults,
        List<RuleEvaluationResult> evaluationResults
) {
}
