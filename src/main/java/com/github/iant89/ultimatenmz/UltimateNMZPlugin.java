package com.github.iant89.ultimatenmz;


import com.github.iant89.ultimatenmz.notifications.*;
import com.github.iant89.ultimatenmz.overlays.*;
import com.github.iant89.ultimatenmz.utils.InventoryUtils;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
@PluginDescriptor(
	name = "Ultimate NMZ",
	description = "AFK at Nightmare Zone, While watching youtube or playing a game? Say no more!",
	tags = { "nmz", "afk", "nightmare", "nightmare zone", "info", "alert", "overlay", "combat", "boosts"}
)
public class UltimateNMZPlugin extends Plugin {

	private static final int[] NMZ_MAP_REGION = {9033};

	private static final Duration HOUR = Duration.ofHours(1);

	private static final Duration OVERLOAD_DURATION = Duration.ofMinutes(5);

	@Inject
	@Getter
	private Client client;

	@Inject
	@Getter
	private Notifier notifier;

	@Inject
	private ClientThread clientThread;

	@Inject
	@Getter
	private OverlayManager overlayManager;

	@Inject
	private UltimateNMZConfig config;

	@Inject
	@Getter
	private SkillIconManager iconManager;

	@Inject
	private UltimateNMZOverlay ultimateNmzOverlay;

	@Inject
	@Getter
	private NotificationOverlay notificationOverlay;

	@Inject
	@Getter
	private CountdownOverlay countdownOverlay;

	@Inject
	@Getter
	private PowerUpOverlay powerUpOverlay;

	@Inject
	@Getter
	private NotificationManager notificationManager;

	@Inject
	@Getter
	private NativeNotification nativeNotificationManager;

	@Getter
	public int pointsPerHour;

	private Instant nmzSessionStartTime;

	private Instant lastOverload;

	private long overloadTimer = -1;

	private Instant sessionStart;
	private Duration sessionDuration;
	private Instant sessionEnd;


	@Override
	protected void startUp() throws Exception {
		overlayManager.add(countdownOverlay);
		overlayManager.add(powerUpOverlay);
		overlayManager.add(notificationOverlay);
		overlayManager.add(ultimateNmzOverlay);

		if(client.getGameState() == GameState.LOGGED_IN) {
			clientThread.invoke(this::start);
		}
	}


	private void start() {

	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(countdownOverlay);
		overlayManager.remove(ultimateNmzOverlay);
		overlayManager.remove(notificationOverlay);
		overlayManager.remove(powerUpOverlay);

		// Restore Nightmare Zone Widget Visibility
		Widget nmzWidget = client.getWidget(WidgetInfo.NIGHTMARE_ZONE);
		if (nmzWidget != null) {
			nmzWidget.setHidden(false);
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals("ultimatenmz")) {
			return;
		}

		if(!config.visualAlerts()) {
			getNotificationManager().clearNotifications();
		}

		getNotificationManager().configUpdated();
		ultimateNmzOverlay.updateConfig();

		Widget nmzWidget = client.getWidget(WidgetInfo.NIGHTMARE_ZONE);
		if (nmzWidget != null) {
			nmzWidget.setHidden(getConfig().removeNMZOverlay());
		}
	}

