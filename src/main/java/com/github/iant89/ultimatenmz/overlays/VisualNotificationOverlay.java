package com.github.iant89.ultimatenmz.overlays;

import com.github.iant89.ultimatenmz.UltimateNMZConfig;
import com.github.iant89.ultimatenmz.UltimateNMZPlugin;
import com.github.iant89.ultimatenmz.drivers.ConstantDriver;
import com.github.iant89.ultimatenmz.drivers.SineDriver;
import com.github.iant89.ultimatenmz.drivers.ValueDriver;
import com.github.iant89.ultimatenmz.notifications.*;
import com.github.iant89.ultimatenmz.utils.InventoryUtils;
import net.runelite.api.*;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class VisualNotificationOverlay extends OverlayPanel {

    private final Client client;
    private final UltimateNMZConfig config;
    private final UltimateNMZPlugin plugin;
    private final VisualNotificationManager notificationManager;
    private final SkillIconManager skillIconManager;
    private final ItemManager itemManager;

    private ValueDriver iconSizeDriver = new SineDriver(2f, 5f, 25);

    @Inject
    private VisualNotificationOverlay(Client client, UltimateNMZConfig config, UltimateNMZPlugin plugin, VisualNotificationManager notificationManager, SkillIconManager skillIconManager, ItemManager itemManager) {
        super(plugin);

        this.plugin = plugin;
        this.client = client;
        this.config = config;
        this.notificationManager = notificationManager;
        this.skillIconManager = skillIconManager;
        this.itemManager = itemManager;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.HIGH);
        getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Ultimate-NMZ Notification Overlay."));

    }

    @Override
    public Dimension render(Graphics2D graphics) {

        if(!config.visualAlerts() || !plugin.isInNightmareZone()) {
            return super.render(graphics);
        }

        if(!plugin.getUltimateNmzOverlay().hasNightmareZoneStarted()) {
            return super.render(graphics);
        }


        // Clean time-based notifications
        notificationManager.cleanNotifications();

        // Make sure we have notifications to show
        if(notificationManager.getNotificationCount() == 0) {
            return super.render(graphics);
        }

        // Get all active notifications
        ArrayList<VisualNotification> notificationList = notificationManager.getNotifications();

        // Sort Notifications based on priority
        notificationList.sort(Comparator.comparingInt(o -> o.getType().getPriority()));

        Iterator notificationIterator = notificationList.iterator();

        int x = 0;
        int width = client.getCanvasWidth() / notificationList.size();

        while(notificationIterator.hasNext()) {
            VisualNotification visualNotification = (VisualNotification) notificationIterator.next();

            if(visualNotification.isExpired()) {
                notificationIterator.remove();
                continue;
            }

            ValueDriver opacityDriver = visualNotification.getOpacityDriver();

            if (opacityDriver == null) {
                opacityDriver = new ConstantDriver();
                opacityDriver.setValue(1f);
            }

            visualNotification.renderNotification(graphics, new Rectangle(x, 0, width, client.getCanvasHeight()));

            BufferedImage icon = null;
            switch (visualNotification.getType()) {
                case HP_BELOW_THRESHOLD:
                    if(config.showMinimumHPIcon()) {
                        icon = skillIconManager.getSkillImage(Skill.HITPOINTS);
                    }
                    break;

                case HP_ABOVE_THRESHOLD:
                    if(config.showMaximumHPIcon()) {
                        icon = skillIconManager.getSkillImage(Skill.HITPOINTS);
                    }
                    break;

                case OVERLOAD_ALMOST_EXPIRED:
                case OVERLOAD_EXPIRED:
                    if(config.showOverloadIcon()) {
                        icon = itemManager.getImage(ItemID.OVERLOAD_4);
                    }
                    break;

                case ABSORPTION_BELOW_THRESHOLD:
                    if(config.showAbsorptionIcon()) {
                        icon = itemManager.getImage(ItemID.ABSORPTION_4);
                    }
                    break;

                default:
                    break;
            }


            if(icon != null) {
                final Composite originalComposite = graphics.getComposite();
                final double iconSize = iconSizeDriver.getValue().doubleValue();
                int iW = (int) (icon.getWidth() * iconSize);
                int iH = (int) (icon.getHeight() * iconSize);

                float iconOpacity = 1f - (float) opacityDriver.getValue();

                if(opacityDriver instanceof ConstantDriver) {
                    iconOpacity = (float) opacityDriver.getValue();
                }

                graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, iconOpacity));
                graphics.drawImage(icon, (x + (width / 2)) - (iW / 2), (client.getCanvasHeight() / 2) - (iH / 2), iW, iH, null);
                graphics.setComposite(originalComposite);
            }

            x += width;
        }

        return super.render(graphics);
    }
}
