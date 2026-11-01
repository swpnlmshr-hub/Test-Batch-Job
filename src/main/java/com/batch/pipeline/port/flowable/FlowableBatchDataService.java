package com.batch.pipeline.port.flowable;

import com.batch.pipeline.model.Entity;
import com.batch.pipeline.model.EntityType;
import com.batch.pipeline.model.Rule;
import com.batch.pipeline.port.BatchDataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Flowable Data Object–backed {@link BatchDataService} implementation.
 * <p>
 * Evidence and flags persistence use <strong>per-row</strong> Flowable DO
 * create-or-modify via {@link DataObjectRuntimeService}:
 * </p>
 * <pre>{@code
 * Optional<String> id = dataObjectRuntimeService.findDataObjectInstanceId(key, identity);
 * if (id.isEmpty()) {
 *   dataObjectRuntimeService.createDataObjectBuilder()
 *       .definitionKey(key).values(values).create();
 * } else {
 *   dataObjectRuntimeService.createDataObjectModificationBuilder()
 *       .id(id.get()).values(values).modify();
 * }
 * }</pre>
 * <p>
 * A chunk is only a transactional boundary around N individual DO operations.
 * Flowable has no batch insert — do not use JDBC multi-row SQL batch writes.
 * Definition keys: {@link FlowableDataObjectKeys}.
 * </p>
 */
public final class FlowableBatchDataService implements BatchDataService {

    private final DataObjectRuntimeService dataObjectRuntimeService;

    public FlowableBatchDataService(DataObjectRuntimeService dataObjectRuntimeService) {
        this.dataObjectRuntimeService = Objects.requireNonNull(
                dataObjectRuntimeService, "dataObjectRuntimeService");
    }

    @Override
    public List<Entity> loadEntitiesToEvaluate() {
        // TODO: load entities from local / Flowable store after UpstreamDataService prep
        throw new UnsupportedOperationException("loadEntitiesToEvaluate: wire local entity load");
    }

    @Override
    public List<ParentRuleCandidate> loadParentRules(Entity entity) {
        // TODO: query FlowableDataObjectKeys.RULE_LIST_DO via dataObjectRuntimeService
        throw new UnsupportedOperationException(
                "loadParentRules: wire " + FlowableDataObjectKeys.RULE_LIST_DO + " query");
    }

    @Override
    public List<Rule> loadMappedChildRules(String parentRuleId, EntityType entityType) {
        // TODO: query RULE_MAPPING_DO + RULES_DO via dataObjectRuntimeService
        throw new UnsupportedOperationException(
                "loadMappedChildRules: wire " + FlowableDataObjectKeys.RULE_MAPPING_DO
                        + " / " + FlowableDataObjectKeys.RULES_DO + " query");
    }

    @Override
    public void createOrModifyEvidenceChunk(List<EvidenceWrite> writes) {
        Objects.requireNonNull(writes, "writes");
        dataObjectRuntimeService.inTransaction(() -> {
            for (EvidenceWrite write : writes) {
                createOrModifyEvidence(write);
            }
            return null;
        });
    }

    @Override
    public List<EvidenceRow> loadAllEvidence() {
        // TODO: query all FlowableDataObjectKeys.EVIDENCE_DO instances
        throw new UnsupportedOperationException(
                "loadAllEvidence: wire " + FlowableDataObjectKeys.EVIDENCE_DO + " query");
    }

    @Override
    public void createOrModifyFlagsChunk(List<FlagsWrite> writes) {
        Objects.requireNonNull(writes, "writes");
        dataObjectRuntimeService.inTransaction(() -> {
            for (FlagsWrite write : writes) {
                createOrModifyFlags(write);
            }
            return null;
        });
    }

    /**
     * One evidenceDO row: lookup by (entityId, parentRuleId) → create or modify.
     */
    private void createOrModifyEvidence(EvidenceWrite write) {
        Map<String, Object> identity = Map.of(
                "entityId", write.entityId(),
                "parentRuleId", write.parentRuleId()
        );
        Map<String, Object> values = evidenceValues(write);

        Optional<String> existingId = dataObjectRuntimeService.findDataObjectInstanceId(
                FlowableDataObjectKeys.EVIDENCE_DO, identity);

        if (existingId.isEmpty()) {
            dataObjectRuntimeService.createDataObjectBuilder()
                    .definitionKey(FlowableDataObjectKeys.EVIDENCE_DO)
                    .values(values)
                    .create();
        } else {
            dataObjectRuntimeService.createDataObjectModificationBuilder()
                    .id(existingId.get())
                    .values(values)
                    .modify();
        }
    }

    /**
     * One flagsDO row: lookup by entityId → create or modify.
     */
    private void createOrModifyFlags(FlagsWrite write) {
        Map<String, Object> identity = Map.of("entityId", write.entityId());
        Map<String, Object> values = flagsValues(write);

        Optional<String> existingId = dataObjectRuntimeService.findDataObjectInstanceId(
                FlowableDataObjectKeys.FLAGS_DO, identity);

        if (existingId.isEmpty()) {
            dataObjectRuntimeService.createDataObjectBuilder()
                    .definitionKey(FlowableDataObjectKeys.FLAGS_DO)
                    .values(values)
                    .create();
        } else {
            dataObjectRuntimeService.createDataObjectModificationBuilder()
                    .id(existingId.get())
                    .values(values)
                    .modify();
        }
    }

    private static Map<String, Object> evidenceValues(EvidenceWrite write) {
        Map<String, Object> values = new HashMap<>();
        values.put("entityId", write.entityId());
        values.put("entityType", write.entityType().name());
        values.put("parentRuleId", write.parentRuleId());
        values.put("valueJson", write.evidence());
        return values;
    }

    private static Map<String, Object> flagsValues(FlagsWrite write) {
        Map<String, Object> values = new HashMap<>();
        values.put("entityId", write.entityId());
        values.put("valueJson", write.parents());
        return values;
    }
}
