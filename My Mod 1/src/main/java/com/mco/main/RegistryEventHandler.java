package com.mco.main;

import com.mco.TUOM;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = TUOM.MODID)
public class RegistryEventHandler 
{

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) 
	{
		TUOMItems.registerItems(event.getRegistry());
	}

}