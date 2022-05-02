package com.github.iant89.ultimatenmz.notifications;

import com.github.iant89.ultimatenmz.UltimateNMZConfig;
import com.github.iant89.ultimatenmz.drivers.ConstantDriver;
import com.github.iant89.ultimatenmz.drivers.SineDriver;
import com.github.iant89.ultimatenmz.drivers.ValueDriver;
import net.runelite.api.Client;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class VisualNotificationManager {

    private static ArrayList<VisualNotification> notificationList = new ArrayList<>();

    @Inject
    private Client client;

    @Inject
    private UltimateNMZConfig config;


    @Inject
    protected VisualNotificationManager(Client client, UltimateNMZConfig config) {
        this.client = client;
        this.config = config;
    }

    public void addNotification(VisualNotification notification) {
        notificationList.add(notification);
    }

    public synchronized void createNotification(VisualNotificationType type) {
        VisualNotification visualNotification = getNotificationByType(type);
        long notificationLength = -1;
        Color notificationColor;
        ValueDriver opacityDriver = null;
        VisualNotificationEffectType notificationEffect;

        if(visualNotification != null) {
            if(visualNotification.getLength() == -1) {
                return;
            } else {
                removeNotification(type);
            }
        }

        switch (type) {
            case HP_BELOW_THRESHOLD:
                notificationColor = config.minimumHPAlertColor();
                notificationLength = 4;
                notificationEffect = config.minimumHPEffectType();
                switch (config.minimumHPEffectType()) {
                    case FADE_IN_OUT:
                        opacityDriver = new SineDriver(0.125f, 0.55f, 25);
                        break;
                    case FLASH:
                    case SOLID:
                        opacityDriver = new ConstantDriver(0.55f);
                        break;
                }
                break;
            case HP_ABOVE_THRESHOLD:
                notificationColor = config.maximumHPAlertColor();
                notificationEffect = config.maximumHPEffectType();
                switch (config.maximumHPEffectType()) {
                    case FADE_IN_OUT:
                        opacityDriver = new SineDriver(0.125f, 0.55f, 25);
                        break;
                    case FLASH:
                    case SOLID:
                        opacityDriver = new ConstantDriver(0.55f);
                        break;
                }
                break;
            case ABSORPTION_BELOW_THRESHOLD:
                notificationColor = config.absorptionAlertColor();
                notificationEffect = config.absorptionEffectType();
                switch (config.absorptionEffectType()) {
                    case FADE_IN_OUT:
                        opacityDriver = new SineDriver(0.125f, 0.55f, 25);
                        break;
                    case FLASH:
                    case SOLID:
                        opacityDriver = new ConstantDriver(0.55f);
                        break;
                }
                break;
            case ZAPPER_SPAWNED:
                notificationColor = config.zapperAlertColor();
                notificationEffect = config.zapperEffectType();
                switch (config.zapperEffectType()) {
                    case FADE_IN_OUT:
                        opacityDriver = new SineDriver(0f, 0.55f, 20);
                        break;
                    case FLASH:
                    case SOLID:
                        opacityDriver = new ConstantDriver(0.45f);
                        break;
                }
                notificationLength = 4;
                break;
            case POWER_SURGE_SPAWNED:
                notificationColor = config.powerSurgeAlertColor();
                notificationEffect = config.powerSurgeEffectType();
                switch (config.powerSurgeEffectType()) {
                    case FADE_IN_OUT:
                        opacityDriver = new SineDriver(0f, 0.55f, 20);
                        break;
                    case FLASH:
                    case SOLID:
                        opacityDriver = new ConstantDriver(0.45f);
                        break;
                }
                notificationLength = 4;
                break;
            case RECURRENT_DAMAGE_SPAWNED:
                notificationColor = config.recurrentDamageAlertColor();
                notificationEffect = config.recurrentDamageEffectType();
                switch (config.recurrentDamageEffectType()) {
                    case FADE_IN_OUT:
                        opacityDriver = new SineDriver(0f, 0.55f, 20);
                        break;
                    case FLASH:
                    case SOLID:
                        opacityDriver = new ConstantDriver(0.45f);
                        break;
                }
                notificationLength = 4;
                break;
            case ULTIMATE_FORCE_SPAWNED:
                notificationColor = config.ultimateForceAlertColor();
                notificationEffect = config.ultimateForceEffectType();
                switch (config.ultimateForceEffectType()) {
                    case FADE_IN_OUT:
                        opacityDriver = new SineDriver(0f, 0.45f, 20);
                        break;
                    case FLASH:
                    case SOLID:
                        opacityDriver = new ConstantDriver(0.45f);
                        break;
                }
                notificationLength = 4;
                break;
            default:
                // Invalid Notification...
                return;
        }

        if(notificationLength > -1) {
            notificationLength = notificationLength * 1000;
        }

        if(opacityDriver == null) {
            opacityDriver = new ConstantDriver(1f);
        }

        switch (notificationEffect) {
            case FADE_IN_OUT:
                visualNotification = new FadedVisualNotification(type, notificationColor, opacityDriver, notificationLength);
                break;
            case FLASH:
                visualNotification = new FlashVisualNotification(type, notificationColor, (float) opacityDriver.getValue(), notificationLength);
                break;
            case SOLID:
                visualNotification = new SolidVisualNotification(type, notificationColor, (float) opacityDriver.getValue(), notificationLength);
                break;
        }

        if(visualNotification != null) {
            notificationList.add(visualNotification);
        }
    }



    public int getNotificationCount() {
        return notificationList.size();
    }

    public synchronized ArrayList<VisualNotification> getNotifications() {
        return new ArrayList<>(notificationList);
    }

    public synchronized void cleanNotifications() {
        notificationList.removeIf(VisualNotification::isExpired);
    }

    public ArrayList<VisualNotification> getNotificationsByPriority(int priority) {
        final ArrayList<VisualNotification> priorityList = new ArrayList<>();

        Iterator<VisualNotification> notificationIterator = notificationList.iterator();
        while (notificationIterator.hasNext()) {
            VisualNotification notification = notificationIterator.next();

            if(notification.isExpired()) {
                notificationIterator.remove();
            }

            if(notification.getType().getPriority() == priority) {
                priorityList.add(notification);
            }
        }

        return priorityList;
    }

    public synchronized boolean hasNotificationType(VisualNotificationType type) {
        return (getNotificationByType(type) != null);
    }

    public synchronized VisualNotification getNotificationByType(VisualNotificationType type) {
        for(VisualNotification notification : notificationList) {
            if(notification.getType() == type) {
                return notification;
            }
        }

        return null;
    }

    public void removeNotification(VisualNotificationType type) {
        Iterator<VisualNotification> notificationIterator = notificationList.iterator();
        while (notificationIterator.hasNext()) {
            VisualNotification notification = notificationIterator.next();

            if(notification.getType() == type) {
                notificationIterator.remove();
                break;
            }
        }
    }

    public void configUpdated() {
        if(!config.visualAlerts()) {
            notificationList.clear();
        } else {
            if(!config.absorptionNotification()) {
                removeNotification(VisualNotificationType.ABSORPTION_BELOW_THRESHOLD);
            }
            if(!config.maximumHPNotification()) {
                removeNotification(VisualNotificationType.HP_ABOVE_THRESHOLD);
            }
            if(!config.minimumHPNotification()) {
                removeNotification(VisualNotificationType.HP_BELOW_THRESHOLD);
            }
            if(!config.powerSurgeNotification()) {
                removeNotification(VisualNotificationType.POWER_SURGE_SPAWNED);
            }
            if(!config.zapperNotification()) {
                removeNotification(VisualNotificationType.ZAPPER_SPAWNED);
            }
            if(!config.recurrentDamageNotification()) {
                removeNotification(VisualNotificationType.RECURRENT_DAMAGE_SPAWNED);
            }
            if(!config.ultimateForceNotification()) {
                removeNotification(VisualNotificationType.ULTIMATE_FORCE_SPAWNED);
            }
        }


    }

    public void clearNotifications() {
        notificationList.clear();
    }
}
