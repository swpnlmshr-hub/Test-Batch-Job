package com.batch.pipeline.port;

import com.batch.pipeline.model.Entity;
import com.batch.pipeline.model.EntityType;
import com.batch.pipeline.model.Evidence;
import com.batch.pipeline.model.ParentEvaluationOutcome;
import com.batch.pipeline.model.Rule;

import java.util.List;

/**
 * In-app Flowable Data Object persistence / load boundary for both batch jobs.
 * <p>
 * All Flowable DO operations live here in this single file (entity/rule loads from
 * local DO store, evidence create-or-modify, flags update-only, etc.). Upstream
 * open-API prep belongs on {@link UpstreamSource} / {@link UpstreamSourceRegistry},
 * not here.
 * </p>
 * <p>
 * Implement against Flowable {@code DataObjectRuntimeService} create / modification
 * builders. Chunking means: process N rows, then commit one transaction — each row
 * is still an individual Flowable DO create or modify (lookup → create if missing,
 * modify if present). Flowable has no batch insert; do not use JDBC multi-row SQL
 * batch writes.
 * </p>
 */
public interface BatchDataService {

    // --- entity + parent-rule load (local / Flowable DO) ---

    /**
     * Entities to evaluate (~3–4k expected), from local store after upstream prep.
     */
    List<Entity> loadEntitiesToEvaluate();

    /**
     * Parent rule candidates for an entity (not child rules), from {@code ruleListDO}.
     * Applicable-stage is used by the orchestrator stage gate.
     */
    List<ParentRuleCandidate> loadParentRules(Entity entity);

    /**
     * Mapped child rules under a parent (always written into {@code evidenceRules}),
     * from {@code ruleMappingDO} / {@code rulesDO}.
     */
    List<Rule> loadMappedChildRules(String parentRuleId, EntityType entityType);

    // --- evidence persistence (job 1 write / job 2 read) ---

    /**
     * Chunked transactional Flowable DO create-or-modify for evidence rows
     * keyed by {@code (entityId, parentRuleId)}. One create or modify builder
     * call per write; no JDBC bulk upsert.
     */
    void createOrModifyEvidenceChunk(List<EvidenceWrite> writes);

    /**
     * Load stored evidence Data Objects for the evaluation job (no upstream refetch).
     */
    List<EvidenceRow> loadAllEvidence();

    // --- flags persistence (job 2) ---

    /**
     * Chunked transactional Flowable DO <strong>update-only</strong> for flags rows
     * keyed by entity id. Flags records always exist — use modification builder only
     * (no create). One modify call per write; no JDBC bulk upsert.
     */
    void updateFlagsChunk(List<FlagsWrite> writes);

    // --- DTOs ---

    /**
     * @param parentRuleId    parent list / parent-rule key (also Source registry key)
     * @param applicableStage stage value that must match the entity stage for gathering
     */
    record ParentRuleCandidate(
            String parentRuleId,
            String applicableStage
    ) {
    }

    /**
     * One evidence Data Object to create-or-modify.
     *
     * @param entityId     entity primary key
     * @param entityType   system or case
     * @param parentRuleId parent rule / list key
     * @param evidence     value JSON object
     */
    record EvidenceWrite(
            String entityId,
            EntityType entityType,
            String parentRuleId,
            Evidence evidence
    ) {
    }

    /**
     * Stored evidence Data Object for evaluation.
     *
     * @param entityId     entity primary key
     * @param entityType   system or case
     * @param parentRuleId parent rule key
     * @param evidence     stored value JSON
     */
    record EvidenceRow(
            String entityId,
            EntityType entityType,
            String parentRuleId,
            Evidence evidence
    ) {
    }

    /**
     * Flags Data Object payload for one entity (all parent outcomes for that entity).
     *
     * @param entityId entity primary key
     * @param parents  parent-level rollups that form the flags value JSON
     */
    record FlagsWrite(
            String entityId,
            List<ParentEvaluationOutcome> parents
    ) {
    }
}
