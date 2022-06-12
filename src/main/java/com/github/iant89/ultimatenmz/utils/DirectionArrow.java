package com.github.iant89.ultimatenmz.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

import static net.runelite.api.Constants.CHUNK_SIZE;

public class DirectionArrow {
	private static WorldPoint rotate(WorldPoint point, int rotation)
	{
		int chunkX = point.getX() & ~(CHUNK_SIZE - 1);
		int chunkY = point.getY() & ~(CHUNK_SIZE - 1);
		int x = point.getX() & (CHUNK_SIZE - 1);
		int y = point.getY() & (CHUNK_SIZE - 1);
		switch (rotation)
		{
			case 1:
				return new WorldPoint(chunkX + y, chunkY + (CHUNK_SIZE - 1 - x), point.getPlane());
			case 2:
				return new WorldPoint(chunkX + (CHUNK_SIZE - 1 - x), chunkY + (CHUNK_SIZE - 1 - y), point.getPlane());
			case 3:
				return new WorldPoint(chunkX + (CHUNK_SIZE - 1 - y), chunkY + x, point.getPlane());
		}
		return point;
	}

	private static Collection<WorldPoint> toLocalInstance(Client client, WorldPoint worldPoint)
	{
		if (!client.isInInstancedRegion()) {
			return Collections.singleton(worldPoint);
		}

		// find instance chunks using the template point. there might be more than one.
		List<WorldPoint> worldPoints = new ArrayList<>();

		int[][][] instanceTemplateChunks = client.getInstanceTemplateChunks();
		for (int z = 0; z < instanceTemplateChunks.length; ++z)
		{
			for (int x = 0; x < instanceTemplateChunks[z].length; ++x)
			{
				for (int y = 0; y < instanceTemplateChunks[z][x].length; ++y)
				{
					int chunkData = instanceTemplateChunks[z][x][y];
					int rotation = chunkData >> 1 & 0x3;
					int templateChunkY = (chunkData >> 3 & 0x7FF) * CHUNK_SIZE;
					int templateChunkX = (chunkData >> 14 & 0x3FF) * CHUNK_SIZE;
					if (worldPoint.getX() >= templateChunkX && worldPoint.getX() < templateChunkX + CHUNK_SIZE
							&& worldPoint.getY() >= templateChunkY && worldPoint.getY() < templateChunkY + CHUNK_SIZE)
					{
						WorldPoint p =
								new WorldPoint(client.getBaseX() + x * CHUNK_SIZE + (worldPoint.getX() & (CHUNK_SIZE - 1)),
										client.getBaseY() + y * CHUNK_SIZE + (worldPoint.getY() & (CHUNK_SIZE - 1)),
										z);
						p = rotate(p, rotation);
						if (p.isInScene(client))
						{
							worldPoints.add(p);
						}
					}
				}
			}
		}
		return worldPoints;
	}

	private static LocalPoint getInstanceLocalPoint(Client client, WorldPoint wp)
	{
		WorldPoint instanceWorldPoint = getInstanceWorldPoint(client, wp);
		if (instanceWorldPoint == null)
		{
			return null;
		}

		return LocalPoint.fromWorld(client, instanceWorldPoint);
	}

	private static WorldPoint getInstanceWorldPoint(Client client, WorldPoint wp)
	{
		Collection<WorldPoint> points = toLocalInstance(client, wp);

		for (WorldPoint point : points)
		{
			if (point != null)
			{
				return point;
			}
		}
		return null;
	}

