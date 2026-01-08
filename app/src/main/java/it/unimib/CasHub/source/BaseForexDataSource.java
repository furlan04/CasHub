package it.unimib.CasHub.source;

import java.util.List;

import it.unimib.CasHub.model.CurrencyEntity;

public abstract class BaseForexDataSource {
    protected ForexCallback callback;

    public void setCallback(ForexCallback callback) {
        this.callback = callback;
    }

    public abstract void getRates(String base);
    public abstract void getCurrencies();

    public abstract void saveCurrencies(List<CurrencyEntity> currencies);
}
