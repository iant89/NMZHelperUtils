package com.github.iant89.ultimatenmz.overlays;

import com.github.iant89.ultimatenmz.UltimateNMZPlugin;
import com.github.iant89.ultimatenmz.drivers.SineDriver;
import com.github.iant89.ultimatenmz.drivers.ValueDriver;
import com.github.iant89.ultimatenmz.utils.DirectionArrow;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;

import java.awt.*;
import java.awt.geom.Line2D;

import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class PowerUpOverlay extends OverlayPanel {

    public static final int OBJECT_ZAPPER = ObjectID.ZAPPER_26256;
    public static final int OBJECT_POWER_SURGE = ObjectID.POWER_SURGE;
    public static final int OBJECT_RECURRENT_DAMAGE = ObjectID.RECURRENT_DAMAGE;
    public static final int OBJECT_ULTIMATE_FORCE = ObjectID.ULTIMATE_FORCE;

    private UltimateNMZPlugin plugin;

    private ValueDriver opacityDriver = new SineDriver(0.125f, 0.55f, 25);

    private ValueDriver sizeDriver = new SineDriver(1f, 3f, 25);
    @Inject
    private PowerUpOverlay(UltimateNMZPlugin plugin) {
        super(plugin);

        this.plugin = plugin;

        setPosition(OverlayPosition.DYNAMIC);
        setMovable(false);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.HIGH);
        getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Ultimate NMZ Overlay."));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if(plugin == null) {
            return super.render(graphics);
        }
        if(!plugin.isInNightmareZone()) {
            return super.render(graphics);
        }

        renderTileObjects(graphics);

        return super.render(graphics);
    }

    public void renderTileObjects(Graphics2D graphics) {
        if(!plugin.isInNightmareZone()) {
            return;
        }

        Scene scene = plugin.getClient().getScene();
        Tile[][][] tiles = scene.getTiles();

        int z = plugin.getClient().getPlane();

        for (int x = 0; x < Constants.SCENE_SIZE; ++x) {
            for (int y = 0; y < Constants.SCENE_SIZE; ++y) {
                Tile tile = tiles[z][x][y];

                if (tile == null) {
                    continue;
                }

                renderGameObjects(graphics, tile, plugin.getClient().getLocalPlayer());
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
                        if(!plugin.getConfig().drawPowerSurgeLocation()) {
                            continue;
                        }
                        objectName = "POWER SURGE";
                        objectColor = plugin.getConfig().powerSurgeAlertColor();
                        break;
                    case OBJECT_RECURRENT_DAMAGE:
                        if(!plugin.getConfig().drawRecurrentDamageLocation()) {
                            continue;
                        }
                        objectName = "RECURRENT DAMAGE";
                        objectColor = plugin.getConfig().recurrentDamageAlertColor();
                        break;
                    case OBJECT_ZAPPER:
                        if(!plugin.getConfig().drawZapperLocation()) {
                            continue;
                        }
                        objectName = "ZAPPER";
                        objectColor = plugin.getConfig().zapperAlertColor();
                        break;
                    case OBJECT_ULTIMATE_FORCE:
                        if(!plugin.getConfig().drawUltimateForceLocation()) {
                            continue;
                        }
                        objectName = "ULTIMATE FORCE";
                        objectColor = plugin.getConfig().ultimateForceAlertColor();
                        break;

                    default:
                        continue;
                }

                Composite originalComposite = graphics.getComposite();

                graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacityDriver.getValue().floatValue()));

                // Draw Arrow
                LocalPoint lp = tile.getLocalLocation();

                Polygon poly = Perspective.getCanvasTilePoly(plugin.getClient(), lp, 30);
                if (poly == null || poly.getBounds() == null)
                {
                    return;
                }

                int arrowWidth = 5;
                int arrowHeight = 4;
                int arrowLineWidth = 9;

                int startX = poly.getBounds().x + (poly.getBounds().width / 2) - (arrowWidth / 2);
                int startY = poly.getBounds().y + (poly.getBounds().height / 2) - (arrowHeight / 2);

                //DirectionArrow.drawArrow(graphics, line, objectColor, arrowWidth, arrowHeight, arrowLineWidth);

                DirectionArrow.drawWorldArrow(graphics, objectColor, startX, startY);


                if(gameObject.getSceneMinLocation().equals(tile.getSceneLocation())) {
                    if (player.getLocalLocation().distanceTo(gameObject.getLocalLocation()) <= 4500) {
                        lp = gameObject.getLocalLocation();
                        Polygon tilePoly = Perspective.getCanvasTileAreaPoly(plugin.getClient(), lp, 1);

                        OverlayUtil.renderPolygon(graphics, tilePoly, objectColor, new BasicStroke(2f));

                        Point textLocation = gameObject.getCanvasTextLocation(graphics, objectName, (int) (gameObject.getCanvasTilePoly().getBounds2D().getHeight() + 140));
                        OverlayUtil.renderTextLocation(graphics, textLocation, objectName, objectColor);
                    }
                }

                graphics.setComposite(originalComposite);
            }
        }
    }

}
