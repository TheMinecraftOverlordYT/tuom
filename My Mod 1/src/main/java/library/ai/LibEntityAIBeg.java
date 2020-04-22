package library.ai;

import library.entities.mobs.entities.LibEntityWolf;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class LibEntityAIBeg extends EntityAIBase {
    private final LibEntityWolf wolf;
    private EntityPlayer player;
    private final World world;
    private final float minPlayerDistance;
    private int timeoutCounter;

    public LibEntityAIBeg(LibEntityWolf wolf, float minDistance) {
        this.wolf = wolf;
        this.world = wolf.world;
        this.minPlayerDistance = minDistance;
        this.setMutexBits(2);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute() {
        this.player = this.world.getClosestPlayerToEntity(this.wolf, (double) this.minPlayerDistance);
        return this.player == null ? false : this.hasTemptationItemInHand(this.player);
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean shouldContinueExecuting() {
        if (!this.player.isEntityAlive()) {
            return false;
        } else if (this.wolf.getDistanceSq(this.player) > (double) (this.minPlayerDistance * this.minPlayerDistance)) {
            return false;
        } else {
            return this.timeoutCounter > 0 && this.hasTemptationItemInHand(this.player);
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting() {
        this.wolf.setBegging(true);
        this.timeoutCounter = 40 + this.wolf.getRNG().nextInt(40);
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    @Override
    public void resetTask() {
        this.wolf.setBegging(false);
        this.player = null;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    @Override
    public void updateTask() {
        this.wolf.getLookHelper().setLookPosition(this.player.posX, this.player.posY + (double) this.player.getEyeHeight(), this.player.posZ, 10.0F, (float) this.wolf.getVerticalFaceSpeed());
        --this.timeoutCounter;
    }

    /**
     * Gets if the Player has the Bone in the hand.
     */
    private boolean hasTemptationItemInHand(EntityPlayer player) {
        for (EnumHand enumhand : EnumHand.values()) {
            ItemStack itemstack = player.getHeldItem(enumhand);

            if (this.wolf.isTamed() && itemstack.getItem() == Items.BONE) {
                return true;
            }

            if (this.wolf.isBreedingItem(itemstack)) {
                return true;
            }
        }

        return false;
    }
}