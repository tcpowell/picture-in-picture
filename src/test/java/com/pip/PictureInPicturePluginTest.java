package com.pip;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PictureInPicturePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PictureInPicturePlugin.class);
		RuneLite.main(args);
	}
}