package com.gmail.zendarva.lagbgon;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;

public class TickHandler {
	private long nextClear;
	private boolean warned = false;
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event)
	{
		ChatComponentText chat;
		if (MainCommand.checkTPS())
		{
			return;
		}
		
		if (nextClear == 0)
		{
			nextClear = (long)( System.currentTimeMillis() + (ConfigManager.timeInterval * 1000 * 60));
			return;
		}
		if (nextClear < System.currentTimeMillis() && ConfigManager.automaticRemoval == true)
		{
			if (warned == true)
			{
				nextClear = (long)( System.currentTimeMillis() + (ConfigManager.timeInterval * 1000 * 60));
				MainCommand.doClear();
				warned = false;
			}
			else
			{
				//Warn of removal in one minute.
				nextClear = (long)(System.currentTimeMillis() + 1000 * 60);
				chat = new ChatComponentText("Lag'B'Gon will be removing items in 1 minute!");					
				MinecraftServer.getServer().getConfigurationManager().sendChatMsg(chat);
				warned = true;
			}
		}
	}
}
