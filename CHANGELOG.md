# Changelog

## 0.3.0

- Added explicit module registration through `IModuleManager`.
- Added immutable module metadata with required and optional dependencies.
- Added deterministic dependency ordering and cycle detection.
- Added module lifecycle states and immutable status snapshots.
- Added failure isolation for non-critical modules and dependent-module disablement.
- Added rollback for partially loaded modules and cleanup after critical failure.
- Added bootstrap abort semantics for required Core modules.
- Added reverse-order module shutdown support.
- Added the mandatory built-in `core` runtime module.
- Added `/furusato modules` and module startup logging.
- Added unit tests for ordering, missing dependencies, optional dependencies,
  cycles, duplicate IDs, lifecycle failures and shutdown order.

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
