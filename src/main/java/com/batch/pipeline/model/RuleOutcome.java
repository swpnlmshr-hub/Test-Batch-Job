package com.batch.pipeline.model;

/**
 * Per-rule outcome values.
 * <ul>
 *   <li>{@code TRUE}/{@code FALSE} — individual applicability rules</li>
 *   <li>{@code PASS}/{@code FAIL} — individual evaluation rules</li>
 *   <li>{@code ERROR} — unexpected evaluation failure</li>
 * </ul>
 */
public enum RuleOutcome {
    TRUE,
    FALSE,
    PASS,
    FAIL,
    ERROR
}
