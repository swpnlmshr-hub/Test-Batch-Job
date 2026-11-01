package com.batch.pipeline.port.flowable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * In-memory {@link DataObjectRuntimeService} for compile-time / smoke wiring.
 * Demonstrates the same createDataObjectBuilder / createDataObjectModificationBuilder
 * call sites used against real Flowable — not a JDBC batch path.
 */
public final class RecordingDataObjectRuntimeService implements DataObjectRuntimeService {

    private final Map<String, Map<String, Object>> store = new LinkedHashMap<>();
    private final List<String> operations = new ArrayList<>();
    private final AtomicInteger seq = new AtomicInteger();

    @Override
    public Optional<String> findDataObjectInstanceId(String definitionKey, Map<String, Object> identityValues) {
        Objects.requireNonNull(definitionKey, "definitionKey");
        Objects.requireNonNull(identityValues, "identityValues");
        for (Map.Entry<String, Map<String, Object>> entry : store.entrySet()) {
            Map<String, Object> row = entry.getValue();
            if (!definitionKey.equals(row.get("__definitionKey"))) {
                continue;
            }
            if (identityMatches(row, identityValues)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    @Override
    public DataObjectCreateBuilder createDataObjectBuilder() {
        return new CreateBuilder();
    }

    @Override
    public DataObjectModificationBuilder createDataObjectModificationBuilder() {
        return new ModificationBuilder();
    }

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        operations.add("beginTransaction");
        try {
            return work.get();
        } finally {
            operations.add("commitTransaction");
        }
    }

    public List<String> operations() {
        return List.copyOf(operations);
    }

    public int size() {
        return store.size();
    }

    private static boolean identityMatches(Map<String, Object> row, Map<String, Object> identity) {
        for (Map.Entry<String, Object> field : identity.entrySet()) {
            if (!Objects.equals(row.get(field.getKey()), field.getValue())) {
                return false;
            }
        }
        return true;
    }

    private final class CreateBuilder implements DataObjectCreateBuilder {
        private String definitionKey;
        private Map<String, Object> values = Map.of();

        @Override
        public DataObjectCreateBuilder definitionKey(String definitionKey) {
            this.definitionKey = Objects.requireNonNull(definitionKey, "definitionKey");
            return this;
        }

        @Override
        public DataObjectCreateBuilder values(Map<String, Object> values) {
            this.values = Map.copyOf(Objects.requireNonNull(values, "values"));
            return this;
        }

        @Override
        public String create() {
            Objects.requireNonNull(definitionKey, "definitionKey");
            String id = definitionKey + "-" + seq.incrementAndGet() + "-" + UUID.randomUUID();
            Map<String, Object> row = new LinkedHashMap<>(values);
            row.put("__definitionKey", definitionKey);
            store.put(id, row);
            operations.add("create:" + definitionKey + ":" + id);
            return id;
        }
    }

    private final class ModificationBuilder implements DataObjectModificationBuilder {
        private String instanceId;
        private Map<String, Object> values = Map.of();

        @Override
        public DataObjectModificationBuilder id(String instanceId) {
            this.instanceId = Objects.requireNonNull(instanceId, "instanceId");
            return this;
        }

        @Override
        public DataObjectModificationBuilder values(Map<String, Object> values) {
            this.values = Map.copyOf(Objects.requireNonNull(values, "values"));
            return this;
        }

        @Override
        public void modify() {
            Objects.requireNonNull(instanceId, "instanceId");
            Map<String, Object> existing = store.get(instanceId);
            if (existing == null) {
                throw new IllegalArgumentException("Unknown Data Object instance: " + instanceId);
            }
            existing.putAll(values);
            operations.add("modify:" + instanceId);
        }
    }
}
