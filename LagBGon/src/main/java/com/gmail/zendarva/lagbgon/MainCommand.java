package com.gmail.zendarva.lagbgon;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class MainCommand extends CommandBase {

	private static ConfigManager config = ConfigManager.instance();
	private static int chunksLoaded = 0;
	private static long nextUnload;
	
	@Override
	public String getCommandName() {
		return "bgon";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {
		// TODO Auto-generated method stub
		return "/bgon clear : Clears all entities and itemstacks";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		
		EntityPlayer plr;
		ChatComponentText chat;
		
		if (sender instanceof EntityPlayer)
		{
			plr = (EntityPlayer) sender;
		}
		else
			return;
		
		System.out.println(args.length);
		
		switch (args.length)
		{
		case 0:
			chat = new ChatComponentText("/bgon toggleitem : toggles the blacklist status of held item\n\r");
			sender.addChatMessage(chat);
			chat = new ChatComponentText("/bgon toggleentitiy <name>: toggles the blacklist status of the named entity\n\r");
			sender.addChatMessage(chat);
			chat = new ChatComponentText("/bgon clear : Clears all items/entities from the world not on blacklist\n\r");
			sender.addChatMessage(chat);
			chat = new ChatComponentText("/bgon interval <minutes> : sets the interval for automatic running of /bgon clear\n\r" );
			sender.addChatMessage(chat);
			chat = new ChatComponentText("/bgon toggleauto : Toggles automatic clearing of entities, and unloading of chunks.");
			sender.addChatMessage(chat);
			break;
		case 1:
			if (args[0].equals("listitems"))
			{
				StringBuilder line = new StringBuilder();
				chat = new ChatComponentText("Item Blacklist contains:");
				sender.addChatMessage(chat);
				for (String item : ConfigManager.itemBlacklist)
				{
					if (line.length() > 40)
					{
						chat = new ChatComponentText(line.toString());
						sender.addChatMessage(chat);
						line = new StringBuilder();
					}
						line.append(item);
						line.append(", ");						
				}
				if (line.length() > 0)
				{
					chat = new ChatComponentText((String) line.toString().subSequence(0, line.length()-2));
					sender.addChatMessage(chat);
				}
				return;
			}
			
			if (args[0].equals("toggleauto"))
			{
				config.toggleAuto();
				
				if (ConfigManager.automaticRemoval)
				{
					chat = new ChatComponentText("Automatic clearing enabled.");
					sender.addChatMessage(chat);
				}
				else
				{
					chat = new ChatComponentText("Automatic clearing disabled.");
					sender.addChatMessage(chat);
				}
				return;
			}
			if (args[0].equals("toggleitem"))
			{
				if (plr.getCurrentEquippedItem() == null)
				{
					chat = new ChatComponentText("You must have an item selected");
					sender.addChatMessage(chat);
					return;
				}
				
				Item item = plr.getCurrentEquippedItem().getItem();
				
				config.toggleItem(item);
				
				if (!config.isBlacklisted(plr.getCurrentEquippedItem().getItem()))
				{
					chat = new ChatComponentText(item.getItemStackDisplayName(plr.getCurrentEquippedItem()) + " removed from blacklist.");
					sender.addChatMessage(chat);
				}
				else
				{
					chat = new ChatComponentText(item.getItemStackDisplayName(plr.getCurrentEquippedItem()) + " added to blacklist.");
					sender.addChatMessage(chat);
				}
				
				return;
			}
			
			if (args[0].equals("clear"))
			{
				if (!DimensionManager.getWorld(0).isRemote) 
					doClear();
				return;
			}
			
		case 2:
			if (args[0].equals("toggleentity"))
			{
				config.toggleEntity(args[1]);
				
				if (config.isBlacklisted(args[1]))
				{
					chat = new ChatComponentText(args[1] + " has been added to the blacklist.");
					sender.addChatMessage(chat);
				}
				else
				{
					chat = new ChatComponentText(args[1] + " has been removed from the blacklist.");
					sender.addChatMessage(chat);
				}
				return;
			}
			
			if (args[0].equals("interval"))
			{
				int newInterval = Integer.parseInt(args[1]);
				
				config.changeInterval(newInterval);
				chat = new ChatComponentText("Automatic removal interval set to: " + (newInterval + 1));
				sender.addChatMessage(chat);
			}
		}

	}
	
	public static void doClear()
	{
		EntityItem item;
		Entity entity;
		int itemsRemoved =0;
		int entitiesRemoved = 0;
		chunksLoaded = 0;
		for (World world : DimensionManager.getWorlds())
		{
			if (world == null)
				continue;
			for (Object obj  :  world.loadedEntityList)
			{
				if (obj instanceof EntityItem)
				{
					item = (EntityItem) obj;
					if (!config.isBlacklisted(item.getEntityItem().getItem()))
					{
						item.setDead();
						itemsRemoved++;
					}
						
				}
				else if (!(obj instanceof EntityPlayer))
				{
					entity = (Entity) obj;
					if (!config.isBlacklisted(entity))
					{
						entity.setDead();
						entitiesRemoved++;
					}
				}
			}
		}
		ChatComponentText chat = new ChatComponentText("Lag'B'Gon has removed " + itemsRemoved + " items and ");
		chat.appendText(entitiesRemoved + " entities");
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(chat);
		chat = new ChatComponentText("There are " + chunksLoaded + " chunks loaded");
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(chat);
	}
	

	@Override
	public int getRequiredPermissionLevel() {
		// TODO Auto-generated method stub
		return 2;
	}

	
	
	private long mean(long num[])
	{
		long val = 0;
		for (long n : num)
		{
			val+=n;
		}
		return val/num.length;
	}
	
	private void checkTPS()
	{
		double meanTickTime = mean(MinecraftServer.getServer().tickTimeArray) * 1.0E-6D;
        double meanTPS = Math.min(1000.0/meanTickTime, 20);
        if (nextUnload < System.currentTimeMillis())
        {
        	if (meanTPS < ConfigManager.TPSForUnload)
        	{
        		chunksLoaded = 0;
        		for (World world : DimensionManager.getWorlds())
        		{
        			chunksLoaded += world.provider.worldObj.getChunkProvider().getLoadedChunkCount();
        			world.provider.worldObj.getChunkProvider().unloadQueuedChunks();
        			System.out.println(world.provider.worldObj.getChunkProvider().getLoadedChunkCount()+ " in " + world.provider.dimensionId);
        		}
        		nextUnload = (long)( System.currentTimeMillis() + (ConfigManager.timeInterval * 1000 * 60));
        	}
        }
	}
	

}
