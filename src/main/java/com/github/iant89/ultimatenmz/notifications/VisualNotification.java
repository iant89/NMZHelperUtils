package com.github.iant89.ultimatenmz.notifications;

import com.github.iant89.ultimatenmz.drivers.ValueDriver;


import java.awt.*;

public abstract class VisualNotification {

    private VisualNotificationType notificationType;
    private Color notificationColor;
    private long notificationLength = -1;
    private long notificationExpireTime = -1;
    private ValueDriver notificationOpacityDriver;
    private boolean notificationExpired = false;
    private boolean notificationVisible = true;

    public VisualNotification(final VisualNotificationType type, final Color notificationColor, final ValueDriver notificationOpacityDriver, final long length) {
        this.notificationType = type;
        this.notificationColor = notificationColor;
        this.notificationOpacityDriver = notificationOpacityDriver;
        this.notificationLength = length;

        if(length != -1) {
            notificationExpireTime = System.currentTimeMillis() + length;
        }
    }

    public VisualNotificationType getType() {
        return notificationType;
    }

    public long getLength() {
        return notificationLength;
    }

    public boolean isVisible() {
        return notificationVisible;
    }

    public void setVisible(boolean visible) {
        notificationVisible = visible;
    }

    public final Color getColor() {
        return this.notificationColor;
    }

    public ValueDriver getOpacityDriver() {
        return notificationOpacityDriver;
    }

    public void extendLength(final long length) {
        if(notificationExpireTime == -1 || isExpired()) {
            notificationExpireTime = System.currentTimeMillis() + length;
        } else {
            notificationExpireTime += length;
        }
    }

    public void expire() {
        notificationExpired = true;
    }

    public boolean isExpired() {
        if(!notificationExpired) {
            if (notificationExpireTime == -1) {
                return false;
            }

            if (System.currentTimeMillis() >= notificationExpireTime) {
                notificationExpired = true;
            }
        }

        return notificationExpired;
    }

    public abstract void renderNotification(Graphics2D graphics, Rectangle bounds);
}
