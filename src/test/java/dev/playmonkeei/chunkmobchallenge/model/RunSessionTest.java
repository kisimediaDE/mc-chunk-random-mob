package dev.playmonkeei.chunkmobchallenge.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class RunSessionTest {
    @Test
    void visibilityHelpersAreDisabledByDefault() {
        RunSession run = new RunSession();
        assertFalse(run.nameTagsVisible);
        assertFalse(run.glowing);
    }

    @Test
    void activeTimerExcludesOfflineGap() {
        RunSession run = new RunSession();
        run.resumeTimer(1_000);
        run.pauseTimer(11_000);
        run.resumeTimer(100_000);
        assertEquals(15_000, run.activeMillis(105_000));
    }

    @Test
    void repeatedResumeAndPauseAreIdempotent() {
        RunSession run = new RunSession();
        run.resumeTimer(1_000);
        run.resumeTimer(2_000);
        run.pauseTimer(3_000);
        run.pauseTimer(9_000);
        assertEquals(2_000, run.activeMillis(20_000));
    }
}
