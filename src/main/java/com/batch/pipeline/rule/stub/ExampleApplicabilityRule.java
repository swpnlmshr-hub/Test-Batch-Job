package com.batch.pipeline.rule.stub;

import com.batch.pipeline.model.Evidence;
import com.batch.pipeline.model.EvidenceItem;
import com.batch.pipeline.model.RuleEvaluationResult;
import com.batch.pipeline.model.RuleOutcome;
import com.batch.pipeline.model.RuleUsage;
import com.batch.pipeline.rule.ChildRule;

/**
 * Example applicability {@link ChildRule} stub so the scaffold compiles.
 * Returns {@link RuleOutcome#TRUE} when any evidence item is present.
 */
public final class ExampleApplicabilityRule implements ChildRule {

    public static final String RULE_CODE = "EXAMPLE_APPLICABILITY_RULE";

    @Override
    public String ruleCode() {
        return RULE_CODE;
    }

    @Override
    public RuleUsage ruleUsage() {
        return RuleUsage.APPLICABILITY;
    }

    @Override
    public RuleEvaluationResult evaluate(Evidence evidence) {
        EvidenceItem first = evidence.evidenceItems().isEmpty()
                ? null
                : evidence.evidenceItems().get(0);

        if (first == null) {
            return new RuleEvaluationResult(null, RULE_CODE, RuleOutcome.FALSE, "NO_EVIDENCE");
        }
        return new RuleEvaluationResult(first.id(), RULE_CODE, RuleOutcome.TRUE, null);
    }
}
