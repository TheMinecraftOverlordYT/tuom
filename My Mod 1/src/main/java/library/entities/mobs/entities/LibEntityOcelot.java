package library.entities.mobs.entities;

import library.ai.LibEntityAIOcelotSit;
import library.entities.LibEntityTameable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;

public class LibEntityOcelot extends LibEntityTameable {

    /** COMMON VARIABLES **/

    // Sounds
    protected SoundEvent purrSound = SoundEvents.ENTITY_CAT_PURR;
    protected SoundEvent purreowSound = SoundEvents.ENTITY_CAT_PURREOW;
    protected SoundEvent ambientSound = SoundEvents.ENTITY_CAT_AMBIENT;
    protected SoundEvent hurtSound = SoundEvents.ENTITY_CAT_HURT;
    protected SoundEvent deathSound = SoundEvents.ENTITY_CAT_DEATH;

    // Loot table
    protected ResourceLocation lootTable = LootTableList.ENTITIES_OCELOT;

    /** COMMON METHODS **/

    // Sets the Entity's attributes
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();

        double maxHealth = 10.0D; // max health
        double movementSpeed = 0.3D; // how fast it moves normally

        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealth);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(movementSpeed);
    }

    // Sets the Entity's core behaviors
    @Override
    protected void initEntityAI() {
        this.aiSit = new EntityAISit(this);
        this.aiTempt = new EntityAITempt(this, 0.6D, Items.FISH, true);
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, this.aiSit);
        this.tasks.addTask(3, this.aiTempt);
        this.tasks.addTask(5, new EntityAIFollowOwner(this, 1.0D, 10.0F, 5.0F));
        this.tasks.addTask(6, new LibEntityAIOcelotSit(this, 0.8D));
        this.tasks.addTask(7, new EntityAILeapAtTarget(this, 0.3F));
        this.tasks.addTask(8, new EntityAIOcelotAttack(this));
        this.tasks.addTask(9, new EntityAIMate(this, 0.8D));
        this.tasks.addTask(10, new EntityAIWanderAvoidWater(this, 0.8D, 1.0000001E-5F));
        this.tasks.addTask(11, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
        this.targetTasks.addTask(1, new EntityAITargetNonTamed(this, EntityChicken.class, false, null));
    }

    // Override this to add (random) drops when this entity dies
    protected void dropItems(DamageSource source) {
    }

    // Drop an itemstack in the world at a specific location
    public void dropItem(ItemStack stack) {
        BlockPos pos = getPosition();
        EntityItem entityItem = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        world.spawnEntity(entityItem);
    }

    // Drop an item in the world at a specific location
    public void dropItem(Block block, int amount) {
        dropItem(new ItemStack(block, amount));
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (this.isTamed()) {
            if (this.isInLove()) {
                return purrSound;
            } else {
                return this.rand.nextInt(4) == 0 ? purreowSound : ambientSound;
            }
        } else {
            return null;
        }
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

    private static final DataParameter<Integer> OCELOT_VARIANT = EntityDataManager.<Integer>createKey(LibEntityOcelot.class, DataSerializers.VARINT);
    private EntityAIAvoidEntity<EntityPlayer> avoidEntity;
    /**
     * The tempt AI task for this mob, used to prevent taming while it is fleeing.
     */
    public EntityAITempt aiTempt;

    private boolean canSit = false;

    public boolean canSit() {
        return canSit;
    }

    public void enableSitting() {
        this.canSit = true;
    }

    public void disableSitting() {
        this.canSit = false;
    }

    public LibEntityOcelot(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 0.7F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(OCELOT_VARIANT, Integer.valueOf(0));
    }

    @Override
    public void updateAITasks() {
        if (this.getMoveHelper().isUpdating()) {
            double d0 = this.getMoveHelper().getSpeed();

            if (d0 == 0.6D) {
                this.setSneaking(true);
                this.setSprinting(false);
            } else if (d0 == 1.33D) {
                this.setSneaking(false);
                this.setSprinting(true);
            } else {
                this.setSneaking(false);
                this.setSprinting(false);
            }
        } else {
            this.setSneaking(false);
            this.setSprinting(false);
        }
    }

    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    @Override
    protected boolean canDespawn() {
        return !this.isTamed() && this.ticksExisted > 2400;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    public static void registerFixesOcelot(DataFixer fixer) {
        EntityLiving.registerFixesMob(fixer, LibEntityOcelot.class);
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("CatType", this.getTameSkin());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setTameSkin(compound.getInteger("CatType"));
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        return entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 3.0F);
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else {
            if (this.aiSit != null) {
                this.aiSit.setSitting(false);
            }

            return super.attackEntityFrom(source, amount);
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);

        if (this.isTamed()) {
            if (this.isOwner(player) && !this.world.isRemote && !this.isBreedingItem(itemstack)) {
                this.aiSit.setSitting(!this.isSitting());
            }
        } else if ((this.aiTempt == null || this.aiTempt.isRunning()) && itemstack.getItem() == Items.FISH && player.getDistanceSq(this) < 9.0D) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }

            if (!this.world.isRemote) {
                if (this.rand.nextInt(3) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
                    this.setTamedBy(player);
                    this.setTameSkin(1 + this.world.rand.nextInt(3));
                    this.playTameEffect(true);
                    this.aiSit.setSitting(true);
                    this.world.setEntityState(this, (byte) 7);
                } else {
                    this.playTameEffect(false);
                    this.world.setEntityState(this, (byte) 6);
                }
            }

            return true;
        }

        return super.processInteract(player, hand);
    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
     * the animal type)
     */
    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.FISH;
    }

    /**
     * Returns true if the mob is currently able to mate with the specified mob.
     */
    @Override
    public boolean canMateWith(EntityAnimal otherAnimal) {
        if (otherAnimal == this) {
            return false;
        } else if (!this.isTamed()) {
            return false;
        } else if (!(otherAnimal instanceof LibEntityOcelot)) {
            return false;
        } else {
            LibEntityOcelot entityocelot = (LibEntityOcelot) otherAnimal;

            if (!entityocelot.isTamed()) {
                return false;
            } else {
                return this.isInLove() && entityocelot.isInLove();
            }
        }
    }

    public int getTameSkin() {
        return this.dataManager.get(OCELOT_VARIANT).intValue();
    }

    public void setTameSkin(int skinId) {
        this.dataManager.set(OCELOT_VARIANT, Integer.valueOf(skinId));
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    @Override
    public boolean getCanSpawnHere() {
        return this.world.rand.nextInt(3) != 0;
    }

    /**
     * Checks that the entity is not colliding with any blocks / liquids
     */
    @Override
    public boolean isNotColliding() {
        if (this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this) && this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty() && !this.world.containsAnyLiquid(this.getEntityBoundingBox())) {
            BlockPos blockpos = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);

            if (blockpos.getY() < this.world.getSeaLevel()) {
                return false;
            }

            IBlockState iblockstate = this.world.getBlockState(blockpos.down());
            Block block = iblockstate.getBlock();

            if (block == Blocks.GRASS || block.isLeaves(iblockstate, this.world, blockpos.down())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the name of this object. For players this returns their username
     */
    @Override
    public String getName() {
        if (this.hasCustomName()) {
            return this.getCustomNameTag();
        } else {
            return this.isTamed() ? I18n.translateToLocal("entity.Cat.name") : super.getName();
        }
    }

    @Override
    protected void setupTamedAI() {
        if (this.avoidEntity == null) {
            this.avoidEntity = new EntityAIAvoidEntity<EntityPlayer>(this, EntityPlayer.class, 16.0F, 0.8D, 1.33D);
        }

        this.tasks.removeTask(this.avoidEntity);

        if (!this.isTamed()) {
            this.tasks.addTask(4, this.avoidEntity);
        }
    }

    /**
     * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
     * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory
     */
    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);

        if (this.getTameSkin() == 0 && this.world.rand.nextInt(7) == 0) {
            for (int i = 0; i < 2; ++i) {
                LibEntityOcelot entityocelot = new LibEntityOcelot(this.world);
                entityocelot.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
                entityocelot.setGrowingAge(-24000);
                this.world.spawnEntity(entityocelot);
            }
        }

        return livingdata;
    }

    // Sets the Entity's drop items on Death
    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);

        if (!world.isRemote) {
            // drop items here
            dropItems(cause);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        updateArmSwingProgress();
        if (!canSit && this.aiSit != null) {
            this.aiSit.setSitting(false);
        }
    }
}
