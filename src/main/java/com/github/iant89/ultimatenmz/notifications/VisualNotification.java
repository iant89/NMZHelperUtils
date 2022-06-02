package com.github.iant89.ultimatenmz.notifications;

import com.github.iant89.ultimatenmz.UltimateNMZConfig;
import com.github.iant89.ultimatenmz.drivers.ConstantDriver;
import com.github.iant89.ultimatenmz.drivers.SineDriver;
import com.github.iant89.ultimatenmz.drivers.StepDriver;
import com.github.iant89.ultimatenmz.drivers.ValueDriver;

import java.awt.*;

public class VisualNotification {

    private VisualNotificationType notificationType;

    private ValueDriver opacityDriver;
    private ValueDriver animationDriver;

    private long notificationLength = -1;
    private long notificationExpireTime = -1;
    private boolean notificationExpired = false;
    private boolean notificationVisible = true;

    private long nextFlashTime = -1;
    private long flashDelay = 500;

    private UltimateNMZConfig config;

    public VisualNotification(final UltimateNMZConfig config, final VisualNotificationType type, final long length) {
        this.config = config;
        this.notificationType = type;
        this.notificationLength = length;

        if(length != -1) {
            notificationExpireTime = System.currentTimeMillis() + length;
        }

        switch (getEffect()) {
            case FADE_IN_OUT:
                opacityDriver = new SineDriver(0.125f, 0.55f, getEffectSpeed().getDelay());
                break;

            case FLASH:
            case SOLID:
                flashDelay = getEffectSpeed().getDelay() * 20;
                nextFlashTime = System.currentTimeMillis() + flashDelay;
                opacityDriver = new ConstantDriver(0.55f);
                break;

            default:
                opacityDriver = new ConstantDriver(1f);
                break;
        }

        animationDriver = new StepDriver(0, 4, 250);
    }

    public void configUpdated() {
        float previousValue = -1;
        if(getOpacityDriver() != null && (getOpacityDriver() instanceof SineDriver)) {
            previousValue = ((SineDriver)getOpacityDriver()).getValue().floatValue();
        }

        switch (getEffect()) {
            case FADE_IN_OUT:
                opacityDriver = new SineDriver(0.125f, 0.55f, getEffectSpeed().getDelay());
                break;

            case FLASH:
            case SOLID:
                flashDelay = getEffectSpeed().getDelay() * 20;
                nextFlashTime = System.currentTimeMillis() + flashDelay;
                opacityDriver = new ConstantDriver(0.55f);
                break;

            default:
                opacityDriver = new ConstantDriver(1f);
                break;
        }

        if(opacityDriver != null) {
            if(previousValue > 0) {
                opacityDriver.setValue(previousValue);
            }
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

    public void renderNotification(Graphics2D graphics, Rectangle bounds) {

        switch (getEffect()) {
            case FADE_IN_OUT:
                renderFadeNotification(graphics, bounds);
                break;

            case FLASH:
                renderFlashNotification(graphics, bounds);
                break;
            case SOLID:
                renderSolidNotification(graphics, bounds);
                break;
        }

    }

    private void renderBorder(Graphics2D graphics, Rectangle bounds, Color color, float size) {
        graphics.setColor(color);
        graphics.setStroke(new BasicStroke(size));
        graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        graphics.setStroke(new BasicStroke(1f));
    }

    private void renderFadeNotification(Graphics2D graphics, Rectangle bounds) {
        if(getOpacityDriver() == null) {
            return;
        }

        Composite originalComposite = graphics.getComposite();

        graphics.setColor(getColor());
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) getOpacityDriver().getValue()));
        graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

        // Draw Border
        renderBorder(graphics, bounds, getColor(), 2f);

