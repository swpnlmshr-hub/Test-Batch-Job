package com.batch.pipeline.port.flowable;

import com.batch.pipeline.model.EntityType;
import com.batch.pipeline.model.Evidence;
import com.batch.pipeline.model.ParentEvaluationOutcome;
import com.batch.pipeline.port.BatchDataService.EvidenceWrite;
import com.batch.pipeline.port.BatchDataService.FlagsWrite;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void flagsChunk_createThenModify_perRowBuilders() {
        RecordingDataObjectRuntimeService runtime = new RecordingDataObjectRuntimeService();
        FlowableBatchDataService service = new FlowableBatchDataService(runtime);

        FlagsWrite write = new FlagsWrite("e1", List.<ParentEvaluationOutcome>of());

        service.createOrModifyFlagsChunk(List.of(write));
        assertEquals(1, runtime.size());
        assertTrue(runtime.operations().stream().anyMatch(op -> op.startsWith("create:" + FlowableDataObjectKeys.FLAGS_DO)));

        service.createOrModifyFlagsChunk(List.of(write));
        assertEquals(1, runtime.size());
        assertTrue(runtime.operations().stream().anyMatch(op -> op.startsWith("modify:")));
    }
}