	@Provides
	UltimateNMZConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(UltimateNMZConfig.class);
	}

	public UltimateNMZOverlay getUltimateNmzOverlay() {
		return ultimateNmzOverlay;
	}


	public Duration getOverloadDurationLeft() {

		if(overloadTimer == -1) {
			return null;
		}

		if(System.currentTimeMillis() >= overloadTimer) {
			return null;
		}

		return Duration.ofSeconds((overloadTimer - System.currentTimeMillis()) / 1000);
	}

	public Instant getSessionStart() {
		return sessionStart;
	}

	public Instant getSessionEnd() {
		return sessionEnd;
	}

	public Duration getSessionDuration() {
		if(sessionEnd != null) {
			return Duration.between(sessionStart, sessionEnd);
		}

		return Duration.between(sessionStart, Instant.now());
	}

	@Subscribe
	private void onChatMessage(ChatMessage event) {
		String msg = Text.removeTags(event.getMessage());

		if(msg.contains("You drink some of your overload potion.")) {

			// Drinking an Overload Potion
			overloadTimer = System.currentTimeMillis() + ((5 * 60) * 1000);

			lastOverload = Instant.now();

			getNotificationManager().remove(NotificationType.OVERLOAD_ALMOST_EXPIRED);
			getNotificationManager().remove(NotificationType.OVERLOAD_EXPIRED);

			if(config.maximumHPNotification()) {
				int hpLeft = client.getBoostedSkillLevel(Skill.HITPOINTS) - 50;

				if(hpLeft <= config.maximumHPThresholdValue()) {
					getNotificationManager().remove(NotificationType.HP_ABOVE_THRESHOLD);
					getNotificationManager().disableNotificationType(NotificationType.HP_ABOVE_THRESHOLD, 8);
				}
			}
			return;

		}

		// Check to see if we are in the NMZ before we process any of the other messages.
		if(!isInNightmareZone()) {
			return;
		}

		if (msg.contains("effects of overload have worn off")) {

			overloadTimer = -1;
			// Overload Potion worn off
			if (config.overloadExpiredNotification()) {
				if(InventoryUtils.hasOneOfItems(client, ItemID.OVERLOAD_4, ItemID.OVERLOAD_3, ItemID.OVERLOAD_2, ItemID.OVERLOAD_1)) {
					getNotificationManager().disableNotificationType(NotificationType.HP_ABOVE_THRESHOLD, 3);
					getNotificationManager().remove(NotificationType.OVERLOAD_ALMOST_EXPIRED);
					getNotificationManager().create(NotificationType.OVERLOAD_EXPIRED);

				} else {
					getNotificationManager().remove(NotificationType.OVERLOAD_ALMOST_EXPIRED);
					getNotificationManager().remove(NotificationType.OVERLOAD_EXPIRED);
				}
			} else {
				getNotificationManager().remove(NotificationType.OVERLOAD_ALMOST_EXPIRED);
			}

			getNativeNotificationManager().create(NotificationType.OVERLOAD_EXPIRED);

			lastOverload = null;

		} else if(msg.contains("You wake up feeling refreshed")) {

			// NMZ has ended...
			endSession();

		} else if(msg.contains("25 secs to")) {

			// Started NMZ...
			startSession();

		} else if (msg.contains("A power-up has spawned:")) {

			if (msg.contains("Power surge")) {

				// Power-Surge Spawned
				if(config.powerSurgeNotification()) {
					getNotificationManager().create(NotificationType.POWER_SURGE_SPAWNED);
					getNativeNotificationManager().create(NotificationType.POWER_SURGE_SPAWNED);
				}

			} else if (msg.contains("Recurrent damage")) {

				// Recurrent Damage Spawned
				if(config.recurrentDamageNotification()) {
					getNotificationManager().create(NotificationType.RECURRENT_DAMAGE_SPAWNED);
					getNativeNotificationManager().create(NotificationType.RECURRENT_DAMAGE_SPAWNED);
				}

			} else if (msg.contains("Zapper")) {

				// Zapper Spawned
				if(config.zapperNotification()) {
					getNotificationManager().create(NotificationType.ZAPPER_SPAWNED);
					getNativeNotificationManager().create(NotificationType.ZAPPER_SPAWNED);
				}

			} else if (msg.contains("Ultimate force")) {

				// Ultimate Force Spawned
				if(config.ultimateForceNotification()) {
					getNotificationManager().create(NotificationType.ULTIMATE_FORCE_SPAWNED);
					getNativeNotificationManager().create(NotificationType.ULTIMATE_FORCE_SPAWNED);
				}
			}
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event) {

	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event) {
		switch (event.getGameObject().getId()) {
			case Constants.OBJECT_POWER_SURGE:
				getNotificationManager().remove(NotificationType.POWER_SURGE_SPAWNED);
				break;

			case Constants.OBJECT_RECURRENT_DAMAGE:
				getNotificationManager().remove(NotificationType.RECURRENT_DAMAGE_SPAWNED);
				break;

			case Constants.OBJECT_ZAPPER:
				getNotificationManager().remove(NotificationType.ZAPPER_SPAWNED);
				break;

			case Constants.OBJECT_ULTIMATE_FORCE:
				getNotificationManager().remove(NotificationType.ULTIMATE_FORCE_SPAWNED);
				break;
		}
	}

	private int getAbsorptionPoints() {
		return client.getVar(Varbits.NMZ_ABSORPTION);
	}

	private void checkHitpoints() {

		final int currentHP = client.getBoostedSkillLevel(Skill.HITPOINTS);

		boolean aboveHPThreshold = false;
		boolean belowHPThreshold = false;

		if(config.minimumHPNotification() && config.maximumHPNotification()) {
			if(currentHP < config.minimumHPThresholdValue() && config.minimumHPThresholdValue() != -1) {
				belowHPThreshold = true;
			} else if(currentHP > config.maximumHPThresholdValue() && config.maximumHPThresholdValue() != -1) {
				aboveHPThreshold = true;
			}
		} else if(config.minimumHPNotification()) {
			if(currentHP < config.minimumHPThresholdValue() && config.minimumHPThresholdValue() != -1) {
				belowHPThreshold = true;
			}
		} else if(config.maximumHPNotification()) {
			if(currentHP > config.maximumHPThresholdValue() && config.maximumHPThresholdValue() != -1) {
				aboveHPThreshold = true;
			}
		}

		if(config.minimumHPNotification() && config.maximumHPNotification()) {
			if (belowHPThreshold) {
				getNotificationManager().create(NotificationType.HP_BELOW_THRESHOLD);
			} else {
				getNotificationManager().remove(NotificationType.HP_BELOW_THRESHOLD);
			}
			if(aboveHPThreshold) {
				getNotificationManager().create(NotificationType.HP_ABOVE_THRESHOLD);
			} else {
				getNotificationManager().remove(NotificationType.HP_ABOVE_THRESHOLD);
			}
		} else if(config.minimumHPNotification()) {
			if (belowHPThreshold) {
				getNotificationManager().create(NotificationType.HP_BELOW_THRESHOLD);
			} else {
				getNotificationManager().remove(NotificationType.HP_BELOW_THRESHOLD);
			}
		} else if(config.maximumHPNotification()) {
			if (aboveHPThreshold) {
				getNotificationManager().create(NotificationType.HP_ABOVE_THRESHOLD);
			} else {
				getNotificationManager().remove(NotificationType.HP_ABOVE_THRESHOLD);
			}
		}
	}

	private void checkAbsorption() {
		int absorptionPoints = client.getVar(Varbits.NMZ_ABSORPTION);

		if(absorptionPoints <= config.absorptionThreshold()) {
			if(config.absorptionNotification()) {
				if(InventoryUtils.hasOneOfItems(client, ItemID.ABSORPTION_1, ItemID.ABSORPTION_2, ItemID.ABSORPTION_3, ItemID.ABSORPTION_4)) {
					getNotificationManager().create(NotificationType.ABSORPTION_BELOW_THRESHOLD);
					getNativeNotificationManager().create(NotificationType.ABSORPTION_BELOW_THRESHOLD);
				} else {
					getNotificationManager().remove(NotificationType.ABSORPTION_BELOW_THRESHOLD);
				}
			}
		} else {
			getNotificationManager().remove(NotificationType.ABSORPTION_BELOW_THRESHOLD);
		}
	}

	private void checkOverload() {
		if(overloadTimer == -1) {
			return;
		}

		if(System.currentTimeMillis() >= overloadTimer) {
			overloadTimer = -1;

			if(InventoryUtils.hasOneOfItems(client, ItemID.ABSORPTION_1, ItemID.ABSORPTION_2, ItemID.ABSORPTION_3, ItemID.ABSORPTION_4)) {
				if(config.overloadExpiredNotification()) {

					getNotificationManager().remove(NotificationType.OVERLOAD_ALMOST_EXPIRED);
					getNotificationManager().create(NotificationType.OVERLOAD_EXPIRED);
					getNativeNotificationManager().create(NotificationType.OVERLOAD_EXPIRED);

				}
			} else {
				getNotificationManager().remove(NotificationType.OVERLOAD_EXPIRED);
				getNotificationManager().remove(NotificationType.OVERLOAD_ALMOST_EXPIRED);
			}
			return;
		} else if(System.currentTimeMillis() >= (overloadTimer - (config.overloadRunoutTime() * 1000))) {
			if(config.overloadRunoutNotification()) {
				// If we don't check before creating it will spam native notifications every 30 seconds.
				getNotificationManager().create(NotificationType.OVERLOAD_ALMOST_EXPIRED);
				getNativeNotificationManager().create(NotificationType.OVERLOAD_ALMOST_EXPIRED);
			}
		}
	}

	public boolean isInNightmareZone() {
		if (client.getLocalPlayer() == null) {
			return false;
		}

		// NMZ and the KBD lair uses the same region ID but NMZ uses planes 1-3 and KBD uses plane 0
		return client.getLocalPlayer().getWorldLocation().getPlane() > 0 && Arrays.equals(client.getMapRegions(), NMZ_MAP_REGION);
	}

	private int calculatePointsPerHour() {
		Instant now = Instant.now();
		final int currentPoints = client.getVar(Varbits.NMZ_POINTS);

		if (nmzSessionStartTime == null) {
			nmzSessionStartTime = now;
		}

		Duration timeSinceStart = Duration.between(nmzSessionStartTime, now);

		if (!timeSinceStart.isZero()) {
			return (int) ((double) currentPoints * (double) HOUR.toMillis() / (double) timeSinceStart.toMillis());
		}

		return 0;
	}

	private void resetPointsPerHour() {
		nmzSessionStartTime = null;
		pointsPerHour = 0;
	}

	@Subscribe
	private void onGameTick(GameTick event) {
		if (!isInNightmareZone()) {
			if (nmzSessionStartTime != null) {
				resetPointsPerHour();
			}

			return;
		}

		if(config.visualAlerts() || config.nativeAlerts()) {
			if (config.absorptionNotification()) {
				checkAbsorption();
			}

			if (config.overloadExpiredNotification() || config.overloadRunoutNotification()) {
				checkOverload();
			}

			if (config.minimumHPNotification() || config.maximumHPNotification()) {
				checkHitpoints();
			}
		}

		pointsPerHour = calculatePointsPerHour();
	}

	private void startSession() {

		sessionStart = Instant.now();
		sessionEnd = null;
		ultimateNmzOverlay.nightmareZoneStarted();
		countdownOverlay.triggerCountdown(Instant.now().plus(Duration.ofSeconds(25)));

		if(config.nativeAlerts()) {
			notifier.notify(Constants.NATIVE_NOTIFICATION_NMZ_STARTED_MESSAGE);
		}

		resetPointsPerHour();

		// Restore nmz widget
		Widget nmzWidget = client.getWidget(WidgetInfo.NIGHTMARE_ZONE);
		if (nmzWidget != null) {
			nmzWidget.setHidden(getConfig().removeNMZOverlay());
		}
	}
	private void endSession() {
		// Grab the Time, to calculate total session length.
		sessionEnd = Instant.now();

		ultimateNmzOverlay.nightmareZoneEnded();

		lastOverload = null;

		// Remove all notifications, if they havent already been removed.
		getNotificationManager().removeAll();

		if(config.nativeAlerts()) {
			notifier.notify(Constants.NATIVE_NOTIFICATION_NMZ_ENDED_MESSAGE);
		}

		// Restore nmz widget
		Widget nmzWidget = client.getWidget(WidgetInfo.NIGHTMARE_ZONE);
		if (nmzWidget != null) {
			nmzWidget.setHidden(false);
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event) {

	}

	public UltimateNMZConfig getConfig() {
		return config;
	}

}
