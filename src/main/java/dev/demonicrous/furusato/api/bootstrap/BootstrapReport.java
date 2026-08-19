package dev.demonicrous.furusato.api.bootstrap;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.OptionalLong;

public final class BootstrapReport {
    private final long startedAtEpochMillis;
    private final long totalNanos;
    private final long elapsedNanos;
    private final boolean complete;
    private final Map<BootstrapStage, Long> stageDurationsNanos;

    public BootstrapReport(long startedAtEpochMillis, long totalNanos, long elapsedNanos,
            boolean complete,
            Map<BootstrapStage, Long> stageDurationsNanos) {
        this.startedAtEpochMillis = startedAtEpochMillis;
        this.totalNanos = Math.max(0L, totalNanos);
        this.elapsedNanos = Math.max(0L, elapsedNanos);
        this.complete = complete;
        this.stageDurationsNanos = Collections.unmodifiableMap(
                new EnumMap<BootstrapStage, Long>(stageDurationsNanos));
    }

    public long startedAtEpochMillis() {
        return startedAtEpochMillis;
    }

    public boolean isComplete() {
        return complete;
    }

    public long totalNanos() {
        return totalNanos;
    }

    public double totalMillis() {
        return totalNanos / 1_000_000.0D;
    }

    /** Wall-clock interval from Core construction to Forge load-complete. */
    public long elapsedNanos() {
        return elapsedNanos;
    }

    public double elapsedMillis() {
        return elapsedNanos / 1_000_000.0D;
    }

    public OptionalLong stageNanos(BootstrapStage stage) {
        Long duration = stageDurationsNanos.get(stage);
        return duration == null ? OptionalLong.empty() : OptionalLong.of(duration);
    }

    public Map<BootstrapStage, Long> stageDurationsNanos() {
        return stageDurationsNanos;
    }
}
