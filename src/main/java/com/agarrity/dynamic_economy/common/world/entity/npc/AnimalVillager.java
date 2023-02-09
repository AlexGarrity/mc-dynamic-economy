package com.agarrity.dynamic_economy.common.world.entity.npc;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyHelper;
import com.agarrity.dynamic_economy.common.network.syncher.DEEntityDataSerializers;
import com.agarrity.dynamic_economy.common.world.inventory.AssessMenu;
import com.agarrity.dynamic_economy.common.world.inventory.BankingMenu;
import com.agarrity.dynamic_economy.common.world.inventory.TradeMenu;
import com.agarrity.dynamic_economy.common.world.inventory.TraderItemStackHandler;
import com.agarrity.dynamic_economy.init.EntityInit;
import com.agarrity.dynamic_economy.init.ItemInit;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class AnimalVillager extends Animal implements ITrader {

    public static final int INVENTORY_SIZE = 12;

    private static final EntityDataAccessor<AnimalVillagerData> DATA_ANIMAL_VILLAGER = SynchedEntityData.defineId(AnimalVillager.class, DEEntityDataSerializers.ANIMAL_VILLAGER_DATA);
    private final TraderItemStackHandler traderItemStackHandler = new TraderItemStackHandler();
    private Player tradingPlayer = null;

    public AnimalVillager(final EntityType<? extends AnimalVillager> pEntityType, final Level pLevel) {
        this(pEntityType, pLevel, AnimalVillagerSpecies.getRandomSpecies(), AnimalVillagerProfession.getRandomProfession());
    }

    public AnimalVillager(final EntityType<? extends AnimalVillager> pEntityType, final Level pLevel, final int species, final int profession) {
        super(pEntityType, pLevel);
        ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);
        this.getNavigation().setCanFloat(true);
        this.setCanPickUpLoot(false);
        this.setAnimalVillagerData(this.getAnimalVillagerData().setSpecies(species).setProfession(profession));
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        final var builder = Mob.createMobAttributes();
        builder.add(Attributes.MAX_HEALTH, 20.0D);
        builder.add(Attributes.MOVEMENT_SPEED, 0.2D);
        builder.add(Attributes.FOLLOW_RANGE, 48.0D);
        return builder;
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
        if (itemstack.getItem() == ItemInit.FIXED_CURRENCY.get()) {
            final var optCurrencyValue = CurrencyHelper.getCurrencyValue(itemstack);
            if (optCurrencyValue.isEmpty()) {
                return InteractionResult.PASS;
            }
            if (optCurrencyValue.get().isLessThan(CurrencyHelper.getLargestCurrency())) {
                return InteractionResult.PASS;
            }

            this.setInLove(pPlayer);
            this.gameEvent(GameEvent.MOB_INTERACT, this.eyeBlockPosition());
            if (!this.level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        } else if (itemstack.getItem() != Items.VILLAGER_SPAWN_EGG && this.isAlive() && !this.isTrading() && !this.isSleeping() && !pPlayer.isSecondaryUseActive()) {
            if (!this.isBaby()) {
                if (pHand == InteractionHand.MAIN_HAND) {
                    pPlayer.awardStat(Stats.TALKED_TO_VILLAGER);
                }

                if (!this.level.isClientSide) {
                    this.startTrading(pPlayer);
                }

            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else {
            return super.mobInteract(pPlayer, pHand);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(2, new BreedGoal(this, 0.5D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0D, Ingredient.of(ItemInit.CURRENCY_TAG), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25D));
        this.goalSelector.addGoal(5, new MoveBackToVillageGoal(this, 1.0D, false));
        this.goalSelector.addGoal(4, new GolemRandomStrollInVillageGoal(this, 0.6D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
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
                compoundTag.putUUID("seller", traderItemStackHandler.getSellerOfSlot(i));
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
            final var itemStack = ItemStack.of(inventoryTag.getCompound(i));
            final var seller = inventoryTag.getCompound(i).getUUID("seller");
            if (!itemStack.isEmpty()) {
                this.traderItemStackHandler.insertItem(i, itemStack, false);
                this.traderItemStackHandler.setSellerOfSlot(i, seller);
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

    @Override
    public void openTradingScreen(@NotNull final Player pPlayer, @NotNull final Component pDisplayName) {
        SimpleMenuProvider menu;
        switch (this.getAnimalVillagerData().getProfession()) {
            case AnimalVillagerProfession.NONE -> {
                return;
            }
            case AnimalVillagerProfession.BANKER ->
                    menu = new SimpleMenuProvider((id, inventory, player) -> new BankingMenu(id, inventory, this), pDisplayName);
            case AnimalVillagerProfession.TRADER ->
                    menu = new SimpleMenuProvider((id, inventory, player) -> new TradeMenu(id, inventory, this.traderItemStackHandler, this), pDisplayName);
            case AnimalVillagerProfession.ASSESSOR ->
                    menu = new SimpleMenuProvider((id, inventory, player) -> new AssessMenu(id, inventory, this), pDisplayName);
            default ->
                    throw new IllegalStateException("Unexpected value: " + this.getAnimalVillagerData().getProfession());
        }

        NetworkHooks.openGui((ServerPlayer) pPlayer, menu);
    }

    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
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
}