        graphics.setComposite(originalComposite);
    }

    private void renderFlashNotification(Graphics2D graphics, Rectangle bounds) {
        if(isVisible()) {
            if(System.currentTimeMillis() >= nextFlashTime) {
                setVisible(false);
                nextFlashTime = System.currentTimeMillis() + flashDelay;
            }
        } else {
            if(System.currentTimeMillis() >= nextFlashTime) {
                setVisible(true);
                nextFlashTime = System.currentTimeMillis() + flashDelay;
            } else {
                return;
            }
        }

        Composite originalComposite = graphics.getComposite();
        graphics.setColor(getColor());

        if (getOpacityDriver() == null) {
            return;
        }

        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) getOpacityDriver().getValue()));

        graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

        // Draw Border
        renderBorder(graphics, bounds, getColor(), 2f);

        graphics.setComposite(originalComposite);
    }

    public void renderSolidNotification(Graphics2D graphics, Rectangle bounds) {
        Composite originalComposite = graphics.getComposite();
        graphics.setColor(getColor());

        if(getOpacityDriver() == null) {
            return;
        }

        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) getOpacityDriver().getValue()));

        graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

        // Draw Border
        renderBorder(graphics, bounds, getColor(), 2f);

        graphics.setComposite(originalComposite);
    }

    public Color getColor() {

        switch (getType()) {
            case HP_BELOW_THRESHOLD:
                return config.minimumHPAlertColor();

            case HP_ABOVE_THRESHOLD:
                return config.maximumHPAlertColor();

            case ABSORPTION_BELOW_THRESHOLD:
                return config.absorptionAlertColor();

            case OVERLOAD_ALMOST_EXPIRED:
                return config.overloadRunOutColor();

            case OVERLOAD_EXPIRED:
                return config.overloadExpiredColor();

            case ZAPPER_SPAWNED:
                return config.zapperAlertColor();

            case POWER_SURGE_SPAWNED:
                return config.powerSurgeAlertColor();

            case RECURRENT_DAMAGE_SPAWNED:
                return config.recurrentDamageAlertColor();

            case ULTIMATE_FORCE_SPAWNED:
                return config.ultimateForceAlertColor();

            default:
                return null;
        }
    }


    public VisualNotificationEffectType getEffect() {

        switch (getType()) {
            case HP_BELOW_THRESHOLD:
                return config.minimumHPEffectType();

            case HP_ABOVE_THRESHOLD:
                return config.maximumHPEffectType();

            case ABSORPTION_BELOW_THRESHOLD:
                return config.absorptionEffectType();

            case OVERLOAD_ALMOST_EXPIRED:
                return config.overloadRunOutEffectType();

            case OVERLOAD_EXPIRED:
                return config.overloadExpiredEffectType();

            case ZAPPER_SPAWNED:
                return config.zapperEffectType();

            case POWER_SURGE_SPAWNED:
                return config.powerSurgeEffectType();

            case RECURRENT_DAMAGE_SPAWNED:
                return config.recurrentDamageEffectType();

            case ULTIMATE_FORCE_SPAWNED:
                return config.ultimateForceEffectType();

            default:
                return null;
        }
    }

    public VisualNotificationSpeed getEffectSpeed() {

        switch (getType()) {
            case HP_BELOW_THRESHOLD:
                return config.minimumHPEffectSpeed();

            case HP_ABOVE_THRESHOLD:
                return config.maximumHPEffectSpeed();

            case ABSORPTION_BELOW_THRESHOLD:
                return config.absorptionEffectSpeed();

            case OVERLOAD_ALMOST_EXPIRED:
                return config.overloadRunOutEffectSpeed();

            case OVERLOAD_EXPIRED:
                return config.overloadExpiredEffectSpeed();

            case ZAPPER_SPAWNED:
                return config.zapperEffectSpeed();

            case POWER_SURGE_SPAWNED:
                return config.powerSurgeEffectSpeed();

            case RECURRENT_DAMAGE_SPAWNED:
                return config.recurrentDamageEffectSpeed();

            case ULTIMATE_FORCE_SPAWNED:
                return config.ultimateForceEffectSpeed();

            default:
                return null;
        }
    }

    public ValueDriver getOpacityDriver() {
        return opacityDriver;
    }

    public ValueDriver getAnimationDriver() { return animationDriver; }
}
