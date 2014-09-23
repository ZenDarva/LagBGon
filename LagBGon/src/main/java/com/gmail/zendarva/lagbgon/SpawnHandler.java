package com.gmail.zendarva.lagbgon;


import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class SpawnHandler {

	@SubscribeEvent
	public void perChunkLimiter(LivingSpawnEvent.CheckSpawn event)
	{
		
		
	}
	
	@SubscribeEvent
	public void checkCrowding(EntityJoinWorldEvent e)
	{
		
	
		if (ConfigManager.perChunkSpawnLimit > 0 && e.entity instanceof EntityLiving) {

			Chunk chunk = e.world.getChunkFromBlockCoords((int) e.entity.posX, (int)e.entity.posZ);

			int count = 0;

			for (List list : chunk.entityLists) {
				count += list.size();
			}
			System.out.println("Amount: " + count);
			if (count >= ConfigManager.perChunkSpawnLimit) {
				e.setCanceled(true);
				System.out.println("Denied!!");
				return;
			}
			System.out.println("Allowed");

		}		
		
		AxisAlignedBB bb;
		Entity ent = e.entity;
		EntityAgeable eA;
		if (!ConfigManager.policeCrowd)
			return;
		if (e.entity instanceof EntityAgeable)
		{
			eA= (EntityAgeable) ent;
			if (eA.getGrowingAge() < 0)
			{
				bb = AxisAlignedBB.getBoundingBox(ent.posX -5, ent.posY-5, ent.posZ-5, ent.posX+5,ent.posY+5,ent.posZ+5);
				int amt = e.entity.worldObj.getEntitiesWithinAABB(EntityAgeable.class, bb).size();
				if (amt > ConfigManager.crowdLimit)
				{
					e.setCanceled(true);
				}
			}
		}
			
	}
}
