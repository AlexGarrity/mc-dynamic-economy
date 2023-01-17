package com.agarrity.dynamic_economy.client.gui.screens.inventory;

import com.agarrity.dynamic_economy.common.economy.bank.CurrencyAmount;
import org.jetbrains.annotations.NotNull;

public interface ICashScreen {

    void setBalance(@NotNull final CurrencyAmount balance);

}
