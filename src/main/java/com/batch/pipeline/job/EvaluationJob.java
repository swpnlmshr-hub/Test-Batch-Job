package com.batch.pipeline.job;

import com.batch.pipeline.model.ApplicabilityAggregate;
import com.batch.pipeline.model.EvaluationAggregate;
import com.batch.pipeline.model.Evidence;
import com.batch.pipeline.model.ParentEvaluationOutcome;
import com.batch.pipeline.model.Rule;
import com.batch.pipeline.model.RuleEvaluationResult;
import com.batch.pipeline.model.RuleOutcome;
import com.batch.pipeline.model.RuleUsage;
import com.batch.pipeline.port.BatchDataService;
import com.batch.pipeline.port.BatchDataService.EvidenceRow;
import com.batch.pipeline.port.BatchDataService.FlagsWrite;
import com.batch.pipeline.rule.ChildRule;
import com.batch.pipeline.rule.ChildRuleRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Job 2 — Rule evaluation (stub orchestrator skeleton).
 * <p>
 * Reads stored evidence; for each parent runs applicability rules first, then
 * evaluation rules only when aggregate is {@link ApplicabilityAggregate#APPLICABLE}
 * (short-circuit otherwise). Writes flags via {@link BatchDataService}.
 * Does not refetch entities from upstream.
 * </p>
 */
public final class EvaluationJob {

    private final BatchDataService dataService;
    private final ChildRuleRegistry childRuleRegistry;
    private final int chunkSize;

    public EvaluationJob(
            BatchDataService dataService,
            ChildRuleRegistry childRuleRegistry,
            int chunkSize
    ) {
        this.dataService = Objects.requireNonNull(dataService, "dataService");
        this.childRuleRegistry = Objects.requireNonNull(childRuleRegistry, "childRuleRegistry");
        if (chunkSize < 1) {
            throw new IllegalArgumentException("chunkSize must be >= 1");
        }
        this.chunkSize = chunkSize;
    }

    public void run() {
        List<EvidenceRow> rows = dataService.loadAllEvidence();

        // Group parent outcomes by entity for flags upsert shape.
        Map<String, List<ParentEvaluationOutcome>> byEntity = new LinkedHashMap<>();
        for (EvidenceRow row : rows) {
            ParentEvaluationOutcome parentOutcome = evaluateParent(row.parentRuleId(), row.evidence());
            byEntity.computeIfAbsent(row.entityId(), id -> new ArrayList<>()).add(parentOutcome);
        }

        List<FlagsWrite> buffer = new ArrayList<>();
        for (Map.Entry<String, List<ParentEvaluationOutcome>> entry : byEntity.entrySet()) {
            buffer.add(new FlagsWrite(entry.getKey(), List.copyOf(entry.getValue())));
            if (buffer.size() >= chunkSize) {
                flush(buffer);
            }
        }
        if (!buffer.isEmpty()) {
            flush(buffer);
        }
    }

    private ParentEvaluationOutcome evaluateParent(String parentRuleId, Evidence evidence) {
        List<RuleEvaluationResult> applicabilityResults = new ArrayList<>();
        List<RuleEvaluationResult> evaluationResults = new ArrayList<>();

        // Stage-fail evidence: treat parent as notApplicable without running rules.
        if (!evidence.isApplicable()) {
            return new ParentEvaluationOutcome(
                    parentRuleId,
                    ApplicabilityAggregate.NOT_APPLICABLE,
                    null,
                    List.of(),
                    List.of()
            );
        }

        for (Rule mapped : evidence.evidenceRules()) {
            ChildRule child = childRuleRegistry.find(mapped.ruleCode()).orElse(null);
            if (child == null || child.ruleUsage() != RuleUsage.APPLICABILITY) {
                continue;
            }
            applicabilityResults.add(child.evaluate(evidence));
        }

        ApplicabilityAggregate applicability = rollupApplicability(applicabilityResults);
        EvaluationAggregate evaluation = null;

        if (applicability == ApplicabilityAggregate.APPLICABLE) {
            for (Rule mapped : evidence.evidenceRules()) {
                ChildRule child = childRuleRegistry.find(mapped.ruleCode()).orElse(null);
                if (child == null || child.ruleUsage() != RuleUsage.EVALUATION) {
                    continue;
                }
                evaluationResults.add(child.evaluate(evidence));
            }
            evaluation = rollupEvaluation(evaluationResults);
        }
        // Short-circuit: notApplicable → do not run evaluation rules.

        return new ParentEvaluationOutcome(
                parentRuleId,
                applicability,
                evaluation,
                List.copyOf(applicabilityResults),
                List.copyOf(evaluationResults)
        );
    }

    private static ApplicabilityAggregate rollupApplicability(List<RuleEvaluationResult> results) {
        for (RuleEvaluationResult result : results) {
            if (result.outcome() != RuleOutcome.TRUE) {
                return ApplicabilityAggregate.NOT_APPLICABLE;
            }
        }
        return ApplicabilityAggregate.APPLICABLE;
    }

    private static EvaluationAggregate rollupEvaluation(List<RuleEvaluationResult> results) {
        for (RuleEvaluationResult result : results) {
            if (result.outcome() != RuleOutcome.PASS) {
                return EvaluationAggregate.INCOMPLETE;
            }
        }
        return EvaluationAggregate.COMPLETE;
    }

    private void flush(List<FlagsWrite> buffer) {
        dataService.upsertFlagsChunk(List.copyOf(buffer));
        buffer.clear();
    }
}
