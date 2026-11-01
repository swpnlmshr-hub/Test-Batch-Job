package com.batch.pipeline.port.stub;

import com.batch.pipeline.model.Entity;
import com.batch.pipeline.model.Rule;
import com.batch.pipeline.port.EntityLoadPort;
import com.batch.pipeline.port.EvidenceReadPort;
import com.batch.pipeline.port.EvidenceUpsertPort;
import com.batch.pipeline.port.FlagsUpsertPort;
import com.batch.pipeline.port.ParentRuleLoadPort;
import com.batch.pipeline.port.UpstreamPrepPort;

import java.util.List;

/**
 * No-op / empty stub ports so job constructors can be wired in smoke tests without Flowable.
 * Replace with real adapters (Flowable DO / JDBC) behind the same interfaces.
 */
public final class NoOpPorts {

    private NoOpPorts() {
    }

    public static UpstreamPrepPort upstreamPrep() {
        return () -> {
            // no-op: real impl fetches upstream and stores locally
        };
    }

    public static EntityLoadPort entityLoad() {
        return List::of;
    }

    public static ParentRuleLoadPort parentRuleLoad() {
        return new ParentRuleLoadPort() {
            @Override
            public List<ParentRuleCandidate> loadParentRules(Entity entity) {
                return List.of();
            }

            @Override
            public List<Rule> loadMappedChildRules(String parentRuleId, com.batch.pipeline.model.EntityType entityType) {
                return List.of();
            }
        };
    }

    public static EvidenceUpsertPort evidenceUpsert() {
        return writes -> {
            // no-op chunk upsert
        };
    }

    public static EvidenceReadPort evidenceRead() {
        return List::of;
    }

    public static FlagsUpsertPort flagsUpsert() {
        return writes -> {
            // no-op chunk upsert
        };
    }
}
