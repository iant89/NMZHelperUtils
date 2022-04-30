package com.github.iant89.ultimatenmz.notifications;

import com.github.iant89.ultimatenmz.drivers.ValueDriver;


import java.awt.*;
import java.awt.image.BufferedImage;

public class VisualNotification {

    private final VisualNotificationType type;

    private Color color = new Color(0, 179, 255);
    private VisualNotificationEffectType effectType = VisualNotificationEffectType.FADE_IN_OUT;
    private long expireTime = -1;
    private ValueDriver<Float> opacityDriver;
    private boolean expired = false;
    private boolean visible = true;

    public VisualNotification(final VisualNotificationType type, final Color color, final ValueDriver opacityDriver, final long length) {
        this.color = color;
        this.type = type;
        this.opacityDriver = opacityDriver;

        if(length != -1) {
            expireTime = System.currentTimeMillis() + length;
        }
    }

    public final VisualNotificationType getType() {
        return type;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public final Color getColor() {
        return this.color;
    }

    public void setEffectType(VisualNotificationEffectType type) {
        effectType = type;
    }

    public VisualNotificationEffectType getEffectType() {
        return effectType;
    }

    public ValueDriver getOpacityDriver() {
        return opacityDriver;
    }

    public void extendLength(final long length) {
        if(expireTime == -1 || isExpired()) {
            expireTime = System.currentTimeMillis() + length;
        } else {
            expireTime += length;
        }
    }

    public void expire() {
        expired = true;
    }

    public boolean isExpired() {
        if(!expired) {
            if (expireTime == -1) {
                return false;
            }

            if (System.currentTimeMillis() >= expireTime) {
                expired = true;
            }
        }

        return expired;
    }


}
