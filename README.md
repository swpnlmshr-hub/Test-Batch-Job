# Test Batch Job

Java 21 Maven scaffold for a two-step nightly batch pipeline.

## Jobs (sequential)

1. **Evidence gathering** (`com.batch.pipeline.job.EvidenceGatheringJob`)
   - Upstream prep via `UpstreamDataService` (open API fetch & store locally)
   - Load entities / rules via `BatchDataService` (local Flowable DO store)
   - Per entity / parent rule: **stage gate** → gather evidence via `Source` (or write `isApplicable=false` shell with `evidenceRules`)
   - Chunked transactional Flowable DO create-or-modify via `BatchDataService` / `FlowableBatchDataService`
   - Does **not** run applicability or evaluation rules

2. **Evaluation** (`com.batch.pipeline.job.EvaluationJob`)
   - Reads stored evidence (no upstream refetch)
   - Per parent: run **applicability** child rules, then **evaluation** rules only if aggregate is `APPLICABLE` (short-circuit otherwise)
   - Chunked transactional Flowable DO create-or-modify for flags via `BatchDataService`

## Layout

| Package | Role |
|---------|------|
| `com.batch.pipeline.model` | Enums + records (`Entity`, `Evidence`, outcomes, …) |
| `com.batch.pipeline.source` | `Source` interface + `SourceRegistry` |
| `com.batch.pipeline.rule` | `ChildRule` interface + `ChildRuleRegistry` |
| `com.batch.pipeline.port` | `UpstreamDataService` (upstream open API) + `BatchDataService` (Flowable DO) |
| `com.batch.pipeline.port.flowable` | DO keys, `DataObjectRuntimeService` mirror, `FlowableBatchDataService` |
| `com.batch.pipeline.job` | Job orchestrator skeletons |
| `*.stub` | Example Source / rule and no-op upstream + batch services |

## Build

Requires **JDK 21**.

```bash
mvn -q clean compile
```

## Flowable persistence

**Upstream** prep/fetch is a separate contract: `UpstreamDataService` (not Flowable DO).

**In-app DB** is strictly **Flowable Data Objects** on `BatchDataService` — no JDBC bulk upsert / multi-row SQL batch insert.

- Contract: `BatchDataService` (local entity/rule loads + `createOrModifyEvidenceChunk` / `createOrModifyFlagsChunk`)
- Implementation: `FlowableBatchDataService` using `DataObjectRuntimeService`
  - `findDataObjectInstanceId` → lookup
  - `createDataObjectBuilder().definitionKey(...).values(...).create()` if missing
  - `createDataObjectModificationBuilder().id(...).values(...).modify()` if present
- Chunking = process N rows then **one transaction**; each row is still one create or one modify (Flowable has no batch insert)
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
