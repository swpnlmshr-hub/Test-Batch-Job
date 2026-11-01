package com.batch.pipeline.port.flowable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Thin compile-safe mirror of Flowable {@code DataObjectRuntimeService}.
 * <p>
 * Method and builder names match the Flowable Data Object API pattern
 * (create builder / modification builder). Wire a real Flowable Work /
 * platform {@code DataObjectRuntimeService} behind this interface when the
 * dependency is available — Flowable has <strong>no batch insert</strong>;
 * each instance is one create or one modify.
 * </p>
 */
public interface DataObjectRuntimeService {

    /**
     * Look up an existing Data Object instance id by definition key + identity field values.
     */
    Optional<String> findDataObjectInstanceId(String definitionKey, Map<String, Object> identityValues);

    /**
     * Start a Flowable Data Object create builder (mirrors
     * {@code dataObjectRuntimeService.createDataObjectBuilder()}).
     */
    DataObjectCreateBuilder createDataObjectBuilder();

    /**
     * Start a Flowable Data Object modification builder (mirrors
     * {@code dataObjectRuntimeService.createDataObjectModificationBuilder()}).
     */
    DataObjectModificationBuilder createDataObjectModificationBuilder();

    /**
     * Run work inside one engine transaction (chunk commit boundary only —
     * still N individual create/modify calls inside).
     */
    <T> T inTransaction(Supplier<T> work);

    /**
     * Mirrors Flowable create-builder chain:
     * {@code createDataObjectBuilder().definitionKey(...).values(...).create()}.
     */
    interface DataObjectCreateBuilder {
        DataObjectCreateBuilder definitionKey(String definitionKey);

        DataObjectCreateBuilder values(Map<String, Object> values);

        /** Persist a new Data Object instance. */
        String create();
    }

    /**
     * Mirrors Flowable modification-builder chain:
     * {@code createDataObjectModificationBuilder().id(...).values(...).modify()}.
     */
    interface DataObjectModificationBuilder {
        DataObjectModificationBuilder id(String instanceId);

        DataObjectModificationBuilder values(Map<String, Object> values);

        /** Apply field updates to an existing Data Object instance. */
        void modify();
    }
}
