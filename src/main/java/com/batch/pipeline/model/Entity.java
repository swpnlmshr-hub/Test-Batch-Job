package com.batch.pipeline.model;

/**
 * Entity fetched for batch processing.
 *
 * @param id    entity primary key
 * @param stage current stage (compared to parent rule applicable-stage)
 * @param type  system or case
 */
public record Entity(
        String id,
        String stage,
        EntityType type
) {
}
