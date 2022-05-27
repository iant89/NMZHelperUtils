package com.github.iant89.ultimatenmz.utils;

import java.time.Duration;

public class DurationUtils {

    public static String toTimeString(Duration duration) {
        String durationString = "";

        int dH = duration.toHoursPart();
        int dM = duration.toMinutesPart();
        int dS = duration.toSecondsPart();

        durationString += (dH < 10 ? "0" : "") + dH + ":";
        durationString += (dM < 10 ? "0" : "") + dM + ":";
        durationString += (dS < 10 ? "0" : "") + dS;

        return durationString;
    }
    
}
