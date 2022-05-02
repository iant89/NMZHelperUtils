package com.github.iant89.ultimatenmz.notifications;

import com.github.iant89.ultimatenmz.drivers.ValueDriver;

import java.awt.*;

public class FadedVisualNotification extends VisualNotification {

    public FadedVisualNotification(final VisualNotificationType type, final Color color, ValueDriver opacityDriver, long length) {
        super(type, color, opacityDriver, length);
    }

    @Override
    public void renderNotification(Graphics2D graphics, Rectangle bounds) {
        Composite originalComposite = graphics.getComposite();
        graphics.setColor(getColor());

        if(getOpacityDriver() == null) {
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
