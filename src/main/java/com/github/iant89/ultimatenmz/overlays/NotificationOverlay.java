package com.github.iant89.ultimatenmz.overlays;

import com.github.iant89.ultimatenmz.UltimateNMZPlugin;
import com.github.iant89.ultimatenmz.drivers.ConstantDriver;
import com.github.iant89.ultimatenmz.drivers.SineDriver;
import com.github.iant89.ultimatenmz.drivers.ValueDriver;
import com.github.iant89.ultimatenmz.icons.IconManager;
import com.github.iant89.ultimatenmz.notifications.*;
import net.runelite.api.*;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class NotificationOverlay extends OverlayPanel {

    private UltimateNMZPlugin plugin;

    @Inject
    private IconManager iconManager;
    private ValueDriver iconSizeDriver = new SineDriver(2f, 5f, 25);

    @Inject
    private NotificationOverlay(UltimateNMZPlugin plugin) {
        super(plugin);

        this.plugin = plugin;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setMovable(false);
        setPriority(OverlayPriority.HIGH);
        getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Ultimate-NMZ Notification Overlay."));
    }

    @Override
    public Dimension render(Graphics2D graphics) {


        if(!plugin.getConfig().visualAlerts() || !plugin.isInNightmareZone()) {
            return super.render(graphics);
        }

        if(!plugin.getUltimateNmzOverlay().hasNightmareZoneStarted()) {
            return super.render(graphics);
        }


        // Clean time-based notifications
        plugin.getNotificationManager().cleanNotifications();

        // Make sure we have notifications to show
        if(plugin.getNotificationManager().getCount() == 0) {
            return super.render(graphics);
        }

        Iterator notificationIterator = plugin.getNotificationManager().getNotifications().iterator();

        int x = 0;
        int width = plugin.getClient().getCanvasWidth() / plugin.getNotificationManager().getCount();

        Rectangle notificationBounds;
        while(notificationIterator.hasNext()) {
            Notification notification = (Notification) notificationIterator.next();

            if(notification.isExpired()) {
                notificationIterator.remove();
                continue;
            }

            ValueDriver opacityDriver = notification.getOpacityDriver();

            if (opacityDriver == null) {
                opacityDriver = new ConstantDriver();
                opacityDriver.setValue(1f);
            }

            notificationBounds = new Rectangle(x, 0, width, plugin.getClient().getCanvasHeight());

            notification.renderNotification(graphics, notificationBounds);

            BufferedImage icon = iconManager.getIconForNotification(notification);

            if(icon != null) {
                final Composite originalComposite = graphics.getComposite();
                double iconSize = iconSizeDriver.getValue().doubleValue();

                switch (notification.getType()) {
                    case ZAPPER_SPAWNED:
                    case ULTIMATE_FORCE_SPAWNED:
                    case POWER_SURGE_SPAWNED:
                    case RECURRENT_DAMAGE_SPAWNED:
                        iconSize /= 5;
                        break;
                }

                int iW = (int) (icon.getWidth() * iconSize);
                int iH = (int) (icon.getHeight() * iconSize);

                float iconOpacity = 1f - opacityDriver.getValue().floatValue();

                if(opacityDriver instanceof ConstantDriver) {
                    iconOpacity = opacityDriver.getValue().floatValue();
                }

                graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, iconOpacity));
                graphics.drawImage(icon, (x + (width / 2)) - (iW / 2), (plugin.getClient().getCanvasHeight() / 2) - (iH / 2), iW, iH, null);
                graphics.setComposite(originalComposite);
            }

            x += width;




        }

        return super.render(graphics);
    }
}
