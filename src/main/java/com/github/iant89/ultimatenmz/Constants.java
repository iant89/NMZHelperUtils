package com.github.iant89.ultimatenmz;

import net.runelite.api.ObjectID;

import java.time.Duration;

public class Constants {

    public static final int OBJECT_ZAPPER = ObjectID.ZAPPER_26256;
    public static final int OBJECT_POWER_SURGE = ObjectID.POWER_SURGE;
    public static final int OBJECT_RECURRENT_DAMAGE = ObjectID.RECURRENT_DAMAGE;
    public static final int OBJECT_ULTIMATE_FORCE = ObjectID.ULTIMATE_FORCE;

    public static final Duration NATIVE_NOTIFICATION_DELAY = Duration.ofSeconds(30);


    public static final String NATIVE_NOTIFICATION_NMZ_STARTED_MESSAGE = "Nightmare Zone has started!";
    public static final String NATIVE_NOTIFICATION_NMZ_ENDED_MESSAGE = "Oh dear! You have died in the Nightmare Zone.";
    public static final String NATIVE_NOTIFICATION_MINIMUM_HP_MESSAGE = "Your Hitpoints has dropped below your threshold.";
    public static final String NATIVE_NOTIFICATION_MAXIMUM_HP_MESSAGE = "Your Hitpoints has went above your threshold.";
    public static final String NATIVE_NOTIFICATION_ABSORPTION_MESSAGE = "Your Absorption points has dropped below your threshold.";
    public static final String NATIVE_NOTIFICATION_OVERLOAD_WARNING_MESSAGE = "Your Overload Potion is about to wear off.";
    public static final String NATIVE_NOTIFICATION_OVERLOAD_EXPIRED_MESSAGE = "Your Overload Potion has worn off.";
    public static final String NATIVE_NOTIFICATION_POWERUP_ZAPPER_MESSAGE = "A Zapper Power-Up has spawned near you!";
    public static final String NATIVE_NOTIFICATION_POWERUP_RECURRENT_DAMAGE_MESSAGE = "A Recurrent Damage Power-Up has spawned near you!";
    public static final String NATIVE_NOTIFICATION_POWERUP_POWER_SURGE_MESSAGE = "A Power Surge Power-Up has spawned near you!";
    public static final String NATIVE_NOTIFICATION_POWERUP_ULTIMATE_FORCE_MESSAGE = "A Ultimate Force Power-Up has spawned near you!";

}
