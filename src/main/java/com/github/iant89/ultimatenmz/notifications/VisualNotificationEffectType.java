package com.github.iant89.ultimatenmz.notifications;

public enum VisualNotificationEffectType {
    FADE_IN_OUT("Fade IN/OUT"),
    FLASH("Flash"),
    SOLID("Solid"),

    ;

    final String name;

    VisualNotificationEffectType(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}
