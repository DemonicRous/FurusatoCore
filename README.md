
# FurusatoCore
## Техническое задание на разработку ядра экосистемы Furusato

**Целевая платформа:** Minecraft Java Edition 1.12.2  
**Mod Loader:** Minecraft Forge 1.12.2  
**Тип проекта:** Core / Runtime / API / Diagnostics  
**Mod ID:** `furusatocore`  
**Основной package:** `dev.demonicrous.furusato.core`  
**Минимальная Java:** Java 8  
**Лицензия:** определяется отдельно

---

# 1. Назначение проекта

`FurusatoCore` является фундаментальным модом экосистемы **Furusato**.

Все основные моды линейки:

```text
FurusatoCore
FurusatoUI
FurusatoHUD
FurusatoInventory
FurusatoTweaks
FurusatoPerformance
FurusatoWhatever
```

могут использовать API, сервисы и инфраструктуру `FurusatoCore`.

Главные задачи ядра:

1. предоставить единый runtime для модов Furusato;
2. предоставить стабильный публичный API;
3. реализовать систему модулей и сервисов;
4. исправить проблемы Unicode-шрифтов Minecraft 1.12.2;
5. предоставить централизованную систему конфигурации;
6. предоставить единую систему логирования;
7. предоставить диагностическую инфраструктуру;
8. собирать информацию о Minecraft, Forge, JVM, видеосистеме и установленных модах;
9. обнаруживать потенциальные конфликты;
10. предоставлять другим Furusato-модам profiler API;
11. предоставлять crash diagnostics API;
12. иметь удобный developer mode;
13. минимально вмешиваться в gameplay;
14. сохранять максимальную совместимость со сторонними модами.

`FurusatoCore` не должен становиться набором пользовательских QoL-функций.

Например:

```text
Auto Refill           → FurusatoInventory
Armor HUD             → FurusatoHUD
GUI framework         → FurusatoUI
Toggle Sprint         → FurusatoTweaks
Entity optimizations  → FurusatoPerformance
```

В `FurusatoCore` должны находиться только функции, необходимые экосистеме в целом.

---

# 2. Основная философия

Архитектура строится вокруг следующего принципа:

```text
                  FurusatoCore
                       │
        ┌──────────────┼──────────────┐
        │              │              │
      Runtime         API        Diagnostics
        │              │              │
   ┌────┼─────┐   ┌────┼────┐    ┌────┼────┐
   │    │     │   │    │    │    │    │    │
Config Event Service HUD Profiler │ Crash  Env
                                  │
               ┌──────────────────┴────────────────┐
               │                                   │
        FurusatoInventory                    FurusatoHUD
               │                                   │
        FurusatoTweaks                       FurusatoUI
```

Ядро должно быть:

- маленьким;
- предсказуемым;
- стабильным;
- расширяемым;
- диагностируемым;
- максимально безопасным при ошибках зависимых модов.

---

# 3. Версионирование

Использовать Semantic Versioning:

```text
MAJOR.MINOR.PATCH
```

Например:

```text
1.0.0
1.1.0
1.1.1
2.0.0
```

Значение:

```text
PATCH
исправления без изменения публичного API

MINOR
новые обратно совместимые API

MAJOR
breaking changes
```

Пример зависимости FurusatoHUD:

```text
required-after:furusatocore@[1.2.0,2.0.0)
```

Публичный API должен иметь собственную версию:

```java
FurusatoAPI.VERSION
```

Например:

```java
public final class FurusatoAPI {

    public static final int API_VERSION = 1;

}
```

---

# 4. Структура проекта

Публичный API должен находиться в отдельном корневом package:

```text
dev.demonicrous.furusato.api
```

Реализация ядра:

```text
dev.demonicrous.furusato.core

├── FurusatoCore.java
│
├── bootstrap
│   ├── FurusatoBootstrap.java
│   ├── BootstrapStage.java
│   └── BootstrapReport.java
│
├── module
│   ├── ModuleManager.java
│   ├── ModuleContainer.java
│   ├── ModuleState.java
│   └── DependencyResolver.java
│
├── service
│   ├── ServiceRegistry.java
│   ├── ServiceContainer.java
│   └── ServiceException.java
│
├── config
│   ├── ConfigManager.java
│   ├── ConfigSchema.java
│   ├── ConfigMigration.java
│   └── ConfigValidation.java
│
├── diagnostics
│   ├── DiagnosticsManager.java
│   ├── EnvironmentScanner.java
│   ├── ModScanner.java
│   ├── ConflictScanner.java
│   └── DiagnosticReport.java
│
├── profiler
│   ├── ProfilerManager.java
│   ├── ProfilerSection.java
│   ├── ProfilerSnapshot.java
│   └── PerformanceMetric.java
│
├── crash
│   ├── CrashAnalyzer.java
│   ├── CrashContext.java
│   ├── StackTraceAnalyzer.java
│   └── ClassOwnerResolver.java
│
├── font
│   ├── UnicodeFontFix.java
│   ├── FontDiagnostics.java
│   └── FontHooks.java
│
├── network
│   ├── FurusatoNetwork.java
│   ├── PacketRegistry.java
│   └── packet
│
├── platform
│   ├── ForgePlatform.java
│   ├── MinecraftPlatform.java
│   └── JavaPlatform.java
│
├── hooks
│   ├── client
│   └── common
│
├── event
│   ├── FurusatoEventBus.java
│   └── events
│
├── command
│   └── FurusatoCommand.java
│
├── util
│
└── internal
```

