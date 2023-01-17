package com.agarrity.dynamic_economy.common.data.advancements.criterion;

import com.agarrity.dynamic_economy.DynamicEconomy;
import com.agarrity.dynamic_economy.common.economy.bank.CurrencyAmount;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class BalanceTrigger extends SimpleCriterionTrigger<BalanceTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation(DynamicEconomy.MOD_ID, "has_balance");

    @Override
    protected @NotNull TriggerInstance createInstance(final JsonObject pJson, @NotNull final EntityPredicate.Composite pPlayer, @NotNull final DeserializationContext pContext) {
        final var amount = new CurrencyAmount(pJson.get("amount").getAsInt());
        final var predicate = new BalancePredicate(amount);
        return new BalanceTrigger.TriggerInstance(pPlayer, predicate);
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return ID;
    }

    public void trigger(final @NotNull ServerPlayer pPlayer, final CurrencyAmount amount) {
        this.trigger(
            pPlayer,
            (triggerInstance) -> triggerInstance.matches(amount)
        );
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final BalancePredicate predicate;

        public TriggerInstance(final EntityPredicate.Composite pPlayer, final BalancePredicate balancePredicate) {
            super(BalanceTrigger.ID, pPlayer);
            this.predicate = balancePredicate;
        }

        public static TriggerInstance instance(final EntityPredicate.Composite pPlayer, final BalancePredicate predicate) {
            return new TriggerInstance(pPlayer, predicate);
        }

        public boolean matches(final CurrencyAmount pAmount) {
            return this.predicate.test(pAmount);
        }
    }

}
