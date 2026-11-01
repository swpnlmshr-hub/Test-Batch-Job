package com.batch.pipeline.port.flowable;

import com.batch.pipeline.model.EntityType;
import com.batch.pipeline.model.Evidence;
import com.batch.pipeline.model.ParentEvaluationOutcome;
import com.batch.pipeline.port.BatchDataService.EvidenceWrite;
import com.batch.pipeline.port.BatchDataService.FlagsWrite;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FlowableBatchDataServiceTest {

    @Test
    void evidenceChunk_createThenModify_perRowBuilders() {
        RecordingDataObjectRuntimeService runtime = new RecordingDataObjectRuntimeService();
        FlowableBatchDataService service = new FlowableBatchDataService(runtime);

        Evidence evidence = new Evidence(true, Instant.EPOCH, List.of(), List.of());
        EvidenceWrite write = new EvidenceWrite("e1", EntityType.CASE, "parent-1", evidence);

        service.createOrModifyEvidenceChunk(List.of(write));
        assertEquals(1, runtime.size());
        assertTrue(runtime.operations().stream().anyMatch(op -> op.startsWith("create:" + FlowableDataObjectKeys.EVIDENCE_DO)));

        service.createOrModifyEvidenceChunk(List.of(write));
        assertEquals(1, runtime.size());
        assertTrue(runtime.operations().stream().anyMatch(op -> op.startsWith("modify:")));
    }

    @Test
    void flagsChunk_updateOnly_usesModificationBuilder() {
        RecordingDataObjectRuntimeService runtime = new RecordingDataObjectRuntimeService();
        FlowableBatchDataService service = new FlowableBatchDataService(runtime);

        // Flags rows always exist — seed via runtime create, then service must only modify.
        runtime.createDataObjectBuilder()
                .definitionKey(FlowableDataObjectKeys.FLAGS_DO)
                .values(Map.of("entityId", "e1", "valueJson", List.of()))
                .create();

        FlagsWrite write = new FlagsWrite("e1", List.<ParentEvaluationOutcome>of());
        service.updateFlagsChunk(List.of(write));

        assertEquals(1, runtime.size());
        assertTrue(runtime.operations().stream().anyMatch(op -> op.startsWith("modify:")));
        // Service path must not create flagsDO (seed create above is outside updateFlagsChunk).
        long flagsCreatesInTxn = runtime.operations().stream()
                .filter(op -> op.startsWith("create:" + FlowableDataObjectKeys.FLAGS_DO))
                .count();
        assertEquals(1, flagsCreatesInTxn); // only the seed create
    }

    @Test
    void flagsChunk_missingRow_failsWithoutCreate() {
        RecordingDataObjectRuntimeService runtime = new RecordingDataObjectRuntimeService();
        FlowableBatchDataService service = new FlowableBatchDataService(runtime);

        FlagsWrite write = new FlagsWrite("missing", List.<ParentEvaluationOutcome>of());
        assertThrows(IllegalStateException.class, () -> service.updateFlagsChunk(List.of(write)));
        assertEquals(0, runtime.size());
    }
}
