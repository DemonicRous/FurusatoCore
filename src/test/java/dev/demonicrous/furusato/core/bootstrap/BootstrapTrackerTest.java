package dev.demonicrous.furusato.core.bootstrap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dev.demonicrous.furusato.api.bootstrap.BootstrapReport;
import dev.demonicrous.furusato.api.bootstrap.BootstrapStage;
import org.junit.Test;

public final class BootstrapTrackerTest {
    @Test
    public void recordsStagesAndCompletesAtLoadComplete() {
        MutableClock clock = new MutableClock();
        BootstrapTracker tracker = new BootstrapTracker(clock, 1234L);

        tracker.begin(BootstrapStage.CONSTRUCT);
        clock.advance(2_000_000L);
        tracker.end(BootstrapStage.CONSTRUCT);

        BootstrapReport partial = tracker.snapshot();
        assertFalse(partial.isComplete());
        assertEquals(2_000_000L,
                partial.stageNanos(BootstrapStage.CONSTRUCT).getAsLong());

        clock.advance(7_000_000L);
        tracker.begin(BootstrapStage.LOAD_COMPLETE);
        clock.advance(3_000_000L);
        tracker.end(BootstrapStage.LOAD_COMPLETE);

        BootstrapReport complete = tracker.snapshot();
        assertTrue(complete.isComplete());
        assertEquals(5_000_000L, complete.totalNanos());
        assertEquals(12_000_000L, complete.elapsedNanos());
        assertEquals(1234L, complete.startedAtEpochMillis());
    }

    @Test(expected = IllegalStateException.class)
    public void rejectsOverlappingStages() {
        BootstrapTracker tracker = new BootstrapTracker(new MutableClock(), 0L);
        tracker.begin(BootstrapStage.CONSTRUCT);
        tracker.begin(BootstrapStage.PRE_INIT);
    }

    @Test(expected = IllegalStateException.class)
    public void rejectsRepeatedStage() {
        BootstrapTracker tracker = new BootstrapTracker(new MutableClock(), 0L);
        tracker.begin(BootstrapStage.INIT);
        tracker.end(BootstrapStage.INIT);
        tracker.begin(BootstrapStage.INIT);
    }

    private static final class MutableClock implements NanoClock {
        private long nanos;

        @Override
        public long nanoTime() {
            return nanos;
        }

        void advance(long amount) {
            nanos += amount;
        }
    }
}
