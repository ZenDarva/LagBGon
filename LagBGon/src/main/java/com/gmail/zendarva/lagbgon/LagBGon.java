package com.gmail.zendarva.lagbgon;



import java.util.Map;

import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;

@Mod(name = "Lag'B'Gon", modid = LagBGon.MODID, version = LagBGon.VERSION)
public class LagBGon
{
    public static final String MODID = "LagBGon";
    public static final String VERSION = ".1.2";
    
    @Mod.Instance("lagbgon")
    public static LagBGon instance;
    
    @SidedProxy(clientSide = "com.gmail.zendarva.lagbgon.ClientProxy", serverSide = "com.gmail.zendarva.lagbgon.CommonProxy")
    public static CommonProxy proxy;
    
    public static TickHandler tickHandler;
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	
    	
    }
    
    @NetworkCheckHandler
    public boolean checkMods(Map<String, String> mods, Side side)
    {
    	
    	//Look here later for client side gui config checking.
    	return true;
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	ConfigManager.instance().Init(event);
    	ConfigManager.instance().Load();
		tickHandler = new TickHandler();
   		//MinecraftForge.EVENT_BUS.register(tickHandler);
		FMLCommonHandler.instance().bus().register(tickHandler);
		MinecraftForge.EVENT_BUS.register(new SpawnHandler());
    }
    
    @EventHandler
    public void serverStart(FMLServerStartingEvent event)
    {
             MinecraftServer server = MinecraftServer.getServer();
             ((ServerCommandManager)(server.getCommandManager())).registerCommand(new MainCommand());
    }
}
