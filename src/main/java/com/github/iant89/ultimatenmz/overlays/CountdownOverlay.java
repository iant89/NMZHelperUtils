package com.github.iant89.ultimatenmz.overlays;

import com.github.iant89.ultimatenmz.UltimateNMZConfig;
import com.github.iant89.ultimatenmz.UltimateNMZPlugin;
import com.github.iant89.ultimatenmz.utils.DurationUtils;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;

import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class CountdownOverlay extends OverlayPanel {

    private UltimateNMZPlugin plugin;

    private final Client client;
    private final UltimateNMZConfig config;

    private Instant endTime;

    @Inject
    private CountdownOverlay(Client client, UltimateNMZConfig config, UltimateNMZPlugin plugin) {
        super(plugin);

        this.plugin = plugin;
        this.client = client;
        this.config = config;

        setPosition(OverlayPosition.BOTTOM_LEFT);
        setPriority(OverlayPriority.HIGH);
        getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Ultimate-NMZ Overlay."));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if(endTime == null || !plugin.getConfig().showCountdownOverlay()) {
            return super.render(graphics);
        }

        long secondsRemaining = DurationUtils.toSecondsPart(Duration.between(Instant.now(), endTime));

        if(secondsRemaining > 0) {
            panelComponent.getChildren().add(TitleComponent.builder().text("NMZ STARTING IN").build());
            panelComponent.getChildren().add(TitleComponent.builder().text("" + secondsRemaining + " SECOND" + (secondsRemaining > 1 ? "S" : "") + ".").color(Color.GREEN).build());
        } else {
            endTime = null;
        }

        return super.render(graphics);
    }

    public void triggerCountdown(Instant endTime) {
        this.endTime = endTime;
    }
}
