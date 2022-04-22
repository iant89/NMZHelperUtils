package com.nmzhelperutils.notifications;

import com.nmzhelperutils.drivers.AlphaSineDriver;


import java.awt.*;

public class VisualNotification {

    private final VisualNotificationType type;

    private Color color = new Color(0, 179, 255);
    private long expireTime = -1;
    private AlphaSineDriver alphaDriver;

    public VisualNotification(final VisualNotificationType type, final Color color, final AlphaSineDriver alphaDriver, final long length) {
        this.color = color;
        this.type = type;
        this.alphaDriver = alphaDriver;

        if(length != -1) {
            expireTime = System.currentTimeMillis() + length;
        }
    }

    public final VisualNotificationType getType() {
        return type;
    }

    public final Color getColor() {
        return this.color;
    }

    public AlphaSineDriver getAlphaDriver() {
        return alphaDriver;
    }

    public void extendLength(final long length) {
        if(expireTime == -1 || isExpired()) {
            expireTime = System.currentTimeMillis() + length;
        } else {
            expireTime += length;
        }
    }

    public boolean isExpired() {
        if(expireTime == -1) {
            return false;
        }

        return (System.currentTimeMillis() >= expireTime);
    }


}
