package com.github.iant89.ultimatenmz;




import com.github.iant89.ultimatenmz.drivers.ConstantDriver;
import com.github.iant89.ultimatenmz.drivers.SineDriver;
import com.github.iant89.ultimatenmz.notifications.*;
import com.github.iant89.ultimatenmz.overlays.PowerUpOverlay;
import com.github.iant89.ultimatenmz.overlays.UltimateNMZOverlay;
import com.github.iant89.ultimatenmz.overlays.VisualNotificationOverlay;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
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
	private PowerUpOverlay powerUpOverlay;

	@Getter
	public int pointsPerHour;

	private Instant nmzSessionStartTime;

	// Has the NMZ Started?
	private boolean nmzStarted = false;

	private boolean absorptionNotificationSend = true;

	@Override
	protected void startUp() throws Exception {
		overlayManager.add(powerUpOverlay);
		overlayManager.add(notificationOverlay);
		overlayManager.add(ultimateNmzOverlay);

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
	public void onGameStateChanged(GameStateChanged gameStateChanged) {

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

	@Subscribe
	private void onChatMessage(ChatMessage event) {
		if (event.getType() != ChatMessageType.GAMEMESSAGE || !isInNightmareZone()) {
			return;
		}

		String msg = Text.removeTags(event.getMessage());

		if (msg.contains("The effects of overload have worn off, and you feel normal again.")) {
			if (config.overloadNotification()) {
				notifier.notify("Your overload has worn off");
			}
		} else if(msg.contains("You wake up feeling refreshed")) {
			ultimateNmzOverlay.nightmareZoneEnded();
			return;
		}

		if(msg.contains("25 secs to")) {
			// Started NMZ...
			ultimateNmzOverlay.nightmareZoneStarted();
			return;
		}

		if(msg.contains("You activate the")) {
			if(msg.contains("zapper")) {
				getNotificationManager().removeNotification(VisualNotificationType.ZAPPER_SPAWNED);
			}
		}

		if(msg.contains("You now have")) {
			if(msg.contains("recurrent damage")) {
				getNotificationManager().removeNotification(VisualNotificationType.RECURRENT_DAMAGE_SPAWNED);
			}
		}

		if(msg.contains("You feel a surge of special")) {
			getNotificationManager().removeNotification(VisualNotificationType.POWER_SURGE_SPAWNED);
		}

		if (msg.contains("A power-up has spawned:")) {
			if (msg.contains("Power surge")) {
				if (config.powerSurgeNotification()) {
					if(config.visualAlerts()) {
						getNotificationManager().createNotification(VisualNotificationType.POWER_SURGE_SPAWNED);
					}
					notifier.notify(msg);
				}
			} else if (msg.contains("Recurrent damage")) {
				if (config.recurrentDamageNotification()) {
					if(config.visualAlerts()) {
						getNotificationManager().createNotification(VisualNotificationType.RECURRENT_DAMAGE_SPAWNED);
					}
					notifier.notify(msg);
				}
			} else if (msg.contains("Zapper")) {
				if (config.zapperNotification()) {
					if(config.visualAlerts()) {
						getNotificationManager().createNotification(VisualNotificationType.ZAPPER_SPAWNED);
					}
					notifier.notify(msg);
				}
			} else if (msg.contains("Ultimate force")) {
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

		if (config.moveOverlay()) {
			pointsPerHour = calculatePointsPerHour();
		}
	}
}