Ключевой принцип:

```text
dev.demonicrous.furusato.api
```

никогда не должен импортировать:

```text
internal
```

Зависимые моды Furusato также не должны обращаться к `internal`.

---

# 5. Lifecycle FurusatoCore

Ядро должно разделять глобальное состояние runtime, этапы Forge bootstrap и повторяемые события мира.

```text
NEW → STARTING → AVAILABLE → STOPPING → STOPPED
                    └→ FAILED
```

Глобальное состояние:

```java
public enum CoreState {
    NEW,
    STARTING,
    AVAILABLE,
    STOPPING,
    STOPPED,
    FAILED
}
```

Этапы bootstrap:

```java
public enum BootstrapStage {
    CONSTRUCT,
    PRE_INIT,
    INIT,
    POST_INIT,
    LOAD_COMPLETE
}
```

`WORLD_LOAD` и `WORLD_UNLOAD` не являются глобальными состояниями: они оформляются как повторяемые `WorldLoadedEvent` и `WorldUnloadedEvent`. Это позволяет корректно перезаходить в миры без искажения состояния Core.

Необходимо отслеживать время каждого этапа:

```text
FurusatoCore Bootstrap

BOOTSTRAP       84 ms
PRE_INIT       137 ms
INIT            42 ms
POST_INIT       18 ms

Total          281 ms
```

Эта информация должна попадать в diagnostic report.

---

# 6. Module System

`FurusatoCore` должен содержать собственную лёгкую систему модулей.

Пример:

```java
public interface IFurusatoModule {

    String getId();

    String getName();

    void initialize();

    void shutdown();

}
```

Более развитый вариант:

```java
public interface IFurusatoModule {

    ModuleMetadata metadata();

    void onLoad(ModuleContext context);

    void onEnable();

    void onDisable();

}
```

Metadata:

```java
public final class ModuleMetadata {

    private final String id;
    private final String name;
    private final String version;

}
```

Пример внутренних модулей:

```text
font
diagnostics
profiler
network
environment
crash
compatibility
```

Module Manager:

```java
FurusatoModules.register(...);

FurusatoModules.enable("diagnostics");

FurusatoModules.disable("profiler");
```

Внутренний граф:

```text
Diagnostics
    │
    ├── Environment
    ├── ModScanner
    └── Profiler

CrashAnalyzer
    │
    ├── Environment
    └── ModScanner
```

Обязательный контракт Module Manager:

- module ID уникален и соответствует `[a-z][a-z0-9_.-]{1,63}`;
- поддерживаются required и optional dependencies;
- циклы и отсутствующие required dependencies обнаруживаются до запуска модулей;
- порядок запуска детерминирован топологической сортировкой;
- состояния: `DISCOVERED`, `LOADED`, `ENABLED`, `DISABLED`, `FAILED`;
- отказ required dependency переводит зависимый модуль в `DISABLED` с диагностической причиной;
- lifecycle модулей выполняется на основном потоке;
- повторное включение разрешено только модулям, явно объявившим такую capability;
- mandatory bootstrap-модули нельзя отключать в runtime;
- ошибка optional-модуля не должна останавливать Core.

---

# 7. Service Registry

Для связи между модулями Furusato необходимо реализовать Service Registry.

Пример:

```java
FurusatoAPI.services()
        .get(IProfilerService.class);
```

Регистрация:

```java
services.register(
    IProfilerService.class,
    profilerManager
);
```

API:

```java
public interface IServiceRegistry {

    <T> void register(
        Class<T> service,
        T implementation
    );

    <T> T get(Class<T> service);

    <T> Optional<T> find(Class<T> service);

}
```

Это позволит избежать статических зависимостей вроде:

```java
FurusatoCore.INSTANCE.profiler.manager...
```

Контракт registry v1:

- на один service type регистрируется один implementation;
- повторная регистрация является ошибкой;
- `get()` выбрасывает `ServiceNotFoundException`, `find()` возвращает `Optional`;
- реестр изменяется только до `CoreState.AVAILABLE`, затем замораживается;
- runtime replacement в API v1 не поддерживается;
- после заморозки read-операции thread-safe;
- lifecycle implementation принадлежит Module Manager, а не Service Registry.

---

# 8. Furusato API

Основная точка входа:

```java
FurusatoAPI.get();
```

Например:

```java
FurusatoAPI
    .get()
    .services()
    .get(IProfilerService.class);
```

Предлагаемый API:

```java
public interface IFurusatoAPI {

    IServiceRegistry services();

    IEnvironmentService environment();

    IDiagnosticsService diagnostics();

    IProfilerService profiler();

    IEventService events();

}
```

При этом:

```text
FurusatoCore implementation
          ↑
          │
    Furusato API
          ↑
   ┌──────┼────────┐
   │      │        │
 HUD  Inventory  Tweaks
```

Зависимые моды должны обращаться преимущественно к интерфейсам.

---

# 9. Unicode Font Fix

Это первая встроенная пользовательская функция ядра.

Задача:

**исправить отображение Unicode-символов и обеспечить одинаковое корректное текстовое отображение для всех Furusato-модов.**

Модуль:

```text
furusato.font
```

Реализация разделяется на два компонента:

```text
UnicodeGuiScalePatch
UnicodeFontFix
```

