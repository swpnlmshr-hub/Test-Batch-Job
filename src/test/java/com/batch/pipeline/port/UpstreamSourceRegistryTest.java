package com.batch.pipeline.port;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UpstreamSourceRegistryTest {

    @Test
    void prepareAll_runsEveryRegisteredSourceInOrder() {
        List<String> order = new ArrayList<>();
        UpstreamSource first = source("a", () -> order.add("a"));
        UpstreamSource second = source("b", () -> order.add("b"));

        UpstreamSourceRegistry registry = UpstreamSourceRegistry.of(first, second);
        registry.prepareAll();

        assertEquals(List.of("a", "b"), order);
        assertEquals(2, registry.size());
    }

    @Test
    void registryUpstreamDataService_delegatesToAllSources() {
        AtomicInteger calls = new AtomicInteger();
        UpstreamSourceRegistry registry = UpstreamSourceRegistry.of(
                source("only", calls::incrementAndGet));

        new RegistryUpstreamDataService(registry).prepareUpstream();
        assertEquals(1, calls.get());
    }

    @Test
    void duplicateKeys_rejected() {
        assertThrows(IllegalArgumentException.class, () ->
                UpstreamSourceRegistry.of(source("dup", () -> {
                }), source("dup", () -> {
                })));
    }

    private static UpstreamSource source(String key, Runnable prepare) {
        return new UpstreamSource() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public void prepare() {
                prepare.run();
            }
        };
    }
}
