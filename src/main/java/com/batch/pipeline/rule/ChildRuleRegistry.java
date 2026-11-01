package com.batch.pipeline.rule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Maps ruleCode → {@link ChildRule} implementation.
 */
public final class ChildRuleRegistry {

    private final Map<String, ChildRule> rulesByCode;

    public ChildRuleRegistry(Collection<ChildRule> rules) {
        Objects.requireNonNull(rules, "rules");
        Map<String, ChildRule> map = new LinkedHashMap<>();
        for (ChildRule rule : rules) {
            Objects.requireNonNull(rule, "rule");
            String code = Objects.requireNonNull(rule.ruleCode(), "ruleCode");
            ChildRule previous = map.put(code, rule);
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate ChildRule code: " + code);
            }
        }
        this.rulesByCode = Collections.unmodifiableMap(map);
    }

    public Optional<ChildRule> find(String ruleCode) {
        return Optional.ofNullable(rulesByCode.get(ruleCode));
    }

    public ChildRule require(String ruleCode) {
        return find(ruleCode)
                .orElseThrow(() -> new IllegalArgumentException("No ChildRule registered for code: " + ruleCode));
    }

    public Map<String, ChildRule> asMap() {
        return rulesByCode;
    }
}