`UnicodeGuiScalePatch` — точечный ASM-патч `ScaledResolution`, который не позволяет vanilla Minecraft принудительно уменьшать нечётный GUI Scale при Unicode-шрифте. Он не считается полноценным font renderer fix.

`UnicodeFontFix` отвечает за glyph resources, метрики, кэши, reload и совместимость с vanilla font pipeline.

Исходная реализация `UnicodeGuiScalePatch`, `glyph_sizes.bin` и страницы `unicode_page_*.png` взяты из авторского проекта `DemonicRous/Furusato-OLD`. Автор подтвердил право их переноса и использования в FurusatoCore. Перед публичной дистрибуцией это разрешение должно быть закреплено в выбранной лицензии проекта.

## 9.1 Требования

Unicode fix должен:

- корректно отображать кириллицу;
- корректно отображать Latin Extended;
- корректно отображать распространённые Unicode glyphs;
- устранять визуальные дефекты масштабирования;
- сохранять Minecraft formatting codes;
- корректно работать с GUI Scale;
- корректно работать с resource packs;
- корректно работать при смене языка;
- не ломать vanilla FontRenderer API.

## 9.2 Архитектура

API:

```java
public interface IFontService {

    int getStringWidth(String text);

    void drawString(
        String text,
        float x,
        float y,
        int color
    );

    boolean canRender(char character);

}
```

Но сторонние моды не должны быть обязаны использовать этот интерфейс.

Базовый Unicode fix должен применяться к vanilla font pipeline таким образом, чтобы:

```text
Minecraft
Forge mods
Furusato mods
```

получали исправление автоматически.

Патч должен fail closed: если структура target-класса не совпадает с ожидаемой, он оставляет исходный bytecode, фиксирует `SKIPPED` в diagnostics и не мешает запуску Minecraft.

---

# 10. Font Diagnostics

Developer mode должен предоставлять:

```text
/furusato font
```

Пример:

```text
Furusato Font Diagnostics

Renderer:
Vanilla Unicode Renderer

Language:
ru_RU

Unicode:
Enabled

Loaded glyph pages:
47

Missing glyphs:
3
```

Команда:

```text
/furusato font test
```

отображает test sheet:

```text
English:
ABCDEFGHIJKLMNOPQRSTUVWXYZ

Russian:
АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ

Symbols:
→ ← ↑ ↓ ✓ ✕ • ★ ♥
```

---

# 11. Configuration System

Несмотря на наличие Forge config API, FurusatoCore должен предоставить унифицированный слой конфигурации для всей экосистемы. Forge 1.12.2 имеет как традиционный `Configuration`, так и аннотационный `@Config` механизм. citeturn979746search1

Файлы:

```text
config/
└── furusato/
    ├── core.cfg
    ├── diagnostics.cfg
    ├── font.cfg
    ├── profiler.cfg
    └── modules/
```

Позднее:

```text
furusato/
├── core.cfg
├── hud.cfg
├── inventory.cfg
└── tweaks.cfg
```

API:

```java
FurusatoConfig.register(
    "inventory",
    InventoryConfig.class
);
```

---

# 12. Config Validation

Конфигурация не должна молча принимать некорректные значения.

Например:

```text
profiler.sampleRate = -500
```

результат:

```text
[WARN] Invalid config value:
profiler.sampleRate=-500

Allowed:
1..1000

Using default:
60
```

Необходимо поддерживать:

```text
range validation
enum validation
null validation
dependency validation
```

---

# 13. Config Migration

При изменении схемы:

```text
v1
↓
v2
↓
v3
```

старые конфиги должны мигрировать автоматически.

Интерфейс:

```java
public interface IConfigMigration {

    int fromVersion();

    int toVersion();

    void migrate(ConfigData config);

}
```

В конфиге:

```text
schemaVersion=3
```

---

# 14. Environment Scanner

Это одна из ключевых частей FurusatoCore.

Environment scanner должен собирать:

```text
Minecraft
Forge
FurusatoCore
Java
JVM
OS
CPU
Memory
GPU
OpenGL
Display
Mods
Coremods
Mixins
Resource Packs
Languages
Arguments
```

Пример:

```text
ENVIRONMENT

Minecraft:
1.12.2

Forge:
14.23.5.x

FurusatoCore:
1.2.0

Java:
1.8.0_xxx

JVM:
OpenJDK 64-Bit Server VM

OS:
Windows 11 x64

Memory:
4096 / 8192 MB

GPU:
NVIDIA ...

OpenGL:
4.x
```

При этом sensitive/private параметры JVM или системного окружения не должны автоматически попадать в пользовательские export-файлы.

---

# 15. Mod Scanner

Необходимо построить внутреннее представление установленных модов.

Модель:

```java
public final class ModDescriptor {

    String modId;
    String name;
    String version;

    File source;

    List<String> dependencies;

}
```

Получение:

```java
FurusatoAPI
    .get()
    .environment()
    .mods();
```

Поиск:

```java
environment.hasMod("jei");
```

или:

```java
environment.findMod("jei");
```

---

# 16. Class Owner Resolver

Один из важнейших диагностических компонентов.

Задача:

по Java class определить, из какого JAR/mod она была загружена.

Например:

```text
Class:
crazypants.enderio.conduits.render.ConduitRenderer

Owner:
Ender IO

Mod ID:
enderio
```

API:

```java
Optional<ModDescriptor> findOwner(
    Class<?> clazz
);
```

и:

```java
Optional<ModDescriptor> findOwner(
    String className
);
```

Используется:

