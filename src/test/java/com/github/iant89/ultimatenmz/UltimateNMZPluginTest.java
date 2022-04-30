package com.github.iant89.ultimatenmz;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class UltimateNMZPluginTest {

	public static void main(String[] args) throws Exception {
		ExternalPluginManager.loadBuiltin(UltimateNMZPlugin.class);
		RuneLite.main(args);
	}
}