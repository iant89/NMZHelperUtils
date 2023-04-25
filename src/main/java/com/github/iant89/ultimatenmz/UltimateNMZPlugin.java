package com.github.iant89.ultimatenmz;




import com.github.iant89.ultimatenmz.notifications.*;
import com.github.iant89.ultimatenmz.overlays.CountdownOverlay;
import com.github.iant89.ultimatenmz.overlays.PowerUpOverlay;
import com.github.iant89.ultimatenmz.overlays.UltimateNMZOverlay;
import com.github.iant89.ultimatenmz.overlays.VisualNotificationOverlay;
import com.github.iant89.ultimatenmz.skills.SkillConstants;
import com.github.iant89.ultimatenmz.skills.SkillTracker;
import com.github.iant89.ultimatenmz.utils.InventoryUtils;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
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

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
	private Client client;

	@Inject
	private Notifier notifier;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private UltimateNMZConfig config;

	@Inject
	private SkillIconManager iconManager;

	@Inject
	private UltimateNMZOverlay ultimateNmzOverlay;

	@Inject
	private VisualNotificationManager notificationManager;

	@Inject
	private VisualNotificationOverlay notificationOverlay;

	@Inject
	private CountdownOverlay countdownOverlay;

	@Inject
	private PowerUpOverlay powerUpOverlay;

	@Inject
	private SkillTracker skillTracker;

	@Getter
	public int pointsPerHour;

	private Instant nmzSessionStartTime;

	// Has the NMZ Started?
	private boolean nmzStarted = false;

	private boolean absorptionNotificationSend = true;

	// Overload Variables
	private boolean overloadNotificationSend = true;


	/*
	 * Timers for Anti-Spamming of Native Notifications.
	 */
	private Instant lastHPAboveThresholdNotification;
	private Instant lastHPBelowThresholdNotification;
	private Instant lastAbsorptionBelowThresholdNotification;
	private Instant lastOverloadWarningNotification;
	private Instant lastOverloadExpiredNotification;
	private Instant lastZapperNotification;
	private Instant lastRecurrentDamageNotification;
	private Instant lastUltimateForceNotification;
	private Instant lastPowerSurgeNotification;


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

	public VisualNotificationManager getNotificationManager() {
		return notificationManager;
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

		getNotificationManager().configUpdated();
		ultimateNmzOverlay.updateConfig();
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
			log.debug("Player drank a overload potion");

			overloadTimer = System.currentTimeMillis() + ((5 * 60) * 1000);

			final Instant now = Instant.now();
			lastOverload = now;
			overloadNotificationSend = true;

			getNotificationManager().removeNotification(VisualNotificationType.OVERLOAD_ALMOST_EXPIRED);
			getNotificationManager().removeNotification(VisualNotificationType.OVERLOAD_EXPIRED);

			if(config.maximumHPNotification()) {
				int hpLeft = client.getBoostedSkillLevel(Skill.HITPOINTS) - 50;

				if(hpLeft <= config.maximumHPThresholdValue()) {
					getNotificationManager().removeNotification(VisualNotificationType.HP_ABOVE_THRESHOLD);
					getNotificationManager().blockNotification(VisualNotificationType.HP_ABOVE_THRESHOLD, 15);
				}
			}
			return;

		}

		// Check to see if we are in the NMZ before we process any of the other messages.
		if(!isInNightmareZone()) {
			return;
		}

		if (msg.contains("effects of overload have worn off")) {

			getNotificationManager().blockNotification(VisualNotificationType.HP_ABOVE_THRESHOLD, 2);

			overloadTimer = -1;
			// Overload Potion worn off
			if (config.overloadExpiredNotification()) {
				if(InventoryUtils.hasOneOfItems(client, ItemID.OVERLOAD_4, ItemID.OVERLOAD_3, ItemID.OVERLOAD_2, ItemID.OVERLOAD_1)) {
					notificationManager.removeNotification(VisualNotificationType.OVERLOAD_ALMOST_EXPIRED);

					if(config.visualAlerts()) {
						getNotificationManager().createNotification(VisualNotificationType.OVERLOAD_EXPIRED);
					}
				} else {
					getNotificationManager().removeNotification(VisualNotificationType.OVERLOAD_ALMOST_EXPIRED);
					getNotificationManager().removeNotification(VisualNotificationType.OVERLOAD_EXPIRED);
				}
			}

			sendNativeNotification(VisualNotificationType.OVERLOAD_EXPIRED);

			lastOverload = null;

		} else if(msg.contains("You wake up feeling refreshed")) {

			// NMZ has ended...
			endSession();

		} else if(msg.contains("25 secs to")) {

			// Started NMZ...
			startSession();

		} else if(msg.contains("You activate the")) {

			// Activated Zapper
			if(msg.contains("zapper")) {
				getNotificationManager().removeNotification(VisualNotificationType.ZAPPER_SPAWNED);
			}

		} else if(msg.contains("You now have")) {

			// Activated Recurrent Damage
			if(msg.contains("recurrent damage")) {
				getNotificationManager().removeNotification(VisualNotificationType.RECURRENT_DAMAGE_SPAWNED);
			}

		} else if(msg.contains("You feel a surge of special")) {

			// Activated Power-Surge
			getNotificationManager().removeNotification(VisualNotificationType.POWER_SURGE_SPAWNED);

		} else if (msg.contains("A power-up has spawned:")) {

			if (msg.contains("Power surge")) {

				// Power-Surge Spawned
				if (config.powerSurgeNotification()) {
					if(config.visualAlerts()) {
						getNotificationManager().createNotification(VisualNotificationType.POWER_SURGE_SPAWNED);
					}
				}

				sendNativeNotification(VisualNotificationType.POWER_SURGE_SPAWNED);

			} else if (msg.contains("Recurrent damage")) {

				// Recurrent Damage Spawned
				if (config.recurrentDamageNotification()) {
					if(config.visualAlerts()) {
						getNotificationManager().createNotification(VisualNotificationType.RECURRENT_DAMAGE_SPAWNED);
					}
				}

				sendNativeNotification(VisualNotificationType.RECURRENT_DAMAGE_SPAWNED);

			} else if (msg.contains("Zapper")) {

				// Zapper Spawned
				if (config.zapperNotification()) {
					if(config.visualAlerts()) {
						getNotificationManager().createNotification(VisualNotificationType.ZAPPER_SPAWNED);
					}
				}

				sendNativeNotification(VisualNotificationType.ZAPPER_SPAWNED);

			} else if (msg.contains("Ultimate force")) {

				// Ultimate Force Spawned
				if (config.ultimateForceNotification()) {
					if(config.visualAlerts()) {
						getNotificationManager().createNotification(VisualNotificationType.ULTIMATE_FORCE_SPAWNED);
					}
				}

				sendNativeNotification(VisualNotificationType.ULTIMATE_FORCE_SPAWNED);
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
				getNotificationManager().removeNotification(VisualNotificationType.POWER_SURGE_SPAWNED);
				break;

			case Constants.OBJECT_RECURRENT_DAMAGE:
				getNotificationManager().removeNotification(VisualNotificationType.RECURRENT_DAMAGE_SPAWNED);
				break;

			case Constants.OBJECT_ZAPPER:
				getNotificationManager().removeNotification(VisualNotificationType.ZAPPER_SPAWNED);
				break;

			case Constants.OBJECT_ULTIMATE_FORCE:
				getNotificationManager().removeNotification(VisualNotificationType.ULTIMATE_FORCE_SPAWNED);
				break;
		}
	}

	private int getAbsorptionPoints() {
		return client.getVar(Varbits.NMZ_ABSORPTION);
	}

	private void checkAbsorption() {
		int absorptionPoints = client.getVar(Varbits.NMZ_ABSORPTION);

		if(absorptionPoints < config.absorptionThreshold()) {
			if(config.absorptionNotification()) {
				if(InventoryUtils.hasOneOfItems(client, ItemID.ABSORPTION_1, ItemID.ABSORPTION_2, ItemID.ABSORPTION_3, ItemID.ABSORPTION_4)) {
					notificationManager.createNotification(VisualNotificationType.ABSORPTION_BELOW_THRESHOLD);

					sendNativeNotification(VisualNotificationType.ABSORPTION_BELOW_THRESHOLD);
				} else {
					notificationManager.removeNotification(VisualNotificationType.ABSORPTION_BELOW_THRESHOLD);
				}

			}
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
					notificationManager.removeNotification(VisualNotificationType.OVERLOAD_ALMOST_EXPIRED);
					notificationManager.createNotification(VisualNotificationType.OVERLOAD_EXPIRED);

					sendNativeNotification(VisualNotificationType.OVERLOAD_EXPIRED);
				}
			} else {
				notificationManager.removeNotification(VisualNotificationType.OVERLOAD_EXPIRED);
				notificationManager.removeNotification(VisualNotificationType.OVERLOAD_ALMOST_EXPIRED);
			}
			return;
		} else if(System.currentTimeMillis() >= (overloadTimer - (config.overloadRunoutTime() * 1000))) {
			if(config.overloadRunoutNotification()) {

				// If we don't check before creating it will spam native notifications every 30 seconds.
				if(!notificationManager.hasNotificationType(VisualNotificationType.OVERLOAD_ALMOST_EXPIRED)) {
					notificationManager.createNotification(VisualNotificationType.OVERLOAD_ALMOST_EXPIRED);

					sendNativeNotification(VisualNotificationType.OVERLOAD_ALMOST_EXPIRED);
				}
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
	public void onStatChanged(StatChanged event) {
		skillTracker.onStatChanged(event);
	}

	@Subscribe
	private void onGameTick(GameTick event) {
		if (!isInNightmareZone()) {
			if (!absorptionNotificationSend) {
				absorptionNotificationSend = true;
			}
			if(!overloadNotificationSend) {
				overloadNotificationSend = true;
			}

			if (nmzSessionStartTime != null) {
				resetPointsPerHour();
			}

			return;
		}

		if (config.absorptionNotification()) {
			checkAbsorption();
		}

		if(config.overloadExpiredNotification() || config.overloadRunoutNotification()) {
			checkOverload();
		}

		pointsPerHour = calculatePointsPerHour();

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

		if(config.visualAlerts()) {
			// Absorption
			if(config.absorptionNotification()){
				if (getAbsorptionPoints() <= config.absorptionThreshold()) {
					getNotificationManager().createNotification(VisualNotificationType.ABSORPTION_BELOW_THRESHOLD);
				} else {
					getNotificationManager().removeNotification(VisualNotificationType.ABSORPTION_BELOW_THRESHOLD);
				}
			} else {
				getNotificationManager().removeNotification(VisualNotificationType.ABSORPTION_BELOW_THRESHOLD);
			}

			// Overload Expired
			if(config.overloadExpiredNotification()) {

			}

			// Overload Expire Warning
			if(config.overloadRunoutNotification()) {

			}

			// Hitpoints
			if(config.minimumHPNotification() && config.maximumHPNotification()) {
				if (belowHPThreshold) {
					getNotificationManager().createNotification(VisualNotificationType.HP_BELOW_THRESHOLD);
				} else {
					getNotificationManager().removeNotification(VisualNotificationType.HP_BELOW_THRESHOLD);
				}
				if(aboveHPThreshold) {
					getNotificationManager().createNotification(VisualNotificationType.HP_ABOVE_THRESHOLD);
				} else {
					getNotificationManager().removeNotification(VisualNotificationType.HP_ABOVE_THRESHOLD);
				}
			} else if(config.minimumHPNotification()) {
				if (belowHPThreshold) {
					getNotificationManager().createNotification(VisualNotificationType.HP_BELOW_THRESHOLD);
				} else {
					getNotificationManager().removeNotification(VisualNotificationType.HP_BELOW_THRESHOLD);
				}
			} else if(config.maximumHPNotification()) {
				if (aboveHPThreshold) {
					getNotificationManager().createNotification(VisualNotificationType.HP_ABOVE_THRESHOLD);
				} else {
					getNotificationManager().removeNotification(VisualNotificationType.HP_ABOVE_THRESHOLD);
				}
			}

			// Skills
			Set<Skill> skillsToNotify = skillTracker.getUnboostedSkills(getMinimumBoosts())
					.stream()
					.filter(skill -> InventoryUtils.hasOneOfItems(client, SkillConstants.skillPotions.get(skill)))
					.collect(Collectors.toSet());
			for (Skill skill : SkillConstants.trackedSkills) {
				if (skillsToNotify.contains(skill)) {
					getNotificationManager().createNotification(SkillConstants.skillNotificationType.get(skill));
				} else {
					getNotificationManager().removeNotification(SkillConstants.skillNotificationType.get(skill));
				}
			}


		} else {
			getNotificationManager().clearNotifications();
		}
	}

	private void startSession() {

		sessionStart = Instant.now();
		sessionEnd = null;
		ultimateNmzOverlay.nightmareZoneStarted();
		countdownOverlay.triggerCountdown(Instant.now().plus(Duration.ofSeconds(25)));

		if(config.nativeAlerts()) {
			notifier.notify("Nightmare Zone has started!", TrayIcon.MessageType.INFO);
		}

		resetPointsPerHour();
	}
	private void endSession() {

		// Grab the Time, to calculate total session length
		sessionEnd = Instant.now();

		ultimateNmzOverlay.nightmareZoneEnded();

		// Clear Tracking Variables
		lastOverload = null;

		// Remove all notifications, if they havent already been removed.
		getNotificationManager().removeAll();

		if(config.nativeAlerts()) {
			notifier.notify("You have left the Nightmare Zone.", TrayIcon.MessageType.INFO);
		}
	}

	/**
	 * Checks if the time between now and instant has been atleast the duration seconds.
	 *
	 * @param instant
	 * @return
	 */
	private boolean checkDurationForNotification(Instant instant, Duration duration) {
		if(instant == null) {
			return true;
		}

		if(Duration.between(Instant.now(), instant).getSeconds() > duration.getSeconds()) {
			return true;
		}

		return false;
	}
	private void sendNativeNotification(VisualNotificationType type) {
		if(!config.nativeAlerts()) {
			return;
		}

		switch (type) {
			case HP_BELOW_THRESHOLD:
				if(!config.minimumHPNotification()) {
					break;
				}

				if(checkDurationForNotification(lastHPBelowThresholdNotification, Duration.ofSeconds(30))) {
					lastHPBelowThresholdNotification = Instant.now();
					notifier.notify("Your Hitpoints are below " + config.minimumHPThresholdValue(), TrayIcon.MessageType.INFO);
				}
				break;

			case HP_ABOVE_THRESHOLD:
				if(!config.maximumHPNotification()) {
					break;
				}

				if(checkDurationForNotification(lastHPAboveThresholdNotification, Duration.ofSeconds(30))) {
					lastHPAboveThresholdNotification = Instant.now();
					notifier.notify("Your Hitpoints are above " + config.maximumHPThresholdValue(), TrayIcon.MessageType.INFO);
				}
				break;
			case ABSORPTION_BELOW_THRESHOLD:
				if(!config.absorptionNotification()) {
					break;
				}

				if(InventoryUtils.hasOneOfItems(client, ItemID.ABSORPTION_4, ItemID.ABSORPTION_3, ItemID.ABSORPTION_2, ItemID.ABSORPTION_1)) {
					if (checkDurationForNotification(lastAbsorptionBelowThresholdNotification, Duration.ofSeconds(30))) {
						lastAbsorptionBelowThresholdNotification = Instant.now();
						notifier.notify("Absorption Points is below threshold: " + getAbsorptionPoints(), TrayIcon.MessageType.INFO);
					}
				}
				break;

			case OVERLOAD_ALMOST_EXPIRED:
				if(!config.overloadRunoutNotification()) {
					break;
				}

				if(InventoryUtils.hasOneOfItems(client, ItemID.OVERLOAD_4, ItemID.OVERLOAD_3, ItemID.OVERLOAD_2, ItemID.OVERLOAD_1)) {
					if (checkDurationForNotification(lastOverloadWarningNotification, Duration.ofSeconds(30))) {
						lastOverloadWarningNotification = Instant.now();
						notifier.notify("Your Overload Potion is about to expire.", TrayIcon.MessageType.INFO);
					}
				}
				break;

			case OVERLOAD_EXPIRED:
				if(!config.overloadExpiredNotification()) {
					break;
				}

				if(InventoryUtils.hasOneOfItems(client, ItemID.OVERLOAD_4, ItemID.OVERLOAD_3, ItemID.OVERLOAD_2, ItemID.OVERLOAD_1)) {
					if (checkDurationForNotification(lastOverloadExpiredNotification, Duration.ofSeconds(60))) {
						lastOverloadExpiredNotification = Instant.now();
						notifier.notify("Your Overload Potion has expired.", TrayIcon.MessageType.INFO);
					}
				}
				break;

			case ZAPPER_SPAWNED:
				if(!config.zapperNotification()) {
					break;
				}

				if (checkDurationForNotification(lastZapperNotification, Duration.ofSeconds(60))) {
					lastZapperNotification = Instant.now();
					notifier.notify("A Zapper Power-Up has Spawned!", TrayIcon.MessageType.INFO);
				}
				break;

			case POWER_SURGE_SPAWNED:
				if(!config.powerSurgeNotification()) {
					break;
				}

				if (checkDurationForNotification(lastPowerSurgeNotification, Duration.ofSeconds(60))) {
					lastPowerSurgeNotification = Instant.now();
					notifier.notify("A Power Surge Power-Up has Spawned!", TrayIcon.MessageType.INFO);
				}
				break;

			case RECURRENT_DAMAGE_SPAWNED:
				if(!config.recurrentDamageNotification()) {
					break;
				}

				if (checkDurationForNotification(lastRecurrentDamageNotification, Duration.ofSeconds(60))) {
					lastRecurrentDamageNotification = Instant.now();
					notifier.notify("A Recurrent Damage Power-Up has Spawned!", TrayIcon.MessageType.INFO);
				}
				break;

			case ULTIMATE_FORCE_SPAWNED:
				if(!config.ultimateForceNotification()) {
					break;
				}

				if (checkDurationForNotification(lastUltimateForceNotification, Duration.ofSeconds(60))) {
					lastUltimateForceNotification = Instant.now();
					notifier.notify("A Ultimate Force Power-Up has Spawned!", TrayIcon.MessageType.INFO);
				}
				break;
		}
	}

	private Map<Skill, Integer> getMinimumBoosts() {
		Map<Skill, Integer> minimumBoosts = new EnumMap<>(Skill.class);
		
		if (config.attackBoostNotification()) {
			minimumBoosts.put(Skill.ATTACK, config.minimumAttackThresholdValue());
		}

		if (config.strengthBoostNotification()) {
			minimumBoosts.put(Skill.STRENGTH, config.minimumStrengthThresholdValue());
		}

		if (config.rangedBoostNotification()) {
			minimumBoosts.put(Skill.RANGED, config.minimumRangedThresholdValue());
		}

		if (config.magicBoostNotification()) {
			minimumBoosts.put(Skill.MAGIC, config.minimumMagicThresholdValue());
		}

		return minimumBoosts;
	}
}
