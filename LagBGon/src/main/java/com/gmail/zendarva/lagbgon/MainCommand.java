package com.gmail.zendarva.lagbgon;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;

public class MainCommand extends CommandBase {

	private static ConfigManager config = ConfigManager.instance();
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
		
		
		switch (args.length)
		{
		case 0:
			chat = new ChatComponentText("/bgon toggleitem : toggles the blacklist status of held item");
			sender.addChatMessage(chat);
			chat = new ChatComponentText("/bgon toggleentitiy <name>: toggles the blacklist status of the named entity");
			sender.addChatMessage(chat);
			chat = new ChatComponentText("/bgon clear : Clears all items/entities from the world not on blacklist");
			sender.addChatMessage(chat);
			chat = new ChatComponentText("/bgon interval <minutes> : sets the interval for automatic running of /bgon clear" );
			sender.addChatMessage(chat);
			chat = new ChatComponentText("/bgon toggleauto : Toggles automatic clearing of entities, and unloading of chunks.");
			sender.addChatMessage(chat);
			chat = new ChatComponentText("/bgon listitems : Lists the items in the blacklist.");
			sender.addChatMessage(chat);
			chat = new ChatComponentText("/bgon listentities : Lists the entities in the blacklist.");
			sender.addChatMessage(chat);
			chat = new ChatComponentText("/bgon settps <target tps> : Sets the maximum TPS for unloading chunks.");
			sender.addChatMessage(chat);
			chat = new ChatComponentText("/bgon unload : Unloads unused chunks.");
			sender.addChatMessage(chat);

			break;
		case 1:
			if (args[0].equals("unload"))
			{
				unloadChunks();
			}
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
			
			if (args[0].equals("listentities"))
			{
				StringBuilder line = new StringBuilder();
				chat = new ChatComponentText("Entity Blacklist contains:");
				sender.addChatMessage(chat);
				for (String item : ConfigManager.entityBlacklist)
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
			
			if (args[0].equals("settps"))
			{
				int newTPS = Integer.parseInt(args[1]);
				config.changeTPSForUnload(newTPS);
				chat = new ChatComponentText("New TPS minimum set to: " + newTPS);
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
		ArrayList<Object> toRemove = new ArrayList<Object>();
		for (World world : DimensionManager.getWorlds())
		{
			if (world == null)
				continue;
			if (world.isRemote)
			{
				System.out.println("How?!?");
			}
			//Seriously? I'm passing you to an object.  Who the hell cares?!?
			@SuppressWarnings("unchecked")
			Iterator<Object> iter = world.loadedEntityList.iterator();
			Object obj;
			while (iter.hasNext())
			{
				obj = iter.next();
				if (obj instanceof EntityItem)
				{
					item = (EntityItem) obj;
					if (!config.isBlacklisted(item.getEntityItem().getItem()))
					{
						toRemove.add(item);
						itemsRemoved++;
					}
						
				}
				else if (!(obj instanceof EntityPlayer))
				{
					entity = (Entity) obj;
					if (!config.isBlacklisted(entity))
					{
						toRemove.add(entity);
						entitiesRemoved++;
					}
				}
			}
			for (Object o : toRemove)
			{
					((Entity)o).setDead();
			}
			toRemove.clear();
		}
		ChatComponentText chat = new ChatComponentText("Lag'B'Gon has removed " + itemsRemoved + " items and ");
		chat.appendText(entitiesRemoved + " entities");
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(chat);
	}
	

	@Override
	public int getRequiredPermissionLevel() {
		// TODO Auto-generated method stub
		return 2;
	}

	
	
	private static long mean(long num[])
	{
		long val = 0;
		for (long n : num)
		{
			val+=n;
		}
		return val/num.length;
	}
	
	private static boolean unloadChunks()
	{
		
        ChunkProviderServer cPS;
        int oldChunksLoaded;
        int newChunksLoaded;
        Chunk chunk;
        EntityPlayerMP player;
        boolean unloadSafe = true;

		
		oldChunksLoaded = 0;
		newChunksLoaded = 0;
		for (WorldServer world : DimensionManager.getWorlds())
		{
			oldChunksLoaded += world.getChunkProvider().getLoadedChunkCount();
			if (world.getChunkProvider() instanceof ChunkProviderServer)
			{
				cPS = (ChunkProviderServer) world.getChunkProvider();
				
				Iterator iter = cPS.loadedChunks.iterator();
				
				while (iter.hasNext())
				{
					chunk = (Chunk) iter.next();
					unloadSafe = true;
					for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
					{
						player = (EntityPlayerMP) obj;
						
						if ((player.chunkCoordX == chunk.xPosition && player.chunkCoordZ == chunk.zPosition))
						{
							unloadSafe = false;
						}
					}
					if (unloadSafe)
						cPS.unloadChunksIfNotNearSpawn(chunk.xPosition, chunk.zPosition);
						
				}
				cPS.unloadQueuedChunks();
				
			}
			newChunksLoaded +=world.getChunkProvider().getLoadedChunkCount();
		}
		nextUnload = (long)( System.currentTimeMillis() + ((new Random().nextInt(3)+1) * 1000 * 60));
		ChatComponentText chat = new ChatComponentText((oldChunksLoaded - newChunksLoaded) + " chunks unloaded by Lag'B'Gon.");
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(chat);
		
		
		for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
		{
			player = (EntityPlayerMP) obj;
			
			
		}
			return true;
		
	}
	
	public static boolean checkTPS()
	{
		
		double meanTickTime = mean(MinecraftServer.getServer().tickTimeArray) * 1.0E-6D;
        double meanTPS = Math.min(1000.0/meanTickTime, 20);
        
        if (nextUnload < System.currentTimeMillis())
        {
        	if (meanTPS < ConfigManager.TPSForUnload)
        	{
        		unloadChunks();
        		return true;
        	}
        }
        return false;
	}
	
}
	
	
	