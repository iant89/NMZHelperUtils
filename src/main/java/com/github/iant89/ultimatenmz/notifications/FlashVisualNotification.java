package com.github.iant89.ultimatenmz.notifications;

import com.github.iant89.ultimatenmz.drivers.ConstantDriver;

import java.awt.*;

public class FlashVisualNotification extends VisualNotification {

    private long nextFlashTime = -1;
    private long flashDelay = 500;

    public FlashVisualNotification(final VisualNotificationType type, Color color, float opacity, long length) {
        super(type, color, new ConstantDriver(opacity), length);

        nextFlashTime = System.currentTimeMillis() + flashDelay;
    }

    @Override
    public void renderNotification(Graphics2D graphics, Rectangle bounds) {
        if(isVisible()) {
            if(System.currentTimeMillis() >= nextFlashTime) {
                setVisible(false);
                nextFlashTime = System.currentTimeMillis() + flashDelay;
            }
        } else {
            if(System.currentTimeMillis() >= nextFlashTime) {
                setVisible(true);
                nextFlashTime = System.currentTimeMillis() + (flashDelay * 3);
            } else {
                return;
            }
        }

        Composite originalComposite = graphics.getComposite();
        graphics.setColor(getColor());

        if (getOpacityDriver() == null) {
            return;
        }

        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) getOpacityDriver().getValue()));

        graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

        graphics.setStroke(new BasicStroke(2f));
        graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        graphics.setStroke(new BasicStroke(1f));

        graphics.setComposite(originalComposite);
    }
}