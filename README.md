# Test Batch Job

Java 21 Maven scaffold for a two-step nightly batch pipeline.

## Jobs (sequential)

1. **Evidence gathering** (`com.batch.pipeline.job.EvidenceGatheringJob`)
   - Upstream prep hook (fetch & store locally)
   - Load entities to evaluate
   - Per entity / parent rule: **stage gate** → gather evidence via `Source` (or write `isApplicable=false` shell with `evidenceRules`)
   - Chunked upsert through `BatchDataService` (Flowable/JDBC behind the service)
   - Does **not** run applicability or evaluation rules

2. **Evaluation** (`com.batch.pipeline.job.EvaluationJob`)
   - Reads stored evidence (no upstream refetch)
   - Per parent: run **applicability** child rules, then **evaluation** rules only if aggregate is `APPLICABLE` (short-circuit otherwise)
   - Upserts flags via `BatchDataService`

## Layout

| Package | Role |
|---------|------|
| `com.batch.pipeline.model` | Enums + records (`Entity`, `Evidence`, outcomes, …) |
| `com.batch.pipeline.source` | `Source` interface + `SourceRegistry` |
| `com.batch.pipeline.rule` | `ChildRule` interface + `ChildRuleRegistry` |
| `com.batch.pipeline.port` | `BatchDataService` — single persistence/load boundary (Flowable) |
| `com.batch.pipeline.job` | Job orchestrator skeletons |
| `*.stub` | Example Source / rule and no-op `BatchDataService` for wiring |

## Build

Requires **JDK 21**.

```bash
mvn -q compile
```

## Flowable

Flowable is intentionally **not** a hard compile dependency. Persistence stays behind a single `BatchDataService` interface so both jobs can share one service implementation, and the scaffold builds without Flowable credentials or engine config.

In production, that implementation will use **Flowable Data Objects** — per-instance create/modification builders and **chunked transactional upserts**. There is no assumed bulk Data Object API; multi-instance DO updates are still N updates, so prefer chunked create-or-modify (or custom JDBC batch upsert later if needed). A commented `provided`-scope placeholder lives in `pom.xml` for when the adapter is wired.
