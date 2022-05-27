package com.github.iant89.ultimatenmz.notifications;

public enum VisualNotificationType {
    HP_BELOW_THRESHOLD("HP_BELOW_THRESHOLD", 7),
    HP_ABOVE_THRESHOLD("HP_ABOVE_THRESHOLD",2),
    ABSORPTION_BELOW_THRESHOLD("ABSORPTION_BELOW_THRESHOLD",1),

    /*
     * Overload Notifications
     */

    OVERLOAD_ALMOST_EXPIRED("", 1),
    OVERLOAD_EXPIRED("", 2),


    /*
     * Power-Up Spawn Notifications
     */
    ZAPPER_SPAWNED("ZAPPER_SPAWNED",3),
    POWER_SURGE_SPAWNED("POWER_SURGE_SPAWNED",4),
    RECURRENT_DAMAGE_SPAWNED("RECURRENT_DAMAGE_SPAWNED",5),
    ULTIMATE_FORCE_SPAWNED("ULTIMATE_FORCE_SPAWNED",6),
    ;

    final String name;
    final int priority;

    VisualNotificationType(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public String toString() {
        return "" + name;
    }
}
