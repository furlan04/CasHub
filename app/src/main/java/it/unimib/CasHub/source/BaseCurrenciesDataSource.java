package it.unimib.CasHub.source;

public abstract class BaseCurrenciesDataSource {
    protected ForexCallback callback;

    public void setCallback(ForexCallback callback) {
        this.callback = callback;
    }

    public abstract void getCurrencies();
}
