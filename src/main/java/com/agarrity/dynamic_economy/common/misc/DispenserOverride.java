package com.agarrity.dynamic_economy.common.misc;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.common.economy.resources.WorldResourceTracker;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BoatDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class DispenserOverride {

    public static void overwriteDispenserBehaviours() {
        DynamicEconomy.LOGGER.debug("Overwriting vanilla dispense behaviour");

        DispenserBlock.DISPENSER_REGISTRY = Util.make(new Object2ObjectOpenHashMap<>(), (p_52723_) -> p_52723_.defaultReturnValue(new ResourceTrackedDefaultItemDispenseBehaviour()));
        DropperBlock.DISPENSE_BEHAVIOUR = new ResourceTrackedDefaultItemDispenseBehaviour();

        DispenserBlock.registerBehavior(Items.ARROW, new ResourceTrackedAbstractProjectileDispenseBehaviour() {
            /**
             * Return the projectile entity spawned by this dispense behavior.
             */
            protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
                Arrow arrow = new Arrow(level, position.x(), position.y(), position.z());
                arrow.pickup = AbstractArrow.Pickup.ALLOWED;
                return arrow;
            }
        });
        DispenserBlock.registerBehavior(Items.TIPPED_ARROW, new ResourceTrackedAbstractProjectileDispenseBehaviour() {
            /**
             * Return the projectile entity spawned by this dispense behavior.
             */
            protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
                Arrow arrow = new Arrow(level, position.x(), position.y(), position.z());
                arrow.setEffectsFromItem(itemStack);
                arrow.pickup = AbstractArrow.Pickup.ALLOWED;
                return arrow;
            }
        });
        DispenserBlock.registerBehavior(Items.SPECTRAL_ARROW, new ResourceTrackedAbstractProjectileDispenseBehaviour() {
            /**
             * Return the projectile entity spawned by this dispense behavior.
             */
            protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
                AbstractArrow abstractarrow = new SpectralArrow(level, position.x(), position.y(), position.z());
                abstractarrow.pickup = AbstractArrow.Pickup.ALLOWED;
                return abstractarrow;
            }
        });
        DispenserBlock.registerBehavior(Items.EGG, new ResourceTrackedAbstractProjectileDispenseBehaviour() {
            /**
             * Return the projectile entity spawned by this dispense behavior.
             */
            protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
                return Util.make(new ThrownEgg(level, position.x(), position.y(), position.z()), (thrownEgg) -> {
                    thrownEgg.setItem(itemStack);
                });
            }
        });
        DispenserBlock.registerBehavior(Items.SNOWBALL, new ResourceTrackedAbstractProjectileDispenseBehaviour() {
            /**
             * Return the projectile entity spawned by this dispense behavior.
             */
            protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
                return Util.make(new Snowball(level, position.x(), position.y(), position.z()), (snowball) -> {
                    snowball.setItem(itemStack);
                });
            }
        });
        DispenserBlock.registerBehavior(Items.EXPERIENCE_BOTTLE, new ResourceTrackedAbstractProjectileDispenseBehaviour() {
            /**
             * Return the projectile entity spawned by this dispense behavior.
             */
            protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
                return Util.make(new ThrownExperienceBottle(level, position.x(), position.y(), position.z()), (thrownExperienceBottle) -> {
                    thrownExperienceBottle.setItem(itemStack);
                });
            }

            protected float getUncertainty() {
                return super.getUncertainty() * 0.5F;
            }

            protected float getPower() {
                return super.getPower() * 1.25F;
            }
        });
        DispenserBlock.registerBehavior(Items.SPLASH_POTION, new DispenseItemBehavior() {
            public ItemStack dispense(BlockSource source, ItemStack itemStack) {
                return (new ResourceTrackedAbstractProjectileDispenseBehaviour() {
                    /**
                     * Return the projectile entity spawned by this dispense behavior.
                     */
                    protected Projectile getProjectile(Level level, Position position, ItemStack projectileItemStack) {
                        return Util.make(new ThrownPotion(level, position.x(), position.y(), position.z()), (thrownPotion) -> {
                            thrownPotion.setItem(projectileItemStack);
                        });
                    }

                    protected float getUncertainty() {
                        return super.getUncertainty() * 0.5F;
                    }

                    protected float getPower() {
                        return super.getPower() * 1.25F;
                    }
                }).dispense(source, itemStack);
            }
        });
        DispenserBlock.registerBehavior(Items.LINGERING_POTION, new DispenseItemBehavior() {
            public ItemStack dispense(BlockSource source, ItemStack itemStack) {
                return (new ResourceTrackedAbstractProjectileDispenseBehaviour() {
                    /**
                     * Return the projectile entity spawned by this dispense behavior.
                     */
                    protected Projectile getProjectile(Level level, Position position, ItemStack projectileItemStack) {
                        return Util.make(new ThrownPotion(level, position.x(), position.y(), position.z()), (thrownPotion) -> {
                            thrownPotion.setItem(projectileItemStack);
                        });
                    }

                    protected float getUncertainty() {
                        return super.getUncertainty() * 0.5F;
                    }

                    protected float getPower() {
                        return super.getPower() * 1.25F;
                    }
                }).dispense(source, itemStack);
            }
        });

        ResourceTrackedDefaultItemDispenseBehaviour dispenseEggItemBehaviour = new ResourceTrackedDefaultItemDispenseBehaviour() {
            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            public ItemStack execute(BlockSource source, ItemStack itemStack) {
                Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
                EntityType<?> entityType = ((SpawnEggItem) itemStack.getItem()).getType(itemStack.getTag());

                try {
                    entityType.spawn(source.getLevel(), itemStack, null, source.getPos().relative(direction), MobSpawnType.DISPENSER, direction != Direction.UP, false);
                } catch (Exception exception) {
                    LOGGER.error("Error while dispensing spawn egg from dispenser at {}", source.getPos(), exception);
                    return ItemStack.EMPTY;
                }

                WorldResourceTracker.removeItemsFromEconomy(itemStack, 1);
                itemStack.shrink(1);
                source.getLevel().gameEvent(GameEvent.ENTITY_PLACE, source.getPos());
                return itemStack;
            }
        };

        for (SpawnEggItem spawneggitem : SpawnEggItem.eggs()) {
            DispenserBlock.registerBehavior(spawneggitem, dispenseEggItemBehaviour);
        }

        DispenserBlock.registerBehavior(Items.ARMOR_STAND, new ResourceTrackedDefaultItemDispenseBehaviour() {
            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            public ItemStack execute(BlockSource source, ItemStack itemStack) {
                Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos blockPosition = source.getPos().relative(direction);
                Level level = source.getLevel();
                ArmorStand armourStand = new ArmorStand(level, (double) blockPosition.getX() + 0.5D, blockPosition.getY(), (double) blockPosition.getZ() + 0.5D);
                EntityType.updateCustomEntityTag(level, null, armourStand, itemStack.getTag());
                armourStand.setYRot(direction.toYRot());
                level.addFreshEntity(armourStand);
                itemStack.shrink(1);
                WorldResourceTracker.removeItemsFromEconomy(itemStack, 1);
                return itemStack;
            }
        });
        DispenserBlock.registerBehavior(Items.SADDLE, new ResourceTrackedOptionalItemDispenseBehaviour() {
            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            public ItemStack execute(BlockSource source, ItemStack itemStack) {
                BlockPos blockPosition = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
                List<LivingEntity> list = source.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(blockPosition), (p_123527_) -> {
                    if (!(p_123527_ instanceof Saddleable saddleable)) {
                        return false;
                    } else {
                        return !saddleable.isSaddled() && saddleable.isSaddleable();
                    }
                });
                // Horse can be saddled, don't count it as removed from the economy
                if (!list.isEmpty()) {
                    ((Saddleable) list.get(0)).equipSaddle(SoundSource.BLOCKS);
                    itemStack.shrink(1);
                    this.setSuccess(true);
                    return itemStack;
                } else {
                    return super.execute(source, itemStack);
                }
            }
        });
        ResourceTrackedDefaultItemDispenseBehaviour resourceTrackedDefaultItemDispenseBehaviour = new ResourceTrackedOptionalItemDispenseBehaviour() {
            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            protected ItemStack execute(BlockSource source, ItemStack itemStack) {
                BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));

                for (AbstractHorse abstractHorse : source.getLevel().getEntitiesOfClass(AbstractHorse.class, new AABB(blockpos), (horse) -> {
                    return horse.isAlive() && horse.canWearArmor();
                })) {
                    if (abstractHorse.isArmor(itemStack) && !abstractHorse.isWearingArmor() && abstractHorse.isTamed()) {
                        abstractHorse.getSlot(401).set(itemStack.split(1));
                        this.setSuccess(true);
                        WorldResourceTracker.removeItemsFromEconomy(itemStack, 1);
                        return itemStack;
                    }
                }

                return super.execute(source, itemStack);
            }
        };
        DispenserBlock.registerBehavior(Items.LEATHER_HORSE_ARMOR, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.IRON_HORSE_ARMOR, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.GOLDEN_HORSE_ARMOR, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.DIAMOND_HORSE_ARMOR, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.WHITE_CARPET, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.ORANGE_CARPET, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.CYAN_CARPET, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.BLUE_CARPET, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.BROWN_CARPET, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.BLACK_CARPET, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.GRAY_CARPET, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.GREEN_CARPET, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.LIGHT_BLUE_CARPET, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.LIGHT_GRAY_CARPET, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.LIME_CARPET, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.MAGENTA_CARPET, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.PINK_CARPET, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.PURPLE_CARPET, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.RED_CARPET, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.YELLOW_CARPET, resourceTrackedDefaultItemDispenseBehaviour);
        DispenserBlock.registerBehavior(Items.CHEST, new ResourceTrackedOptionalItemDispenseBehaviour() {
            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            public ItemStack execute(BlockSource source, ItemStack itemStack) {
                BlockPos blockPosition = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));

                for (AbstractChestedHorse abstractChestedHorse : source.getLevel().getEntitiesOfClass(AbstractChestedHorse.class, new AABB(blockPosition), (p_123539_) -> {
                    return p_123539_.isAlive() && !p_123539_.hasChest();
                })) {
                    if (abstractChestedHorse.isTamed() && abstractChestedHorse.getSlot(499).set(itemStack)) {
                        WorldResourceTracker.removeItemsFromEconomy(itemStack, 1);
                        itemStack.shrink(1);
                        this.setSuccess(true);
                        return itemStack;
                    }
                }

                return super.execute(source, itemStack);
            }
        });
        DispenserBlock.registerBehavior(Items.FIREWORK_ROCKET, new ResourceTrackedDefaultItemDispenseBehaviour() {
            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            public ItemStack execute(BlockSource source, ItemStack itemStack) {
                Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
                FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(source.getLevel(), itemStack, source.x(), source.y(), source.x(), true);
                DispenseItemBehavior.setEntityPokingOutOfBlock(source, fireworkRocketEntity, direction);
                fireworkRocketEntity.shoot(direction.getStepX(), direction.getStepY(), direction.getStepZ(), 0.5F, 1.0F);
                source.getLevel().addFreshEntity(fireworkRocketEntity);
                WorldResourceTracker.removeItemsFromEconomy(itemStack, 1);
                itemStack.shrink(1);
                return itemStack;
            }

            /**
             * Play the dispense sound from the specified block.
             */
            protected void playSound(BlockSource source) {
                source.getLevel().levelEvent(1004, source.getPos(), 0);
            }
        });
        DispenserBlock.registerBehavior(Items.FIRE_CHARGE, new ResourceTrackedDefaultItemDispenseBehaviour() {
            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            public ItemStack execute(BlockSource source, ItemStack itemStack) {
                Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
                Position position = DispenserBlock.getDispensePosition(source);
                double d0 = position.x() + (double) ((float) direction.getStepX() * 0.3F);
                double d1 = position.y() + (double) ((float) direction.getStepY() * 0.3F);
                double d2 = position.z() + (double) ((float) direction.getStepZ() * 0.3F);
                Level level = source.getLevel();
                Random random = level.random;
                double d3 = random.nextGaussian() * 0.05D + (double) direction.getStepX();
                double d4 = random.nextGaussian() * 0.05D + (double) direction.getStepY();
                double d5 = random.nextGaussian() * 0.05D + (double) direction.getStepZ();
                SmallFireball smallfireball = new SmallFireball(level, d0, d1, d2, d3, d4, d5);
                level.addFreshEntity(Util.make(smallfireball, (p_123552_) -> {
                    p_123552_.setItem(itemStack);
                }));
                WorldResourceTracker.removeItemsFromEconomy(itemStack, 1);
                itemStack.shrink(1);
                return itemStack;
            }

            /**
             * Play the dispense sound from the specified block.
             */
            protected void playSound(BlockSource source) {
                source.getLevel().levelEvent(1018, source.getPos(), 0);
            }
        });
        DispenserBlock.registerBehavior(Items.OAK_BOAT, new BoatDispenseItemBehavior(Boat.Type.OAK));
        DispenserBlock.registerBehavior(Items.SPRUCE_BOAT, new BoatDispenseItemBehavior(Boat.Type.SPRUCE));
        DispenserBlock.registerBehavior(Items.BIRCH_BOAT, new BoatDispenseItemBehavior(Boat.Type.BIRCH));
        DispenserBlock.registerBehavior(Items.JUNGLE_BOAT, new BoatDispenseItemBehavior(Boat.Type.JUNGLE));
        DispenserBlock.registerBehavior(Items.DARK_OAK_BOAT, new BoatDispenseItemBehavior(Boat.Type.DARK_OAK));
        DispenserBlock.registerBehavior(Items.ACACIA_BOAT, new BoatDispenseItemBehavior(Boat.Type.ACACIA));
        DispenseItemBehavior filledBucketDispenseItemBehaviour = new ResourceTrackedDefaultItemDispenseBehaviour() {
            private final ResourceTrackedDefaultItemDispenseBehaviour defaultDispenseItemBehavior = new ResourceTrackedDefaultItemDispenseBehaviour();

            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            public ItemStack execute(BlockSource source, ItemStack itemStack) {
                DispensibleContainerItem dispensibleContainerItem = (DispensibleContainerItem) itemStack.getItem();
                BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
                Level level = source.getLevel();
                if (dispensibleContainerItem.emptyContents(null, level, blockpos, null)) {
                    dispensibleContainerItem.checkExtraContent(null, level, itemStack, blockpos);
                    WorldResourceTracker.removeItemsFromEconomy(itemStack, 1);
                    final var bucketItemStack = new ItemStack(Items.BUCKET);
                    WorldResourceTracker.addItemsToEconomy(bucketItemStack);
                    return bucketItemStack;
                } else {
                    return this.defaultDispenseItemBehavior.dispense(source, itemStack);
                }
            }
        };
        DispenserBlock.registerBehavior(Items.LAVA_BUCKET, filledBucketDispenseItemBehaviour);
        DispenserBlock.registerBehavior(Items.WATER_BUCKET, filledBucketDispenseItemBehaviour);
        DispenserBlock.registerBehavior(Items.POWDER_SNOW_BUCKET, filledBucketDispenseItemBehaviour);
        DispenserBlock.registerBehavior(Items.SALMON_BUCKET, filledBucketDispenseItemBehaviour);
        DispenserBlock.registerBehavior(Items.COD_BUCKET, filledBucketDispenseItemBehaviour);
        DispenserBlock.registerBehavior(Items.PUFFERFISH_BUCKET, filledBucketDispenseItemBehaviour);
        DispenserBlock.registerBehavior(Items.TROPICAL_FISH_BUCKET, filledBucketDispenseItemBehaviour);
        DispenserBlock.registerBehavior(Items.AXOLOTL_BUCKET, filledBucketDispenseItemBehaviour);
        DispenserBlock.registerBehavior(Items.BUCKET, new ResourceTrackedDefaultItemDispenseBehaviour() {
            private final ResourceTrackedDefaultItemDispenseBehaviour resourceTrackedDefaultItemDispenseBehaviour = new ResourceTrackedDefaultItemDispenseBehaviour();

            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            public ItemStack execute(BlockSource source, ItemStack emptyBucketItemStack) {
                LevelAccessor level = source.getLevel();
                BlockPos blockPosition = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
                BlockState blockState = level.getBlockState(blockPosition);
                Block block = blockState.getBlock();
                if (block instanceof BucketPickup) {
                    ItemStack itemStackFromPickingUpBlock = ((BucketPickup) block).pickupBlock(level, blockPosition, blockState);
                    if (itemStackFromPickingUpBlock.isEmpty()) {
                        return super.execute(source, emptyBucketItemStack);
                    } else {
                        level.gameEvent(null, GameEvent.FLUID_PICKUP, blockPosition);
                        Item item = itemStackFromPickingUpBlock.getItem();
                        WorldResourceTracker.removeItemsFromEconomy(emptyBucketItemStack, 1);
                        emptyBucketItemStack.shrink(1);
                        if (emptyBucketItemStack.isEmpty()) {
                            final var filledBucketItemStack = new ItemStack(item);
                            WorldResourceTracker.addItemsToEconomy(filledBucketItemStack);
                            return filledBucketItemStack;
                        } else {
                            if (source.<DispenserBlockEntity>getEntity().addItem(new ItemStack(item)) < 0) {
                                this.resourceTrackedDefaultItemDispenseBehaviour.dispense(source, new ItemStack(item));
                            }

                            return emptyBucketItemStack;
                        }
                    }
                } else {
                    return super.execute(source, emptyBucketItemStack);
                }
            }
        });
        DispenserBlock.registerBehavior(Items.FLINT_AND_STEEL, new ResourceTrackedOptionalItemDispenseBehaviour() {
            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            protected ItemStack execute(BlockSource source, ItemStack itemStack) {
                Level level = source.getLevel();
                this.setSuccess(true);
                Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos blockpos = source.getPos().relative(direction);
                BlockState blockstate = level.getBlockState(blockpos);
                if (BaseFireBlock.canBePlacedAt(level, blockpos, direction)) {
                    level.setBlockAndUpdate(blockpos, BaseFireBlock.getState(level, blockpos));
                    level.gameEvent(null, GameEvent.BLOCK_PLACE, blockpos);
                } else if (!CampfireBlock.canLight(blockstate) && !CandleBlock.canLight(blockstate) && !CandleCakeBlock.canLight(blockstate)) {
                    if (blockstate.isFlammable(level, blockpos, source.getBlockState().getValue(DispenserBlock.FACING).getOpposite())) {
                        blockstate.onCaughtFire(level, blockpos, source.getBlockState().getValue(DispenserBlock.FACING).getOpposite(), null);
                        if (blockstate.getBlock() instanceof TntBlock)
                            level.removeBlock(blockpos, false);
                    } else {
                        this.setSuccess(false);
                    }
                } else {
                    level.setBlockAndUpdate(blockpos, blockstate.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)));
                    level.gameEvent(null, GameEvent.BLOCK_CHANGE, blockpos);
                }

                if (this.isSuccess() && itemStack.hurt(1, level.random, null)) {
                    WorldResourceTracker.removeItemsFromEconomy(itemStack);
                    itemStack.setCount(0);
                }

                return itemStack;
            }
        });
        DispenserBlock.registerBehavior(Items.BONE_MEAL, new ResourceTrackedOptionalItemDispenseBehaviour() {
            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            protected ItemStack execute(BlockSource source, ItemStack itemStack) {
                this.setSuccess(true);
                Level level = source.getLevel();
                BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
                if (!BoneMealItem.growCrop(itemStack, level, blockpos) && !BoneMealItem.growWaterPlant(itemStack, level, blockpos, null)) {
                    this.setSuccess(false);
                } else if (!level.isClientSide) {
                    level.levelEvent(1505, blockpos, 0);
                }

                WorldResourceTracker.removeItemsFromEconomy(itemStack, 1);
                return itemStack;
            }
        });
        DispenserBlock.registerBehavior(Blocks.TNT, new ResourceTrackedDefaultItemDispenseBehaviour() {
            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            protected ItemStack execute(BlockSource source, ItemStack itemStack) {
                Level level = source.getLevel();
                BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
                PrimedTnt primedtnt = new PrimedTnt(level, (double) blockpos.getX() + 0.5D, blockpos.getY(), (double) blockpos.getZ() + 0.5D, null);
                level.addFreshEntity(primedtnt);
                level.playSound(null, primedtnt.getX(), primedtnt.getY(), primedtnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.ENTITY_PLACE, blockpos);
                WorldResourceTracker.removeItemsFromEconomy(itemStack, 1);
                itemStack.shrink(1);
                return itemStack;
            }
        });

        DispenseItemBehavior dispenseHeadItemBehaviour = new OptionalDispenseItemBehavior() {
            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            protected ItemStack execute(BlockSource p_123429_, ItemStack p_123430_) {
                this.setSuccess(ArmorItem.dispenseArmor(p_123429_, p_123430_));
                return p_123430_;
            }
        };
        DispenserBlock.registerBehavior(Items.CREEPER_HEAD, dispenseHeadItemBehaviour);
        DispenserBlock.registerBehavior(Items.ZOMBIE_HEAD, dispenseHeadItemBehaviour);
        DispenserBlock.registerBehavior(Items.DRAGON_HEAD, dispenseHeadItemBehaviour);
        DispenserBlock.registerBehavior(Items.SKELETON_SKULL, dispenseHeadItemBehaviour);
        DispenserBlock.registerBehavior(Items.PLAYER_HEAD, dispenseHeadItemBehaviour);
        DispenserBlock.registerBehavior(Items.WITHER_SKELETON_SKULL, new OptionalDispenseItemBehavior() {
            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            protected ItemStack execute(BlockSource source, ItemStack itemStack) {
                Level level = source.getLevel();
                Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos blockPosition = source.getPos().relative(direction);
                if (level.isEmptyBlock(blockPosition) && WitherSkullBlock.canSpawnMob(level, blockPosition, itemStack)) {
                    level.setBlock(blockPosition, Blocks.WITHER_SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, Integer.valueOf(direction.getAxis() == Direction.Axis.Y ? 0 : direction.getOpposite().get2DDataValue() * 4)), 3);
                    level.gameEvent(null, GameEvent.BLOCK_PLACE, blockPosition);
                    BlockEntity blockEntity = level.getBlockEntity(blockPosition);
                    if (blockEntity instanceof SkullBlockEntity) {
                        WitherSkullBlock.checkSpawn(level, blockPosition, (SkullBlockEntity) blockEntity);
                    }

                    WorldResourceTracker.removeItemsFromEconomy(itemStack, 1);
                    itemStack.shrink(1);
                    this.setSuccess(true);
                } else {
                    this.setSuccess(ArmorItem.dispenseArmor(source, itemStack));
                }

                return itemStack;
            }
        });
        DispenserBlock.registerBehavior(Blocks.CARVED_PUMPKIN, new OptionalDispenseItemBehavior() {
            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            protected ItemStack execute(BlockSource source, ItemStack itemStack) {
                Level level = source.getLevel();
                BlockPos blockPosition = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
                CarvedPumpkinBlock carvedPumpkinBlock = (CarvedPumpkinBlock) Blocks.CARVED_PUMPKIN;
                if (level.isEmptyBlock(blockPosition) && carvedPumpkinBlock.canSpawnGolem(level, blockPosition)) {
                    if (!level.isClientSide) {
                        level.setBlock(blockPosition, carvedPumpkinBlock.defaultBlockState(), 3);
                        level.gameEvent(null, GameEvent.BLOCK_PLACE, blockPosition);
                    }

                    WorldResourceTracker.removeItemsFromEconomy(itemStack, 1);
                    itemStack.shrink(1);
                    this.setSuccess(true);
                } else {
                    this.setSuccess(ArmorItem.dispenseArmor(source, itemStack));
                }

                return itemStack;
            }
        });
        DispenserBlock.registerBehavior(Blocks.SHULKER_BOX.asItem(), new ResourceTrackedShulkerBoxDispenseBehavior());

        for (DyeColor dyecolor : DyeColor.values()) {
            DispenserBlock.registerBehavior(ShulkerBoxBlock.getBlockByColor(dyecolor).asItem(), new ResourceTrackedShulkerBoxDispenseBehavior());
        }

        DispenserBlock.registerBehavior(Items.GLASS_BOTTLE.asItem(), new ResourceTrackedOptionalItemDispenseBehaviour() {
            private final ResourceTrackedDefaultItemDispenseBehaviour resourceTrackedDefaultItemDispenseBehaviour = new ResourceTrackedDefaultItemDispenseBehaviour();

            private ItemStack takeLiquid(BlockSource source, ItemStack bottleItemStack, ItemStack filledBottleItemStack) {
                WorldResourceTracker.removeItemsFromEconomy(bottleItemStack, 1);
                bottleItemStack.shrink(1);
                if (bottleItemStack.isEmpty()) {
                    source.getLevel().gameEvent(null, GameEvent.FLUID_PICKUP, source.getPos());
                    WorldResourceTracker.addItemsToEconomy(filledBottleItemStack, 1);
                    return filledBottleItemStack.copy();
                } else {
                    if (source.<DispenserBlockEntity>getEntity().addItem(filledBottleItemStack.copy()) < 0) {
                        this.resourceTrackedDefaultItemDispenseBehaviour.dispense(source, filledBottleItemStack.copy());
                    }

                    return bottleItemStack;
                }
            }

            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            public ItemStack execute(BlockSource source, ItemStack itemStack) {
                this.setSuccess(false);
                ServerLevel level = source.getLevel();
                BlockPos blockPosition = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
                BlockState blockState = level.getBlockState(blockPosition);
                if (blockState.is(BlockTags.BEEHIVES, (p_123442_) -> {
                    return p_123442_.hasProperty(BeehiveBlock.HONEY_LEVEL) && p_123442_.getBlock() instanceof BeehiveBlock;
                }) && blockState.getValue(BeehiveBlock.HONEY_LEVEL) >= 5) {
                    ((BeehiveBlock) blockState.getBlock()).releaseBeesAndResetHoneyLevel(level, blockState, blockPosition, null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
                    this.setSuccess(true);
                    return this.takeLiquid(source, itemStack, new ItemStack(Items.HONEY_BOTTLE));
                } else if (level.getFluidState(blockPosition).is(FluidTags.WATER)) {
                    this.setSuccess(true);
                    return this.takeLiquid(source, itemStack, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));
                } else {
                    return super.execute(source, itemStack);
                }
            }
        });
        DispenserBlock.registerBehavior(Items.GLOWSTONE, new ResourceTrackedOptionalItemDispenseBehaviour() {
            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
                Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos blockPosition = blockSource.getPos().relative(direction);
                Level level = blockSource.getLevel();
                BlockState blockState = level.getBlockState(blockPosition);
                this.setSuccess(true);
                if (blockState.is(Blocks.RESPAWN_ANCHOR)) {
                    if (blockState.getValue(RespawnAnchorBlock.CHARGE) != 4) {
                        RespawnAnchorBlock.charge(level, blockPosition, blockState);
                        WorldResourceTracker.removeItemsFromEconomy(itemStack, 1);
                        itemStack.shrink(1);
                    } else {
                        this.setSuccess(false);
                    }

                    return itemStack;
                } else {
                    return super.execute(blockSource, itemStack);
                }
            }
        });
        DispenserBlock.registerBehavior(Items.SHEARS.asItem(), new ResourceTrackedShearsDispenseItemBehavior());
        DispenserBlock.registerBehavior(Items.HONEYCOMB, new ResourceTrackedOptionalItemDispenseBehaviour() {
            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            public ItemStack execute(BlockSource source, ItemStack itemStack) {
                BlockPos blockPosition = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
                Level level = source.getLevel();
                BlockState blockState = level.getBlockState(blockPosition);
                Optional<BlockState> optionalBlockState = HoneycombItem.getWaxed(blockState);
                if (optionalBlockState.isPresent()) {
                    level.setBlockAndUpdate(blockPosition, optionalBlockState.get());
                    level.levelEvent(3003, blockPosition, 0);
                    WorldResourceTracker.removeItemsFromEconomy(itemStack, 1);
                    itemStack.shrink(1);
                    this.setSuccess(true);
                    return itemStack;
                } else {
                    return super.execute(source, itemStack);
                }
            }
        });
    }

    public static class ResourceTrackedDefaultItemDispenseBehaviour implements DispenseItemBehavior {
        public static void spawnItem(Level level, ItemStack itemStack, int speed, Direction directionFacing, Position position) {
            double xPosition = position.x();
            double yPosition = position.y();
            double zPosition = position.z();
            if (directionFacing.getAxis() == Direction.Axis.Y) {
                yPosition -= 0.125D;
            } else {
                yPosition -= 0.15625D;
            }

            ItemEntity itemEntity = new ItemEntity(level, xPosition, yPosition, zPosition, itemStack);
            double velocityMultiplier = level.random.nextDouble() * 0.1D + 0.2D;
            itemEntity.setDeltaMovement(level.random.nextGaussian() * (double) 0.0075F * (double) speed + (double) directionFacing.getStepX() * velocityMultiplier, level.random.nextGaussian() * (double) 0.0075F * (double) speed + (double) 0.2F, level.random.nextGaussian() * (double) 0.0075F * (double) speed + (double) directionFacing.getStepZ() * velocityMultiplier);
            level.addFreshEntity(itemEntity);
            WorldResourceTracker.removeItemsFromEconomy(itemStack, 1);
        }

        public final @NotNull ItemStack dispense(@NotNull BlockSource source, @NotNull ItemStack itemStack) {
            ItemStack itemstack = this.execute(source, itemStack);
            this.playSound(source);
            this.playAnimation(source, source.getBlockState().getValue(DispenserBlock.FACING));
            return itemstack;
        }

        /**
         * Dispense the specified stack, play the dispense sound and spawn particles.
         */
        protected ItemStack execute(BlockSource source, ItemStack itemStack) {
            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            Position position = DispenserBlock.getDispensePosition(source);
            ItemStack itemStackToDispense = itemStack.split(1);
            spawnItem(source.getLevel(), itemStackToDispense, 6, direction, position);
            return itemStack;
        }

        /**
         * Play the dispense sound from the specified block.
         */
        protected void playSound(BlockSource source) {
            source.getLevel().levelEvent(1000, source.getPos(), 0);
        }

        /**
         * Order clients to display dispense particles from the specified block and facing.
         */
        protected void playAnimation(BlockSource source, Direction directionFacing) {
            source.getLevel().levelEvent(2000, source.getPos(), directionFacing.get3DDataValue());
        }
    }

    public abstract static class ResourceTrackedOptionalItemDispenseBehaviour extends ResourceTrackedDefaultItemDispenseBehaviour {
        private boolean success = true;

        public boolean isSuccess() {
            return this.success;
        }

        public void setSuccess(boolean pSuccess) {
            this.success = pSuccess;
        }

        /**
         * Play the dispense sound from the specified block.
         */
        protected void playSound(BlockSource pSource) {
            pSource.getLevel().levelEvent(this.isSuccess() ? 1000 : 1001, pSource.getPos(), 0);
        }
    }

    public abstract static class ResourceTrackedAbstractProjectileDispenseBehaviour extends ResourceTrackedDefaultItemDispenseBehaviour {
        /**
         * Dispense the specified stack, play the dispense sound and spawn particles.
         */
        public ItemStack execute(BlockSource source, ItemStack itemStack) {
            Level level = source.getLevel();
            Position position = DispenserBlock.getDispensePosition(source);
            Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
            Projectile projectile = this.getProjectile(level, position, itemStack);
            projectile.shoot(direction.getStepX(), (float) direction.getStepY() + 0.1F, direction.getStepZ(), this.getPower(), this.getUncertainty());
            level.addFreshEntity(projectile);
            WorldResourceTracker.removeItemsFromEconomy(itemStack, 1);
            itemStack.shrink(1);
            return itemStack;
        }

        /**
         * Play the dispense sound from the specified block.
         */
        protected void playSound(BlockSource source) {
            source.getLevel().levelEvent(1002, source.getPos(), 0);
        }

        /**
         * Return the projectile entity spawned by this dispense behavior.
         */
        protected abstract Projectile getProjectile(Level level, Position position, ItemStack itemStack);

        protected float getUncertainty() {
            return 6.0F;
        }

        protected float getPower() {
            return 1.1F;
        }
    }

    public static class ResourceTrackedShulkerBoxDispenseBehavior extends OptionalDispenseItemBehavior {
        private static final Logger LOGGER = LogUtils.getLogger();

        /**
         * Dispense the specified stack, play the dispense sound and spawn particles.
         */
        protected ItemStack execute(BlockSource source, ItemStack itemStack) {
            this.setSuccess(false);
            Item item = itemStack.getItem();
            if (item instanceof BlockItem) {
                Direction directionDispenserIsFacing = source.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos blockPosition = source.getPos().relative(directionDispenserIsFacing);
                Direction directionShulkerIsFacing = source.getLevel().isEmptyBlock(blockPosition.below()) ? directionDispenserIsFacing : Direction.UP;

                try {
                    this.setSuccess(((BlockItem) item).place(new DirectionalPlaceContext(source.getLevel(), blockPosition, directionDispenserIsFacing, itemStack, directionShulkerIsFacing)).consumesAction());
                } catch (Exception exception) {
                    LOGGER.error("Error trying to place shulker box at {}", blockPosition, exception);
                }
            }

            WorldResourceTracker.removeItemsFromEconomy(itemStack, 1);
            return itemStack;
        }
    }

    public static class ResourceTrackedShearsDispenseItemBehavior extends OptionalDispenseItemBehavior {
        private static boolean tryShearBeehive(ServerLevel level, BlockPos blockPosition) {
            BlockState blockState = level.getBlockState(blockPosition);
            if (blockState.is(BlockTags.BEEHIVES, (p_202454_) -> {
                return p_202454_.hasProperty(BeehiveBlock.HONEY_LEVEL) && p_202454_.getBlock() instanceof BeehiveBlock;
            })) {
                int i = blockState.getValue(BeehiveBlock.HONEY_LEVEL);
                if (i >= 5) {
                    level.playSound(null, blockPosition, SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
                    BeehiveBlock.dropHoneycomb(level, blockPosition);
                    ((BeehiveBlock) blockState.getBlock()).releaseBeesAndResetHoneyLevel(level, blockState, blockPosition, null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
                    level.gameEvent(null, GameEvent.SHEAR, blockPosition);
                    return true;
                }
            }

            return false;
        }

        private static boolean tryShearLivingEntity(ServerLevel level, BlockPos blockPosition) {
            for (LivingEntity livingentity : level.getEntitiesOfClass(LivingEntity.class, new AABB(blockPosition), EntitySelector.NO_SPECTATORS)) {
                if (livingentity instanceof Shearable shearable) {
                    if (shearable.readyForShearing()) {
                        shearable.shear(SoundSource.BLOCKS);
                        level.gameEvent(null, GameEvent.SHEAR, blockPosition);
                        return true;
                    }
                }
            }

            return false;
        }

        /**
         * Dispense the specified stack, play the dispense sound and spawn particles.
         */
        protected ItemStack execute(BlockSource source, ItemStack itemStack) {
            Level level = source.getLevel();
            if (!level.isClientSide()) {
                BlockPos blockPosition = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
                this.setSuccess(tryShearBeehive((ServerLevel) level, blockPosition) || tryShearLivingEntity((ServerLevel) level, blockPosition));
                if (this.isSuccess() && itemStack.hurt(1, level.getRandom(), null)) {
                    WorldResourceTracker.removeItemsFromEconomy(itemStack);
                    itemStack.setCount(0);
                }
            }

            return itemStack;
        }
    }
}
