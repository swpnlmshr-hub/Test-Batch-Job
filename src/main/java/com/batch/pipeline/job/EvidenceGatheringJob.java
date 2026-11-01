package com.batch.pipeline.job;

import com.batch.pipeline.model.Entity;
import com.batch.pipeline.model.Evidence;
import com.batch.pipeline.model.Rule;
import com.batch.pipeline.port.BatchDataService;
import com.batch.pipeline.port.BatchDataService.EvidenceWrite;
import com.batch.pipeline.port.BatchDataService.ParentRuleCandidate;
import com.batch.pipeline.port.UpstreamSource;
import com.batch.pipeline.port.UpstreamSourceRegistry;
import com.batch.pipeline.source.Source;
import com.batch.pipeline.source.SourceRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Job 1 — Source / evidence gathering (stub orchestrator skeleton).
 * <p>
 * Order: prepare every registered {@link UpstreamSource} → load entities
 * ({@link BatchDataService}) → per entity/parent: stage gate → fetch evidence
 * (or stage-fail shell) → chunked transactional Flowable DO create-or-modify.
 * Does not run rules.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public final class EvidenceGatheringJob {

    @NonNull
    private final UpstreamSourceRegistry upstreamSourceRegistry;
    @NonNull
    private final BatchDataService dataService;
    @NonNull
    private final SourceRegistry sourceRegistry;
    private final int chunkSize;

    /**
     * Run the nightly evidence-gathering pipeline.
     * Upstream open API stays behind pluggable {@link UpstreamSource}s;
     * Flowable DO persistence stays behind {@link BatchDataService}.
     */
    public void run() {
        if (chunkSize < 1) {
            throw new IllegalArgumentException("chunkSize must be >= 1");
        }

        log.info("Evidence gathering starting (chunkSize={})", chunkSize);

        // 1. Upstream data prep — each registered source fetches & stores locally.
        for (UpstreamSource upstream : upstreamSourceRegistry.all()) {
            upstream.prepare();
        }

        // 2. Fetch entities to evaluate (~3–4k expected) from local / Flowable store.
        List<Entity> entities = dataService.loadEntitiesToEvaluate();
        log.debug("Loaded {} entities to evaluate", entities.size());

        List<EvidenceWrite> buffer = new ArrayList<>();
        for (Entity entity : entities) {
            List<ParentRuleCandidate> parents = dataService.loadParentRules(entity);
            for (ParentRuleCandidate parent : parents) {
                List<Rule> mappedChildren =
                        dataService.loadMappedChildRules(parent.parentRuleId(), entity.type());

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

        log.info("Evidence gathering finished");
    }

    private void flush(List<EvidenceWrite> buffer) {
        // Chunk = one transaction; each write is an individual Flowable DO create-or-modify.
        log.debug("Flushing {} evidence writes", buffer.size());
        dataService.createOrModifyEvidenceChunk(List.copyOf(buffer));
        buffer.clear();
    }

    private static boolean isStageApplicable(String entityStage, String applicableStage) {
        return Objects.equals(entityStage, applicableStage);
    }
}
