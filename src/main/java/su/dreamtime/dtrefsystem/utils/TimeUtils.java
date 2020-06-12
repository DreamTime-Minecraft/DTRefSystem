package su.dreamtime.dtrefsystem.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class TimeUtils {

    public enum TimeUnit {
        SECOND(1000),
        MINUTE(60000),
        HOUR(3600000),
        DAY(86400000),
        WEEK(604800000);

        private long factor;

        private TimeUnit(long factor) {
            this.factor = factor;
        }

        public long getFactor() {
            return factor;
        }

        public long toMills(long value) {
            return value * this.factor;
        }

        public long toSeconds(long value) {
            return value * (this.factor / 1000);
        }
    }

    public static String getTimeString(long mills) {
        if (mills <= 0) {
            mills = 0;
        }
        long time = mills/1000;
        long sec = time % 60;
        time /= 60;
        long min = time % 60;
        time /= 60;
        long hour = time % 24;
        time /= 24;
        long days = time;

        List<String> timeStringList = new ArrayList<>();
        if (days != 0) {
            timeStringList.add("&e" + days + " &c" + "д.");
        }
        if (hour != 0) {
            timeStringList.add("&e" + hour + " &c" + "ч.");
        }
        if (min != 0) {
            timeStringList.add("&e" + min + " &c" + "мин.");
        }
        if (sec != 0) {
            timeStringList.add("&e" + sec + " &c" + "с.");
        }

        return ChatColor.translateAlternateColorCodes('&', String.join(", ", timeStringList.toArray(new String[]{})));
    }
}
