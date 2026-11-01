package com.batch.pipeline.job;

import com.batch.pipeline.model.Entity;
import com.batch.pipeline.model.Evidence;
import com.batch.pipeline.model.Rule;
import com.batch.pipeline.port.EntityLoadPort;
import com.batch.pipeline.port.EvidenceUpsertPort;
import com.batch.pipeline.port.EvidenceUpsertPort.EvidenceWrite;
import com.batch.pipeline.port.ParentRuleLoadPort;
import com.batch.pipeline.port.ParentRuleLoadPort.ParentRuleCandidate;
import com.batch.pipeline.port.UpstreamPrepPort;
import com.batch.pipeline.source.Source;
import com.batch.pipeline.source.SourceRegistry;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Job 1 — Source / evidence gathering (stub orchestrator skeleton).
 * <p>
 * Order: upstream prep → load entities → per entity/parent: stage gate →
 * fetch evidence (or stage-fail shell) → chunked upsert. Does not run rules.
 * </p>
 */
public final class EvidenceGatheringJob {

    private final UpstreamPrepPort upstreamPrepPort;
    private final EntityLoadPort entityLoadPort;
    private final ParentRuleLoadPort parentRuleLoadPort;
    private final SourceRegistry sourceRegistry;
    private final EvidenceUpsertPort evidenceUpsertPort;
    private final int chunkSize;

    public EvidenceGatheringJob(
            UpstreamPrepPort upstreamPrepPort,
            EntityLoadPort entityLoadPort,
            ParentRuleLoadPort parentRuleLoadPort,
            SourceRegistry sourceRegistry,
            EvidenceUpsertPort evidenceUpsertPort,
            int chunkSize
    ) {
        this.upstreamPrepPort = Objects.requireNonNull(upstreamPrepPort, "upstreamPrepPort");
        this.entityLoadPort = Objects.requireNonNull(entityLoadPort, "entityLoadPort");
        this.parentRuleLoadPort = Objects.requireNonNull(parentRuleLoadPort, "parentRuleLoadPort");
        this.sourceRegistry = Objects.requireNonNull(sourceRegistry, "sourceRegistry");
        this.evidenceUpsertPort = Objects.requireNonNull(evidenceUpsertPort, "evidenceUpsertPort");
        if (chunkSize < 1) {
            throw new IllegalArgumentException("chunkSize must be >= 1");
        }
        this.chunkSize = chunkSize;
    }

    /**
     * Run the nightly evidence-gathering pipeline.
     * Persistence/Flowable details stay behind {@link EvidenceUpsertPort}.
     */
    public void run() {
        // 1. Upstream data prep — fetch & store locally before the entity loop.
        upstreamPrepPort.prepare();

        // 2. Fetch entities to evaluate (~3–4k expected).
        List<Entity> entities = entityLoadPort.loadEntitiesToEvaluate();

        List<EvidenceWrite> buffer = new ArrayList<>();
        for (Entity entity : entities) {
            List<ParentRuleCandidate> parents = parentRuleLoadPort.loadParentRules(entity);
            for (ParentRuleCandidate parent : parents) {
                List<Rule> mappedChildren =
                        parentRuleLoadPort.loadMappedChildRules(parent.parentRuleId(), entity.type());

                Evidence evidence;
                if (!isStageApplicable(entity.stage(), parent.applicableStage())) {
                    // Stage gate fail: isApplicable=false, empty items, evidenceRules still populated.
                    evidence = new Evidence(false, Instant.now(), mappedChildren, List.of());
                } else {
                    Source source = sourceRegistry.require(parent.parentRuleId());
                    evidence = source.fetchEvidence(entity.id());
                }

                buffer.add(new EvidenceWrite(
                        entity.id(),
                        entity.type(),
                        parent.parentRuleId(),
                        evidence
                ));
            }

            if (buffer.size() >= chunkSize) {
                flush(buffer);
            }
        }

        if (!buffer.isEmpty()) {
            flush(buffer);
        }
    }

    private void flush(List<EvidenceWrite> buffer) {
        evidenceUpsertPort.upsertChunk(List.copyOf(buffer));
        buffer.clear();
    }

    private static boolean isStageApplicable(String entityStage, String applicableStage) {
        return Objects.equals(entityStage, applicableStage);
    }
}
