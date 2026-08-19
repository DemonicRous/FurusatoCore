# FurusatoCore Roadmap

Статус: согласованный базовый план. Состав конкретной версии может уточняться до начала её реализации. Новые функции не должны переноситься в более раннюю версию, если это угрожает стабильности её основного milestone.

## Версии

| Версия | Основной результат |
|---|---|
| `0.1.0` | Воспроизводимая Forge-сборка, metadata, logging и `/furusato info` |
| `0.2.0` | `CoreState`, `BootstrapStage`, timing и bootstrap report |
| `0.3.0` | Module Manager, dependency graph и failure isolation |
| `0.4.0` | Service Registry и минимальная API-инфраструктура |
| `0.5.0` | Config v1: defaults, validation и атомарная запись |
| `0.6.0` | Config schema versions, migrations, backup и recovery |
| `0.7.0` | Environment Scanner и Mod Scanner |
| `0.8.0` | Diagnostics model, severity и health aggregation |
| `0.9.0` | TXT/JSON reports, privacy redaction и environment fingerprint |
| `0.10.0` | Commands, RU/EN localization и developer mode |
| `0.11.0` | Patch framework, fail-closed application, status и error codes |
| `0.12.0` | `UnicodeGuiScalePatch`, safe mode и compatibility tests |
| `0.13.0` | Font resources, font diagnostics, resource reload и Unicode test sheet |
| `0.14.0` | Extension API и первый тестовый внешний Furusato-мод |
| `0.15.0` | Profiler API, NOOP mode, sections и базовые метрики |
| `0.16.0` | Hierarchical snapshots, ring buffers, memory и thread diagnostics |
| `0.17.0` | Class Owner Resolver и Stack Trace Analyzer |
| `0.18.0` | Crash Analysis, Crash Context и эвристическая attribution |
| `0.19.0` | Compatibility rules, transformer и Mixin diagnostics |
| `0.20.0` | Stabilization milestone: API candidate, release hardening и compatibility matrix |

`0.20.0` не является `1.0.0`. Это кандидат на заморозку API, после практической проверки которого планируется переход к `1.0.0` или дополнительные `0.x` milestones.

Networking, общий worker pool и сложный render profiler не входят в этот диапазон без подтверждённого use case.

## Release channels

| Формат | Назначение | CurseForge / Modrinth |
|---|---|---|
| `0.x.0-alpha.N` | Раннее тестирование | Alpha |
| `0.x.0-beta.N` | Публичное тестирование | Beta |
| `0.x.0` | Принятый milestone | Release |

## Публикация

Каждая версия должна собираться один раз. Один и тот же production JAR публикуется на CurseForge и Modrinth.

Обязательные артефакты и metadata:

- `furusatocore-<version>.jar`;
- sources JAR;
- SHA-256 и SHA-512 production JAR;
- RU/EN changelog;
- Minecraft `1.12.2` и loader `Forge`;
- client/server environment;
- required, optional и incompatible dependencies;
- release channel;
- Core, API, build и commit versions в manifest.

Публикация блокируется, если:

- не выбрана и не добавлена лицензия;
- JAR не прошёл тесты и clean Forge smoke test;
- metadata двух площадок различаются по версии, loader, environment или dependencies;
- checksum публикуемого JAR не совпадает с release artifact.

