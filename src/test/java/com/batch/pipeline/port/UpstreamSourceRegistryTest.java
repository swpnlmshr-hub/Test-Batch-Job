package com.batch.pipeline.port;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UpstreamSourceRegistryTest {

    @Test
    void prepareAll_runsEveryRegisteredSourceInOrder() {
        List<String> order = new ArrayList<>();
        UpstreamSource first = () -> order.add("a");
        UpstreamSource second = () -> order.add("b");

        UpstreamSourceRegistry registry = UpstreamSourceRegistry.of(first, second);
        registry.prepareAll();

        assertEquals(List.of("a", "b"), order);
        assertEquals(2, registry.size());
    }
}