```text
Crash Analyzer
Profiler
Conflict Detector
Inspector
```

---

# 17. Diagnostics Manager

Главный интерфейс:

```java
IDiagnosticsService
```

API:

```java
DiagnosticReport createReport();
```

Report должен содержать секции:

```text
Environment
Mods
Coremods
Graphics
Memory
Performance
Configuration
Compatibility
Warnings
Errors
Furusato Modules
```

---

# 18. Diagnostic Report

Команда:

```text
/furusato report
```

создаёт:

```text
.minecraft/
furusato/
reports/
2026-08-19_09-41-22.txt
```

или:

```text
.json
```

Предпочтительно генерировать одновременно:

```text
human-readable TXT
machine-readable JSON
```

Пример структуры JSON:

```json
{
  "furusato": {},
  "minecraft": {},
  "java": {},
  "graphics": {},
  "mods": [],
  "warnings": [],
  "performance": {}
}
```

---

# 19. Diagnostic Severity

Каждая проблема получает severity:

```java
INFO
NOTICE
WARNING
ERROR
CRITICAL
```

Например:

```text
[INFO]
OptiFine detected

[WARNING]
Multiple render transformers detected

[ERROR]
Required Furusato API version mismatch

[CRITICAL]
Furusato module failed during bootstrap
```

---

# 20. Health System

Необходимо ввести понятие состояния клиента:

```text
HEALTHY
WARNING
DEGRADED
BROKEN
```

Пример:

```text
Furusato System Health

Status:
WARNING

2 potential compatibility issues
1 configuration warning
0 module failures
```

---

# 21. Compatibility Scanner

Первая версия должна использовать rule-based compatibility database.

Например:

```json
{
  "rule": "render.optifine.example",
  "mods": [
    "optifine",
    "examplemod"
  ],
  "severity": "warning",
  "message": "Both mods modify rendering pipeline"
}
```

Rule API:

```java
public interface ICompatibilityRule {

    boolean matches(Environment environment);

    DiagnosticIssue evaluate(Environment environment);

}
```

---

# 22. Conflict Detection

Позднее система должна уметь видеть:

```text
Coremods
ASM Transformers
Mixins
Render hooks
Duplicate libraries
Keybind conflicts
```

Пример:

```text
POTENTIAL CONFLICT

Class:
net.minecraft.client.renderer.RenderGlobal

Modified by:
OptiFine
ExampleCoremod

Risk:
HIGH
```

Важно:

система должна говорить именно:

```text
potential conflict
```

если конфликт не доказан.

Она не должна выдавать эвристику за факт.

---

# 23. Mixin / Transformer Diagnostics

Mixin представляет собой bytecode transformation framework и применяется во время class loading; конфигурация Mixin разделяет common/client/server-наборы. Поэтому для FurusatoCore следует держать вмешательства минимальными и чётко разделять client/common hooks. citeturn710649search31turn710649search3turn710649search8

Developer mode должен уметь отображать:

```text
MIXIN STATUS

Loaded configs:
furusatocore.mixins.json

Applied:
17

Failed:
0
```

Для собственного Mixin:

```text
MixinFontRenderer
Target:
net.minecraft.client.gui.FontRenderer

Status:
APPLIED
```

---

# 24. Crash Analyzer

Crash Analyzer должен быть отдельным сервисом.

```java
ICrashAnalysisService
```

Вход:

```java
Throwable
```

Выход:

```java
CrashAnalysis
```

Пример:

```text
CRASH ANALYSIS

Exception:
NullPointerException

Root cause:
crazypants.enderio....

Likely mod:
Ender IO

Confidence:
HIGH

Chain:
Minecraft
→ EnderIO
→ NullPointerException
```

---

# 25. Stack Trace Analyzer

Analyzer должен:

1. найти root cause;
2. пройти весь cause chain;
3. убрать Java/Minecraft noise;
4. определить первые mod-owned classes;
5. построить список подозреваемых модов.

Например:

```text
Suspected mods:

1. enderio
   confidence: 87%

2. thermalfoundation
   confidence: 21%
```

Важно:

confidence является эвристикой.

---

# 26. Crash Context

Перед потенциально опасными операциями компоненты Furusato могут добавлять context:

```java
try (
    CrashContext context =
        FurusatoCrash.context("Rendering HUD")
) {

    ...

}
```

При exception:

```text
Furusato Context:
Rendering HUD

Module:
FurusatoHUD

Widget:
ArmorWidget
```

Это значительно упростит диагностику Furusato-модов.

---

# 27. Profiler API

Это одна из самых важных частей ядра.

API:

```java
try (
    ProfilerSection ignored =
        FurusatoProfiler.section("inventory.scan")
) {

    ...
}
```

или:

```java
ProfilerToken token =
    profiler.begin("inventory.scan");

...

profiler.end(token);
```

Первый вариант предпочтительнее.

---

# 28. Hierarchical Profiling

Необходимо поддерживать:

```text
frame
├── hud
│   ├── armor
│   ├── potion
│   └── inventory
├── diagnostics
└── furusatoTweaks
```

Snapshot:

```text
FRAME 7.82 ms

FurusatoHUD
  0.71 ms

  Armor
    0.11 ms

  Inventory
    0.42 ms

FurusatoInventory
  0.18 ms
```

---

# 29. Profiler Metric Types

Поддержать:

```text
TIME
COUNT
GAUGE
BYTES
RATE
```

API:

```java
metrics.counter(
    "inventory.itemsScanned"
).increment();
```

```java
metrics.gauge(
    "memory.cacheSize",
    cache.size()
);
```

---

# 30. Profiler Overhead

Критически важное требование:

выключенный profiler должен иметь практически нулевой runtime overhead.

То есть:

```java
if (!enabled) {
    return NOOP_SECTION;
}
```

Нельзя постоянно:

```text
создавать новые объекты
строить String
собирать stack trace
```

в hot path.

---

# 31. Ring Buffer Metrics

Frametime/history хранить в кольцевом буфере.

Например:

```text
last 600 frames
```

Без бесконечного роста RAM.

Пример:

```java
RingBuffer<FrameMetric>
```

---

# 32. Memory Diagnostics

Сервис должен показывать:

```text
Heap used
Heap committed
Heap max

Non-heap

GC count
GC time

Furusato caches
```

Пример:

```text
MEMORY

Heap:
3821 / 8192 MB

Usage:
46%

GC:
31 collections
842 ms total
```

---

# 33. Render Diagnostics API

FurusatoCore не должен заниматься оптимизацией renderer — это задача возможного `FurusatoPerformance`.

Но ядро должно предоставить точки для измерения:

```text
world render
HUD render
GUI render
entities
tile entities
particles
```

где это возможно без опасного вмешательства.

---

# 34. Thread Diagnostics

Необходимо знать основные потоки:

```text
Client thread
Server thread
Netty threads
Worker threads
```

Developer API:

```java
ThreadGuard.assertClientThread();
```

Например:

```text
[ERROR]
FurusatoInventory attempted Minecraft operation
outside client thread.

Thread:
Furusato-Worker-2
```

---

# 35. Furusato Task Scheduler

Единый scheduler для Furusato-модов.

API:

```java
FurusatoScheduler.client().execute(...);
```

```java
FurusatoScheduler.async().execute(...);
```

Не создавать бесконтрольно:

```text
new Thread(...)
```

из каждого Furusato-мода.

---

# 36. Worker Pool

Ядро может иметь ограниченный worker pool:

```text
Furusato-Worker-1
Furusato-Worker-2
```

Использовать только для задач, не трогающих небезопасные Minecraft объекты.

Например:

```text
report serialization
log analysis
config parsing
file hashing
```

---

# 37. Event API

Forge уже использует event bus для событий vanilla/Forge. FurusatoCore должен использовать Forge events там, где они достаточны, а собственный event layer — только для событий экосистемы Furusato. citeturn979746search2

Примеры собственных событий:

```text
FurusatoReadyEvent

FurusatoModuleEnabledEvent

FurusatoModuleDisabledEvent

FurusatoDiagnosticEvent

FurusatoConfigReloadEvent
```

Нельзя без необходимости дублировать:

```text
RenderGameOverlayEvent
ClientTickEvent
WorldEvent.Load
```

---

# 38. Networking

Forge 1.12.2 предоставляет `SimpleNetworkWrapper`, предназначенный для message-based custom networking. FurusatoCore может обернуть его своим API, но не должен писать собственный сетевой стек без необходимости. citeturn710649search5

Namespace:

```text
furusato
```

Network version:

```text
1
```

Handshake:

```text
Client FurusatoCore version
Server FurusatoCore version
Enabled extensions
Protocol version
```

При этом `FurusatoCore` должен оставаться работоспособным как client-side dependency, если сервер его не имеет, если конкретная функция не требует server-side части.

---

# 39. Packet Registry

Центральная регистрация:

```java
FurusatoNetwork.register(
    PacketDiagnostics.class
);
```

Не позволять каждому модулю самостоятельно выбирать packet ID.

Registry должен гарантировать отсутствие collision.

---

# 40. Client / Server Separation

Клиентские классы нельзя случайно загружать dedicated server-side.

Forge отдельно различает physical и logical side, и некорректное обращение к client-only классам является распространённым источником проблем. citeturn710649search0

Поэтому структура:

```text
common/
client/
server/
```

или proxy abstraction должна соблюдаться строго.

---

# 41. Logging

Единый logger:

```java
FurusatoLog.info(...)
FurusatoLog.warn(...)
FurusatoLog.error(...)
FurusatoLog.debug(...)
FurusatoLog.trace(...)
```

Формат:

```text
[Furusato/Core]
[Furusato/Font]
[Furusato/Profiler]
[Furusato/Diagnostics]
```

Пример:

```text
[Furusato/Font] Unicode font patch applied
```

---

# 42. Structured Logging

Дополнительно предусмотреть:

```java
FurusatoLog.event(
    "module_loaded",
    "module", "font",
    "time", 27
);
```

Для debug builds.

---

# 43. Developer Mode

Config:

```text
developerMode=true
```

Активирует:

```text
extra diagnostics
strict validation
API warnings
profiler tools
debug commands
hook status
class ownership
```

В production/default:

```text
developerMode=false
```

---

# 44. Commands

Главная команда:

```text
/furusato
```

Подкоманды:

```text
/furusato help

/furusato info

/furusato mods

/furusato modules

/furusato health

/furusato report

/furusato profiler

/furusato memory

/furusato font

/furusato conflicts

/furusato class <class>

/furusato config reload
```

---

# 45. `/furusato info`

Пример:

```text
FurusatoCore 1.0.0

Minecraft:
1.12.2

Forge:
14.23.5.x

Java:
8

Modules:
7/7 active

Health:
Healthy
```