	private static Point getMinimapPoint(Client client, WorldPoint start, WorldPoint destination)
	{
		RenderOverview ro = client.getRenderOverview();
		if (ro.getWorldMapData().surfaceContainsPosition(start.getX(), start.getY()) !=
				ro.getWorldMapData().surfaceContainsPosition(destination.getX(), destination.getY()))
		{
			return null;
		}

		int x = (destination.getX() - start.getX());
		int y = (destination.getY() - start.getY());

		float maxDistance = Math.max(Math.abs(x), Math.abs(y));
		x = x * 100;
		y = y * 100;
		x /= maxDistance;
		y /= maxDistance;

		Widget minimapDrawWidget;
		if (client.isResized())
		{
			if (client.getVar(Varbits.SIDE_PANELS) == 1)
			{
				minimapDrawWidget = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_DRAW_AREA);
			}
			else
			{
				minimapDrawWidget = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_STONES_DRAW_AREA);
			}
		}
		else
		{
			minimapDrawWidget = client.getWidget(WidgetInfo.FIXED_VIEWPORT_MINIMAP_DRAW_AREA);
		}

		if (minimapDrawWidget == null)
		{
			return null;
		}

		final int angle = client.getMapAngle() & 0x7FF;

		final int sin = Perspective.SINE[angle];
		final int cos = Perspective.COSINE[angle];

		final int xx = y * sin + cos * x >> 16;
		final int yy = sin * x - y * cos >> 16;

		Point loc = minimapDrawWidget.getCanvasLocation();
		int miniMapX = loc.getX() + xx + minimapDrawWidget.getWidth() / 2;
		int miniMapY = minimapDrawWidget.getHeight() / 2 + loc.getY() + yy;
		return new Point(miniMapX, miniMapY);
	}

	public static void renderMinimapArrow(Graphics2D graphics, Client client, WorldPoint worldPoint, Color color) {
		final int MAX_DRAW_DISTANCE = 16;
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}

		WorldPoint playerLocation = player.getWorldLocation();

		WorldPoint wp = getInstanceWorldPoint(client, worldPoint);

		if (wp == null)
		{
			return;
		}

		if (wp.distanceTo(playerLocation) >= MAX_DRAW_DISTANCE)
		{
			createMinimapDirectionArrow(graphics, client, wp, color);
			return;
		}

		LocalPoint lp = LocalPoint.fromWorld(client, wp);
		if (lp == null)
		{
			return;
		}

		Point posOnMinimap = Perspective.localToMinimap(client, lp);
		if (posOnMinimap == null)
		{
			return;
		}

		Line2D.Double line = new Line2D.Double(posOnMinimap.getX(), posOnMinimap.getY() - 18, posOnMinimap.getX(),
			posOnMinimap.getY() - 8);

		drawMinimapArrow(graphics, line, color);
	}

	protected static void createMinimapDirectionArrow(Graphics2D graphics, Client client, WorldPoint wp, Color color) {
		Player player = client.getLocalPlayer();

		if (player == null)
		{
			return;
		}

		if (wp == null)
		{
			return;
		}

		Point playerPosOnMinimap = player.getMinimapLocation();

		Point destinationPosOnMinimap = getMinimapPoint(client, player.getWorldLocation(), wp);

		if (playerPosOnMinimap == null || destinationPosOnMinimap == null)
		{
			return;
		}

		double xDiff = playerPosOnMinimap.getX() - destinationPosOnMinimap.getX();
		double yDiff = destinationPosOnMinimap.getY() - playerPosOnMinimap.getY();
		double angle = Math.atan2(yDiff, xDiff);

		int startX = (int) (playerPosOnMinimap.getX() - (Math.cos(angle) * 55));
		int startY = (int) (playerPosOnMinimap.getY() + (Math.sin(angle) * 55));

		int endX = (int) (playerPosOnMinimap.getX() - (Math.cos(angle) * 65));
		int endY = (int) (playerPosOnMinimap.getY() + (Math.sin(angle) * 65));

		Line2D.Double line = new Line2D.Double(startX, startY, endX, endY);

		drawMinimapArrow(graphics, line, color);
	}

	public static void drawWorldArrow(Graphics2D graphics, Color color, int startX, int startY) {
		Line2D.Double line = new Line2D.Double(startX, startY - 13, startX, startY);

		int headWidth = 5;
		int headHeight = 4;
		int lineWidth = 9;

		drawArrow(graphics, line, color, lineWidth, headHeight, headWidth);
	}

	public static void drawMinimapArrow(Graphics2D graphics, Line2D.Double line, Color color) {
		drawArrow(graphics, line, color, 6, 2, 2);
	}

	public static void drawArrow(Graphics2D graphics, Line2D.Double line, Color color, int width, int tipHeight, int tipWidth) {
		graphics.setColor(Color.BLACK);
		graphics.setStroke(new BasicStroke(width));
		graphics.draw(line);
		drawWorldArrowHead(graphics, line, tipHeight, tipWidth);

		graphics.setColor(color);
		graphics.setStroke(new BasicStroke(width - 3));
		graphics.draw(line);
		drawWorldArrowHead(graphics, line, tipHeight - 2, tipWidth - 2);
		graphics.setStroke(new BasicStroke(1));
	}


	public static void drawWorldArrowHead(Graphics2D g2d, Line2D.Double line, int extraSizeHeight, int extraSizeWidth) {
		AffineTransform tx = new AffineTransform();

		Polygon arrowHead = new Polygon();
		arrowHead.addPoint(0, 6 + extraSizeHeight);
		arrowHead.addPoint(-6 - extraSizeWidth, -1 - extraSizeHeight);
		arrowHead.addPoint(6 + extraSizeWidth, -1 - extraSizeHeight);

		tx.setToIdentity();
		double angle = Math.atan2(line.y2 - line.y1, line.x2 - line.x1);
		tx.translate(line.x2, line.y2);
		tx.rotate((angle - Math.PI / 2d));

		Graphics2D g = (Graphics2D) g2d.create();
		g.setTransform(tx);
		g.fill(arrowHead);
		g.dispose();
	}

	public static void drawLineArrowHead(Graphics2D g2d, Line2D.Double line) {
		AffineTransform tx = new AffineTransform();

		Polygon arrowHead = new Polygon();
		arrowHead.addPoint( 0,0);
		arrowHead.addPoint( -3, -6);
		arrowHead.addPoint( 3,-6);

		tx.setToIdentity();
		double angle = Math.atan2(line.y2-line.y1, line.x2-line.x1);
		tx.translate(line.x2, line.y2);
		tx.rotate((angle-Math.PI/2d));

		Graphics2D g = (Graphics2D) g2d.create();
		g.setTransform(tx);
		g.fill(arrowHead);
		g.dispose();
	}

	public static void drawLine(Graphics2D graphics, Line2D.Double line, Color color, Rectangle clippingRegion) {
		graphics.setStroke(new BasicStroke(1));
		graphics.setClip(clippingRegion);
		graphics.setColor(color);
		graphics.draw(line);

		drawLineArrowHead(graphics, line);
	}
}