package com.gmail.zendarva.lagbgon;

import java.util.ArrayList;
import java.util.Collections;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class ConfigManager {

	public static ArrayList<String> entityBlacklist;
	public static ArrayList<String> itemBlacklist;
	
	public static ArrayList<Item> itemsBlackList;
	
	public static int timeInterval;
	
	public static boolean automaticRemoval;
	public static int TPSForUnload;
	
	
	private static ConfigManager myInstance; 
	
	private Configuration config;
	
	private ConfigManager()
	{
		entityBlacklist = new ArrayList<String>();
		itemBlacklist = new ArrayList<String>();
		itemsBlackList = new ArrayList<Item>();
	}
	
	public void Init(FMLPreInitializationEvent event)
	{
		config = new Configuration(event.getSuggestedConfigurationFile());
		
	}
	
	public static ConfigManager instance()
	{
		if (myInstance == null)
		{
			myInstance = new ConfigManager();
		
		}
		return myInstance;
	}
	
	
	public void Load()
	{
		config.load();
		
		Property prop;
		String[] entityDefaultList = {"Cow",};
		prop = config.get(Configuration.CATEGORY_GENERAL,"EntityBlackList", entityDefaultList );
		prop.comment ="List of Entities not to destroy.";
		
		Collections.addAll(entityBlacklist, prop.getStringList());
			
		String[] itemDefaultList = {"minecraft:diamond",};
		prop = config.get(Configuration.CATEGORY_GENERAL, "ItemBlackList", itemDefaultList);
		prop.comment = "List of Items not to destroy";
		itemBlacklist = new ArrayList<String>();
		Collections.addAll(itemBlacklist, prop.getStringList());
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Interval", 15);
		prop.comment = "Interval between clearing entities in minutes.";
		timeInterval = prop.getInt();
		
		automaticRemoval = config.get(Configuration.CATEGORY_GENERAL, "AutomaticRemoval", true).getBoolean();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "TPSForUnload", 12);
		prop.comment = "If the server's main TPS drops below this number, \n Lag'B'Gon will try to unload chunks to improve TPS";
		TPSForUnload = prop.getInt();
		
		config.save();
		updateBlacklist();
	}
	
	private void updateBlacklist()
	{
		itemsBlackList.clear();
		for (String str : itemBlacklist)
		{
			itemsBlackList.add((Item) Item.itemRegistry.getObject(str));
		}
	}
	
	public void toggleAuto()
	{
		if (automaticRemoval)
		{
			automaticRemoval = false;
		}
		else
			automaticRemoval = true;
		Save();
	}
	
	public void changeInterval(int newInterval)
	{
		timeInterval = newInterval;
		Save();
	}
	public void changeTPSForUnload(int newTPS)
	{
		TPSForUnload = newTPS;
		Save();
	}
	
	public void toggleItem(Item item)
	{
		if (itemsBlackList.contains(item))
		{
			itemBlacklist.remove(Item.itemRegistry.getNameForObject(item));
			itemsBlackList.remove(item);
		}
		else
		{
			itemBlacklist.add(Item.itemRegistry.getNameForObject(item));
			itemsBlackList.add(item);		}
		Save();
	}
	public void toggleEntity(String name)
	{
		if (entityBlacklist.contains(name))
		{
			entityBlacklist.remove(name);
		}
		else
		{
			entityBlacklist.add(name);
		}
		Save();
	}
	
	
	public boolean isBlacklisted(Item item)
	{
		return( itemsBlackList.contains(item));
	}
	
	public boolean isBlacklisted(Entity entity)
	{
		return entityBlacklist.contains(entity.getCommandSenderName());
	}
	public boolean isBlacklisted(String name)
	{
		if (entityBlacklist.contains(name) || itemBlacklist.contains(name))
			return true;
		return false;
	}
	
	private void Save()
	{
		Property prop;
		prop = config.get(Configuration.CATEGORY_GENERAL, "EntityBlackList", "");
		prop.set(entityBlacklist.toArray(new String[entityBlacklist.size()]));
		prop.comment ="List of Entities not to destroy.";
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "ItemBlackList", "");
		prop.set(itemBlacklist.toArray(new String[entityBlacklist.size()]));
		prop.comment = "List of Items not to destroy";
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "Interval", 0);
		prop.set(timeInterval);
		prop.comment = "Interval between clearing entities in minutes.";
		
		config.get(Configuration.CATEGORY_GENERAL, "AutomaticRemoval", true).set(automaticRemoval);
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "TPSForUnload", 12);
		prop.set(TPSForUnload);
		prop.comment = "If the server's main TPS drops below this number, \n Lag'B'Gon will try to unload chunks to improve TPS";

		
		config.save();
	}
	
}
