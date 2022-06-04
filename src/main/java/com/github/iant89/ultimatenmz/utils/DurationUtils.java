package com.github.iant89.ultimatenmz.utils;

import java.time.Duration;

public class DurationUtils {

    public static long toSecondsPart(Duration duration) {
        return duration.getSeconds() % 60;
    }

    public static long toMinutesPart(Duration duration) {
        return (duration.getSeconds() % 3600) / 60;
    }

    public static long toHoursPart(Duration duration) {
        return duration.getSeconds() / 3600;
    }

    public static String toSessionTimeString(Duration duration) {
        long totalSeconds = duration.getSeconds();

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String toCountDownTimeString(Duration duration) {
        long totalSeconds = duration.getSeconds();

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String timeString = "";

        if(hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}