---

# 46. `/furusato modules`

```text
Furusato Modules

[✓] Bootstrap
[✓] Font
[✓] Environment
[✓] Diagnostics
[✓] Profiler
[✓] Crash Analyzer
[✓] Network
```

---

# 47. `/furusato health`

```text
FURUSATO HEALTH

Overall:
WARNING

Warnings:
2

Errors:
0

Potential conflicts:
1

Configuration issues:
1
```

---

# 48. `/furusato class`

Например:

```text
/furusato class net.minecraft.client.gui.FontRenderer
```

Результат:

```text
CLASS INFO

Name:
net.minecraft.client.gui.FontRenderer

Source:
Minecraft

Transformed:
yes

Known Furusato hooks:
UnicodeFontFix
```

---

# 49. Diagnostic GUI

Для `1.0` GUI не является обязательным.

Первоначально использовать:

```text
commands
chat output
files
logs
```

Позже `FurusatoUI` может предоставить GUI.

То есть архитектура:

```text
FurusatoCore
     │
     └── IDiagnosticsService
              ↑
              │
         FurusatoUI
              │
     DiagnosticScreen
```

Это хороший пример правильной границы ответственности.

---

# 50. Furusato Extension API

Каждый Furusato-мод должен иметь возможность зарегистрировать diagnostic provider.

```java
FurusatoAPI
    .diagnostics()
    .registerProvider(
        new InventoryDiagnosticProvider()
    );
```

Provider:

```java
public interface IDiagnosticProvider {

    String getId();

    void collect(
        DiagnosticCollector collector
    );

}
```

Тогда общий report автоматически содержит:

```text
FurusatoCore
FurusatoHUD
FurusatoInventory
FurusatoTweaks
```

---

# 51. Mod Metadata API

Каждый Furusato-мод регистрируется:

```java
FurusatoExtension extension =
    FurusatoExtension.builder()
        .id("furusatoinventory")
        .name("FurusatoInventory")
        .version(VERSION)
        .build();
```

После чего Core знает всю экосистему.

---

# 52. Extension Capabilities

Extension сообщает:

```text
diagnostics
profiler
network
config
ui
```

Например:

```text
FurusatoInventory

Diagnostics: yes
Profiler: yes
Network: no
UI: yes
```

---

# 53. Failure Isolation

Крайне важный принцип.

Если диагностический provider стороннего Furusato-мода падает:

```java
provider.collect(...)
```

Core не должен падать вместе с ним.

Результат:

```text
[ERROR]

Diagnostic provider failed:

furusatoinventory

Exception:
NullPointerException
```

А report продолжается.

---

# 54. Safe Execution

API:

```java
FurusatoSafe.run(
    "inventory diagnostics",
    () -> ...
);
```

Результат ошибки сохраняется в diagnostics.

Но этим механизмом нельзя маскировать критические ошибки.

---

# 55. Compatibility Mode

Предусмотреть:

```text
NORMAL
SAFE
STRICT
```

`NORMAL`

обычная работа.

`SAFE`

отключение необязательных risky hooks.

`STRICT`

developer mode с жёсткими проверками.

---

# 56. Safe Mode

Если FurusatoCore обнаружил прошлый crash во время собственного bootstrap:

```text
Previous Furusato startup failed.

Safe Mode enabled.
```

Отключаются:

```text
optional mixins
experimental hooks
profiler extensions
compatibility patches
```

Базовые функции остаются:

```text
logging
diagnostics
environment
```

---

# 57. Startup Marker

При запуске:

```text
furusato/runtime.lock
```

или специальный startup marker.

После успешного запуска:

```text
state=healthy
```

Если приложение завершилось до успешного bootstrap, следующий запуск понимает, что предыдущий старт был аварийным.

---

# 58. Cache Manager

Единый API:

```java
FurusatoCaches.register(...)
```

Каждый cache должен предоставлять:

```text
name
entries
estimated size
clear()
```

Команда:

```text
/furusato cache
```

может показать:

```text
FontGlyphCache       1423 entries
ClassOwnerCache       812 entries
ModMetadataCache      217 entries
```

---

# 59. Resource Reload Support

Если Minecraft выполняет resource reload:

```text
fonts
resource-dependent caches
localization-related state
```

должны корректно пересоздаваться.

---

# 60. Localization

Все пользовательские сообщения должны использовать translation keys.

Например:

```text
furusato.command.health.title
furusato.diagnostic.conflict
furusato.font.missing_glyph
```

Файлы:

```text
assets/furusatocore/lang/en_us.lang
assets/furusatocore/lang/ru_ru.lang
```

---

# 61. Public API Stability

Все публичные классы размещаются:

```text
dev.demonicrous.furusato.api
```

Это окончательный package API v1. Вариант `dev.demonicrous.furusato.core.api` не используется.

Всё внутреннее:

```text
dev.demonicrous.furusato.core.internal
```

Зависимым модам запрещается импортировать `internal`.

---

# 62. API Annotation

Можно добавить:

```java
@FurusatoPublicAPI
```

и:

```java
@FurusatoInternal
```

Например:

```java
@FurusatoPublicAPI
public interface IProfilerService {
}
```

---

# 63. Experimental API

```java
@FurusatoExperimental
```

Означает:

API может измениться даже в MINOR release.

Это позволит развивать систему без раннего цементирования плохих решений.

---

# 64. Deprecated API

Стандартный:

```java
@Deprecated
```

плюс:

