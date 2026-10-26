package common;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

public final class ScheduleCommandSupport {
    private ScheduleCommandSupport() {}

    public static String execute(String[] args, ConfigManager config, Runnable reload) {
        try {
            if (args.length == 0 || "status".equalsIgnoreCase(args[0])) {
                if (!config.getBoolean("updates.schedule.enabled")) return "自動更新已停用，可使用手動更新指令。";
                UpdateSchedule.Plan plan = UpdateSchedule.Plan.from(config);
                return "下次更新：" + plan.nextAfter(Instant.now()).atZone(plan.zone);
            }
            String action = args[0].toLowerCase(Locale.ROOT);
            if ("daily".equals(action) && args.length > 1) {
                List<String> times = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
                UpdateSchedule.Plan.parse(config.getString("updates.schedule.timezone"), times, "", 120);
                config.setOption("updates.schedule.times", times);
                config.setOption("updates.schedule.cron", "");
                config.setOption("updates.schedule.enabled", true);
            } else if ("timezone".equals(action) && args.length == 2) {
                ZoneId.of(args[1]);
                config.setOption("updates.schedule.timezone", args[1]);
            } else if ("cron".equals(action) && args.length == 6) {
                String expression = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                UpdateSchedule.Plan.parse(config.getString("updates.schedule.timezone"), Collections.emptyList(), expression, 120);
                config.setOption("updates.schedule.cron", expression);
                config.setOption("updates.schedule.enabled", true);
            } else if ("off".equals(action) && args.length == 1) {
                config.setOption("updates.schedule.enabled", false);
            } else if ("on".equals(action) && args.length == 1) {
                UpdateSchedule.Plan.from(config);
                config.setOption("updates.schedule.enabled", true);
            } else return "用法：schedule daily 04:00 16:00 | timezone Asia/Taipei | cron 0 4 * * * | on | off | status";
            reload.run();
            return "更新排程已儲存並立即套用。";
        } catch (RuntimeException invalid) {
            return "無法設定排程：" + invalid.getMessage();
        }
    }
}
