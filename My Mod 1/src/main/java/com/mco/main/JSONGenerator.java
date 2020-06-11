package com.mco.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

/**
 * @author TheMCO
 * Why spend 1 hour doing something when you could spend 5 automating it?
 */
public class JSONGenerator {

	private static OutputStreamWriter itemJson, blockJson, blockStateJson;
	private static final String directory = "C:/Users/kyure/git/tuom/My Mod 1/src/main/resources/assets/tuom/";
	
	private static final ArrayList<Item> ITEM_BLACKLIST = new ArrayList<Item>() {
		{
			add(TUOMItems.TOPAZ_BOW);
			add(TUOMItems.DARK_STAFF);
		}
	};
	
	private static final ArrayList<Block> BLOCK_BLACKLIST = new ArrayList<Block>() {
		{
			add(TUOMBlocks.DARK_LEAVES);
			add(TUOMBlocks.DARK_LOG);
			add(TUOMBlocks.DARK_SAPLING);
			add(TUOMBlocks.OPAL_FUSER);
			add(TUOMBlocks.DARK_FURNACE);
			add(TUOMBlocks.DARK_PLANKS);
			add(TUOMBlocks.DOPAL_CROP);
			add(TUOMBlocks.FOPAL_CROP);
			add(TUOMBlocks.LOPAL_CROP);
			add(TUOMBlocks.TOPAZ_CROP);
		}
	};

	public static void generateJsonItem(ArrayList<Item> items) throws IOException
	{
		for(Item item : items) 
		{
			File file = new File(directory + "models/item/" + item.getUnlocalizedName().substring(5) + ".json");
			
			if(!ITEM_BLACKLIST.contains(item) && !file.exists())
				createItemFile(item);
		}
	}
	
	public static void generateJsonBlock(ArrayList<Block> blocks) throws IOException
	{
		for(Block block : blocks)
		{
			File file = new File(directory + "models/block" + block.getUnlocalizedName().substring(5) + ".json");
			File blockstate = new File(directory + "blockstates" + block.getUnlocalizedName().substring(5) + ".json");
			
			if(!BLOCK_BLACKLIST.contains(block) && !file.exists())
				createBlockFile(block);
			
			if(!BLOCK_BLACKLIST.contains(block) && !blockstate.exists())
				createBlockState(block);
		}
	}
	
	private static void createItemFile(Item item) throws FileNotFoundException
	{
		try {
			itemJson = new FileWriter(directory + "models/item/" + item.getUnlocalizedName().substring(5) + ".json");			
			itemJson.write("{\n");
			itemJson.write("	\"parent\": \"item/generated\",\n");
			itemJson.write("	\"textures\": {\n");
			itemJson.write("		\"layer0\": \"tuom:items/" + item.getUnlocalizedName().substring(5) + "\"" +"\n");
			itemJson.write("	}\n");
			itemJson.write("}");
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				itemJson.flush();
				itemJson.close();
			}
			catch(IOException e) {
				e.printStackTrace();
			}	
		}
	}
	
	private static void createBlockFile(Block block) throws FileNotFoundException
	{
		try {
			blockJson = new FileWriter(directory + "models/block/" + block.getUnlocalizedName().substring(5) + ".json");			
			blockJson.write("{\n");
			blockJson.write("	\"parent\": \"block/cube_all\",\n");
			blockJson.write("	\"textures\": {\n");
			blockJson.write("		\"all\": \"tuom:blocks/" + block.getUnlocalizedName().substring(5) + "\"" +"\n");
			blockJson.write("	}\n");
			blockJson.write("}");
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				blockJson.flush();
				blockJson.close();
			}
			catch(IOException e) {
				e.printStackTrace();
			}	
		}
	}
	
	private static void createBlockState(Block block) throws FileNotFoundException
	{
		try {
			blockStateJson = new FileWriter(directory + "blockstates/" + block.getUnlocalizedName().substring(5) + ".json");
			blockStateJson.write("{\n");
			blockStateJson.write("	\"forge_marker\": 1,\n");
			blockStateJson.write("	\"defaults\": {\n");
			blockStateJson.write("		\"model\": \"cube_all\",\n");
			blockStateJson.write("		\"textures\": {\n");
			blockStateJson.write("			\"all\": \"tuom:blocks/" + block.getUnlocalizedName().substring(5) +"\"\n");
			blockStateJson.write("		}\n");
			blockStateJson.write("	},\n");
			blockStateJson.write("	\"variants\": {\n");
			blockStateJson.write("		\"normal\": [\n");
			blockStateJson.write("			{}\n");
			blockStateJson.write("		],\n");
			blockStateJson.write("		\"inventory\": [\n");
			blockStateJson.write("			{}\n");
			blockStateJson.write("		]\n");
			blockStateJson.write("	}\n");
			blockStateJson.write("}\n");
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				blockStateJson.flush();
				blockStateJson.close();
			}
			catch(IOException e) {
				e.printStackTrace();
			}			
		}
	}
}