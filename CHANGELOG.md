# Changelog

## 0.4.1

- Replaced the chat command interface with a dedicated status screen on F8.
- Added Overview, Modules and Services tabs with state-aware colors.
- Added a remappable FurusatoCore key binding and non-pausing screen behavior.
- Added scrolling for longer module and service lists.
- Isolated all client-only code behind Forge client/common proxies.
- Removed `/furusato` command registration and its chat output.

## 0.4.0

- Added typed service metadata with stable IDs, owner modules and thread policies.
- Added immutable, deterministically ordered service snapshots.
- Added `require(contract, consumerModuleId)` with consumer tracking.
- Added typed errors for missing, duplicate, invalid and late registrations.
- Added automatic cleanup of services owned by a module that fails to load.
- Preserved the original `register`, `get` and `find` API.
- Registered Module Manager as the first built-in Core service.
- Added service count to `/furusato info`.
- Added colored `/furusato services` output.

## 0.3.1

- Added colored chat output for `/furusato info` and `/furusato modules`.
- Module entries now use distinct colors for enabled, failed, disabled, loaded
  and discovered states.

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
