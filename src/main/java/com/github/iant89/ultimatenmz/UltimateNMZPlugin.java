package com.github.iant89.ultimatenmz;




import com.github.iant89.ultimatenmz.notifications.*;
import com.github.iant89.ultimatenmz.overlays.CountdownOverlay;
import com.github.iant89.ultimatenmz.overlays.PowerUpOverlay;
import com.github.iant89.ultimatenmz.overlays.UltimateNMZOverlay;
import com.github.iant89.ultimatenmz.overlays.VisualNotificationOverlay;
import com.github.iant89.ultimatenmz.utils.InventoryUtils;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
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

import java.time.Duration;
import java.time.Instant;
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

	@Getter
	public int pointsPerHour;

	private Instant nmzSessionStartTime;

	// Has the NMZ Started?
	private boolean nmzStarted = false;

	private boolean absorptionNotificationSend = true;

	// Overload Variables
	private boolean overloadNotificationSend = true;
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

		// Debug
		//notificationManager.addNotification(new SolidVisualNotification(VisualNotificationType.HP_ABOVE_THRESHOLD, config.maximumHPAlertColor(), 0.55f, -1));
		//notificationManager.addNotification(new FlashVisualNotification(VisualNotificationType.RECURRENT_DAMAGE_SPAWNED, config.recurrentDamageAlertColor(), 0.55f, -1));
		//notificationManager.addNotification(new FadedVisualNotification(VisualNotificationType.ZAPPER_SPAWNED, config.zapperAlertColor(), new SineDriver(0f, 0.55f, 20), -1));
		//notificationManager.createNotification(VisualNotificationType.HP_ABOVE_THRESHOLD);
		//notificationManager.createNotification(VisualNotificationType.ABSORPTION_BELOW_THRESHOLD);

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

		/*
		final Instant now = Instant.now();

		if(now == null || lastOverload == null) {
			return null;
		}

		Duration durationLeft = Duration.between(now, lastOverload.plus(OVERLOAD_DURATION));


		if(now.isAfter(lastOverload.plus(OVERLOAD_DURATION))) {
			return null;
		} else {
			return durationLeft;
		}
		 */
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

		//log.info("ChatMessage [" + event.getType().name() + ", \"" + msg + "\")");

		if(msg.contains("You drink some of your overload potion.")) {

			// Drinking a Overload Potion
			log.debug("Player drank a overload potion");

			overloadTimer = System.currentTimeMillis() + ((5 * 60) * 1000);

			final Instant now = Instant.now();
			lastOverload = now;

			getNotificationManager().removeNotification(VisualNotificationType.OVERLOAD_ALMOST_EXPIRED);
			getNotificationManager().removeNotification(VisualNotificationType.OVERLOAD_EXPIRED);
			return;

		}

		// Check to see if we are in the NMZ before we process any of the other messages.
		if(!isInNightmareZone()) {
			return;
		}

		if (msg.contains("effects of overload have worn off")) {

			overloadTimer = -1;
			log.debug("Overload Potion expired.");
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

				notifier.notify("Your overload has worn off");
			}

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
					notifier.notify(msg);
				}

			} else if (msg.contains("Recurrent damage")) {

				// Recurrent Damage Spawned
				if (config.recurrentDamageNotification()) {
					if(config.visualAlerts()) {
						getNotificationManager().createNotification(VisualNotificationType.RECURRENT_DAMAGE_SPAWNED);
					}
					notifier.notify(msg);
				}

			} else if (msg.contains("Zapper")) {

				// Zapper Spawned
				if (config.zapperNotification()) {
					if(config.visualAlerts()) {
						getNotificationManager().createNotification(VisualNotificationType.ZAPPER_SPAWNED);
					}
					notifier.notify(msg);
				}

			} else if (msg.contains("Ultimate force")) {

				// Ultimate Force Spawned
				if (config.ultimateForceNotification()) {
					if(config.visualAlerts()) {
						getNotificationManager().createNotification(VisualNotificationType.ULTIMATE_FORCE_SPAWNED);
					}
					notifier.notify(msg);
				}

			}
		}
	}

	private void checkAbsorption() {
		int absorptionPoints = client.getVar(Varbits.NMZ_ABSORPTION);

		if(absorptionPoints < config.absorptionThreshold()) {
			if(config.absorptionNotification()) {
				if(InventoryUtils.hasOneOfItems(client, ItemID.ABSORPTION_1, ItemID.ABSORPTION_2, ItemID.ABSORPTION_3, ItemID.ABSORPTION_4)) {
					notificationManager.createNotification(VisualNotificationType.ABSORPTION_BELOW_THRESHOLD);
				}
			}
		}

		if (!absorptionNotificationSend) {
			if (absorptionPoints < config.absorptionThreshold()) {
				notifier.notify("Absorption points below: " + config.absorptionThreshold());
				absorptionNotificationSend = true;
			}
		} else {
			if (absorptionPoints > config.absorptionThreshold()) {
				absorptionNotificationSend = false;
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
				if(config.visualAlerts() && config.overloadExpiredNotification()) {
					if(!notificationManager.hasNotificationType(VisualNotificationType.OVERLOAD_EXPIRED)) {
						notificationManager.removeNotification(VisualNotificationType.OVERLOAD_ALMOST_EXPIRED);
						notificationManager.createNotification(VisualNotificationType.OVERLOAD_EXPIRED);
						notifier.notify("Your Overload Potion has now expired.");
					}
				}
			} else {
				notificationManager.removeNotification(VisualNotificationType.OVERLOAD_EXPIRED);
				notificationManager.removeNotification(VisualNotificationType.OVERLOAD_ALMOST_EXPIRED);
			}
			return;
		} else if(System.currentTimeMillis() >= (overloadTimer - (config.overloadRunoutTime() * 1000))) {

			if(config.visualAlerts()) {
				if(config.overloadRunoutNotification()) {
					if (!notificationManager.hasNotificationType(VisualNotificationType.OVERLOAD_ALMOST_EXPIRED)) {
						notificationManager.createNotification(VisualNotificationType.OVERLOAD_ALMOST_EXPIRED);
					}
					notifier.notify("Your overload potion is about to expire!");
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
	private void onGameTick(GameTick event) {
		if (!isInNightmareZone()) {
			if (!absorptionNotificationSend) {
				absorptionNotificationSend = true;
			}

			if (nmzSessionStartTime != null) {
				resetPointsPerHour();
			}

			return;
		}

		if (config.absorptionNotification()) {
			checkAbsorption();
		}

		checkOverload();

		if (config.moveOverlay()) {
			pointsPerHour = calculatePointsPerHour();
		}
	}

	private void startSession() {

		sessionStart = Instant.now();
		sessionEnd = null;
		ultimateNmzOverlay.nightmareZoneStarted();
		countdownOverlay.triggerCountdown(Instant.now().plus(Duration.ofSeconds(25)));

	}
	private void endSession() {

		// Grab the Time, to calculate total session length
		sessionEnd = Instant.now();

		ultimateNmzOverlay.nightmareZoneEnded();

		// Clear Tracking Variables
		lastOverload = null;

		// Remove all notifications, if they havent already been removed.
		getNotificationManager().removeAll();
	}
}
