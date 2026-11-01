package com.batch.pipeline.source.stub;

import com.batch.pipeline.model.Evidence;
import com.batch.pipeline.model.EvidenceItem;
import com.batch.pipeline.model.Rule;
import com.batch.pipeline.source.Source;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Example {@link Source} stub so the scaffold compiles.
 * Parent key is illustrative only — replace with a real parent-rule key and gatherer.
 */
public final class ExampleCaseSource implements Source {

    public static final String PARENT_RULE_KEY = "EXAMPLE_CASE_PARENT";

    @Override
    public String getKey() {
        return PARENT_RULE_KEY;
    }

    @Override
    public Map<String, List<String>> getSourceAttributesMap() {
        return Map.of(
                "example_case_table",
                List.of("id", "status", "opened_at")
        );
    }

    @Override
    public Evidence fetchEvidence(String entityId) {
        EvidenceItem item = new EvidenceItem(
                entityId,
                "example_case_table",
                Map.of(
                        "id", entityId,
                        "status", "OPEN",
                        "opened_at", Instant.EPOCH.toString()
                )
        );

        List<Rule> evidenceRules = List.of(
                new Rule("EXAMPLE_APPLICABILITY_RULE", null, "1"),
                new Rule("EXAMPLE_EVALUATION_RULE", null, "1")
        );

        return new Evidence(true, Instant.now(), evidenceRules, List.of(item));
    }
}
