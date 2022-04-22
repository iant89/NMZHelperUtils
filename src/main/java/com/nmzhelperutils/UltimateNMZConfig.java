package com.nmzhelperutils;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("ultimatenmz")
public interface UltimateNMZConfig extends Config {


    @ConfigSection(
            name = "General",
            description = "General Configuration",
            position = 50,
            closedByDefault = false
    )
    String generalSection = "general";

    @ConfigSection(
            name = "Hitpoints",
            description = "Options pertaining to Hitpoints.",
            position = 51,
            closedByDefault = false
    )
    String hitpointsSection = "hitpoints";

    @ConfigSection(
            name = "Power-Ups",
            description = "Configuration Options pertaining to Power-Ups.",
            position = 51,
            closedByDefault = false
    )
    String powerupSection = "powerup";

    @ConfigSection(
            name = "Absorption",
            description = "Options pertaining to Absorption Potions.",
            position = 53,
            closedByDefault = false
    )
    String absorptionSection = "absorption";

    @ConfigSection(
            name = "Overload",
            description = "Options pertaining to Overloads Potions.",
            position = 54,
            closedByDefault = false
    )
    String overloadSection = "overloads";

    @ConfigSection(
            name = "Super Magic Potions",
            description = "Options pertaining to Super Magic Potions.",
            position = 55,
            closedByDefault = false
    )
    String superMagicPotionSection = "supermagic";

    @ConfigSection(
            name = "Super Ranging Potions",
            description = "Options pertaining to Super Ranging Potions.",
            position = 56,
            closedByDefault = false
    )
    String superRangingPotionSection = "superranging";

    @ConfigSection(
            name = "Paint",
            description = "Options pertaining to the Paint.",
            position = 57,
            closedByDefault = false
    )
    String paintSection = "paint";

    @ConfigItem(
            keyName = "visualAlerts",
            name = "Visual Notifications",
            description = "Toggles visual notifications.",
            position = 0,
            section = generalSection
    )
    default boolean visualAlerts() {
        return true;
    }

    @ConfigItem(
            keyName = "minimumHPNotification",
            name = "Minimum HP Notification",
            description = "Toggles notifications when your HP gets below a threshold.",
            position = 0,
            section = hitpointsSection
    )
    default boolean minimumHPNotification() {
        return true;
    }

    @ConfigItem(
            keyName = "minimumHPThresholdValue",
            name = "Minimum HP Threshold",
            description = "The Minimum HP before triggering a alert.",
            position = 1,
            section = hitpointsSection
    )
    default int minimumHPThresholdValue() {
        return 1;
    }

    @ConfigItem(
            keyName = "maximumHPNotification",
            name = "Maximum HP Notification",
            description = "Toggles notifications when your HP gets above your threshold.",
            position = 2,
            section = hitpointsSection
    )
    default boolean maximumHPNotification() {
        return true;
    }

    @ConfigItem(
            keyName = "maximumHPThreshold",
            name = "Maximum HP Threshold",
            description = "The Maximum HP before triggering a alert.",
            position = 3,
            section = hitpointsSection
    )
    default int maximumHPThresholdValue() {
        return 2;
    }

    @ConfigItem(
            keyName = "showpoweruplocation",
            name = "Show Power-Up Location",
            description = "Toggles highlighting the tile where the power-up spawned.",
            position = 0,
            section = powerupSection
    )
    default boolean drawPowerUpLocation() {
        return true;
    }

    @ConfigItem(
            keyName = "recurrentdamagenotification",
            name = "Recurrent damage Notification",
            description = "Toggles notifications when a recurrent damage power-up spawns.",
            position = 1,
            section = powerupSection
    )
    default boolean recurrentDamageNotification() {
        return true;
    }

    @ConfigItem(
            keyName = "zappernotification",
            name = "Zapper notification",
            description = "Toggles notifications when a zapper power-up spawns.",
            position = 2,
            section = powerupSection
    )
    default boolean zapperNotification() {
        return true;
    }

    @ConfigItem(
            keyName = "powersurgenotification",
            name = "Power-Surge Notification",
            description = "Toggles notifications when a Power surge power-up spawns.",
            position = 3,
            section = powerupSection
    )
    default boolean powerSurgeNotification() {
        return false;
    }

    @ConfigItem(
            keyName = "ultimateforcenotification",
            name = "Ultimate Force notification",
            description = "Toggles notifications when an Ultimate force power-up spawns.",
            position = 4,
            section = powerupSection
    )
    default boolean ultimateForceNotification() {
        return false;
    }

    @ConfigItem(
            keyName = "absorptionnotification",
            name = "Absorption Notification",
            description = "Toggles notifications when your absorption points gets below your threshold",
            position = 0,
            section = absorptionSection
    )
    default boolean absorptionNotification() {
        return true;
    }

    @ConfigItem(
            keyName = "absorptionthreshold",
            name = "Minimum Threshold",
            description = "The Minimum Absorption before triggering a alert.",
            position = 1,
            section = absorptionSection
    )
    default int absorptionThreshold() {
        return 50;
    }

    @ConfigItem(
            keyName = "moveoverlay",
            name = "Override NMZ overlay",
            description = "Overrides the overlay so it doesn't conflict with other RuneLite plugins",
            position = 0,
            section = paintSection
    )
    default boolean moveOverlay() {
        return true;
    }
}
