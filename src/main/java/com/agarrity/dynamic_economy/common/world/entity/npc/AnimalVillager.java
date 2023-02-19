package com.agarrity.dynamic_economy.common.world.entity.npc;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.common.network.syncher.DEEntityDataSerializers;
import com.agarrity.dynamic_economy.common.world.inventory.*;
import com.agarrity.dynamic_economy.init.EntityInit;
import com.agarrity.dynamic_economy.init.SensorInit;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class AnimalVillager extends Villager implements ITrader, Npc {
    public static final int INVENTORY_SIZE = 12;

    private static final ImmutableList<SensorType<? extends Sensor<? super Villager>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.NEAREST_BED, SensorType.HURT_BY, SensorType.VILLAGER_HOSTILES, SensorType.VILLAGER_BABIES, SensorType.SECONDARY_POIS, SensorType.GOLEM_DETECTED, SensorInit.ANIMAL_VILLAGER_TEMPTATIONS);
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.HOME, MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, MemoryModuleType.MEETING_POINT, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.BREED_TARGET, MemoryModuleType.PATH, MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.NEAREST_BED, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.NEAREST_HOSTILE, MemoryModuleType.SECONDARY_JOB_SITE, MemoryModuleType.HIDING_PLACE, MemoryModuleType.HEARD_BELL_TIME, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.LAST_SLEPT, MemoryModuleType.LAST_WOKEN, MemoryModuleType.LAST_WORKED_AT_POI, MemoryModuleType.GOLEM_DETECTED_RECENTLY, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.TEMPTING_PLAYER);

    private static final EntityDataAccessor<AnimalVillagerData> DATA_ANIMAL_VILLAGER = SynchedEntityData.defineId(AnimalVillager.class, DEEntityDataSerializers.ANIMAL_VILLAGER_DATA);
    private final IItemHandler traderItemStackHandler;
    private Player tradingPlayer = null;

    public AnimalVillager(final EntityType<? extends AnimalVillager> pEntityType, final Level pLevel, final int species, final int profession) {
        super(pEntityType, pLevel);
        ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);
        this.getNavigation().setCanFloat(true);
        this.setCanPickUpLoot(false);
        this.setAnimalVillagerData(this.getAnimalVillagerData().setSpecies(species).setProfession(profession));

        this.setVillagerData(this.getVillagerData().setProfession(VillagerProfession.NONE));

        traderItemStackHandler = switch (profession) {
            case AnimalVillagerProfession.PLAYER_TRADER -> new TraderItemStackHandler(INVENTORY_SIZE);
            case AnimalVillagerProfession.TRADER -> new ItemStackHandler(INVENTORY_SIZE);
            default -> new ItemStackHandler(0);
        };
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Villager.createAttributes();
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getCorePackage(float pSpeedModifier) {
        return ImmutableList.of(Pair.of(0, new Swim(0.8F)), Pair.of(0, new InteractWithDoor()), Pair.of(0, new LookAtTargetSink(45, 90)), Pair.of(0, new VillagerPanicTrigger()), Pair.of(0, new WakeUp()), Pair.of(0, new ReactToBell()), Pair.of(0, new SetRaidStatus()), Pair.of(1, new MoveToTargetSink()), Pair.of(3, new LookAndFollowTradingPlayerSink(pSpeedModifier)), Pair.of(10, new AcquirePoi(PoiType.HOME, MemoryModuleType.HOME, false, Optional.of((byte) 14))), Pair.of(10, new AcquirePoi(PoiType.MEETING, MemoryModuleType.MEETING_POINT, true, Optional.of((byte) 14))), Pair.of(10, new ResetProfession()), Pair.of(0, new FollowTemptation((livingEntity) -> pSpeedModifier)));
    }

    public static Ingredient getTemptations() {
        return Ingredient.of(Items.GOLD_INGOT);
    }

    @Override
    protected Brain.@NotNull Provider<Villager> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected @NotNull Brain<?> makeBrain(@NotNull Dynamic<?> pDynamic) {
        final var brain = super.makeBrain(pDynamic);
        ((Brain<Villager>) brain).addActivity(Activity.CORE, getCorePackage(0.5F));
        return brain;
    }

    @Override
    public void refreshBrain(@NotNull ServerLevel pServerLevel) {
        super.refreshBrain(pServerLevel);
        this.getBrain().addActivity(Activity.CORE, getCorePackage(0.5F));
    }

    @Override
    protected void ageBoundaryReached() {
        super.ageBoundaryReached();
        this.setAnimalVillagerData(this.getAnimalVillagerData().setProfession(AnimalVillagerProfession.getRandomProfession()));
    }

    @Override
    public AnimalVillager getBreedOffspring(@NotNull final ServerLevel pLevel, @NotNull final AgeableMob pOtherParent) {
        final var otherParent = (AnimalVillager) pOtherParent;
        final var species = this.getRandom().nextBoolean() ? this.getAnimalVillagerData().getSpecies() : otherParent.getAnimalVillagerData().getSpecies();
        final var profession = this.getRandom().nextBoolean() ? this.getAnimalVillagerData().getProfession() : otherParent.getAnimalVillagerData().getProfession();
        return new AnimalVillager(EntityInit.ANIMAL_VILLAGER.get(), pLevel, species, profession);
    }

    @Override
    public @NotNull InteractionResult mobInteract(final Player pPlayer, @NotNull final InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (itemstack.getItem() == Items.GOLD_INGOT) {
            this.getInventory().addItem(new ItemStack(Items.BREAD, 3));
            this.eatAndDigestFood();
            this.gameEvent(GameEvent.MOB_INTERACT, this.eyeBlockPosition());
        } else if (itemstack.getItem() != Items.VILLAGER_SPAWN_EGG && this.isAlive() && !this.isTrading() && !this.isSleeping() && !pPlayer.isSecondaryUseActive()) {
            if (!this.isBaby()) {
                if (pHand == InteractionHand.MAIN_HAND) {
                    pPlayer.awardStat(Stats.TALKED_TO_VILLAGER);
                }

                if (!this.level.isClientSide) {
                    this.startTrading(pPlayer);
                }
            }
        } else {
            return super.mobInteract(pPlayer, pHand);
        }

        return InteractionResult.sidedSuccess(this.level.isClientSide);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ANIMAL_VILLAGER, new AnimalVillagerData(AnimalVillagerSpecies.CORGI, AnimalVillagerProfession.NONE));
    }

    @Override
    public void addAdditionalSaveData(@NotNull final CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        AnimalVillagerData.CODEC.encodeStart(
                NbtOps.INSTANCE,
                this.getAnimalVillagerData()).resultOrPartial(DynamicEconomy.LOGGER::error).ifPresent(
                (tag) -> pCompound.put("AnimalVillagerData", tag)
        );

        final var itemListTag = new ListTag();

        for (int i = 0; i < this.traderItemStackHandler.getSlots(); ++i) {
            final var itemStack = this.traderItemStackHandler.getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                final var compoundTag = new CompoundTag();
                itemStack.save(compoundTag);
                if (this.getAnimalVillagerData().getProfession() == AnimalVillagerProfession.PLAYER_TRADER) {
                    final var sellerUUID = ((TraderItemStackHandler) this.traderItemStackHandler).getSellerOfSlot(i);
                    if (sellerUUID != null) {
                        compoundTag.putUUID("seller", sellerUUID);
                    }
                }


                itemListTag.add(compoundTag);
            }
        }

        pCompound.put("inventory", itemListTag);
    }

    @Override
    public void readAdditionalSaveData(@NotNull final CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("AnimalVillagerData", Tag.TAG_COMPOUND)) {
            final var dataResult = AnimalVillagerData.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, pCompound.get("AnimalVillagerData")));
            dataResult.resultOrPartial(DynamicEconomy.LOGGER::error).ifPresent(this::setAnimalVillagerData);
        }

        final var inventoryTag = pCompound.getList("inventory", Tag.TAG_COMPOUND);

        for (int i = 0; i < inventoryTag.size(); ++i) {
            final var compoundTag = inventoryTag.getCompound(i);
            final var itemStack = ItemStack.of(compoundTag);
            if (!itemStack.isEmpty()) {
                this.traderItemStackHandler.insertItem(i, itemStack, false);
                if (this.getAnimalVillagerData().getProfession() == AnimalVillagerProfession.PLAYER_TRADER) {
                    if (compoundTag.hasUUID("seller")) {
                        final var seller = compoundTag.getUUID("seller");
                        ((TraderItemStackHandler) this.traderItemStackHandler).setSellerOfSlot(i, seller);
                    }
                }
            }
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return switch (getAnimalVillagerData().getProfession()) {
            case AnimalVillagerProfession.ASSESSOR ->
                    new TranslatableComponent("entity.dynamic_economy.animal_villager.assessor");
            case AnimalVillagerProfession.BANKER ->
                    new TranslatableComponent("entity.dynamic_economy.animal_villager.banker");
            case AnimalVillagerProfession.TRADER ->
                    new TranslatableComponent("entity.dynamic_economy.animal_villager.trader");
            case AnimalVillagerProfession.PLAYER_TRADER ->
                    new TranslatableComponent("entity.dynamic_economy.animal_villager.player_trader");
            default -> new TranslatableComponent("entity.dynamic_economy.animal_villager");
        };
    }

    public boolean isTrading() {
        return (this.tradingPlayer != null);
    }

    public void startTrading(final Player player) {
        this.setTradingPlayer(player);
        openTradingScreen(player, this.getDisplayName());
    }

    public AnimalVillagerData getAnimalVillagerData() {
        return this.entityData.get(DATA_ANIMAL_VILLAGER);
    }

    public void setAnimalVillagerData(@NotNull final AnimalVillagerData data) {
        this.entityData.set(DATA_ANIMAL_VILLAGER, data);
    }

    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    @Override
    public void setTradingPlayer(@Nullable final Player pTradingPlayer) {
        this.tradingPlayer = pTradingPlayer;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    public boolean hasInventoryItems() {
        for (var i = 0; i < this.traderItemStackHandler.getSlots(); ++i) {
            if (this.traderItemStackHandler.getStackInSlot(i) != ItemStack.EMPTY) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void checkDespawn() {
        if (!this.hasInventoryItems()) {
            super.checkDespawn();
        }
    }

    public void openTradingScreen(@NotNull final Player pPlayer, @NotNull final Component pDisplayName) {
        SimpleMenuProvider menu;
        switch (this.getAnimalVillagerData().getProfession()) {
            case AnimalVillagerProfession.NONE -> {
                return;
            }
            case AnimalVillagerProfession.TRADER ->
                    menu = new SimpleMenuProvider((id, inventory, player) -> new SystemTradeMenu(id, inventory, this.traderItemStackHandler, this), pDisplayName);
            case AnimalVillagerProfession.BANKER ->
                    menu = new SimpleMenuProvider((id, inventory, player) -> new BankingMenu(id, inventory, this), pDisplayName);
            case AnimalVillagerProfession.PLAYER_TRADER ->
                    menu = new SimpleMenuProvider((id, inventory, player) -> new PlayerTradeMenu(id, inventory, (TraderItemStackHandler) this.traderItemStackHandler, this), pDisplayName);
            case AnimalVillagerProfession.ASSESSOR ->
                    menu = new SimpleMenuProvider((id, inventory, player) -> new AssessMenu(id, inventory, this), pDisplayName);
            default ->
                    throw new IllegalStateException("Unexpected value: " + this.getAnimalVillagerData().getProfession());
        }

        NetworkHooks.openGui((ServerPlayer) pPlayer, menu);
    }

    @Override
    protected void dropAllDeathLoot(DamageSource pDamageSource) {
        Entity entity = pDamageSource.getEntity();
        int i = net.minecraftforge.common.ForgeHooks.getLootingLevel(this, entity, pDamageSource);

        Collection<ItemEntity> drops = new ArrayList<>();
        for (var slotIndex = 0; slotIndex < this.traderItemStackHandler.getSlots(); ++slotIndex) {
            if (!this.traderItemStackHandler.getStackInSlot(slotIndex).isEmpty()) {
                drops.add(new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), this.traderItemStackHandler.getStackInSlot(slotIndex)));
            }
        }

        if (!net.minecraftforge.common.ForgeHooks.onLivingDrops(this, pDamageSource, drops, i, lastHurtByPlayerTime > 0))
            drops.forEach(e -> level.addFreshEntity(e));
    }


    @Override
    public void die(@NotNull DamageSource pCause) {
        if (this.getAnimalVillagerData().getProfession() == AnimalVillagerProfession.PLAYER_TRADER && this.hasInventoryItems()) {
            return;
        }
        if (this.getAnimalVillagerData().getProfession() == AnimalVillagerProfession.PLAYER_TRADER && this.hasInventoryItems()) {
            return;
        }

        super.die(pCause);
    }
}