```java
@FurusatoRemoval(
    version = "2.0"
)
```

---

# 65. Dependency Validation

При старте Furusato-мода:

```text
FurusatoHUD 1.4.0
requires
FurusatoCore >= 1.2.0
```

Если установлен:

```text
FurusatoCore 1.1.0
```

пользователь должен получить понятную ошибку:

```text
FurusatoHUD cannot start.

Installed:
FurusatoCore 1.1.0

Required:
FurusatoCore >= 1.2.0
```

---

# 66. Startup Diagnostics

После `postInit`:

```text
[Furusato/Core] FurusatoCore 1.0.0 ready
[Furusato/Core] Bootstrap completed in 281 ms
[Furusato/Core] Loaded modules: 7
[Furusato/Core] Detected mods: 214
[Furusato/Core] Health: HEALTHY
```

Если проблема:

```text
[Furusato/Core] Health: WARNING
[Furusato/Core] Run /furusato health for details
```

---

# 67. Performance Budget

Ядро не должно само становиться причиной просадок.

Цели:

```text
idle CPU overhead:
максимально близкий к нулю

per-frame allocations:
минимальные

profiler disabled:
практически no-op

diagnostics:
event-driven / on-demand

environment scan:
однократно + cache
```

---

# 68. Запрещённые архитектурные решения

Не использовать глобально:

```java
public static HashMap...
```

для всего подряд.

Не использовать:

```java
new Thread(...)
```

в произвольных компонентах.

Не использовать exception как обычный control flow.

Не сканировать весь classpath каждый tick.

Не читать файлы каждый frame.

Не строить StackTrace каждый profiler sample.

Не выполнять reflection в hot loop без cache.

Не создавать новый formatter каждый render call.

---

# 69. Thread Safety

Общие сервисы должны документировать:

```text
CLIENT_THREAD_ONLY

THREAD_SAFE

IMMUTABLE

ASYNC_SAFE
```

Например:

```java
@ClientThreadOnly
void reloadFont();
```

---

# 70. Internal Error Handling

Любая внутренняя ошибка получает уникальный error code.

Например:

```text
FUR-BOOT-001
FUR-FONT-003
FUR-PROF-002
FUR-CFG-004
```

Сообщение:

```text
FUR-FONT-003

Failed to load Unicode glyph page.

Resource:
textures/font/unicode_page_04.png
```

Очень удобно для bug reports.

---

# 71. Error Code Structure

```text
FUR-[SUBSYSTEM]-[NUMBER]
```

Subsystem:

```text
BOOT
FONT
CFG
DIAG
PROF
NET
API
CRASH
COMPAT
```

---

# 72. Debug Snapshot

Команда:

```text
/furusato snapshot
```

создаёт моментальный state dump:

```text
Furusato snapshot

tick
FPS
memory
world
player
mods
modules
profiler
threads
```

Без полного crash report.

---

# 73. Privacy

При export diagnostic report по умолчанию не включать:

```text
access tokens
session tokens
authentication data
absolute user home paths
server passwords
sensitive JVM environment variables
```

Путь:

```text
C:\Users\Alexander\...
```

можно нормализовать:

```text
<USER_HOME>/...
```

---

# 74. Report Fingerprint

Для сравнения окружений можно создать fingerprint:

```text
Environment Fingerprint:
FUR-83F42A
```

Hash строится из:

```text
Minecraft version
Forge version
mod IDs + versions
Furusato versions
```

Это позволит сравнивать bug reports.

---

# 75. Build Metadata

В JAR добавить:

```text
Furusato-Version
Furusato-Commit
Furusato-Build
Furusato-Api-Version
Build-Time
```

`/furusato info` показывает:

```text
Version:
1.2.0

Build:
184

Commit:
8cf93fa
```

---

# 76. Reproducible Diagnostics

Каждый report должен содержать:

```text
core version
extension versions
config schema versions
environment fingerprint
```

Чтобы можно было точно воспроизвести состояние.

---

# 77. Тестирование

Минимальные unit tests:

```text
version parsing
dependency resolution
config migration
config validation
compatibility rules
stack trace parser
class-owner detection
ring buffer
module lifecycle
service registry
```

---

# 78. Integration Tests

Проверить окружения:

```text
Vanilla Forge

Forge + JEI

Forge + OptiFine

large modpack

Russian locale

English locale

Unicode enabled

Unicode disabled

resource reload

world reconnect
```

---

# 79. Compatibility Test Matrix

Отдельно создать документ:

```text
compatibility.md
```

Матрица:

```text
Mod              Version       Status

JEI              x.x.x         OK
OptiFine         ...           TESTED
VanillaFix       ...           TESTED
FoamFix          ...           TESTED
MixinBootstrap   ...           TESTED
```

Не писать `compatible`, пока комбинация реально не проверена.

---

# 80. Crash Testing

Намеренно создать:

```text
exception during module init
exception during diagnostics
exception during render hook
exception in async task
invalid config
missing resource
broken font page
```

И проверить, что Core выдаёт осмысленную диагностику.

---

# 81. Logging Tests

Проверить:

```text
10k repeated warnings
```

Система должна уметь suppress одинаковые сообщения:

```text
Warning repeated 9487 times.
```

чтобы лог не разрастался гигантскими объёмами.

---

# 82. Release Channels

Использовать:

```text
0.x.0-alpha.N
0.x.0-beta.N
0.x.0
```

Каналы одинаково отображаются на CurseForge и Modrinth:

