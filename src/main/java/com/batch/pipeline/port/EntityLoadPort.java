package com.batch.pipeline.port;

import com.batch.pipeline.model.Entity;

import java.util.List;

/**
 * Loads entities that should be processed by the evidence-gathering job.
 * Expected volume is on the order of a few thousand entities.
 */
public interface EntityLoadPort {

    List<Entity> loadEntitiesToEvaluate();
}
