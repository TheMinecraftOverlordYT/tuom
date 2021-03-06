package com.mco.entities.mobs.dark.demon.corrupted;

import javax.annotation.Nullable;

import library.entities.LibEntityMob;
import net.minecraft.block.Block;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityCorruptedChicken extends LibEntityMob<LibEntityMob>
{
    public float wingRotation;
    public float destPos;
    public float oFlapSpeed;
    public float oFlap;
    public float wingRotDelta = 1.0F;
	
	public EntityCorruptedChicken(World world) 
	{
		super(world);
		tasks.addTask(0, new EntityAIAttackMelee(this, 1, true));
		tasks.addTask(1, new EntityAIWatchClosest(this, EntityPlayer.class, 20));
        this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 24.0F));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
	}

	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.50000000298023224D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.5);
	}
	
	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();

		 this.oFlap = this.wingRotation;
	        this.oFlapSpeed = this.destPos;
	        this.destPos = (float)((double)this.destPos + (double)(this.onGround ? -1 : 4) * 0.3D);
	        this.destPos = MathHelper.clamp(this.destPos, 0.0F, 1.0F);

	        if (!this.onGround && this.wingRotDelta < 1.0F)
	        {
	            this.wingRotDelta = 1.0F;
	        }

	        this.wingRotDelta = (float)((double)this.wingRotDelta * 0.9D);

	        if (!this.onGround && this.motionY < 0.0D)
	        {
	            this.motionY *= 0.6D;
	        }

	        this.wingRotation += this.wingRotDelta * 2.0F;

	}

    public void fall(float distance, float damageMultiplier)
    {
    }
	
	protected SoundEvent getAmbientSound()
    {
        return SoundEvents.ENTITY_CHICKEN_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
        return SoundEvents.ENTITY_CHICKEN_HURT;
    }

    protected SoundEvent getDeathSound()
    {
        return SoundEvents.ENTITY_CHICKEN_DEATH;
    }

    protected void playStepSound(BlockPos pos, Block blockIn)
    {
        this.playSound(SoundEvents.ENTITY_CHICKEN_STEP, 0.15F, 1.0F);
    }

	
	@Nullable
    protected ResourceLocation getLootTable()
    {
        return LootTableList.ENTITIES_CHICKEN;
    }
	
}
