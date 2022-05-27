package com.github.iant89.ultimatenmz;

import com.github.iant89.ultimatenmz.notifications.VisualNotificationEffectType;
import com.github.iant89.ultimatenmz.notifications.VisualNotificationSpeed;
import net.runelite.client.config.*;

import java.awt.*;

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
            name = "Minimum Hitpoints",
            description = "Options pertaining to Minimum Hitpoints.",
            position = 51,
            closedByDefault = false
    )
    String minimumHitpointsSection = "minimumHitpoints";

    @ConfigSection(
            name = "Maximum Hitpoints",
            description = "Options pertaining to Maximum Hitpoints.",
            position = 52,
            closedByDefault = false
    )
    String maximumHitpointsSection = "maximumHitpoints";

    @ConfigSection(
            name = "Recurrent Damage Power-Up",
            description = "Options pertaining to Recurrent Damage Power-Ups.",
            position = 53,
            closedByDefault = false
    )
    String recurrentDamagePowerupSection = "recurrentDamagePowerup";

    @ConfigSection(
            name = "Zapper Power-Up",
            description = "Options pertaining to Zapper Power-Ups.",
            position = 54,
            closedByDefault = false
    )
    String zapperPowerupSection = "zapperPowerup";

    @ConfigSection(
            name = "Power Surge Power-Up",
            description = "Options pertaining to Power Surge Power-Ups.",
            position = 55,
            closedByDefault = false
    )
    String powerSurgePowerupSection = "powerSurgePowerup";

    @ConfigSection(
            name = "Ultimate Force Power-Up",
            description = "Options pertaining to Ultimate Force Power-Ups.",
            position = 56,
            closedByDefault = false
    )
    String ultimateForcePowerupSection = "ultimateForcePowerup";

    @ConfigSection(
            name = "Absorption",
            description = "Options pertaining to Absorption Potions.",
            position = 57,
            closedByDefault = false
    )
    String absorptionSection = "absorption";

    @ConfigSection(
            name = "Overload",
            description = "Options pertaining to Overloads Potions.",
            position = 58,
            closedByDefault = false
    )
    String overloadSection = "overloads";

    /*
    @ConfigSection(
            name = "Super Magic Potions",
            description = "Options pertaining to Super Magic Potions.",
            position = 59,
            closedByDefault = false
    )
    String superMagicPotionSection = "supermagic";

    @ConfigSection(
            name = "Super Ranging Potions",
            description = "Options pertaining to Super Ranging Potions.",
            position = 60,
            closedByDefault = false
    )
    String superRangingPotionSection = "superranging";
     */

    @ConfigSection(
            name = "Paint",
            description = "Options pertaining to the Paint.",
            position = 59,
            closedByDefault = false
    )
    String paintSection = "paint";

    /*
     * GENERAL SECTION
     */

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

    /*
     * MINIMUM HP SECTION
     */

    @ConfigItem(
            keyName = "minimumHPNotification",
            name = "Show Notifications",
            description = "Enables notifications when your HP gets below the threshold.",
            position = 0,
            section = minimumHitpointsSection
    )
    default boolean minimumHPNotification() {
        return true;
    }

    @ConfigItem(
            keyName = "minimumHPThresholdValue",
            name = "Threshold",
            description = "The Minimum HP before triggering a alert.",
            position = 1,
            section = minimumHitpointsSection
    )
    default int minimumHPThresholdValue() {
        return 1;
    }

    @ConfigItem(
            keyName = "showMinimumHPIcon",
            name = "Show Icon",
            description = "Toggles if the Hitpoints icon is drawn on the visual notification.",
            position = 2,
            section = minimumHitpointsSection
    )
    default boolean showMinimumHPIcon() {
        return true;
    }

    @ConfigItem(
            keyName = "minimumHPAlertColor",
            name = "Color",
            description = "The color of the Hitpoints BELOW Threshold Notification.",
            position = 3,
            section = minimumHitpointsSection
    )
    default Color minimumHPAlertColor() {
        return new Color(221, 79, 1);
    }

    @ConfigItem(
            keyName = "minimumHPEffectType",
            name = "Effect",
            description = "The type of effect for Hitpoints BELOW Threshold Notification.",
            position = 4,
            section = minimumHitpointsSection
    )
    default VisualNotificationEffectType minimumHPEffectType() {
        return VisualNotificationEffectType.FADE_IN_OUT;
    }

    @ConfigItem(
            keyName = "minimumHPEffectSpeed",
            name = "Speed",
            description = "The speed of the notification effect, This does nothing if effect type is `SOLID`.",
            position = 5,
            section = minimumHitpointsSection
    )
    default VisualNotificationSpeed minimumHPEffectSpeed() {
        return VisualNotificationSpeed.DEFAULT;
    }

    /*
     * MAXIMUM HP SECTION
     */

    @ConfigItem(
            keyName = "maximumHPNotification",
            name = "Show Notifications",
            description = "Enables notifications when your HP gets above the threshold.",
            position = 0,
            section = maximumHitpointsSection
    )
    default boolean maximumHPNotification() {
        return true;
    }

    @ConfigItem(
            keyName = "maximumHPThreshold",
            name = "Threshold",
            description = "The Maximum HP before triggering a alert.",
            position = 1,
            section = maximumHitpointsSection
    )
    default int maximumHPThresholdValue() {
        return 2;
    }

    @ConfigItem(
            keyName = "showMaximumHPIcon",
            name = "Show Icon",
            description = "Toggles if the Hitpoints icon is drawn on the visual notification.",
            position = 2,
            section = maximumHitpointsSection
    )
    default boolean showMaximumHPIcon() {
        return true;
    }

    @ConfigItem(
            keyName = "maximumHPAlertColor",
            name = "Color",
            description = "The color of the Hitpoints ABOVE Threshold Notification.",
            position = 3,
            section = maximumHitpointsSection
    )
    default Color maximumHPAlertColor() {
        return new Color(221, 79, 1);
    }

    @ConfigItem(
            keyName = "maximumHPEffectType",
            name = "Effect",
            description = "The type of effect for Hitpoints ABOVE Threshold Notification.",
            position = 4,
            section = maximumHitpointsSection
    )
    default VisualNotificationEffectType maximumHPEffectType() {
        return VisualNotificationEffectType.FADE_IN_OUT;
    }

    @ConfigItem(
            keyName = "maximumHPEffectSpeed",
            name = "Speed",
            description = "The speed of the notification effect, This does nothing if effect type is `SOLID`.",
            position = 5,
            section = maximumHitpointsSection
    )
    default VisualNotificationSpeed maximumHPEffectSpeed() {
        return VisualNotificationSpeed.DEFAULT;
    }

    /*
     * RECURRENT DAMAGE POWER UP SECTION
     */

    @ConfigItem(
            keyName = "drawrecurrentdamagelocation",
            name = "Show Spawn Location",
            description = "Toggles highlighting the tile where the Recurrent Damage Spawned.",
            position = 0,
            section = recurrentDamagePowerupSection
    )
    default boolean drawRecurrentDamageLocation() {
        return true;
    }

    @ConfigItem(
            keyName = "recurrentdamagenotification",
            name = "Recurrent Damage Notification",
            description = "Toggles notifications when a Recurrent Damage Power-up is Spawned.",
            position = 1,
            section = recurrentDamagePowerupSection
    )
    default boolean recurrentDamageNotification() {
        return true;
    }

    @ConfigItem(
            keyName = "recurrentdamageAlertColor",
            name = "Color",
            description = "The color of the Recurrent Damage Power-Up Notification.",
            position = 2,
            section = recurrentDamagePowerupSection
    )
    default Color recurrentDamageAlertColor() {
        return new Color(255, 0, 21);
    }

    @ConfigItem(
            keyName = "recurrentDamageEffectType",
            name = "Effect",
            description = "The type of effect for Recurrent Damage Power-Up Notification.",
            position = 3,
            section = recurrentDamagePowerupSection
    )
    default VisualNotificationEffectType recurrentDamageEffectType() {
        return VisualNotificationEffectType.FADE_IN_OUT;
    }

    @ConfigItem(
            keyName = "recurrentDamageEffectSpeed",
            name = "Speed",
            description = "The speed of the notification effect, This does nothing if effect type is `SOLID`.",
            position = 4,
            section = recurrentDamagePowerupSection
    )
    default VisualNotificationSpeed recurrentDamageEffectSpeed() {
        return VisualNotificationSpeed.DEFAULT;
    }

    /*
     * ZAPPER POWER UP SECTION
     */

    @ConfigItem(
            keyName = "drawzapperlocation",
            name = "Show Spawn Location",
            description = "Toggles highlighting the tile where the Zapper Spawned.",
            position = 0,
            section = zapperPowerupSection
    )
    default boolean drawZapperLocation() {
        return true;
    }

    @ConfigItem(
            keyName = "zappernotification",
            name = "Zapper Notification",
            description = "Toggles notifications when a Zapper Power-up is Spawned.",
            position = 1,
            section = zapperPowerupSection
    )
    default boolean zapperNotification() {
        return true;
    }

    @ConfigItem(
            keyName = "zapperAlertColor",
            name = "Color",
            description = "The color of the Zapper Power-Up Notification.",
            position = 2,
            section = zapperPowerupSection
    )
    default Color zapperAlertColor() {
        return new Color(161, 0, 255);
    }

    @ConfigItem(
            keyName = "zapperEffectType",
            name = "Effect",
            description = "The type of effect for Zapper Power-Up Notification.",
            position = 3,
            section = zapperPowerupSection
    )
    default VisualNotificationEffectType zapperEffectType() {
        return VisualNotificationEffectType.FADE_IN_OUT;
    }

    @ConfigItem(
            keyName = "zapperEffectSpeed",
            name = "Speed",
            description = "The speed of the notification effect, This does nothing if effect type is `SOLID`.",
            position = 4,
            section = zapperPowerupSection
    )
    default VisualNotificationSpeed zapperEffectSpeed() {
        return VisualNotificationSpeed.DEFAULT;
    }

    /*
     * POWER SURGE POWER UP SECTION
     */

    @ConfigItem(
            keyName = "drawpowersurgelocation",
            name = "Show Spawn Location",
            description = "Toggles highlighting the tile where the Power Surge Spawned.",
            position = 0,
            section = powerSurgePowerupSection
    )
    default boolean drawPowerSurgeLocation() {
        return true;
    }

    @ConfigItem(
            keyName = "powersurgenotification",
            name = "Power Surge Notification",
            description = "Toggles notifications when a Power Surge Power-up is Spawned.",
            position = 1,
            section = powerSurgePowerupSection
    )
    default boolean powerSurgeNotification() {
        return true;
    }

    @ConfigItem(
            keyName = "powerSurgeAlertColor",
            name = "Color",
            description = "The color of the Power Surge Power-Up Notification.",
            position = 2,
            section = powerSurgePowerupSection
    )
    default Color powerSurgeAlertColor() {
        return new Color(255, 221, 0);
    }

    @ConfigItem(
            keyName = "powerSurgeEffectType",
            name = "Effect",
            description = "The type of effect for Power Surge Power-Up Notification.",
            position = 3,
            section = powerSurgePowerupSection
    )
    default VisualNotificationEffectType powerSurgeEffectType() {
        return VisualNotificationEffectType.FADE_IN_OUT;
    }

    @ConfigItem(
            keyName = "powerSurgeEffectSpeed",
            name = "Speed",
            description = "The speed of the notification effect, This does nothing if effect type is `SOLID`.",
            position = 4,
            section = powerSurgePowerupSection
    )
    default VisualNotificationSpeed powerSurgeEffectSpeed() {
        return VisualNotificationSpeed.DEFAULT;
    }

    /*
     * ULTIMATE FORCE POWER UP SECTION
     */

    @ConfigItem(
            keyName = "ultimateforcelocation",
            name = "Show Spawn Location",
            description = "Toggles highlighting the tile where the Ultimate Force Spawned.",
            position = 0,
            section = ultimateForcePowerupSection
    )
    default boolean drawUltimateForceLocation() {
        return true;
    }

    @ConfigItem(
            keyName = "ultimateforcenotification",
            name = "Ultimate Force Notification",
            description = "Toggles notifications when a Ultimate Force Power-up is Spawned.",
            position = 1,
            section = ultimateForcePowerupSection
    )
    default boolean ultimateForceNotification() {
        return true;
    }

    @ConfigItem(
            keyName = "ultimateForceAlertColor",
            name = "Color",
            description = "The color of the Ultimate Force Power-Up Notification.",
            position = 2,
            section = ultimateForcePowerupSection
    )
    default Color ultimateForceAlertColor() {
        return new Color(255, 255, 255);
    }

    @ConfigItem(
            keyName = "ultimateForceEffectType",
            name = "Effect",
            description = "The type of effect for Ultimate Force Power-Up Notification.",
            position = 3,
            section = ultimateForcePowerupSection
    )
    default VisualNotificationEffectType ultimateForceEffectType() {
        return VisualNotificationEffectType.FADE_IN_OUT;
    }

    @ConfigItem(
            keyName = "ultimateForceEffectSpeed",
            name = "Speed",
            description = "The speed of the notification effect, This does nothing if effect type is `SOLID`.",
            position = 4,
            section = ultimateForcePowerupSection
    )
    default VisualNotificationSpeed ultimateForceEffectSpeed() {
        return VisualNotificationSpeed.DEFAULT;
    }

    /*
     * OVERLOAD SECTION
     */
    @ConfigItem(
            keyName = "overloadRunoutNotification",
            name = "Warning Notification",
            description = "Toggles notifications when your overload is about to run out.",
            position = 0,
            section = overloadSection
    )
    default boolean overloadRunoutNotification() {
        return true;
    }

    @Range(
            min = 0,
            max = 120
    )
    @ConfigItem(
            keyName = "overloadRunoutTime",
            name = "Warning Seconds",
            description = "The length in seconds before your Overload runs out, to notify you.",
            position = 1,
            section = overloadSection
    )
    default int overloadRunoutTime() {
        return 20;
    }

    @ConfigItem(
            keyName = "showOverloadIcon",
            name = "Show Icon",
            description = "Toggles if a Overload Potion icon is drawn on the visual notification.",
            position = 2,
            section = overloadSection
    )
    default boolean showOverloadIcon() {
        return true;
    }

    @ConfigItem(
            keyName = "overloadRunOutColor",
            name = "Warning Color",
            description = "The color of the Overload Run-out Warning Notification.",
            position = 3,
            section = overloadSection
    )
    default Color overloadRunOutColor() {
        return new Color(93, 91, 91);
    }

    @ConfigItem(
            keyName = "overloadRunOutEffectType",
            name = "Warning Effect",
            description = "The type of effect for Overload Run-out Warning Notification.",
            position = 4,
            section = overloadSection
    )
    default VisualNotificationEffectType overloadRunOutEffectType() {
        return VisualNotificationEffectType.FADE_IN_OUT;
    }

    @ConfigItem(
            keyName = "overloadRunOutEffectSpeed",
            name = "Warning Speed",
            description = "The speed of the notification effect, This does nothing if effect type is `SOLID`.",
            position = 5,
            section = overloadSection
    )
    default VisualNotificationSpeed overloadRunOutEffectSpeed() {
        return VisualNotificationSpeed.DEFAULT;
    }

    @ConfigItem(
            keyName = "overloadExpiredNotification",
            name = "Expired Notification",
            description = "Toggles notifications when your overload is has run out.",
            position = 6,
            section = overloadSection
    )
    default boolean overloadExpiredNotification() {
        return true;
    }
    @ConfigItem(
            keyName = "overloadExpiredColor",
            name = "Expired Color",
            description = "The color of the Overload Expired Notification.",
            position = 7,
            section = overloadSection
    )
    default Color overloadExpiredColor() {
        return new Color(35, 35, 35);
    }

    @ConfigItem(
            keyName = "overloadExpiredEffectType",
            name = "Expired Effect",
            description = "The type of effect for Overload Expired Notification.",
            position = 8,
            section = overloadSection
    )
    default VisualNotificationEffectType overloadExpiredEffectType() {
        return VisualNotificationEffectType.FADE_IN_OUT;
    }

    @ConfigItem(
            keyName = "overloadExpiredEffectSpeed",
            name = "Expired Speed",
            description = "The speed of the notification effect, This does nothing if effect type is `SOLID`.",
            position = 9,
            section = overloadSection
    )
    default VisualNotificationSpeed overloadExpiredEffectSpeed() {
        return VisualNotificationSpeed.DEFAULT;
    }

    /*
     * ABSORPTION SECTION
     */

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
            keyName = "showAbsorptionIcon",
            name = "Show Icon",
            description = "Toggles if a Absorption Potion icon is drawn on the visual notification.",
            position = 2,
            section = absorptionSection
    )
    default boolean showAbsorptionIcon() {
        return true;
    }

    @ConfigItem(
            keyName = "absorptionAlertColor",
            name = "Color",
            description = "The color of the Absorption BELOW Threshold Notification.",
            position = 3,
            section = absorptionSection
    )
    default Color absorptionAlertColor() {
        return new Color(0, 179, 255);
    }

    @ConfigItem(
            keyName = "absorptionEffectType",
            name = "Effect",
            description = "The type of effect for Absorption BELOW Threshold Notification.",
            position = 4,
            section = absorptionSection
    )
    default VisualNotificationEffectType absorptionEffectType() {
        return VisualNotificationEffectType.FADE_IN_OUT;
    }

    @ConfigItem(
            keyName = "absorptionEffectSpeed",
            name = "Speed",
            description = "The speed of the notification effect, This does nothing if effect type is `SOLID`.",
            position = 5,
            section = absorptionSection
    )
    default VisualNotificationSpeed absorptionEffectSpeed() {
        return VisualNotificationSpeed.DEFAULT;
    }

    /*
     * PAINT SECTION
     */

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
