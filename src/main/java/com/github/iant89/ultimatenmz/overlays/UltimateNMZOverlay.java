package com.github.iant89.ultimatenmz.overlays;

import com.github.iant89.ultimatenmz.utils.InventoryUtils;
import com.github.iant89.ultimatenmz.utils.NumberUtils;
import com.github.iant89.ultimatenmz.UltimateNMZConfig;
import com.github.iant89.ultimatenmz.UltimateNMZPlugin;
import com.github.iant89.ultimatenmz.notifications.VisualNotificationType;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.QuantityFormatter;

import javax.inject.Inject;
import java.awt.*;
import java.time.Duration;

import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class UltimateNMZOverlay extends OverlayPanel {



    private final Client client;
    private final UltimateNMZConfig config;
    private final UltimateNMZPlugin plugin;

    private boolean nmzStarted = false;
    private long nmzStartTimer = -1;



    @Inject
    private UltimateNMZOverlay(Client client, UltimateNMZConfig config, UltimateNMZPlugin plugin) {
        super(plugin);

        this.plugin = plugin;
        this.client = client;
        this.config = config;

        setPosition(OverlayPosition.BOTTOM_LEFT);
        setPriority(OverlayPriority.HIGH);
        getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Ultimate-NMZ Overlay."));
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        if (!plugin.isInNightmareZone()) {

            Widget nmzWidget = client.getWidget(WidgetInfo.NIGHTMARE_ZONE);
            if (nmzWidget != null) {
                nmzWidget.setHidden(false);
            }

            return null;
        }

        if(!nmzStarted && nmzStartTimer > -1) {
            if(System.currentTimeMillis() > nmzStartTimer) {
                nmzStarted = true;
                nmzStartTimer = -1;
            }
        }

        if(!nmzStarted) {
            return super.render(graphics);
        }

        Widget nmzWidget = client.getWidget(WidgetInfo.NIGHTMARE_ZONE);
        if(nmzWidget != null) {
            nmzWidget.setHidden(config.removeNMZOverlay());
        }

        panelComponent.getChildren().add(TitleComponent.builder().text("-- Ultimate NMZ --").color(Color.GREEN).build());

        final int currentHP = client.getBoostedSkillLevel(Skill.HITPOINTS);
        Color hpColor;
        Color absorptionColor;
        String str;

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

        if(aboveHPThreshold) {
            hpColor = Color.RED;
        } else if(belowHPThreshold) {
            hpColor = Color.YELLOW;
        } else {
            hpColor = Color.GREEN;
        }

        str = ColorUtil.prependColorTag(Integer.toString(currentHP), hpColor);

        final int currentPoints = client.getVar(Varbits.NMZ_POINTS);
        final int totalPoints = currentPoints + client.getVar(VarPlayer.NMZ_REWARD_POINTS);
        final int absorptionPoints = client.getVar(Varbits.NMZ_ABSORPTION);

        String durationString = ""; //String.format("%02d:%02d:%02d", plugin.getSessionDuration().toHours(), plugin.getSessionDuration().toMinutesPart(), plugin.getSessionDuration().toSecondsPart());

        durationString = (plugin.getSessionDuration().toHoursPart() >= 1 ? "" + plugin.getSessionDuration().toHoursPart() + ":" : "");
        durationString += (plugin.getSessionDuration().toMinutesPart() > 9 ? "" : "0") + plugin.getSessionDuration().toMinutesPart() + ":";
        durationString += (plugin.getSessionDuration().toSecondsPart() > 9 ? "" : "0") + plugin.getSessionDuration().toSecondsPart();

        panelComponent.getChildren().add(LineComponent.builder().left("Session:").right("" + durationString).rightColor(Color.GREEN).build());
        panelComponent.getChildren().add(LineComponent.builder().left("").build());

        panelComponent.getChildren().add(LineComponent.builder().left("Hitpoints:").right(str).rightColor(hpColor).build());

        panelComponent.getChildren().add(LineComponent.builder().left("").build());

        if(absorptionPoints == 0) {
            if(InventoryUtils.hasOneOfItems(client, ItemID.ABSORPTION_4, ItemID.ABSORPTION_3, ItemID.ABSORPTION_2, ItemID.ABSORPTION_1)) {
                panelComponent.getChildren().add(LineComponent.builder().left("Absorption:").right("---").rightColor(Color.WHITE).build());
            }
        } else if(absorptionPoints <= config.absorptionThreshold()) {
            panelComponent.getChildren().add(LineComponent.builder().left("Absorption:").right(QuantityFormatter.formatNumber(absorptionPoints)).rightColor(Color.RED).build());
        } else {
            panelComponent.getChildren().add(LineComponent.builder().left("Absorption:").right(QuantityFormatter.formatNumber(absorptionPoints)).rightColor(Color.GREEN).build());
        }

        durationString = "";
        Color overloadColor = Color.GREEN;
        if(plugin.getOverloadDurationLeft() != null) {
            Duration overloadDuration = plugin.getOverloadDurationLeft();

            if(overloadDuration.toMinutesPart() > 0 || overloadDuration.toSecondsPart() > 0) {
                durationString = (overloadDuration.toMinutesPart() >= 1 ? "" + overloadDuration.toMinutesPart() : "0") + ":";
                durationString += (overloadDuration.toSecondsPart() >= 9 ? "" : "0") + overloadDuration.toSecondsPart();

                if(config.overloadRunoutTime() >= overloadDuration.toSeconds()) {
                    overloadColor = Color.RED;
                }

                panelComponent.getChildren().add(LineComponent.builder().left("Overload:").right("" + durationString).rightColor(overloadColor).build());
            }
        }

        if(config.showPointsPerHour() || config.showTotalPoints() || config.removeNMZOverlay()) {
            panelComponent.getChildren().add(LineComponent.builder().left("").build());
        }

        if(config.removeNMZOverlay()) {
            panelComponent.getChildren().add(LineComponent.builder().left("Points:").right(NumberUtils.format(currentPoints)).build());
        }

        if(config.showPointsPerHour()) {
            panelComponent.getChildren().add(LineComponent.builder().left("Points/Hour:").right(NumberUtils.format(plugin.getPointsPerHour())).build());
        }

        if(config.showTotalPoints()) {
            panelComponent.getChildren().add(LineComponent.builder().left("Total Points:").right(NumberUtils.format(totalPoints)).build());
        }

        return super.render(graphics);
    }

    public void updateConfig() {

    }

    public boolean hasNightmareZoneStarted() {
        return nmzStarted;
    }

    public void nightmareZoneStarted() {
        nmzStartTimer = System.currentTimeMillis() + (25 * 1000);
    }

    public void nightmareZoneEnded() {
        nmzStarted = false;
        nmzStartTimer = -1;
    }
}
