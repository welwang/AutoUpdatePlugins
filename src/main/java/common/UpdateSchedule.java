package common;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

/** One replaceable schedule per plugin instance; reloads invalidate all old callbacks. */
public final class UpdateSchedule implements AutoCloseable {
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "AutoUpdatePlugins-schedule");
        thread.setDaemon(true);
        return thread;
    });
    private ScheduledFuture<?> next;
    private long generation;

    public synchronized void configure(ConfigManager config, Logger logger, Runnable job) {
        long epoch = ++generation;
        if (next != null) next.cancel(false);
        next = null;
        if (!config.getBoolean("updates.schedule.enabled")) {
            logger.info("Automatic updates disabled; manual update commands remain available.");
            return;
        }
        try {
            Plan plan = Plan.from(config);
            Instant now = Instant.now();
            Instant first = plan.intervalSeconds > 0
                    ? now.plusSeconds(Math.max(0, config.getInt("updates.bootTime"))) : plan.nextAfter(now);
            enqueue(epoch, plan, first, logger, job);
        } catch (RuntimeException invalid) {
            logger.warning("Update schedule disabled: " + invalid.getMessage() + ". Correct the settings and reload.");
        }
    }

    private synchronized void enqueue(long epoch, Plan plan, Instant when, Logger logger, Runnable job) {
        if (epoch != generation || timer.isShutdown()) return;
        long delay = Math.max(0, Duration.between(Instant.now(), when).toMillis());
        logger.info("Next plugin update: " + when.atZone(plan.zone));
        next = timer.schedule(() -> {
            synchronized (UpdateSchedule.this) {
                if (epoch != generation) return;
            }
            try { job.run(); }
            catch (Throwable failure) { logger.warning("Scheduled update failed: " + failure.getMessage()); }
            finally { enqueue(epoch, plan, plan.nextAfter(Instant.now()), logger, job); }
        }, delay, TimeUnit.MILLISECONDS);
    }

    @Override public synchronized void close() {
        generation++;
        if (next != null) next.cancel(false);
        timer.shutdownNow();
    }

    public static final class Plan {
        public final ZoneId zone;
        final List<LocalTime> times;
        final ExecutionTime cron;
        final long intervalSeconds;

        private Plan(ZoneId zone, List<LocalTime> times, ExecutionTime cron, long intervalSeconds) {
            this.zone = zone; this.times = times; this.cron = cron; this.intervalSeconds = intervalSeconds;
        }

        public static Plan from(ConfigManager config) {
            String zone = config.getString("updates.schedule.timezone");
            return parse(zone, config.getStringList("updates.schedule.times"),
                    config.getString("updates.schedule.cron"), config.getInt("updates.interval"));
        }

        public static Plan parse(String zoneName, List<String> daily, String expression, int intervalMinutes) {
            ZoneId zone = ZoneId.of(zoneName == null || zoneName.trim().isEmpty() ? "Asia/Taipei" : zoneName.trim());
            if (expression != null && !expression.trim().isEmpty()) {
                com.cronutils.model.Cron cron = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX))
                        .parse(expression.trim());
                cron.validate();
                return new Plan(zone, Collections.emptyList(), ExecutionTime.forCron(cron), 0);
            }
            TreeSet<LocalTime> times = new TreeSet<>();
            if (daily != null) for (String value : daily) {
                if (value == null || !value.matches("[0-2][0-9]:[0-5][0-9]"))
                    throw new IllegalArgumentException("Daily times must use HH:mm, for example 04:00");
                times.add(LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT)));
            }
            if (!times.isEmpty()) return new Plan(zone, new ArrayList<>(times), null, 0);
            if (intervalMinutes <= 0) throw new IllegalArgumentException("updates.interval must be greater than zero");
            return new Plan(zone, Collections.emptyList(), null, intervalMinutes * 60L);
        }

        public Instant nextAfter(Instant now) {
            if (intervalSeconds > 0) return now.plusSeconds(intervalSeconds);
            if (cron != null) return cron.nextExecution(now.atZone(zone))
                    .orElseThrow(() -> new IllegalArgumentException("Cron has no next execution")).toInstant();
            LocalDate day = now.atZone(zone).toLocalDate();
            for (int offset = 0; offset <= 2; offset++) {
                Instant earliest = null;
                for (LocalTime time : times) {
                    // atZone advances nonexistent spring times and uses the first autumn occurrence.
                    Instant candidate = day.plusDays(offset).atTime(time).atZone(zone).toInstant();
                    if (candidate.isAfter(now) && (earliest == null || candidate.isBefore(earliest))) earliest = candidate;
                }
                if (earliest != null) return earliest;
            }
            throw new IllegalArgumentException("No daily update time configured");
        }
    }
}
