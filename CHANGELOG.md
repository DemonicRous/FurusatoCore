# Changelog

## 0.2.0

- Added explicit `CoreState` transition validation.
- Added `BootstrapStage` for construct, pre-init, init, post-init and load-complete.
- Added immutable public `BootstrapReport` snapshots.
- Added monotonic nanosecond timing for every bootstrap stage, Core work total
  and wall-clock startup interval.
- Moved `AVAILABLE` transition to Forge load-complete.
- Added bootstrap timing to `/furusato info` and startup logs.
- Added bootstrap tracker unit tests.

## 0.1.0

- Initial Forge bootstrap, API, service registry, logging and `/furusato info`.
