package com.github.iant89.ultimatenmz.notifications;

import com.github.iant89.ultimatenmz.UltimateNMZConfig;
import com.github.iant89.ultimatenmz.drivers.ConstantDriver;
import com.github.iant89.ultimatenmz.drivers.SineDriver;
import com.github.iant89.ultimatenmz.drivers.ValueDriver;
import com.github.iant89.ultimatenmz.utils.InventoryUtils;
import net.runelite.api.Client;
import net.runelite.api.ItemID;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VisualNotificationManager {

    private static ArrayList<VisualNotification> notificationList = new ArrayList<>();
    private static HashMap<VisualNotificationType, Long> notificationBlockMap = new HashMap<>();
    @Inject
    private Client client;

    @Inject
    private UltimateNMZConfig config;


    @Inject
    protected VisualNotificationManager(Client client, UltimateNMZConfig config) {
        this.client = client;
        this.config = config;
    }

    public void blockNotification(VisualNotificationType type, int seconds) {
        cleanNotifications();

        if(seconds <= 0) {
            return;
        }

        if(notificationBlockMap.containsKey(type)) {
            notificationBlockMap.remove(type);
        }

        notificationBlockMap.put(type, System.currentTimeMillis() + (seconds * 1000));
    }

    public void addNotification(VisualNotification notification) {
        cleanNotifications();

        if(notificationBlockMap.containsKey(notification.getType())) {
            return;
        }

        notificationList.add(notification);
    }

    public synchronized void removeAll() {
        notificationList.clear();
    }

    public synchronized void createNotification(VisualNotificationType type) {
        VisualNotification visualNotification = getNotificationByType(type);
        long notificationLength = -1;

        cleanNotifications();

        if(notificationBlockMap.containsKey(type)) {
            return;
        }

        if(visualNotification != null) {
            if(visualNotification.getLength() == -1) {
                return;
            } else {
                removeNotification(type);
            }
        }

        switch (type) {

            case HP_ABOVE_THRESHOLD:
            case OVERLOAD_ALMOST_EXPIRED:
                notificationLength = -1;
                break;

            case OVERLOAD_EXPIRED:
                // Only allow creation of alert if we have OVERLOAD pots in inventory.
                if(!InventoryUtils.hasOneOfItems(client, ItemID.OVERLOAD_4, ItemID.OVERLOAD_3, ItemID.OVERLOAD_2, ItemID.OVERLOAD_1)) {
                    return;
                }

                notificationLength = -1;

                break;

            case ABSORPTION_BELOW_THRESHOLD:
                // Only allow creation of alert if we have ABSORPTION pots in inventory.
                if(!InventoryUtils.hasOneOfItems(client, ItemID.ABSORPTION_4, ItemID.ABSORPTION_3, ItemID.ABSORPTION_2, ItemID.ABSORPTION_1)) {
                    return;
                }
                notificationLength = -1;

                break;

            case HP_BELOW_THRESHOLD:
            case ZAPPER_SPAWNED:
            case ULTIMATE_FORCE_SPAWNED:
            case POWER_SURGE_SPAWNED:
            case RECURRENT_DAMAGE_SPAWNED:
                notificationLength = 10;
                break;

            default:
                // Invalid Notification...
                return;
        }

        if(notificationLength > -1) {
            notificationLength = notificationLength * 1000;
        }

        visualNotification = new VisualNotification(config, type, notificationLength);

        notificationList.add(visualNotification);
    }



    public int getNotificationCount() {
        return notificationList.size();
    }

    public synchronized ArrayList<VisualNotification> getNotifications() {
        return new ArrayList<>(notificationList);
    }

    public synchronized void cleanNotifications() {
        notificationList.removeIf(VisualNotification::isExpired);

        for(Map.Entry<VisualNotificationType, Long> entry : notificationBlockMap.entrySet()) {
            if(System.currentTimeMillis() >= entry.getValue()) {
                notificationBlockMap.remove(entry.getKey());
            }
        }
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
            if(!config.overloadRunoutNotification()) {
                removeNotification(VisualNotificationType.OVERLOAD_ALMOST_EXPIRED);
            }
            if(!config.overloadExpiredNotification()) {
                removeNotification(VisualNotificationType.OVERLOAD_EXPIRED);
            }
            if(!config.overloadRunoutNotification()) {
                removeNotification(VisualNotificationType.OVERLOAD_ALMOST_EXPIRED);
            }
            if(!config.overloadExpiredNotification()) {
                removeNotification(VisualNotificationType.OVERLOAD_EXPIRED);
            }
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

        for(VisualNotification notification : notificationList) {
            notification.configUpdated();
        }
    }

    public void clearNotifications() {
        notificationList.clear();
    }
}
