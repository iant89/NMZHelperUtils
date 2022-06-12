package com.github.iant89.ultimatenmz.notifications;

import com.github.iant89.ultimatenmz.Constants;
import com.github.iant89.ultimatenmz.UltimateNMZPlugin;
import com.github.iant89.ultimatenmz.utils.InventoryUtils;
import net.runelite.api.ItemID;

import javax.inject.Inject;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;

public class NativeNotification {

    private Instant lastHPAboveThresholdNotification;
    private Instant lastHPBelowThresholdNotification;
    private Instant lastAbsorptionBelowThresholdNotification;
    private Instant lastOverloadWarningNotification;
    private Instant lastOverloadExpiredNotification;
    private Instant lastZapperNotification;
    private Instant lastRecurrentDamageNotification;
    private Instant lastUltimateForceNotification;
    private Instant lastPowerSurgeNotification;

    private UltimateNMZPlugin plugin;

    @Inject
    protected NativeNotification(UltimateNMZPlugin plugin) {
        this.plugin = plugin;
    }

    public void create(NotificationType type) {
        if(!plugin.getConfig().nativeAlerts()) {
            return;
        }

        switch (type) {
            case HP_BELOW_THRESHOLD:
                if(!plugin.getConfig().minimumHPNotification()) {
                    break;
                }

                if(checkDuration(lastHPBelowThresholdNotification, Constants.NATIVE_NOTIFICATION_DELAY)) {
                    lastHPBelowThresholdNotification = Instant.now();
                    plugin.getNotifier().notify(Constants.NATIVE_NOTIFICATION_MINIMUM_HP_MESSAGE);
                }
                break;

            case HP_ABOVE_THRESHOLD:
                if(!plugin.getConfig().maximumHPNotification()) {
                    break;
                }

                if(checkDuration(lastHPAboveThresholdNotification, Constants.NATIVE_NOTIFICATION_DELAY)) {
                    lastHPAboveThresholdNotification = Instant.now();
                    plugin.getNotifier().notify(Constants.NATIVE_NOTIFICATION_MAXIMUM_HP_MESSAGE);
                }
                break;
            case ABSORPTION_BELOW_THRESHOLD:
                if(!plugin.getConfig().absorptionNotification()) {
                    break;
                }

                if(InventoryUtils.hasOneOfItems(plugin.getClient(), ItemID.ABSORPTION_4, ItemID.ABSORPTION_3, ItemID.ABSORPTION_2, ItemID.ABSORPTION_1)) {
                    if (checkDuration(lastAbsorptionBelowThresholdNotification, Constants.NATIVE_NOTIFICATION_DELAY)) {
                        lastAbsorptionBelowThresholdNotification = Instant.now();
                        plugin.getNotifier().notify(Constants.NATIVE_NOTIFICATION_ABSORPTION_MESSAGE);
                    }
                }
                break;

            case OVERLOAD_ALMOST_EXPIRED:
                if(!plugin.getConfig().overloadRunoutNotification()) {
                    break;
                }

                if(InventoryUtils.hasOneOfItems(plugin.getClient(), ItemID.OVERLOAD_4, ItemID.OVERLOAD_3, ItemID.OVERLOAD_2, ItemID.OVERLOAD_1)) {
                    if (checkDuration(lastOverloadWarningNotification, Constants.NATIVE_NOTIFICATION_DELAY)) {
                        lastOverloadWarningNotification = Instant.now();
                        plugin.getNotifier().notify(Constants.NATIVE_NOTIFICATION_OVERLOAD_WARNING_MESSAGE);
                    }
                }
                break;

            case OVERLOAD_EXPIRED:
                if(!plugin.getConfig().overloadExpiredNotification()) {
                    break;
                }

                if(InventoryUtils.hasOneOfItems(plugin.getClient(), ItemID.OVERLOAD_4, ItemID.OVERLOAD_3, ItemID.OVERLOAD_2, ItemID.OVERLOAD_1)) {
                    if (checkDuration(lastOverloadExpiredNotification, Constants.NATIVE_NOTIFICATION_DELAY)) {
                        lastOverloadExpiredNotification = Instant.now();
                        plugin.getNotifier().notify(Constants.NATIVE_NOTIFICATION_OVERLOAD_EXPIRED_MESSAGE);
                    }
                }
                break;

            case ZAPPER_SPAWNED:
                if(!plugin.getConfig().zapperNotification()) {
                    break;
                }

                if (checkDuration(lastZapperNotification, Constants.NATIVE_NOTIFICATION_DELAY)) {
                    lastZapperNotification = Instant.now();
                    plugin.getNotifier().notify(Constants.NATIVE_NOTIFICATION_POWERUP_ZAPPER_MESSAGE);
                }
                break;

            case POWER_SURGE_SPAWNED:
                if(!plugin.getConfig().powerSurgeNotification()) {
                    break;
                }

                if (checkDuration(lastPowerSurgeNotification, Constants.NATIVE_NOTIFICATION_DELAY)) {
                    lastPowerSurgeNotification = Instant.now();
                    plugin.getNotifier().notify(Constants.NATIVE_NOTIFICATION_POWERUP_POWER_SURGE_MESSAGE);
                }
                break;

            case RECURRENT_DAMAGE_SPAWNED:
                if(!plugin.getConfig().recurrentDamageNotification()) {
                    break;
                }

                if (checkDuration(lastRecurrentDamageNotification, Constants.NATIVE_NOTIFICATION_DELAY)) {
                    lastRecurrentDamageNotification = Instant.now();
                    plugin.getNotifier().notify(Constants.NATIVE_NOTIFICATION_POWERUP_RECURRENT_DAMAGE_MESSAGE);
                }
                break;

            case ULTIMATE_FORCE_SPAWNED:
                if(!plugin.getConfig().ultimateForceNotification()) {
                    break;
                }

                if (checkDuration(lastUltimateForceNotification, Constants.NATIVE_NOTIFICATION_DELAY)) {
                    lastUltimateForceNotification = Instant.now();
                    plugin.getNotifier().notify(Constants.NATIVE_NOTIFICATION_POWERUP_ULTIMATE_FORCE_MESSAGE);
                }
                break;
        }
    }

    /**
     * Checks if the time between now and instant has been atleast the duration seconds.
     *
     * @param instant
     * @return
     */
    private boolean checkDuration(Instant instant, Duration duration) {
        if(instant == null) {
            return true;
        }

        if(Duration.between(Instant.now(), instant).getSeconds() > duration.getSeconds()) {
            return true;
        }

        return false;
    }
}
