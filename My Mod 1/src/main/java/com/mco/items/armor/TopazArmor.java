package com.mco.items.armor;

import library.items.LibItemArmor;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class TopazArmor extends LibItemArmor {

	public TopazArmor(ArmorMaterial materialIn, EntityEquipmentSlot equipmentSlotIn) {
		super(materialIn, equipmentSlotIn);
	}

	@Override
	protected String getArmorWrapTexture() {
		return "topaz_armor";
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
		if (!player.isSpectator()) {
			int i = MathHelper.floor(player.posX);
			int j = MathHelper.floor(player.posY);
			int k = MathHelper.floor(player.posZ);

			NonNullList<ItemStack> armor = player.inventory.armorInventory;
			int armorPieces = 0;

			for (ItemStack itemArmor : armor) {
				if (itemArmor != null && itemArmor.getItem() instanceof TopazArmor) {
					armorPieces += 1;
				}
			}

		//	if(armorPieces >= 4)
			//	freezeNearby(player, world, player.getPosition(), 0);
		}
	}

	public static void freezeNearby(EntityLivingBase living, World worldIn, BlockPos pos, int level) {
		if (living.onGround)
		{
			float f = (float)Math.min(16, 2 + level);
			BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(0, 0, 0);

			for (BlockPos.MutableBlockPos blockpos$mutableblockpos1 : BlockPos.getAllInBoxMutable(pos.add((double)(-f), -1.0D, (double)(-f)), pos.add((double)f, -1.0D, (double)f)))
			{
				if (blockpos$mutableblockpos1.distanceSqToCenter(living.posX, living.posY, living.posZ) <= (double)(f * f))
				{
					blockpos$mutableblockpos.setPos(blockpos$mutableblockpos1.getX(), blockpos$mutableblockpos1.getY() + 1, blockpos$mutableblockpos1.getZ());
					IBlockState iblockstate = worldIn.getBlockState(blockpos$mutableblockpos);

					if (iblockstate.getMaterial() == Material.AIR)
					{
						IBlockState iblockstate1 = worldIn.getBlockState(blockpos$mutableblockpos1);

						if (iblockstate1.getMaterial() == Material.WATER && (iblockstate1.getBlock() == net.minecraft.init.Blocks.WATER || iblockstate1.getBlock() == net.minecraft.init.Blocks.FLOWING_WATER) && ((Integer)iblockstate1.getValue(BlockLiquid.LEVEL)).intValue() == 0 && worldIn.mayPlace(Blocks.ICE, blockpos$mutableblockpos1, false, EnumFacing.DOWN, (Entity)null))
						{
							worldIn.setBlockState(blockpos$mutableblockpos1, Blocks.ICE.getDefaultState());
							worldIn.scheduleUpdate(blockpos$mutableblockpos1.toImmutable(), Blocks.ICE, MathHelper.getInt(living.getRNG(), 60, 120));
						}
					}
				}
			}
		}
	}
}
