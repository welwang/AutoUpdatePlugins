package common;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import static org.junit.Assert.*;

public class UpdateScheduleTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test public void dailyTimesUseTaipeiAndSkipPastTimes() {
        UpdateSchedule.Plan plan = UpdateSchedule.Plan.parse("Asia/Taipei", Arrays.asList("16:00", "04:00", "04:00"), "", 120);
        assertEquals(Instant.parse("2026-10-26T20:00:00Z"), plan.nextAfter(Instant.parse("2026-10-26T08:00:00Z")));
        assertEquals(Instant.parse("2026-10-26T08:00:00Z"), plan.nextAfter(Instant.parse("2026-10-26T07:59:59Z")));
    }
    @Test public void cronOverridesDailyTimes() {
        UpdateSchedule.Plan plan = UpdateSchedule.Plan.parse("Asia/Taipei", Arrays.asList("04:00"), "30 3 * * *", 120);
        assertEquals(Instant.parse("2026-10-26T19:30:00Z"), plan.nextAfter(Instant.parse("2026-10-26T08:00:00Z")));
    }
    @Test public void intervalUsedWhenDailyAndCronAreEmpty() {
        UpdateSchedule.Plan plan = UpdateSchedule.Plan.parse("UTC", Collections.emptyList(), "", 15);
        assertEquals(Instant.parse("2026-10-26T00:15:00Z"), plan.nextAfter(Instant.parse("2026-10-26T00:00:00Z")));
    }
    @Test public void daylightSavingGapStillRunsOnceOnThatDay() {
        UpdateSchedule.Plan plan = UpdateSchedule.Plan.parse("America/New_York", Arrays.asList("02:30"), "", 120);
        assertEquals(Instant.parse("2026-03-08T07:30:00Z"), plan.nextAfter(Instant.parse("2026-03-08T00:00:00Z")));
    }
    @Test(expected = RuntimeException.class) public void rejectsInvalidHour() {
        UpdateSchedule.Plan.parse("UTC", Arrays.asList("25:00"), "", 120);
    }
    @Test(expected = RuntimeException.class) public void rejectsInvalidTimezone() {
        UpdateSchedule.Plan.parse("Bad/Zone", Arrays.asList("04:00"), "", 120);
    }
    @Test(expected = RuntimeException.class) public void rejectsZeroInterval() {
        UpdateSchedule.Plan.parse("UTC", Collections.emptyList(), "", 0);
    }
    @Test public void disablingScheduleCancelsOldPendingRun() throws Exception {
        ConfigManager config = config();
        config.setOption("updates.schedule.times", Collections.emptyList());
        config.setOption("updates.bootTime", 1);
        CountDownLatch ran = new CountDownLatch(1);
        try (UpdateSchedule schedule = new UpdateSchedule()) {
            schedule.configure(config, Logger.getAnonymousLogger(), ran::countDown);
            config.setOption("updates.schedule.enabled", false);
            schedule.configure(config, Logger.getAnonymousLogger(), ran::countDown);
            assertFalse(ran.await(1300, TimeUnit.MILLISECONDS));
        }
    }
    @Test public void commandsPersistAndReloadDailyTimeAndTimezone() throws Exception {
        ConfigManager config = config();
        AtomicInteger reloads = new AtomicInteger();
        assertTrue(ScheduleCommandSupport.execute(new String[]{"daily", "04:00", "16:00"}, config, reloads::incrementAndGet).contains("套用"));
        assertEquals(Arrays.asList("04:00", "16:00"), config.getStringList("updates.schedule.times"));
        assertTrue(ScheduleCommandSupport.execute(new String[]{"timezone", "Asia/Taipei"}, config, reloads::incrementAndGet).contains("套用"));
        assertEquals(2, reloads.get());
        String invalid = ScheduleCommandSupport.execute(new String[]{"daily", "24:99"}, config, reloads::incrementAndGet);
        assertTrue(invalid.contains("無法"));
        assertEquals(2, reloads.get());
        assertEquals(Arrays.asList("04:00", "16:00"), config.getStringList("updates.schedule.times"));
        config.reloadConfig();
        assertEquals("Asia/Taipei", config.getString("updates.schedule.timezone"));
    }
    private ConfigManager config() throws Exception {
        ConfigManager config = new ConfigManager(temporary.newFolder(), "config.yml");
        config.setOption("updates.schedule.enabled", true);
        config.setOption("updates.schedule.timezone", "UTC");
        config.setOption("updates.schedule.cron", "");
        config.setOption("updates.schedule.times", Arrays.asList("04:00"));
        config.setOption("updates.interval", 120);
        return config;
    }
}
