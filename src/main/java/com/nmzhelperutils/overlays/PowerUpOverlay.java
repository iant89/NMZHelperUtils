package com.nmzhelperutils.overlays;

import com.nmzhelperutils.UltimateNMZConfig;
import com.nmzhelperutils.UltimateNMZPlugin;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import javax.inject.Inject;

import java.awt.*;

import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class PowerUpOverlay extends OverlayPanel {

    public static final int OBJECT_ZAPPER = ObjectID.ZAPPER_26256;
    public static final int OBJECT_POWER_SURGE = ObjectID.POWER_SURGE;
    public static final int OBJECT_RECURRENT_DAMAGE = ObjectID.RECURRENT_DAMAGE;
    public static final int OBJECT_ULTIMATE_FORCE = ObjectID.ULTIMATE_FORCE;

    private final Client client;
    private final UltimateNMZConfig config;
    private final UltimateNMZPlugin plugin;

    @Inject
    private PowerUpOverlay(Client client, UltimateNMZConfig config, UltimateNMZPlugin plugin) {
        super(plugin);

        this.plugin = plugin;
        this.client = client;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.HIGH);
        getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Utimate NMZ Overlay."));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if(!plugin.isInNightmareZone()) {
            return super.render(graphics);
        }

        renderTileObjects(graphics);

        return super.render(graphics);
    }

    public void renderTileObjects(Graphics2D graphics) {
        if(!config.drawPowerUpLocation()) {
            return;
        }

        Scene scene = client.getScene();
        Tile[][][] tiles = scene.getTiles();

        int z = client.getPlane();

        for (int x = 0; x < Constants.SCENE_SIZE; ++x) {
            for (int y = 0; y < Constants.SCENE_SIZE; ++y) {
                Tile tile = tiles[z][x][y];

                if (tile == null) {
                    continue;
                }


                renderGameObjects(graphics, tile, client.getLocalPlayer());
            }
        }
    }

    private void renderGameObjects(Graphics2D graphics, Tile tile, Player player) {
        GameObject[] gameObjects = tile.getGameObjects();



        if (gameObjects != null) {
            for (GameObject gameObject : gameObjects) {
                if(gameObject == null) {
                    continue;
                }

                String objectName;
                Color objectColor;

                switch(gameObject.getId()) {
                    case OBJECT_POWER_SURGE:
                        if(!config.powerSurgeNotification()) {
                            continue;
                        }
                        objectName = "POWER SURGE";
                        objectColor = new Color(255, 221, 0);
                        break;
                    case OBJECT_RECURRENT_DAMAGE:
                        if(!config.recurrentDamageNotification()) {
                            continue;
                        }
                        objectName = "RECURRENT DAMAGE";
                        objectColor = new Color(255, 0, 21);
                        break;
                    case OBJECT_ZAPPER:
                        if(!config.zapperNotification()) {
                            continue;
                        }
                        objectName = "ZAPPER";
                        objectColor = new Color(161, 0, 255);
                        break;
                    case OBJECT_ULTIMATE_FORCE:
                        if(!config.ultimateForceNotification()) {
                            continue;
                        }
                        objectName = "ULTIMATE FORCE";
                        objectColor = new Color(255, 255, 255);
                        break;

                    default:
                        continue;
                }

                if(gameObject.getSceneMinLocation().equals(tile.getSceneLocation())) {
                    if (player.getLocalLocation().distanceTo(gameObject.getLocalLocation()) <= 1500) {
                        LocalPoint lp = gameObject.getLocalLocation();
                        Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, 1);

                        OverlayUtil.renderPolygon(graphics, tilePoly, objectColor, new BasicStroke(2f));

                        Point textLocation = gameObject.getCanvasTextLocation(graphics, objectName, (int) (gameObject.getCanvasTilePoly().getBounds2D().getHeight() + 140));
                        OverlayUtil.renderTextLocation(graphics, textLocation, objectName, objectColor);
                    }
                }
            }
        }
    }

}
