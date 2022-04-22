package com.nmzhelperutils.notifications;

import com.nmzhelperutils.UltimateNMZConfig;
import com.nmzhelperutils.UltimateNMZPlugin;
import com.nmzhelperutils.drivers.AlphaSineDriver;
import net.runelite.api.*;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class VisualNotificationManager extends OverlayPanel {

    private static ArrayList<VisualNotification> notificationList = new ArrayList<>();

    private final Client client;
    private final UltimateNMZConfig config;
    private final UltimateNMZPlugin plugin;
    private final SkillIconManager skillIconManager;
    private final ItemManager itemManager;



    private int priorityIndex = -1;

    @Inject
    private VisualNotificationManager(Client client, UltimateNMZPlugin plugin, UltimateNMZConfig config, SkillIconManager skillIconManager, ItemManager itemManager) {
        super(plugin);

        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.skillIconManager = skillIconManager;
        this.itemManager = itemManager;

        setPosition(OverlayPosition.DYNAMIC);
        setBounds(new Rectangle(0, 0, client.getCanvasWidth(), client.getCanvasHeight()));
        setPriority(OverlayPriority.HIGHEST);
        getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Ultimate NMZ Alert"));
    }

    public synchronized void createNotification(VisualNotificationType type) {
        VisualNotification visualNotification = getNotificationByType(type);

        if(visualNotification != null) {
            if(visualNotification.isExpired()) {
                removeNotification(type);
                visualNotification = null;
            }
        }

        long notificationLength = -1;
        Color notificationColor = null;
        AlphaSineDriver alphaDriver = null;

        float alphaMin = 0f;
        float alphaMax = 0.75f;
        long alphaDuration = 20;

        switch (type) {
            case HP_BELOW_THRESHOLD:
                notificationColor = new Color(221, 79, 1);
                alphaDriver = new AlphaSineDriver(0.125f, 0.55f, 25);
                notificationLength = 4;
                break;
            case HP_ABOVE_THRESHOLD:
                notificationColor = new Color(221, 79, 1);
                alphaDriver = new AlphaSineDriver(0.125f, 0.55f, 25);
                notificationLength = -1;
                break;
            case ABSORPTION_BELOW_THRESHOLD:
                notificationColor = new Color(0, 179, 255);
                alphaDriver = new AlphaSineDriver(0.125f, 0.55f, 25);
                notificationLength = -1;
                break;
            case ZAPPER_SPAWNED:
                notificationColor = new Color(161, 0, 255);
                alphaDriver = new AlphaSineDriver(0f, 0.55f, 20);
                notificationLength = 4;
                break;
            case POWER_SURGE_SPAWNED:
                notificationColor = new Color(255, 221, 0);
                alphaDriver = new AlphaSineDriver(0f, 0.55f, 20);
                notificationLength = 4;
                break;
            case RECURRENT_DAMAGE_SPAWNED:
                notificationColor = new Color(255, 0, 21);
                alphaDriver = new AlphaSineDriver(0f, 0.55f, 20);
                notificationLength = 4;
                break;
            case ULTIMATE_FORCE_SPAWNED:
                notificationColor = new Color(255, 255, 255);
                alphaDriver = new AlphaSineDriver(0f, 0.45f, 20);
                notificationLength = 4;
                break;
            default:
                // Invalid Notification...
                return;
        }

        if(notificationLength > 0 -1) {
            notificationLength = notificationLength * 1000;
        }

        if(alphaDriver == null) {
            alphaDriver = new AlphaSineDriver(alphaMin, alphaMax, alphaDuration);
        }

        if(visualNotification == null) {
            visualNotification = new VisualNotification(type, notificationColor, alphaDriver, notificationLength);
            notificationList.add(visualNotification);
        } else {
            if(notificationLength != -1) {
                visualNotification.extendLength(notificationLength);
            }
        }
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if(!config.visualAlerts() || !plugin.isInNightmareZone()) {
            return super.render(graphics);
        }

        if(notificationList.size() == 0) {
            return super.render(graphics);
        }

        if(!plugin.getUltimateNmzOverlay().hasNightmareZoneStarted()) {
            return super.render(graphics);
        }

        cleanNotifications();

        // Sort
        Collections.sort(notificationList, (o1, o2) -> {
            if(o1.getType().getPriority() < o2.getType().getPriority()) {
                return -1;
            } else if(o1.getType().getPriority() == o2.getType().getPriority()) {
                return 0;
            } else {
                return 1;
            }
        });

        if(notificationList.size() == 0) {
            return super.render(graphics);
        }

        Iterator notificationIterator = notificationList.iterator();

        int x = 0;
        int width = client.getCanvasWidth() / notificationList.size();

        while(notificationIterator.hasNext()) {
            VisualNotification visualNotification = (VisualNotification) notificationIterator.next();

            if(visualNotification.isExpired()) {
                notificationIterator.remove();
                continue;
            }

            float alpha = 1f;

            if (visualNotification.getAlphaDriver() != null) {
                alpha = visualNotification.getAlphaDriver().getValue();
            }

            renderNotificationScreen(graphics, visualNotification.getColor(), alpha, new Rectangle(x, 0, width, client.getCanvasHeight()));

            BufferedImage icon = null;
            switch (visualNotification.getType()) {
                case HP_ABOVE_THRESHOLD:
                    icon = skillIconManager.getSkillImage(Skill.HITPOINTS);
                    break;

                case ABSORPTION_BELOW_THRESHOLD:
                    icon = itemManager.getImage(ItemID.ABSORPTION_4);
                    break;

                default:
                    break;
            }

            if(icon != null) {
                int iW = (int) (icon.getWidth() * 2.5);
                int iH = (int) (icon.getHeight() * 2.5);
                graphics.drawImage(icon, (x + (width / 2)) - (iW / 2), (client.getCanvasHeight() / 2) - (iH / 2), iW, iH, null);
            }

            x += width;
        }

        return super.render(graphics);
    }

    private void renderNotificationScreen(Graphics2D graphics, Color color, float alpha, Rectangle bounds) {
        Composite originalComposite = graphics.getComposite();
        graphics.setColor(color);

        if (alpha != 1f) {
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }

        graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        graphics.setComposite(originalComposite);
    }

    public int getNotificationCount() {
        return notificationList.size();
    }

    protected synchronized void cleanNotifications() {
        Iterator notificationIterator = notificationList.iterator();
        while (notificationIterator.hasNext()) {
            VisualNotification notification = (VisualNotification) notificationIterator.next();

            if(notification.isExpired()) {
                notificationIterator.remove();
            }
        }
    }

    public ArrayList<VisualNotification> getNotificationsByPriority(int priority) {
        final ArrayList<VisualNotification> priorityList = new ArrayList<>();

        Iterator notificationIterator = notificationList.iterator();
        while (notificationIterator.hasNext()) {
            VisualNotification notification = (VisualNotification) notificationIterator.next();

            if(notification.isExpired()) {
                notificationIterator.remove();
            }

            if(notification.getType().getPriority() == priority) {
                priorityList.add(notification);
            }
        }

        return priorityList;
    }

    public synchronized boolean hasNotification(VisualNotificationType type) {
        return (getNotificationByType(type) == null ? false : true);
    }

    public synchronized VisualNotification getNotificationByType(VisualNotificationType type) {
        for(VisualNotification notification : notificationList) {
            if(notification.getType() == type) {
                return notification;
            }
        }

        return null;
    }

    public void removeNotification(VisualNotificationType type) {
        Iterator notificationIterator = notificationList.iterator();
        while (notificationIterator.hasNext()) {
            VisualNotification notification = (VisualNotification) notificationIterator.next();

            if(notification.getType() == type) {
                notificationIterator.remove();
                break;
            }
        }
    }

    public void configUpdated() {
        if(!config.visualAlerts()) {
            notificationList.clear();
        } else {
            if(!config.absorptionNotification()) {
                removeNotification(VisualNotificationType.ABSORPTION_BELOW_THRESHOLD);
            }
            if(!config.maximumHPNotification()) {
                removeNotification(VisualNotificationType.HP_ABOVE_THRESHOLD);
            }
            if(!config.minimumHPNotification()) {
                removeNotification(VisualNotificationType.HP_BELOW_THRESHOLD);
            }
            if(!config.powerSurgeNotification()) {
                removeNotification(VisualNotificationType.POWER_SURGE_SPAWNED);
            }
            if(!config.zapperNotification()) {
                removeNotification(VisualNotificationType.ZAPPER_SPAWNED);
            }
            if(!config.recurrentDamageNotification()) {
                removeNotification(VisualNotificationType.RECURRENT_DAMAGE_SPAWNED);
            }
            if(!config.ultimateForceNotification()) {
                removeNotification(VisualNotificationType.ULTIMATE_FORCE_SPAWNED);
            }
        }


    }

    public void clearNotifications() {
        notificationList.clear();
    }
}
