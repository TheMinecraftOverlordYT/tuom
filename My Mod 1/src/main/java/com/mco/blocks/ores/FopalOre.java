package com.mco.blocks.ores;

import com.mco.TUOM;
import com.mco.main.TUOMItems;
import library.LibRegistry;
import library.blocks.LibBlockOre;

public class FopalOre extends LibBlockOre 
{

	public FopalOre(String registryName, String harvestTool, int harvestLevel) 
	{
		super(registryName, harvestTool, harvestLevel);
		this.setCreativeTab(TUOM.tuom_tab);
		this.setHardness(10F);
		this.setLightLevel(.5F);
		this.setResistance(20F);
	}

	@Override
	public void initRecipe() 
	{
		LibRegistry.addSmeltingRecipe(TUOMItems.FIRE_OPAL, 1, 10, this);
	}

	
}
