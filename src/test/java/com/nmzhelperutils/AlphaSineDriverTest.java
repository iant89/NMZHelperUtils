package com.nmzhelperutils;

import com.nmzhelperutils.drivers.AlphaSineDriver;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class AlphaSineDriverTest {

    public static void main(String[] args) {

        // 30 = 1906
        // 31 = 1968, 1953
        AlphaSineDriver alphaDriver = new AlphaSineDriver(0.125f, 0.75f, 20);

        System.out.println("Step = " + alphaDriver.getStep());

        boolean minReached = false;
        boolean maxReached = false;

        long startTimer = System.currentTimeMillis();

        StringBuilder sb = new StringBuilder();

        float previousValue = Float.MAX_VALUE;
        while(true) {
            float value = alphaDriver.getValue();

            if(value == alphaDriver.getMaximum()) {
                maxReached = true;
            } else if(value == alphaDriver.getMinimum()) {
                minReached = true;
            }

            if(minReached && maxReached) {
                final long endTime = System.currentTimeMillis();
                double seconds = ((endTime - startTimer) / 1000.0);
                long milliseconds = endTime - startTimer;
                if(seconds < 1) {
                    sb.append("Done, Took " + seconds + " Seconds\n\n");
                } else {
                    sb.append("Done, Took " + milliseconds + " Milliseconds.");
                }

                break;
            }

            if(previousValue != Float.MAX_VALUE) {
                if(previousValue != value) {
                    sb.append("" + value + "\n");
                    previousValue = value;
                }
            } else {
                previousValue = value;
            }
        }

        System.out.println("" + sb.toString());

    }

}
