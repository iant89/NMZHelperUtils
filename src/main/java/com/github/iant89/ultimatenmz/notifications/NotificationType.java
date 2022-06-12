package com.github.iant89.ultimatenmz.notifications;

public enum NotificationType {
    HP_BELOW_THRESHOLD,
    HP_ABOVE_THRESHOLD,
    ABSORPTION_BELOW_THRESHOLD,

    /*
     * Overload Notifications
     */

    OVERLOAD_ALMOST_EXPIRED,
    OVERLOAD_EXPIRED,


    /*
     * Power-Up Spawn Notifications
     */
    ZAPPER_SPAWNED,
    POWER_SURGE_SPAWNED,
    RECURRENT_DAMAGE_SPAWNED,
    ULTIMATE_FORCE_SPAWNED,
    ;

    public String toString() {
        return "" + this.name();
    }
}
