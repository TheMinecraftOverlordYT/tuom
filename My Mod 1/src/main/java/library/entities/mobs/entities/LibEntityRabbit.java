package library.entities.mobs.entities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCarrot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.*;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDesert;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class LibEntityRabbit extends LibEntityGenericPassive {

    /** COMMON VARIABLES **/

    // Sounds
    protected SoundEvent jumpSound = SoundEvents.ENTITY_RABBIT_JUMP;
    protected SoundEvent ambientSound = SoundEvents.ENTITY_RABBIT_AMBIENT;
    protected SoundEvent hurtSound = SoundEvents.ENTITY_RABBIT_HURT;
    protected SoundEvent deathSound = SoundEvents.ENTITY_RABBIT_DEATH;
    protected SoundEvent attackSound = SoundEvents.ENTITY_RABBIT_ATTACK;

    // Loot table
    protected ResourceLocation lootTable = LootTableList.ENTITIES_RABBIT;

    // Sets the Entity's attributes
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();

        double maxHealth = 3.0D; // max health
        double movementSpeed = 0.3D; // how fast it moves normally
        double attackDamage = 3D; // how many half-hearts of damage it deals

        this.registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(attackDamage);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealth);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(movementSpeed);
    }

    // Sets the Entity's core behaviors
    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(1, new LibEntityRabbit.AIPanic(this, 2.2D));
        this.tasks.addTask(2, new EntityAIMate(this, 0.8D));
        this.tasks.addTask(3, new EntityAITempt(this, 1.0D, Items.CARROT, false));
        this.tasks.addTask(3, new EntityAITempt(this, 1.0D, Items.GOLDEN_CARROT, false));
        this.tasks.addTask(3, new EntityAITempt(this, 1.0D, Item.getItemFromBlock(Blocks.YELLOW_FLOWER), false));
        this.tasks.addTask(4, new LibEntityRabbit.AIAvoidEntity(this, EntityPlayer.class, 8.0F, 2.2D, 2.2D));
        this.tasks.addTask(4, new LibEntityRabbit.AIAvoidEntity(this, EntityWolf.class, 10.0F, 2.2D, 2.2D));
        this.tasks.addTask(4, new LibEntityRabbit.AIAvoidEntity(this, EntityMob.class, 4.0F, 2.2D, 2.2D));
        this.tasks.addTask(5, new LibEntityRabbit.AIRaidFarm(this));
        this.tasks.addTask(6, new EntityAIWanderAvoidWater(this, 0.6D));
        this.tasks.addTask(11, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
    }

    protected SoundEvent getJumpSound() {
        return jumpSound;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ambientSound;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_184601_1_) {
        return hurtSound;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return deathSound;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return lootTable;
    }


    /** OTHER METHODS **/

    private static final DataParameter<Integer> RABBIT_TYPE = EntityDataManager.<Integer>createKey(LibEntityRabbit.class, DataSerializers.VARINT);
    private int jumpTicks;
    private int jumpDuration;
    private boolean wasOnGround;
    private int currentMoveTypeDuration;
    private int carrotTicks;

    public LibEntityRabbit(World worldIn) {
        super(worldIn);
        this.setSize(0.4F, 0.5F);
        this.jumpHelper = new LibEntityRabbit.RabbitJumpHelper(this);
        this.moveHelper = new LibEntityRabbit.RabbitMoveHelper(this);
        this.setMovementSpeed(0.0D);
    }

    @Override
    protected float getJumpUpwardsMotion() {
        if (!this.collidedHorizontally && (!this.moveHelper.isUpdating() || this.moveHelper.getY() <= this.posY + 0.5D)) {
            Path path = this.navigator.getPath();

            if (path != null && path.getCurrentPathIndex() < path.getCurrentPathLength()) {
                Vec3d vec3d = path.getPosition(this);

                if (vec3d.y > this.posY + 0.5D) {
                    return 0.5F;
                }
            }

            return this.moveHelper.getSpeed() <= 0.6D ? 0.2F : 0.3F;
        } else {
            return 0.5F;
        }
    }

    /**
     * Causes this entity to do an upwards motion (jumping).
     */
    @Override
    protected void jump() {
        super.jump();
        double d0 = this.moveHelper.getSpeed();

        if (d0 > 0.0D) {
            double d1 = this.motionX * this.motionX + this.motionZ * this.motionZ;

            if (d1 < 0.010000000000000002D) {
                this.moveRelative(0.0F, 0.0F, 1.0F, 0.1F);
            }
        }

        if (!this.world.isRemote) {
            this.world.setEntityState(this, (byte) 1);
        }
    }

    @SideOnly(Side.CLIENT)
    public float setJumpCompletion(float p_175521_1_) {
        return this.jumpDuration == 0 ? 0.0F : ((float) this.jumpTicks + p_175521_1_) / (float) this.jumpDuration;
    }

    public void setMovementSpeed(double newSpeed) {
        this.getNavigator().setSpeed(newSpeed);
        this.moveHelper.setMoveTo(this.moveHelper.getX(), this.moveHelper.getY(), this.moveHelper.getZ(), newSpeed);
    }

    @Override
    public void setJumping(boolean jumping) {
        super.setJumping(jumping);

        if (jumping) {
            this.playSound(this.getJumpSound(), this.getSoundVolume(), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 0.8F);
        }
    }

    public void startJumping() {
        this.setJumping(true);
        this.jumpDuration = 10;
        this.jumpTicks = 0;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(RABBIT_TYPE, Integer.valueOf(0));
    }

    @Override
    public void updateAITasks() {
        if (this.currentMoveTypeDuration > 0) {
            --this.currentMoveTypeDuration;
        }

        if (this.carrotTicks > 0) {
            this.carrotTicks -= this.rand.nextInt(3);

            if (this.carrotTicks < 0) {
                this.carrotTicks = 0;
            }
        }

        if (this.onGround) {
            if (!this.wasOnGround) {
                this.setJumping(false);
                this.checkLandingDelay();
            }

            if (this.getRabbitType() == 99 && this.currentMoveTypeDuration == 0) {
                EntityLivingBase entitylivingbase = this.getAttackTarget();

                if (entitylivingbase != null && this.getDistanceSq(entitylivingbase) < 16.0D) {
                    this.calculateRotationYaw(entitylivingbase.posX, entitylivingbase.posZ);
                    this.moveHelper.setMoveTo(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, this.moveHelper.getSpeed());
                    this.startJumping();
                    this.wasOnGround = true;
                }
            }

            LibEntityRabbit.RabbitJumpHelper entityrabbit$rabbitjumphelper = (LibEntityRabbit.RabbitJumpHelper) this.jumpHelper;

            if (!entityrabbit$rabbitjumphelper.getIsJumping()) {
                if (this.moveHelper.isUpdating() && this.currentMoveTypeDuration == 0) {
                    Path path = this.navigator.getPath();
                    Vec3d vec3d = new Vec3d(this.moveHelper.getX(), this.moveHelper.getY(), this.moveHelper.getZ());

                    if (path != null && path.getCurrentPathIndex() < path.getCurrentPathLength()) {
                        vec3d = path.getPosition(this);
                    }

                    this.calculateRotationYaw(vec3d.x, vec3d.z);
                    this.startJumping();
                }
            } else if (!entityrabbit$rabbitjumphelper.canJump()) {
                this.enableJumpControl();
            }
        }

        this.wasOnGround = this.onGround;
    }

    /**
     * Attempts to create sprinting particles if the entity is sprinting and not in water.
     */
    @Override
    public void spawnRunningParticles() {
    }

    private void calculateRotationYaw(double x, double z) {
        this.rotationYaw = (float) (MathHelper.atan2(z - this.posZ, x - this.posX) * (180D / Math.PI)) - 90.0F;
    }

    private void enableJumpControl() {
        ((LibEntityRabbit.RabbitJumpHelper) this.jumpHelper).setCanJump(true);
    }

    private void disableJumpControl() {
        ((LibEntityRabbit.RabbitJumpHelper) this.jumpHelper).setCanJump(false);
    }

    private void updateMoveTypeDuration() {
        if (this.moveHelper.getSpeed() < 2.2D) {
            this.currentMoveTypeDuration = 10;
        } else {
            this.currentMoveTypeDuration = 1;
        }
    }

    private void checkLandingDelay() {
        this.updateMoveTypeDuration();
        this.disableJumpControl();
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (this.jumpTicks != this.jumpDuration) {
            ++this.jumpTicks;
        } else if (this.jumpDuration != 0) {
            this.jumpTicks = 0;
            this.jumpDuration = 0;
            this.setJumping(false);
        }
    }

    public static void registerFixesRabbit(DataFixer fixer) {
        EntityLiving.registerFixesMob(fixer, LibEntityRabbit.class);
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("RabbitType", this.getRabbitType());
        compound.setInteger("MoreCarrotTicks", this.carrotTicks);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setRabbitType(compound.getInteger("RabbitType"));
        this.carrotTicks = compound.getInteger("MoreCarrotTicks");
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.getRabbitType() == 99) {
            this.playSound(attackSound, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            return entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 8.0F);
        } else {
            return entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
        }
    }

    @Override
    public SoundCategory getSoundCategory() {
        return this.getRabbitType() == 99 ? SoundCategory.HOSTILE : SoundCategory.NEUTRAL;
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return this.isEntityInvulnerable(source) ? false : super.attackEntityFrom(source, amount);
    }

    private boolean isRabbitBreedingItem(Item itemIn) {
        return itemIn == Items.CARROT || itemIn == Items.GOLDEN_CARROT || itemIn == Item.getItemFromBlock(Blocks.YELLOW_FLOWER);
    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
     * the animal type)
     */
    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return this.isRabbitBreedingItem(stack.getItem());
    }

    public int getRabbitType() {
        return ((Integer) this.dataManager.get(RABBIT_TYPE)).intValue();
    }

    public void setRabbitType(int rabbitTypeId) {
        if (rabbitTypeId == 99) {
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(8.0D);
            this.tasks.addTask(4, new LibEntityRabbit.AIEvilAttack(this));
            this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
            this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
            this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityWolf.class, true));

            if (!this.hasCustomName()) {
                this.setCustomNameTag(I18n.translateToLocal("entity.KillerBunny.name"));
            }
        }

        this.dataManager.set(RABBIT_TYPE, Integer.valueOf(rabbitTypeId));
    }

    /**
     * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
     * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory
     */
    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        int i = this.getRandomRabbitType();
        boolean flag = false;

        if (livingdata instanceof LibEntityRabbit.RabbitTypeData) {
            i = ((LibEntityRabbit.RabbitTypeData) livingdata).typeData;
            flag = true;
        } else {
            livingdata = new LibEntityRabbit.RabbitTypeData(i);
        }

        this.setRabbitType(i);

        if (flag) {
            this.setGrowingAge(-24000);
        }

        return livingdata;
    }

    private int getRandomRabbitType() {
        Biome biome = this.world.getBiome(new BlockPos(this));
        int i = this.rand.nextInt(100);

        if (biome.isSnowyBiome()) {
            return i < 80 ? 1 : 3;
        } else if (biome instanceof BiomeDesert) {
            return 4;
        } else {
            return i < 50 ? 0 : (i < 90 ? 5 : 2);
        }
    }

    /**
     * Returns true if {@link LibEntityRabbit#carrotTicks carrotTicks} has reached zero
     */
    private boolean isCarrotEaten() {
        return this.carrotTicks == 0;
    }

    protected void createEatingParticles() {
        BlockCarrot blockcarrot = (BlockCarrot) Blocks.CARROTS;
        IBlockState iblockstate = blockcarrot.withAge(blockcarrot.getMaxAge());
        this.world.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + 0.5D + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, 0.0D, 0.0D, 0.0D, Block.getStateId(iblockstate));
        this.carrotTicks = 40;
    }

    /**
     * Handler for {@link World#setEntityState}
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == 1) {
            this.createRunningParticles();
            this.jumpDuration = 10;
            this.jumpTicks = 0;
        } else {
            super.handleStatusUpdate(id);
        }
    }

    public static class AIAvoidEntity<T extends Entity> extends EntityAIAvoidEntity<T> {
        private final LibEntityRabbit rabbit;

        public AIAvoidEntity(LibEntityRabbit rabbit, Class<T> p_i46403_2_, float p_i46403_3_, double p_i46403_4_, double p_i46403_6_) {
            super(rabbit, p_i46403_2_, p_i46403_3_, p_i46403_4_, p_i46403_6_);
            this.rabbit = rabbit;
        }

        /**
         * Returns whether the EntityAIBase should begin execution.
         */
        @Override
        public boolean shouldExecute() {
            return this.rabbit.getRabbitType() != 99 && super.shouldExecute();
        }
    }

    public static class AIEvilAttack extends EntityAIAttackMelee {
        public AIEvilAttack(LibEntityRabbit rabbit) {
            super(rabbit, 1.4D, true);
        }

        @Override
        protected double getAttackReachSqr(EntityLivingBase attackTarget) {
            return (double) (4.0F + attackTarget.width);
        }
    }

    public static class AIPanic extends EntityAIPanic {
        private final LibEntityRabbit rabbit;

        public AIPanic(LibEntityRabbit rabbit, double speedIn) {
            super(rabbit, speedIn);
            this.rabbit = rabbit;
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        @Override
        public void updateTask() {
            super.updateTask();
            this.rabbit.setMovementSpeed(this.speed);
        }
    }

    public static class AIRaidFarm extends EntityAIMoveToBlock {
        private final LibEntityRabbit rabbit;
        private boolean wantsToRaid;
        private boolean canRaid;

        public AIRaidFarm(LibEntityRabbit rabbitIn) {
            super(rabbitIn, 0.699999988079071D, 16);
            this.rabbit = rabbitIn;
        }

        /**
         * Returns whether the EntityAIBase should begin execution.
         */
        @Override
        public boolean shouldExecute() {
            if (this.runDelay <= 0) {
                if (!this.rabbit.world.getGameRules().getBoolean("mobGriefing")) {
                    return false;
                }

                this.canRaid = false;
                this.wantsToRaid = this.rabbit.isCarrotEaten();
                this.wantsToRaid = true;
            }

            return super.shouldExecute();
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        @Override
        public boolean shouldContinueExecuting() {
            return this.canRaid && super.shouldContinueExecuting();
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        @Override
        public void updateTask() {
            super.updateTask();
            this.rabbit.getLookHelper().setLookPosition((double) this.destinationBlock.getX() + 0.5D, (double) (this.destinationBlock.getY() + 1), (double) this.destinationBlock.getZ() + 0.5D, 10.0F, (float) this.rabbit.getVerticalFaceSpeed());

            if (this.getIsAboveDestination()) {
                World world = this.rabbit.world;
                BlockPos blockpos = this.destinationBlock.up();
                IBlockState iblockstate = world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();

                if (this.canRaid && block instanceof BlockCarrot) {
                    Integer integer = (Integer) iblockstate.getValue(BlockCarrot.AGE);

                    if (integer.intValue() == 0) {
                        world.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 2);
                        world.destroyBlock(blockpos, true);
                    } else {
                        world.setBlockState(blockpos, iblockstate.withProperty(BlockCarrot.AGE, Integer.valueOf(integer.intValue() - 1)), 2);
                        world.playEvent(2001, blockpos, Block.getStateId(iblockstate));
                    }

                    this.rabbit.createEatingParticles();
                }

                this.canRaid = false;
                this.runDelay = 10;
            }
        }

        /**
         * Return true to set given position as destination
         */
        @Override
        protected boolean shouldMoveTo(World worldIn, BlockPos pos) {
            Block block = worldIn.getBlockState(pos).getBlock();

            if (block == Blocks.FARMLAND && this.wantsToRaid && !this.canRaid) {
                pos = pos.up();
                IBlockState iblockstate = worldIn.getBlockState(pos);
                block = iblockstate.getBlock();

                if (block instanceof BlockCarrot && ((BlockCarrot) block).isMaxAge(iblockstate)) {
                    this.canRaid = true;
                    return true;
                }
            }

            return false;
        }
    }

    public class RabbitJumpHelper extends EntityJumpHelper {
        private final LibEntityRabbit rabbit;
        private boolean canJump;

        public RabbitJumpHelper(LibEntityRabbit rabbit) {
            super(rabbit);
            this.rabbit = rabbit;
        }

        public boolean getIsJumping() {
            return this.isJumping;
        }

        public boolean canJump() {
            return this.canJump;
        }

        public void setCanJump(boolean canJumpIn) {
            this.canJump = canJumpIn;
        }

        /**
         * Called to actually make the entity jump if isJumping is true.
         */
        @Override
        public void doJump() {
            if (this.isJumping) {
                this.rabbit.startJumping();
                this.isJumping = false;
            }
        }
    }

    static class RabbitMoveHelper extends EntityMoveHelper {
        private final LibEntityRabbit rabbit;
        private double nextJumpSpeed;

        public RabbitMoveHelper(LibEntityRabbit rabbit) {
            super(rabbit);
            this.rabbit = rabbit;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.rabbit.onGround && !this.rabbit.isJumping && !((LibEntityRabbit.RabbitJumpHelper) this.rabbit.jumpHelper).getIsJumping()) {
                this.rabbit.setMovementSpeed(0.0D);
            } else if (this.isUpdating()) {
                this.rabbit.setMovementSpeed(this.nextJumpSpeed);
            }

            super.onUpdateMoveHelper();
        }

        /**
         * Sets the speed and location to move to
         */
        @Override
        public void setMoveTo(double x, double y, double z, double speedIn) {
            if (this.rabbit.isInWater()) {
                speedIn = 1.5D;
            }

            super.setMoveTo(x, y, z, speedIn);

            if (speedIn > 0.0D) {
                this.nextJumpSpeed = speedIn;
            }
        }
    }

    public static class RabbitTypeData implements IEntityLivingData {
        public int typeData;

        public RabbitTypeData(int type) {
            this.typeData = type;
        }
    }
}