package com.agarrity.dynamic_economy.init;

import com.agarrity.dynamic_economy.common.data.advancements.criterion.BalanceTrigger;
import net.minecraft.advancements.CriteriaTriggers;

public class TriggerInit {

    public static BalanceTrigger BALANCE_TRIGGER = CriteriaTriggers.register(new BalanceTrigger());

    public static void registerTriggers() {
        CriteriaTriggers.register(BALANCE_TRIGGER);
    }

}
