package dev.demonicrous.furusato.core.bootstrap;

import dev.demonicrous.furusato.api.bootstrap.BootstrapReport;
import dev.demonicrous.furusato.api.bootstrap.BootstrapStage;
import java.util.EnumMap;
import java.util.Map;

public final class BootstrapTracker {
    private final NanoClock clock;
    private final long startedAtEpochMillis;
    private final long startedAtNanos;
    private final Map<BootstrapStage, Long> durations =
            new EnumMap<BootstrapStage, Long>(BootstrapStage.class);
    private BootstrapStage activeStage;
    private long activeStageStartedNanos;
    private long completedAtNanos = -1L;

    public BootstrapTracker() {
        this(new NanoClock() {
            @Override
            public long nanoTime() {
                return System.nanoTime();
            }
        }, System.currentTimeMillis());
    }

    BootstrapTracker(NanoClock clock, long startedAtEpochMillis) {
        this.clock = clock;
        this.startedAtEpochMillis = startedAtEpochMillis;
        this.startedAtNanos = clock.nanoTime();
    }

    public synchronized void begin(BootstrapStage stage) {
        if (completedAtNanos >= 0L) {
            throw new IllegalStateException("Bootstrap is already complete");
        }
        if (activeStage != null) {
            throw new IllegalStateException("Bootstrap stage is already active: " + activeStage);
        }
        if (durations.containsKey(stage)) {
            throw new IllegalStateException("Bootstrap stage was already measured: " + stage);
        }
        activeStage = stage;
        activeStageStartedNanos = clock.nanoTime();
    }

    public synchronized long end(BootstrapStage stage) {
        if (activeStage != stage) {
            throw new IllegalStateException(
                    "Cannot end " + stage + "; active stage is " + activeStage);
        }
        long now = clock.nanoTime();
        long duration = Math.max(0L, now - activeStageStartedNanos);
        durations.put(stage, duration);
        activeStage = null;
        if (stage == BootstrapStage.LOAD_COMPLETE) {
            completedAtNanos = now;
        }
        return duration;
    }

    public synchronized BootstrapReport snapshot() {
        long end = completedAtNanos >= 0L ? completedAtNanos : clock.nanoTime();
        long total = 0L;
        for (Long duration : durations.values()) {
            total += duration;
        }
        if (activeStage != null) {
            total += Math.max(0L, end - activeStageStartedNanos);
        }
        return new BootstrapReport(startedAtEpochMillis, total,
                Math.max(0L, end - startedAtNanos), completedAtNanos >= 0L, durations);
    }
}
