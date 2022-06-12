package com.github.iant89.ultimatenmz.notifications;

import com.github.iant89.ultimatenmz.UltimateNMZPlugin;
import com.github.iant89.ultimatenmz.utils.InventoryUtils;
import net.runelite.api.ItemID;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NotificationManager {

    private static ArrayList<Notification> notificationList = new ArrayList<>();
    private static HashMap<NotificationType, Long> notificationBlockMap = new HashMap<>();

    private UltimateNMZPlugin plugin;

    @Inject
    protected NotificationManager(UltimateNMZPlugin plugin) {

        this.plugin = plugin;
    }

    public void disableNotificationType(NotificationType type) {
        disableNotificationType(type, -1);
    }

    public void disableNotificationType(NotificationType type, int seconds) {
        cleanNotifications();

        if(notificationBlockMap.containsKey(type)) {
            notificationBlockMap.remove(type);
        }

        if(seconds > 0) {
            notificationBlockMap.put(type, System.currentTimeMillis() + (seconds * 1000));
        } else if(seconds == -1) {
            notificationBlockMap.put(type, (long) -1);
        }
    }

    public void enableNotificationType(NotificationType type) {
        cleanNotifications();

        if(notificationBlockMap.containsKey(type)) {
            notificationBlockMap.remove(type);
        }
    }

    public boolean isBlocked(NotificationType type) {
        cleanNotifications();

        return notificationBlockMap.containsKey(type);
    }

    public synchronized void removeAll() {
        notificationList.clear();
    }

    public synchronized void create(NotificationType type) {
        Notification notification = getNotification(type);
        long notificationLength = -1;

        cleanNotifications();

        if(notificationBlockMap.containsKey(type)) {
            return;
        }

        if(notification != null) {
            if(notification.getLength() == -1) {
                return;
            } else {
                remove(type);
            }
        }

        switch (type) {

            case HP_ABOVE_THRESHOLD:
            case OVERLOAD_ALMOST_EXPIRED:
                break;

            case OVERLOAD_EXPIRED:
                // Only allow creation of alert if we have OVERLOAD pots in inventory.
                if(!InventoryUtils.hasOneOfItems(plugin.getClient(), ItemID.OVERLOAD_4, ItemID.OVERLOAD_3, ItemID.OVERLOAD_2, ItemID.OVERLOAD_1)) {
                    return;
                }
                break;

            case ABSORPTION_BELOW_THRESHOLD:
                // Only allow creation of alert if we have ABSORPTION pots in inventory.
                if(!InventoryUtils.hasOneOfItems(plugin.getClient(), ItemID.ABSORPTION_4, ItemID.ABSORPTION_3, ItemID.ABSORPTION_2, ItemID.ABSORPTION_1)) {
                    return;
                }
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
            // Convert Seconds to milliseconds
            notificationLength = notificationLength * 1000;
        }

        notification = new Notification(plugin, type, notificationLength);

        notificationList.add(notification);
    }



    public int getCount() {
        return notificationList.size();
    }

    public synchronized ArrayList<Notification> getNotifications() {
        return new ArrayList<>(notificationList);
    }

    public synchronized void cleanNotifications() {
        notificationList.removeIf(Notification::isExpired);

        if(notificationBlockMap.size() > 0) {
            for (Map.Entry<NotificationType, Long> entry : notificationBlockMap.entrySet()) {
                if (System.currentTimeMillis() >= entry.getValue()) {
                    notificationBlockMap.remove(entry.getKey());
                }
            }
        }
    }

    public synchronized boolean hasType(NotificationType type) {
        return (getNotification(type) != null);
    }

    public synchronized Notification getNotification(NotificationType type) {
        for(Notification notification : notificationList) {
            if(notification.getType() == type) {
                return notification;
            }
        }

        return null;
    }

    public void remove(NotificationType type) {
        Iterator<Notification> notificationIterator = notificationList.iterator();
        while (notificationIterator.hasNext()) {
            Notification notification = notificationIterator.next();

            if(notification.getType() == type) {
                notificationIterator.remove();
                break;
            }
        }
    }

    public void configUpdated() {
        if(!plugin.getConfig().visualAlerts()) {
            notificationList.clear();
        } else {
            if(!plugin.getConfig().overloadRunoutNotification()) {
                remove(NotificationType.OVERLOAD_ALMOST_EXPIRED);
            }
            if(!plugin.getConfig().overloadExpiredNotification()) {
                remove(NotificationType.OVERLOAD_EXPIRED);
            }
            if(!plugin.getConfig().overloadRunoutNotification()) {
                remove(NotificationType.OVERLOAD_ALMOST_EXPIRED);
            }
            if(!plugin.getConfig().overloadExpiredNotification()) {
                remove(NotificationType.OVERLOAD_EXPIRED);
            }
            if(!plugin.getConfig().absorptionNotification()) {
                remove(NotificationType.ABSORPTION_BELOW_THRESHOLD);
            }
            if(!plugin.getConfig().maximumHPNotification()) {
                remove(NotificationType.HP_ABOVE_THRESHOLD);
            }
            if(!plugin.getConfig().minimumHPNotification()) {
                remove(NotificationType.HP_BELOW_THRESHOLD);
            }
            if(!plugin.getConfig().powerSurgeNotification()) {
                remove(NotificationType.POWER_SURGE_SPAWNED);
            }
            if(!plugin.getConfig().zapperNotification()) {
                remove(NotificationType.ZAPPER_SPAWNED);
            }
            if(!plugin.getConfig().recurrentDamageNotification()) {
                remove(NotificationType.RECURRENT_DAMAGE_SPAWNED);
            }
            if(!plugin.getConfig().ultimateForceNotification()) {
                remove(NotificationType.ULTIMATE_FORCE_SPAWNED);
            }
        }

        for(Notification notification : notificationList) {
            notification.configUpdated();
        }
    }

    public void clearNotifications() {
        notificationList.clear();
    }
}
