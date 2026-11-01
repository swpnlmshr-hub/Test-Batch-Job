package com.batch.pipeline.port.flowable;

import com.batch.pipeline.model.Entity;
import com.batch.pipeline.model.EntityType;
import com.batch.pipeline.model.Rule;
import com.batch.pipeline.port.BatchDataService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Flowable Data Object–backed {@link BatchDataService} implementation.
 * <p>
 * Evidence persistence uses <strong>per-row</strong> Flowable DO create-or-modify
 * via {@link DataObjectRuntimeService}. Flags persistence is <strong>update-only</strong>
 * (records always exist) — modification builder only, no create branch.
 * </p>
 * <pre>{@code
 * // evidence: lookup → create or modify
 * // flags: lookup → modify (required); never create
 * dataObjectRuntimeService.createDataObjectModificationBuilder()
 *     .id(existingId).values(values).modify();
 * }</pre>
 * <p>
 * A chunk is only a transactional boundary around N individual DO operations.
 * Flowable has no batch insert — do not use JDBC multi-row SQL batch writes.
 * Definition keys: {@link FlowableDataObjectKeys}.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public final class FlowableBatchDataService implements BatchDataService {

    @NonNull
    private final DataObjectRuntimeService dataObjectRuntimeService;

    @Override
    public List<Entity> loadEntitiesToEvaluate() {
        // TODO: load entities from local / Flowable store after upstream prep
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
        log.debug("createOrModifyEvidenceChunk size={}", writes.size());
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
    public void updateFlagsChunk(List<FlagsWrite> writes) {
        Objects.requireNonNull(writes, "writes");
        log.debug("updateFlagsChunk size={}", writes.size());
        dataObjectRuntimeService.inTransaction(() -> {
            for (FlagsWrite write : writes) {
                updateFlags(write);
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
            log.trace("Creating evidenceDO for entityId={} parentRuleId={}",
                    write.entityId(), write.parentRuleId());
            dataObjectRuntimeService.createDataObjectBuilder()
                    .definitionKey(FlowableDataObjectKeys.EVIDENCE_DO)
                    .values(values)
                    .create();
        } else {
            log.trace("Modifying evidenceDO id={}", existingId.get());
            dataObjectRuntimeService.createDataObjectModificationBuilder()
                    .id(existingId.get())
                    .values(values)
                    .modify();
        }
    }

    /**
     * One flagsDO row: lookup by entityId → modification builder only.
     * Flags records always exist; missing instance is an error (no create).
     */
    private void updateFlags(FlagsWrite write) {
        Map<String, Object> identity = Map.of("entityId", write.entityId());
        Map<String, Object> values = flagsValues(write);

        String existingId = dataObjectRuntimeService.findDataObjectInstanceId(
                        FlowableDataObjectKeys.FLAGS_DO, identity)
                .orElseThrow(() -> new IllegalStateException(
                        "flagsDO missing for entityId=" + write.entityId()
                                + "; flags rows must already exist (update-only)"));

        log.trace("Modifying flagsDO id={}", existingId);
        dataObjectRuntimeService.createDataObjectModificationBuilder()
                .id(existingId)
                .values(values)
                .modify();
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
