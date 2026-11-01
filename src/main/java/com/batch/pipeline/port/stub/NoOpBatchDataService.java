package com.batch.pipeline.port.stub;

import com.batch.pipeline.model.Entity;
import com.batch.pipeline.model.EntityType;
import com.batch.pipeline.model.Rule;
import com.batch.pipeline.port.BatchDataService;
import com.batch.pipeline.port.flowable.FlowableBatchDataService;

import java.util.List;

/**
 * No-op {@link BatchDataService} so jobs can be wired in smoke tests without Flowable.
 * Prefer {@link FlowableBatchDataService} for the real create-or-modify DO path.
 * Upstream prep is {@link ExampleUpstreamSource} / {@link com.batch.pipeline.port.UpstreamSourceRegistry}, not this class.
 */
public final class NoOpBatchDataService implements BatchDataService {

    @Override
    public List<Entity> loadEntitiesToEvaluate() {
        return List.of();
    }

    @Override
    public List<ParentRuleCandidate> loadParentRules(Entity entity) {
        return List.of();
    }

    @Override
    public List<Rule> loadMappedChildRules(String parentRuleId, EntityType entityType) {
        return List.of();
    }

    @Override
    public void createOrModifyEvidenceChunk(List<EvidenceWrite> writes) {
        // no-op: real path is per-row Flowable DO create-or-modify in a chunk transaction
    }

    @Override
    public List<EvidenceRow> loadAllEvidence() {
        return List.of();
    }

    @Override
    public void createOrModifyFlagsChunk(List<FlagsWrite> writes) {
        // no-op: real path is per-row Flowable DO create-or-modify in a chunk transaction
    }
}
