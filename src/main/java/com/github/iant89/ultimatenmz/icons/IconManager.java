package com.github.iant89.ultimatenmz.icons;

import com.github.iant89.ultimatenmz.UltimateNMZConfig;
import com.github.iant89.ultimatenmz.UltimateNMZPlugin;
import com.github.iant89.ultimatenmz.notifications.Notification;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class IconManager {

    private final UltimateNMZPlugin plugin;

    @Inject
    private SkillIconManager skillIconManager;
    @Inject
    private ItemManager itemManager;

    private static BufferedImage[] powerupIcons;

    @Inject
    protected IconManager(UltimateNMZPlugin plugin, SkillIconManager skillIconManager, ItemManager itemManager) {
        this.plugin = plugin;

        this.skillIconManager = skillIconManager;
        this.itemManager = itemManager;

        loadPowerupIcons();
    }

    private void loadPowerupIcons() {
        powerupIcons = new BufferedImage[4];

        // 0 - Zapper
        powerupIcons[0] = loadImageFromResources("/zapper.png");

        // 1 - Recurrent Damage
        powerupIcons[1] = loadImageFromResources("/recurrent_damage.png");

        // 2 - Ultimate Force
        powerupIcons[2] = loadImageFromResources("/ultimate_force.png");

        // 3 - Power Surge
        powerupIcons[3] = loadImageFromResources("/power_surge.png");

    }

    private BufferedImage loadImageFromResources(String url) {
        BufferedImage image = null;

        try {
            image = ImageIO.read(IconManager.class.getResource(url));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return image;
    }

    public BufferedImage getIconForNotification(Notification notification) {
        switch (notification.getType()) {
            case ZAPPER_SPAWNED:
                return powerupIcons[0];

            case RECURRENT_DAMAGE_SPAWNED:
                return powerupIcons[1];

            case ULTIMATE_FORCE_SPAWNED:
                return powerupIcons[2];

            case POWER_SURGE_SPAWNED:
                return powerupIcons[3];

            case HP_BELOW_THRESHOLD:
                if(plugin.getConfig().showMinimumHPIcon()) {
                    return skillIconManager.getSkillImage(Skill.HITPOINTS);
                }

                return null;

            case HP_ABOVE_THRESHOLD:
                if(plugin.getConfig().showMaximumHPIcon()) {
                    return skillIconManager.getSkillImage(Skill.HITPOINTS);
                }

                return null;

            case OVERLOAD_ALMOST_EXPIRED:
            case OVERLOAD_EXPIRED:
                if(!plugin.getConfig().showOverloadIcon()) {
                    return null;
                }

                switch (notification.getAnimationDriver().getValue().intValue()) {
                    case 0:
                        return itemManager.getImage(ItemID.OVERLOAD_4);

                    case 1:
                        return itemManager.getImage(ItemID.OVERLOAD_3);

                    case 2:
                        return itemManager.getImage(ItemID.OVERLOAD_2);

                    case 3:
                        return itemManager.getImage(ItemID.OVERLOAD_1);

                    case 4:
                        return itemManager.getImage(ItemID.VIAL);

                }
                return null;

            case ABSORPTION_BELOW_THRESHOLD:
                if(!plugin.getConfig().showAbsorptionIcon()) {
                    return null;
                }

                switch (notification.getAnimationDriver().getValue().intValue()) {
                    case 0:
                        return itemManager.getImage(ItemID.ABSORPTION_4);

                    case 1:
                        return itemManager.getImage(ItemID.ABSORPTION_3);

                    case 2:
                        return itemManager.getImage(ItemID.ABSORPTION_2);

                    case 3:
                        return itemManager.getImage(ItemID.ABSORPTION_1);

                    case 4:
                        return itemManager.getImage(ItemID.VIAL);

                }
                return null;

            default:
                return null;
        }
    }
}
