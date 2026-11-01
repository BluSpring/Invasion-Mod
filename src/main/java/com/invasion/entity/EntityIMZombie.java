package com.invasion.entity;

import java.util.List;

import com.invasion.InvSounds;
import com.invasion.entity.ai.goal.AttackNexusGoal;
import com.invasion.entity.ai.goal.GoToNexusGoal;
import com.invasion.entity.ai.goal.MineBlockGoal;
import com.invasion.entity.ai.goal.MobMeleeAttackGoal;
import com.invasion.entity.ai.goal.NoNexusPathGoal;
import com.invasion.entity.ai.goal.PredicatedGoal;
import com.invasion.entity.ai.goal.ProvideSupportGoal;
import com.invasion.entity.ai.goal.SprintGoal;
import com.invasion.entity.ai.goal.StoopGoal;
import com.invasion.entity.ai.goal.target.CustomRangeActiveTargetGoal;
import com.invasion.entity.ai.goal.target.RetaliateGoal;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class EntityIMZombie extends AbstractIMZombieEntity {
    static final int OLD_ZOMBIE = 0;
    static final int ZOMBIE = 1;
    static final int ZOMBIE_T2 = 2;
    static final int ZOMBIE_PIGMAN = 3;
    static final int ZOMBIE_T2A = 4;
    static final int TAR = 5;
    static final int BRUTE = 6;

    // TODO: Account for self-harm when breaking blocks
    // selfDamage = points of damage dealt per block broken
    // masSelfDamage = max damage loss before the mob can no longer mine blocks. If health is less than (maxHealth - maxSelfDamage) it stops.
    protected int selfDamage = 2;
    protected int maxSelfDamage = 6;

    private static AttributeSupplier.Builder createBaseAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.19F)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    public static AttributeSupplier.Builder createTierT1V0Attributes() {
        return createBaseAttributes().add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    public static AttributeSupplier.Builder createTierT1V1Attributes() {
        return createBaseAttributes().add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    public static AttributeSupplier.Builder createTierT2V0Attributes() {
        return createBaseAttributes().add(Attributes.ATTACK_DAMAGE, 7.0);
    }

    public static AttributeSupplier.Builder createTierT2V1Attributes() {
        return createBaseAttributes().add(Attributes.ATTACK_DAMAGE, 10.0);
    }

    /**
     * Tar Zombie
     */
    public static AttributeSupplier.Builder createTierT2V2ttributes() {
        return createBaseAttributes().add(Attributes.ATTACK_DAMAGE, 5.0);
    }
    /**
     * Zombie Pigman
     */
    public static AttributeSupplier.Builder createTierT2V3ttributes() {
        return createBaseAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.ATTACK_DAMAGE, 8.0);
    }
    /**
     * Zombie Brute
     */
    public static AttributeSupplier.Builder createTierT3V0Attributes() {
        return createBaseAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.17F)
                .add(Attributes.ATTACK_DAMAGE, 18.0);
    }

    public EntityIMZombie(EntityType<EntityIMZombie> type, Level world) {
        super(type, world, 2F);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new PredicatedGoal(new FloatGoal(this), () -> getTier() != 2 || getFlavour() != 2));
        goalSelector.addGoal(0, new MineBlockGoal(this));
        goalSelector.addGoal(1, new AttackNexusGoal<>(this));
        goalSelector.addGoal(3, new ProvideSupportGoal(this, 4, true));
        goalSelector.addGoal(3, new PredicatedGoal(new SprintGoal<>(this), () -> getTier() == 3));
        goalSelector.addGoal(4, new PredicatedGoal(new StoopGoal(this), () -> getTier() == 3));
        goalSelector.addGoal(5, new GoToNexusGoal(this));
        goalSelector.addGoal(6, new MobMeleeAttackGoal(this, 1.3F, false));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, IMCreeperEntity.class, 12));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        targetSelector.addGoal(0, new RetaliateGoal(this));
        targetSelector.addGoal(1, new PredicatedGoal(new CustomRangeActiveTargetGoal<>(this, Player.class, this::getSenseRange, false), () -> getTier() != 3));
        targetSelector.addGoal(2, new CustomRangeActiveTargetGoal<>(this, Player.class, this::getAggroRange, true));
        targetSelector.addGoal(3, new PredicatedGoal(new CustomRangeActiveTargetGoal<>(this, PigmanEngineerEntity.class, 3.5F), () -> getTier() != 3 && NoNexusPathGoal.isLostPathToNexus(this)));
        targetSelector.addGoal(4, new CustomRangeActiveTargetGoal<>(this, AbstractVillager.class, this::getAggroRange, true));
        targetSelector.addGoal(4, new CustomRangeActiveTargetGoal<>(this, IronGolem.class, this::getAggroRange, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && flammability >= 20 && isOnFire()) {
            doFireball();
        }
    }

    public boolean isTar() {
        return isTar(this);
    }

    public boolean isPigman() {
        return getTier() == 2 && getFlavour() == 3;
    }

    @Override
    protected Component getTypeName() {
        if (isTar()) {
            return Component.translatable(getType().toShortString() + ".tar");
        }
        if (isPigman()) {
            return Component.translatable(getType().toShortString() + ".pigman");
        }
        return super.getTypeName();
    }

    @Override
    protected void initTieredAttributes() {
        setBaseMovementSpeed(0.19F);
        setAttackStrength(4);

        if (getTier() == 1) {
            maxSelfDamage = 6;
            selfDamage = 3;
            flammability = 3;

            if (getFlavour() == 0) {
                getNavigatorNew().setCanDestroyBlocks(true);
            } else if (getFlavour() == 1) {
                setAttackStrength(6);
                setItemInHand(InteractionHand.MAIN_HAND, Items.WOODEN_SWORD.getDefaultInstance());
                setDropChance(EquipmentSlot.MAINHAND, 0.2F);
                getNavigatorNew().setCanDestroyBlocks(false);
            }
        } else if (getTier() == 2) {
            setBaseMovementSpeed(0.19F);
            if (getFlavour() == 0) {
                setAttackStrength(7);
                selfDamage = 4;
                maxSelfDamage = 12;
                flammability = 4;
                setItemSlot(EquipmentSlot.CHEST, Items.IRON_CHESTPLATE.getDefaultInstance());
                setDropChance(EquipmentSlot.CHEST, 0.25F);
                getNavigatorNew().setCanDestroyBlocks(true);
            } else if (getFlavour() == 1) {
                setAttackStrength(10);
                selfDamage = 3;
                maxSelfDamage = 9;
                setItemInHand(InteractionHand.MAIN_HAND, Items.IRON_SWORD.getDefaultInstance());
                setDropChance(EquipmentSlot.MAINHAND, 0.25F);
                getNavigatorNew().setCanDestroyBlocks(false);
            }
        }

        if (isTar()) {
            setAttackStrength(5);
            selfDamage = 3;
            maxSelfDamage = 9;
            flammability = 30;
            getNavigatorNew().setCanDestroyBlocks(true);
        }

        if (isPigman()) {
            setBaseMovementSpeed(0.25F);
            setAttackStrength(8);
            setFireImmune(true);
            setItemInHand(InteractionHand.MAIN_HAND, Items.GOLDEN_SWORD.getDefaultInstance());
            setDropChance(EquipmentSlot.MAINHAND, 0.2F);
            getNavigatorNew().setCanDestroyBlocks(true);
        }

        if (isBrute()) {
            setBaseMovementSpeed(0.17F);
            setAttackStrength(18);
            selfDamage = 4;
            maxSelfDamage = 20;
            flammability = 4;
            setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            setDropChance(EquipmentSlot.MAINHAND, 0);
            getNavigatorNew().setCanDestroyBlocks(true);
        }
    }

    @Override
    public int getTextureId() {
        return switch(getTier()) {
            case 2 -> switch(getFlavour()) {
                case 2 -> TAR;
                case 3 -> ZOMBIE_PIGMAN;
                default -> (((int)(Math.abs(getUUID().getLeastSignificantBits()) % 2)) + 1) * 2; //2,4
            };
            case 3 -> BRUTE;
            default -> (int)(Math.abs(getUUID().getLeastSignificantBits()) % 2); // 0,1
        };
    }

    @Override
    protected void sunlightDamageTick() {
        if (isTar()) {
            hurt(damageSources().generic(), 3);
        } else {
            super.sunlightDamageTick();
        }
    }

    @Override
    public void updateAnimation(boolean override) {

    }

    @Override
    public SoundEvent getAmbientSound() {
        if (super.getTier() == 3) {
            return getRandom().nextInt(3) == 0 ? InvSounds.ENTITY_BIG_ZOMBIE_AMBIENT : null;
        }

        return SoundEvents.ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }

    private void doFireball() {
        for (BlockPos pos : BlockPos.withinManhattan(blockPosition(), 2, 2, 2)) {
            if (level().isEmptyBlock(pos) && level().getBlockState(pos.below()).ignitedByLava()) {
                level().setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
            }
        }

        List<Entity> entities = level().getEntities(this, getBoundingBox().inflate(1.5, 1.5, 1.5));
        for (int el = entities.size() - 1; el >= 0; el--) {
            entities.get(el).setRemainingFireTicks(8);
        }
        hurt(damageSources().explosion(this, this), 500);
    }
}
