package com.gmail.zendarva.lagbgon;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class SpawnHandler {

	
	@SubscribeEvent
	public void checkCrowding(EntityJoinWorldEvent e)
	{
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
