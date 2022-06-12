package com.github.iant89.ultimatenmz.notifications;

public enum NotificationEffectType {
    FADE_IN_OUT("Fade IN/OUT"),
    FLASH("Flash"),
    SOLID("Solid"),

    ;

    final String name;

    NotificationEffectType(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}
