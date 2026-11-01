package com.batch.pipeline.port;

import com.batch.pipeline.model.Entity;
import com.batch.pipeline.model.EntityType;
import com.batch.pipeline.model.Evidence;
import com.batch.pipeline.model.ParentEvaluationOutcome;
import com.batch.pipeline.model.Rule;

import java.util.List;

/**
 * Single persistence / data-access boundary for both batch jobs.
 * <p>
 * Implement this once (e.g. Flowable Data Object adapters) so evidence gathering
 * and evaluation share one service. Hides Flowable / JDBC details; prefer chunked
 * transactional upserts on unique keys. No assumed bulk Data Object API.
 * </p>
 */
public interface BatchDataService {

    // --- upstream prep (job 1) ---

    /**
     * Prefetch and store upstream data needed for this nightly run.
     * Gatherers then read local copies, not live upstream, during the entity loop.
     */
    void prepareUpstream();

    // --- entity + parent-rule load (job 1) ---

    /**
     * Entities to evaluate (~3–4k expected).
     */
    List<Entity> loadEntitiesToEvaluate();

    /**
     * Parent rule candidates for an entity (not child rules).
     * Applicable-stage is used by the orchestrator stage gate.
     */
    List<ParentRuleCandidate> loadParentRules(Entity entity);

    /**
     * Mapped child rules under a parent (always written into {@code evidenceRules}).
     */
    List<Rule> loadMappedChildRules(String parentRuleId, EntityType entityType);

    // --- evidence persistence (job 1 write / job 2 read) ---

    /**
     * Upsert a batch of evidence payloads in one chunk/transaction
     * on unique {@code (entityId, parentRuleId)}.
     */
    void upsertEvidenceChunk(List<EvidenceWrite> writes);

    /**
     * Load stored evidence rows for the evaluation job (no upstream refetch).
     */
    List<EvidenceRow> loadAllEvidence();

    // --- flags persistence (job 2) ---

    /**
     * Upsert flags for a chunk of entities.
     */
    void upsertFlagsChunk(List<FlagsWrite> writes);

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
     * One evidence row to create-or-modify.
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
     * Stored evidence row for evaluation.
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
     * Flags payload for one entity (all parent outcomes for that entity).
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
