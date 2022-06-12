package com.github.iant89.ultimatenmz.overlays;

import com.github.iant89.ultimatenmz.notifications.NotificationType;
import com.github.iant89.ultimatenmz.utils.DurationUtils;
import com.github.iant89.ultimatenmz.utils.InventoryUtils;
import com.github.iant89.ultimatenmz.utils.NumberUtils;
import com.github.iant89.ultimatenmz.UltimateNMZPlugin;
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

    private boolean nmzStarted = false;
    private long nmzStartTimer = -1;

    private UltimateNMZPlugin plugin;

    @Inject
    private UltimateNMZOverlay(UltimateNMZPlugin plugin) {
        super(plugin);

        this.plugin = plugin;

        setPosition(OverlayPosition.BOTTOM_LEFT);
        setPriority(OverlayPriority.HIGH);
        getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Ultimate-NMZ Overlay."));
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        if (!plugin.isInNightmareZone()) {

            Widget nmzWidget = plugin.getClient().getWidget(WidgetInfo.NIGHTMARE_ZONE);
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

        if(!plugin.getConfig().showNMZOverlay()) {
            return super.render(graphics);
        }

        //panelComponent.getChildren().add(TitleComponent.builder().text("-- Ultimate NMZ --").color(Color.GREEN).build());

        final int currentHP = plugin.getClient().getBoostedSkillLevel(Skill.HITPOINTS);
        Color hpColor = Color.GREEN;
        Color absorptionColor;
        String str;

        boolean aboveHPThreshold = false;
        boolean belowHPThreshold = false;

        str = ColorUtil.prependColorTag(Integer.toString(currentHP), hpColor);

        final int currentPoints = plugin.getClient().getVar(Varbits.NMZ_POINTS);
        final int totalPoints = currentPoints + plugin.getClient().getVar(VarPlayer.NMZ_REWARD_POINTS);
        final int absorptionPoints = plugin.getClient().getVar(Varbits.NMZ_ABSORPTION);

        if(plugin.getConfig().showSessionTime()) {
            panelComponent.getChildren().add(LineComponent.builder().left("Session:").right(DurationUtils.toSessionTimeString(plugin.getSessionDuration())).rightColor(Color.GREEN).build());
            panelComponent.getChildren().add(LineComponent.builder().left("").build());
        }

        if(plugin.getConfig().showHitpoints()) {
            if(plugin.getConfig().minimumHPNotification() && plugin.getConfig().maximumHPNotification()) {
                if(currentHP < plugin.getConfig().minimumHPThresholdValue() && plugin.getConfig().minimumHPThresholdValue() != -1) {
                    belowHPThreshold = true;
                } else if(currentHP > plugin.getConfig().maximumHPThresholdValue() && plugin.getConfig().maximumHPThresholdValue() != -1) {
                    aboveHPThreshold = true;
                }
            } else if(plugin.getConfig().minimumHPNotification()) {
                if(currentHP < plugin.getConfig().minimumHPThresholdValue() && plugin.getConfig().minimumHPThresholdValue() != -1) {
                    belowHPThreshold = true;
                }
            } else if(plugin.getConfig().maximumHPNotification()) {
                if(currentHP > plugin.getConfig().maximumHPThresholdValue() && plugin.getConfig().maximumHPThresholdValue() != -1) {
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

            if(plugin.getNotificationManager().isBlocked(NotificationType.HP_ABOVE_THRESHOLD)) {
                hpColor = Color.YELLOW;
            }

            panelComponent.getChildren().add(LineComponent.builder().left("Hitpoints:").right(str).rightColor(hpColor).build());
        }

        if(plugin.getConfig().absorptionNotification() || plugin.getConfig().showOverloadTimer()) {
            panelComponent.getChildren().add(LineComponent.builder().left("").build());
        }

        if(plugin.getConfig().showAbsorptionPoints()) {
            if (plugin.getConfig().absorptionNotification()) {
                if (absorptionPoints == 0) {
                    if (InventoryUtils.hasOneOfItems(plugin.getClient(), ItemID.ABSORPTION_4, ItemID.ABSORPTION_3, ItemID.ABSORPTION_2, ItemID.ABSORPTION_1)) {
                        panelComponent.getChildren().add(LineComponent.builder().left("Absorption:").right("0").rightColor(Color.RED).build());
                    }
                } else if (absorptionPoints <= plugin.getConfig().absorptionThreshold()) {
                    panelComponent.getChildren().add(LineComponent.builder().left("Absorption:").right(QuantityFormatter.formatNumber(absorptionPoints)).rightColor(Color.RED).build());
                } else {
                    panelComponent.getChildren().add(LineComponent.builder().left("Absorption:").right(QuantityFormatter.formatNumber(absorptionPoints)).rightColor(Color.GREEN).build());
                }
            }
        }

        if(plugin.getConfig().showOverloadTimer()) {
            Color overloadColor = Color.GREEN;
            if (plugin.getOverloadDurationLeft() != null) {
                Duration overloadDuration = plugin.getOverloadDurationLeft();

                if (overloadDuration.toMinutes() > 0 || overloadDuration.getSeconds() > 0) {
                    if (plugin.getConfig().overloadRunoutTime() >= overloadDuration.getSeconds()) {
                        overloadColor = Color.RED;
                    }

                    panelComponent.getChildren().add(LineComponent.builder().left("Overload:").right(DurationUtils.toCountDownTimeString(overloadDuration)).rightColor(overloadColor).build());
                }
            }
        }

        if(plugin.getConfig().showPointsPerHour() || plugin.getConfig().showTotalPoints() || plugin.getConfig().showNightmarePoints()) {
            panelComponent.getChildren().add(LineComponent.builder().left("").build());
        }

        if(plugin.getConfig().showNightmarePoints()) {
            if(plugin.getConfig().formatOverlayPoints()) {
                panelComponent.getChildren().add(LineComponent.builder().left("Points:").right(QuantityFormatter.quantityToRSDecimalStack(currentPoints, true)).build());
            } else {
                panelComponent.getChildren().add(LineComponent.builder().left("Points:").right(QuantityFormatter.formatNumber(currentPoints)).build());
            }
        }

        if(plugin.getConfig().showPointsPerHour()) {
            if(plugin.getConfig().formatOverlayPoints()) {
                panelComponent.getChildren().add(LineComponent.builder().left("Points/Hour:").right(QuantityFormatter.quantityToRSDecimalStack(plugin.getPointsPerHour(), true)).build());
            } else {
                panelComponent.getChildren().add(LineComponent.builder().left("Points/Hour:").right(QuantityFormatter.formatNumber(plugin.getPointsPerHour())).build());
            }
        }

        if(plugin.getConfig().showTotalPoints()) {
            if(plugin.getConfig().formatOverlayPoints()) {
                panelComponent.getChildren().add(LineComponent.builder().left("Total Points:").right(QuantityFormatter.quantityToRSDecimalStack(totalPoints, true)).build());
            } else {
                panelComponent.getChildren().add(LineComponent.builder().left("Total Points:").right(QuantityFormatter.formatNumber(totalPoints)).build());
            }
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