```text
alpha → Alpha
beta  → Beta
final → Release
```

Один и тот же production JAR публикуется на обеих площадках. Полный release contract описан в `ROADMAP.md`.

---

# 83. Roadmap 0.1.0–0.20.0

Согласованный поэтапный план, release channels и требования к публикации вынесены в:

```text
ROADMAP.md
```

Диапазон `0.1.0–0.20.0` считается pre-1.0 development cycle. `0.20.0` — stabilization milestone и API candidate, а не автоматический stable release.

---

# 91. Что НЕ должно попасть в 1.0

Не надо сразу делать:

```text
полноценный profiler всего Minecraft
ASM conflict decompiler
GUI framework
inventory manager
FPS optimizer
entity culling
network inspector
packet logger
full crash recovery
```

Эти задачи резко увеличат scope.

Сначала `FurusatoCore` должен стать **надёжным фундаментом**.

---

# 92. Первый публичный API

Я бы заморозил только этот минимальный API:

```text
FurusatoAPI

IServiceRegistry

IFurusatoExtension

IEnvironmentService

IDiagnosticsService

IDiagnosticProvider

IProfilerService

ProfilerSection

IConfigProvider
```

Остальное до `1.0` считать internal/experimental.

---

# 93. Пример использования из FurusatoInventory

```java
@Mod(
    modid = "furusatoinventory",
    name = "FurusatoInventory",
    version = VERSION,
    dependencies =
        "required-after:furusatocore@[1.0.0,2.0.0)"
)
public final class FurusatoInventory {

    @Mod.EventHandler
    public void init(
        FMLInitializationEvent event
    ) {

        FurusatoAPI
            .get()
            .diagnostics()
            .registerProvider(
                new InventoryDiagnostics()
            );

    }

}
```

Profiler:

```java
try (
    ProfilerSection ignored =
        FurusatoAPI
            .get()
            .profiler()
            .section(
                "furusatoinventory.scan"
            )
) {

    scanInventory();

}
```

---

# 94. Пример итогового diagnostic report

```text
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        FURUSATO REPORT
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

FurusatoCore:
1.0.0

Minecraft:
1.12.2

Forge:
14.23.5.x

Environment:
HEALTHY

──────────────────────────────
FURUSATO MODULES
──────────────────────────────

Core               OK
Font               OK
Environment        OK
Diagnostics        OK
Profiler           OK
Crash Analyzer     OK

──────────────────────────────
EXTENSIONS
──────────────────────────────

FurusatoHUD
1.0.2
OK

FurusatoInventory
0.8.4
OK

──────────────────────────────
MEMORY
──────────────────────────────

Heap:
3.2 / 8.0 GB

──────────────────────────────
WARNINGS
──────────────────────────────

1 potential compatibility issue.

──────────────────────────────
COMPATIBILITY
──────────────────────────────

OptiFine detected.

No confirmed incompatibility.

──────────────────────────────
PERFORMANCE
──────────────────────────────

FurusatoCore:
0.06 ms/frame

FurusatoHUD:
0.32 ms/frame

FurusatoInventory:
0.04 ms/frame

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

---

# 95. Итоговое архитектурное правило

`FurusatoCore` отвечает на вопрос:

> **Что нужно каждому Furusato-моду?**

Если функциональность нужна практически всем:

```text
Core
```

Если она относится к конкретной пользовательской функции:

```text
отдельный Furusato-мод
```

Например:

```text
logging                   Core
configuration             Core
diagnostics               Core
profiler API              Core
extension API             Core
Unicode compatibility     Core

HUD widgets               HUD
inventory logic           Inventory
GUI framework             UI
QoL                       Tweaks
render optimization       Performance
```

Это правило должно соблюдаться на протяжении всей жизни проекта.

---

# 96. Определение готовности FurusatoCore 1.0

`FurusatoCore 1.0.0` считается готовым, если:

- запускается на чистом Forge 1.12.2;
- не требует других Furusato-модов;
- корректно работает как dependency;
- Unicode fix работает с RU/EN;
- resource reload не ломает font renderer;
- Module Manager проходит тесты;
- Service Registry проходит тесты;
- публичный API документирован;
- конфиги валидируются и мигрируют;
- diagnostic report создаётся;
- crash analyzer способен определить mod-owned stack frames;
- profiler API работает с выключенным и включённым profiler;
- Core переживает отсутствие optional компонентов;
- dedicated server не загружает client-only классы;
- предусмотрен safe mode;
- в штатном idle состоянии Core не создаёт заметной нагрузки;
- все public API классы отделены от internal implementation;
- FurusatoHUD или другой тестовый extension успешно использует Core без доступа к internal API.

---

# 97. Главная цель

В результате FurusatoCore должен восприниматься не как:

> «обязательная библиотека, которую зачем-то надо установить».

А как:

> **центральный runtime и диагностический слой экосистемы Furusato.**

Пользователь получает:

```text
Unicode fixes
better diagnostics
meaningful crash reports
compatibility information
```

Разработчик Furusato-мода получает:

```text
API
Services
Config
Profiler
Diagnostics
Logging
Scheduling
Environment
Networking
```

А вся экосистема получает единый фундамент:

```text
             FurusatoCore
                  │
      ┌───────────┼───────────┐
      ↓           ↓           ↓
 FurusatoUI  FurusatoHUD  FurusatoInventory
      ↓           ↓           ↓
          FurusatoTweaks
```
