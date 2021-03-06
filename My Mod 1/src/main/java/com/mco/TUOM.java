package com.mco;

import com.mco.dimensions.TUOMWorldGen;
import com.mco.main.TUOMItems;
import com.mco.potions.TUOMPotions;
import com.mco.proxies.IProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * MOD INFO =================================================
 */
@Mod(modid = "tuom", name = "The Ultimate Ore Mod", version = "0.2.0",
	dependencies = "required-after:llibrary@[1.7.14,)")

/**
 * DECLARE =================================================
 */
public class TUOM {

	// ID
	public static final String MODID = "tuom";
	
	public static final int GUI_OPAL_FUSER = 0;

	// Creative Tabs
	public static CreativeTabs tuom_tab, dim_tab;
		
	public static ResourceLocation DARK_CHEST_DEMON, DARK_CHEST_LAVA, DARK_CHEST_TOP;
	
	public static final Logger TUOM_LOG = LogManager.getLogger(MODID);
	
	private static Logger LOGGER = LogManager.getLogger();

	/**
	 * PROXIES =================================================
	 */
	@Mod.Instance
	private static TUOM instance;

	public static TUOM instance() 
	{
		return instance;
	}
	
	@SidedProxy(clientSide = "com.mco.proxies.ClientProxy", serverSide = "com.mco.proxies.ServerProxy")
	public static IProxy proxy;

	/**
	 * REGISTER =================================================
	 */
	
	/**
	 * Run before anything else. <s>Read your config, create blocks, items, etc, and register them with the GameRegistry</s>
	 *
	 * @param event the event
	 * @see ForgeModContainer#preInit(FMLPreInitializationEvent)
	 */
	@EventHandler
	public void preInit(final FMLPreInitializationEvent event) 
	{
		LOGGER.debug("preInit");
		proxy.logPhysicalSide(TUOM_LOG);

	//	PacketHandler.registerMessages("tuom");
		
		// Creative Tabs
		tuom_tab = new CreativeTabs("tuom_tab") 
		{
			@Override
			public ItemStack getTabIconItem() 
			{
				return new ItemStack(TUOMItems.ITEM_TOPAZ);
			}
		};
		
		dim_tab = new CreativeTabs("dim_tab") 
		{
			@Override
			public ItemStack getTabIconItem()
			{
				return new ItemStack(TUOMItems.DARK_TELEPORTER);
			}
		};
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) 
	{
		MinecraftForge.EVENT_BUS.register(new TUOMPotions());

    	DARK_CHEST_DEMON = LootTableList.register(new ResourceLocation(MODID, "chests/dark_tower_demon"));
    	DARK_CHEST_LAVA = LootTableList.register(new ResourceLocation(MODID, "chests/dark_tower_lava"));
    	DARK_CHEST_TOP = LootTableList.register(new ResourceLocation(MODID, "chests/dark_tower_top"));
    	
		TUOMWorldGen.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) 
	{}
}
