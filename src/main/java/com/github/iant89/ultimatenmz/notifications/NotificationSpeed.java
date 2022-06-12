package com.github.iant89.ultimatenmz.notifications;

public enum NotificationSpeed {
    SLOW("Slow", 75),
    MEDIUM("Medium", 50),
    DEFAULT("Default", 25),
    FAST("Fast", 15),
    EXTREME("Extreme", 8),

    ;

    String name;
    int ms;

    NotificationSpeed(String name, int delay) {
        this.name = name;
        ms = delay;
    }

    public String toString() {
        return this.name;
    }

    public int getDelay() {
        return this.ms;
    }
}
