package com.batch.pipeline.model;

/**
 * Per-rule outcome written into flags value JSON.
 *
 * @param evidenceId  primary key of the evidence item (when applicable)
 * @param ruleCode    child rule code
 * @param outcome     applicability {@code TRUE}/{@code FALSE} or evaluation {@code PASS}/{@code FAIL}
 * @param failureCode required when outcome is {@code FALSE} or {@code FAIL}
 */
public record RuleEvaluationResult(
        String evidenceId,
        String ruleCode,
        RuleOutcome outcome,
        String failureCode
) {
}
