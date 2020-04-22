package com.mco.entities.projectiles;

import java.util.Iterator;
import java.util.List;

import com.mco.entities.mobs.dark.demon.EntityDarkOpalDemon;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * An enhanced Falling Block
 *
 * @author gummby8
 * He's no longer modding, but check
 * out his mod: Modular Bosses, it's 
 * super cool 
 */

public class EntityCustomFallingBlock extends Entity implements IEntityAdditionalSpawnData{

	public IBlockState fallTile;
    public int fallTime;
    public boolean shouldDropItem = true;
    private boolean field_145808_f;
    private boolean hurtEntities;
    private int fallHurtMax = 40;
    private float fallHurtAmount = 2.0F;
    public NBTTagCompound tileEntityData;
    private static final String __OBFID = "CL_00001668";
    public BlockPos bPos;
	public int damage;
	public Entity shooter;
    protected static final DataParameter<BlockPos> ORIGIN = EntityDataManager.<BlockPos>createKey(EntityCustomFallingBlock.class, DataSerializers.BLOCK_POS);

    public EntityCustomFallingBlock(World worldIn)
    {
        super(worldIn);
    }

    public EntityCustomFallingBlock(World worldIn, Entity shooter, double x, double y, double z, double mY, float yaw, BlockPos pos, int dmg)
    {
        super(worldIn);
        this.bPos = pos;
        this.fallTile = this.world.getBlockState(pos);
        this.preventEntitySpawning = true;
        this.setSize(1F, 1F);
        this.setPositionAndRotation(x, y, z, yaw, 0);
        this.motionX = 0.0D;
        this.motionY = mY;
        this.motionZ = 0.0D;
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.noClip = true;
        this.damage = dmg;
        this.shooter = shooter;
        
    } 

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        this.motionY -= 0.03999999910593033D;
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;
        
        if (this.ticksExisted > 20){this.setDead();}
       
        this.collideWithEntities(this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox()));
    }
       
    /**
     * Pushes all entities inside the list away from the entity.
     */
    private void collideWithEntities(List list)
    {
        double d0 = (this.getEntityBoundingBox().minX + this.getEntityBoundingBox().maxX) / 2.0D;
        double d1 = (this.getEntityBoundingBox().minZ + this.getEntityBoundingBox().maxZ) / 2.0D;
        Iterator iterator = list.iterator();

        while (iterator.hasNext())
        {
            Entity entity = (Entity)iterator.next();


            	
                double d2 = entity.posX - d0;
                double d3 = entity.posZ - d1;
                double d4 = d2 * d2 + d3 * d3;
                
                if (entity.hurtResistantTime == 0 && !(entity instanceof EntityCustomFallingBlock) && !(entity instanceof EntityDarkOpalDemon) && !(entity instanceof EntityGolem)){
                entity.addVelocity(d2 / d4 * 0.2D, 1.2D, d3 / d4 * 0.2D);
                entity.attackEntityFrom(DamageSource.FALL, damage);
                entity.hurtResistantTime = 10;
                }
            
        }
    }
    
    @SideOnly(Side.CLIENT)
    public World getWorldObj()
    {
        return this.world;
    }


    public IBlockState getBlock()
    {
        return this.fallTile;
    }

	@Override
	protected void entityInit() {
        this.dataManager.register(ORIGIN, BlockPos.ORIGIN);		
	}

	  /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    protected void writeEntityToNBT(NBTTagCompound tagCompound){}


    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readEntityFromNBT(NBTTagCompound tagCompund){}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		
		
		if (this.bPos != null){
		buffer.writeInt(this.bPos.getX());
		buffer.writeInt(this.bPos.getY());
		buffer.writeInt(this.bPos.getZ());
		
		buffer.writeDouble(this.motionY);
		buffer.writeFloat(this.rotationYaw);
		}
		
	}

	@Override
	public void readSpawnData(ByteBuf additionalData) {
		int x = additionalData.readInt();
		int y = additionalData.readInt();
		int z = additionalData.readInt();
				
		this.bPos = new BlockPos(x,y,z);
		this.fallTile = this.getWorldObj().getBlockState(this.bPos);
		
        if (this.fallTile.getBlock() instanceof BlockStaticLiquid){ //kills the block if it is a liquid, liquid blocks don't show textures. 
        	this.setDead();
        	return;
        }
		
		this.motionY = additionalData.readDouble();
		this.noClip = true;
		this.rotationYaw = additionalData.readFloat();
	}
	
	public void setOrigin(BlockPos p_184530_1_)
    {
        this.dataManager.set(ORIGIN, p_184530_1_);
    }

    @SideOnly(Side.CLIENT)
    public BlockPos getOrigin()
    {
        return (BlockPos)this.dataManager.get(ORIGIN);
    }
    
}
