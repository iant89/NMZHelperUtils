package com.nmzhelperutils;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class UltimateNMZPluginTest {

	public static void main(String[] args) throws Exception {
		ExternalPluginManager.loadBuiltin(UltimateNMZPlugin.class);
		RuneLite.main(args);
	}
}