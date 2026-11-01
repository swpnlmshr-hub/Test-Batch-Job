package com.batch.pipeline.port;

import com.batch.pipeline.model.Entity;
import com.batch.pipeline.model.EntityType;
import com.batch.pipeline.model.Rule;

import java.util.List;

/**
 * Loads candidate parent rules (and mapped child rules) for evidence gathering.
 * Parent applicable-stage is used by the orchestrator stage gate.
 */
public interface ParentRuleLoadPort {

    /**
     * Parent rule candidates for an entity (not child rules).
     */
    List<ParentRuleCandidate> loadParentRules(Entity entity);

    /**
     * Mapped child rules under a parent (always written into {@code evidenceRules}).
     */
    List<Rule> loadMappedChildRules(String parentRuleId, EntityType entityType);

    /**
     * @param parentRuleId     parent list / parent-rule key (also Source registry key)
     * @param applicableStage  stage value that must match the entity stage for gathering
     */
    record ParentRuleCandidate(
            String parentRuleId,
            String applicableStage
    ) {
    }
}
