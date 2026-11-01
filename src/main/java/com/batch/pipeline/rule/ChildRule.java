package com.batch.pipeline.rule;

import com.batch.pipeline.model.Evidence;
import com.batch.pipeline.model.RuleEvaluationResult;
import com.batch.pipeline.model.RuleUsage;

/**
 * One child / subrule implementation (applicability or evaluation).
 * Prefer passing stored {@link Evidence} into {@link #evaluate(Evidence)} rather than adding fetch methods.
 */
public interface ChildRule {

    String ruleCode();

    RuleUsage ruleUsage();

    /**
     * Evaluate this rule against stored parent evidence.
     */
    RuleEvaluationResult evaluate(Evidence evidence);
}
