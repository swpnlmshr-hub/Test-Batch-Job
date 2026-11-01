# Test Batch Job

Java 21 Maven scaffold for a two-step nightly batch pipeline.

## Jobs (sequential)

1. **Evidence gathering** (`com.batch.pipeline.job.EvidenceGatheringJob`)
   - Upstream prep: Job 1 iterates every `UpstreamSource` in `UpstreamSourceRegistry` (each may write different local tables); add another source by implementing + registering — no per-source key
   - Load entities / rules via `BatchDataService` (local Flowable DO store)
   - Per entity / parent rule: **stage gate** → gather evidence via `Source` (or write `isApplicable=false` shell with `evidenceRules`)
   - Chunked transactional Flowable DO create-or-modify via `BatchDataService` / `FlowableBatchDataService`
   - Does **not** run applicability or evaluation rules

2. **Evaluation** (`com.batch.pipeline.job.EvaluationJob`)
   - Reads stored evidence (no upstream refetch)
   - Per parent: run **applicability** child rules, then **evaluation** rules only if aggregate is `APPLICABLE` (short-circuit otherwise)
   - Chunked transactional Flowable DO <strong>update-only</strong> for flags via `BatchDataService` (flags rows always exist)

## Layout

| Package | Role |
|---------|------|
| `com.batch.pipeline.model` | Enums + records (`Entity`, `Evidence`, outcomes, …) |
| `com.batch.pipeline.source` | `Source` interface + `SourceRegistry` |
| `com.batch.pipeline.rule` | `ChildRule` interface + `ChildRuleRegistry` |
| `com.batch.pipeline.port` | `UpstreamSource` + registry (multi-upstream prep) + `BatchDataService` (Flowable DO) |
| `com.batch.pipeline.port.flowable` | DO keys, `DataObjectRuntimeService` mirror, `FlowableBatchDataService` |
| `com.batch.pipeline.job` | Job orchestrator skeletons |
| `*.stub` | Example Source / rule and no-op upstream + batch services |

## Build

Requires **JDK 21**. Uses **Lombok** (`provided`) for constructors/logging on jobs and services; model types stay as Java records.

```bash
mvn -q clean test
```

## Flowable persistence

**Upstream** prep/fetch is separate from Flowable DO: pluggable `UpstreamSource` (`prepare()` only), held as an ordered list in `UpstreamSourceRegistry`. Job 1 always runs every registered source. Stub: `ExampleUpstreamSource`.

**In-app DB** is strictly **Flowable Data Objects** on `BatchDataService` — no JDBC bulk upsert / multi-row SQL batch insert.

- Contract: `BatchDataService` (local entity/rule loads + `createOrModifyEvidenceChunk` / `updateFlagsChunk`)
- Implementation: `FlowableBatchDataService` using `DataObjectRuntimeService`
  - Evidence: `findDataObjectInstanceId` → create builder if missing, modification builder if present
  - Flags: lookup → **modification builder only** (no create; rows always exist)
- Chunking = process N rows then **one transaction**; each row is still one DO create or modify (Flowable has no batch insert)
- `DataObjectRuntimeService` is a compile-safe mirror of Flowable’s API (Data Objects are not on public Maven Central). Wire the real platform service behind it in production. `RecordingDataObjectRuntimeService` exercises the same builder calls in-memory.

### Data Object definition keys

Constants: `com.batch.pipeline.port.flowable.FlowableDataObjectKeys`

| Key | Constant | Purpose |
|-----|----------|---------|
| `ruleListDO` | `RULE_LIST_DO` | Parent rule list (applicable stage, …) |
| `rulesDO` | `RULES_DO` | Child / subrule definitions |
| `ruleMappingDO` | `RULE_MAPPING_DO` | Parent → child rule mapping |
| `evidenceDO` | `EVIDENCE_DO` | Evidence per entity + parent rule (`entityId`, `entityType`, `parentRuleId`, `valueJson`) |
| `flagsDO` | `FLAGS_DO` | Flags/result per entity (`entityId`, `valueJson`) |
| `failureCodeDO` | `FAILURE_CODE_DO` | Failure code lookup |
