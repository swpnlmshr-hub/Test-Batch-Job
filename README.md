# Test Batch Job

Java 17+ Maven scaffold for a two-step nightly batch pipeline.

## Jobs (sequential)

1. **Evidence gathering** (`com.batch.pipeline.job.EvidenceGatheringJob`)
   - Upstream prep hook (fetch & store locally)
   - Load entities to evaluate
   - Per entity / parent rule: **stage gate** → gather evidence via `Source` (or write `isApplicable=false` shell with `evidenceRules`)
   - Chunked upsert through `EvidenceUpsertPort` (Flowable/JDBC behind the port)
   - Does **not** run applicability or evaluation rules

2. **Evaluation** (`com.batch.pipeline.job.EvaluationJob`)
   - Reads stored evidence (no upstream refetch)
   - Per parent: run **applicability** child rules, then **evaluation** rules only if aggregate is `APPLICABLE` (short-circuit otherwise)
   - Upserts flags via `FlagsUpsertPort`

## Layout

| Package | Role |
|---------|------|
| `com.batch.pipeline.model` | Enums + records (`Entity`, `Evidence`, outcomes, …) |
| `com.batch.pipeline.source` | `Source` interface + `SourceRegistry` |
| `com.batch.pipeline.rule` | `ChildRule` interface + `ChildRuleRegistry` |
| `com.batch.pipeline.port` | Persistence / load ports (Flowable integration boundary) |
| `com.batch.pipeline.job` | Job orchestrator skeletons |
| `*.stub` | Example Source / rule and no-op ports for compile-time wiring |

## Build

```bash
mvn -q compile
```

Flowable is intentionally **not** a hard dependency; wire adapters that implement the ports when integrating.
